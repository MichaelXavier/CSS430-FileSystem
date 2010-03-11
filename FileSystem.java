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

  public int read(FileTableEntry ftEnt, byte[] buffer) {
    //Make sure to check inode first	
    //TODO
  }

  public int write(FileTableEntry ftEnt, byte[] buffer) {
    //Make sure to check inode first	
    //TODO
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
}
