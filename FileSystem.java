public class FileSystem {
  private SuperBlock superblock;
  private Directory directory;
  private FileTable filetable;

  public static final int SEEK_SET = 0;
  public static final int SEEK_CUR = 1;
  public static final int SEEK_END = 2;

  public FileSystem(int diskBlocks) {
    superblock = new SuperBlock(diskBlocks);
    directory = new Directory(superblock.totalInodes);
    filetable = new FileTable(directory);

    //read the "/" file from disk
    FiletableEntry dirEnt = open("/", "r");
    int dirSize = fsize(dirEnt);
    if (dirSize > 0) {
      // the directory has some data.
      byte[] dirData = new byte[dirSize];
      read(dirEnt, dirData);
      directory.bytes2directory(dirdata);
    }
    close(dirEnt);
  }

  public FileTableEntry open(String filename, String mode) {
    FileTableEntry ftEnt = filetable.falloc(filename, mode);
    if (mode.equals("w")) {
      if (deallocAllBlocks(ftEnt) == false) { //TODO: need to implement
        return null;
      }
    }
    return ftEnt;
  }

  //reads up to buffer.length bytes from the file indicated by fd, starting at
  //the position currently pointed to by the seek pointer. If bytes remaining
  //between the current seek pointer and the end of file are less than
  //buffer.length, SysLib.read reads as many bytes as possible, putting them
  //into the beginning of buffer. It increments the seek pointer by the number
  //of bytes to have been read. The return value is the number of bytes that
  //have been read, or a negative value upon an error.

  //FIXME: I considered making this synchronized but it would cause indefinite starvation as no thread could ever unset the write flag I THINK. doublecheck
  //public synchronized int read(FileTableEntry ftEnt, byte[] buffer) {
  public int read(FileTableEntry ftEnt, byte[] buffer) {
    //FIXME: check logic: should we be checking
    //Make sure to check inode first	
    //FIXME: forgot what on the inode we are checking? flag?


    //TODO: check modes, wait/notify, etc
    while (true) {

      switch(ftEnt.inode.flag) {
        case Inode.WRITE:
          //We cannot read if something is writing
          try { wait(); } catch (InterruptedException e) {}
          break;
        case Inode.DELETE:
          return -1;
        default:
          //First we mark the inode as being in a read state. this SHOULD prevent writer threads from interfering even if this method's execution gets preempted
          ftEnt.inode.flag = Inode.READ;
          //read a block at a time
          byte temp_block = new byte[Disk.blockSize];


          int bytes_read = 0;

          //Something terrible has happened
          if (block_num == -1) {
            return -1;
          }

          while (bytes_read < buffer.length) {

            short block_num = seekToBlock(ftEnt.seekPtr, ftEnt.inode);

            //Read from disk
            if (SysLib.rawread(block_num, temp_block) == -1) {
              return -1;
            }

            //TODO: beginning offset is it possible to have a read start part of the way into the buffer? do we care?

            //If we are on the last block and it doesn't use all the space, dont read all of it
            int read_length;

            boolean last_block = (ftEnt.inode.length - ftEnt.seekPtr - buffer.length) < Disk.blockSize;

            int read_length = last_block ? (ftEnt.inode.length - ftEnt.seekPtr) : Disk.blockSize;

            System.arraycopy(temp_block, 0, buffer, 0, read_length);
            bytes_read += read_length;
            ftEnt.seekPtr += read_length;
          }


          //FIXME!!!!!!!!!!!!!! is it safe to assume filesystem is supposed to decrease # of threads waiting on this ftEnt?
          //FIXME: also would we decrement this ALWAYS or only when done reading the file?
          ftEnt.count--;

          //If there's another thread waiting, wake it up
          //FIXME!!!!!!!!! do we care if the inode flag is READ/WRITE?
          //if (ftEnt.count > 0 && (inode.flag == Inode.READ || inode.flag == Inode.WRITE)) {
          if (ftEnt.count > 0) {
            notify();         
          } else {
            //FIXME: check logic
            //This inode is no longer in a read state.
            ftEnt.inode.flag = Inode.USED;
          }

          return read_length;

          //end default switch case
      }
    }
  }

    //writes the contents of buffer to the file indicated by fd, starting at the position indicated by the seek pointer. The operation may overwrite existing data in the file and/or append to the end of the file. SysLib.write increments the seek pointer by the number of bytes to have been written. The return value is the number of bytes that have been written, or a negative value upon an error.
  public int write(FileTableEntry ftEnt, byte[] buffer) {
    //FIXME: check logic: should we be checking
    //Make sure to check inode first	
    //FIXME: forgot what on the inode we are checking? flag?


    //TODO: check modes, wait/notify, etc
    /*while (true) {

      switch(ftEnt.inode.flag) {
        case Inode.WRITE:
        case Inode.READ:
          //We cannot write if something is writing or reading
          try { wait(); } catch (InterruptedException e) {}
          break;
        case Inode.DELETE:
          return -1;
        default:
          //First we mark the inode as being in a write state. this SHOULD prevent reader/writer threads from interfering even if this method's execution gets preempted
          ftEnt.inode.flag = Inode.WRITE;
          //read a block at a time
          byte temp_block = new byte[Disk.blockSize];

          short block_num = seekToBlock(ftEnt.seekPtr, ftEnt.inode);

          int bytes_written = 0;

          //Something terrible has happened
          if (block_num == -1) {
            return -1;
          }

          while (bytes_written < buffer.length) {

            //Read from disk
            if (SysLib.rawread(block_num, temp_block) == -1) {
              return -1;
            }

            //TODO: beginning offset is it possible to have a read start part of the way into the buffer? do we care?

            //If we are on the last block and it doesn't use all the space, dont read all of it
            int read_length;

            boolean last_block = (ftEnt.inode.length - ftEnt.seekPtr - buffer.length) < Disk.blockSize;

            int read_length = last_block ? (ftEnt.inode.length - ftEnt.seekPtr) : Disk.blockSize;

            System.arraycopy(temp_block, 0, buffer, 0, read_length);
            bytes_read += read_length;
            ftEnt.seekPtr += read_length;
          }


          //FIXME!!!!!!!!!!!!!! is it safe to assume filesystem is supposed to decrease # of threads waiting on this ftEnt?
          //FIXME: also would we decrement this ALWAYS or only when done reading the file?
          ftEnt.count--;

          //If there's another thread waiting, wake it up
          //FIXME!!!!!!!!! do we care if the inode flag is READ/WRITE?
          //if (ftEnt.count > 0 && (inode.flag == Inode.READ || inode.flag == Inode.WRITE)) {
          if (ftEnt.count > 0) {
            notify();         
          } else {
            //FIXME: check logic
            //This inode is no longer in a read state.
            ftEnt.inode.flag = Inode.USED;
          }

          return read_length;

          //end default switch case
      }*/
    }

















  }

  public int fsize(FileTableEntry ftEnt) {
    return ftEnt.inode.length;
  }

  //NOTE: we are a bit iffy on the arguments here but it should return 0 on
  //success, -1 on failure
  //TODO: doublecheck arguments
  public int close(FileTableEntry ftEnt) {
    //TODO
  }

  //TODO: doublecheck arguments
  public int seek(FileTableEntry ftEnt, int offset, int whence) {
    //offset can never be greater than the filesize
    /*if (offset > fsize(ftEnt)) {
      return -1;//this is always bad, will read off the end no matter what
    }*/
    
    switch(whence) {
      case SEEK_SET: 
        ftEnt.seekPtr = offset;
        break;
      case SEEK_CUR: 
        /*if ((offset < 0 && (-1 * offset) > ftEnt.seekPtr) || //read backwards past the beginning
            (offset > (fsize(ftEnt) - ftEnt.seekPtr))) { //read off the end
          return -1; //this would read backwards past the start of the file
        }*/
        ftEnt.seekPtr += offset;
        break;
      case SEEK_END: 
        ftEnt.seekPtr = fsize(ftEnt) + offset;
      default:
        return -1;//FIXME: why does this return int?
    }

    return Kernel.OK;
  }

  public int format(int files) {
    //TODO
  }

  public int delete(String filename) {
    //TODO
  }

  //TODO more

  //FIXME: should this be private?
  private boolean deallocAllBlocks(FileTableEntry ftEnt) {
    //TODO
  }

  //Given the seek pointer for a file, returns the block number at which it can
  //be found, -1 otherwise
  private short seekToBlock(short f_pos, Inode inode) {
    return inode.findTargetBlock(f_pos / Disk.blockSize);
  }
}
