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

  //FIXME: is this complete
  public FileTableEntry open(String filename, String mode) {
    FileTableEntry ftEnt = filetable.falloc(filename, mode);
    if (mode.equals("w")) {
      if (deallocAllBlocks(ftEnt) == false) { //TODO: need to implement
        return null;
      }
    }
    return ftEnt;
  }

  //NOTE: returns number of bytes read
  public int read(FileTableEntry ftEnt, byte[] buffer) {
    //TODO
  }

  public int write(FileTableEntry ftEnt, byte[] buffer) {
    //TODO
  }

  public int fsize(FileTableEntry ftEnt) {
    //TODO
  }

  //NOTE: we are a bit iffy on the arguments here but it should return 0 on
  //success, -1 on failure
  //TODO: doublecheck arguments
  public int close(FileTableEntry ftEnt) {
    //TODO
  }

  //TODO: doublecheck arguments
  // Updates the seek pointer corresponding to fd as follows:
  // If whence is SEEK_SET (= 0), the file's seek pointer is set to offset bytes from the beginning of the file
  // If whence is SEEK_CUR (= 1), the file's seek pointer is set to its current value plus the offset. The offset can be positive or negative.
  // If whence is SEEK_END (= 2), the file's seek pointer is set to the size of the file plus the offset. The offset can be positive or negative.

  //FIXME: check logic, do we need an in-block offset or is it just offset within the file
  public int seek(FileTableEntry ftEnt, int offset, int whence) {
    switch (whence) {
      case SEEK_SET:
        seekPtr = offset;
      case SEEK_CUR:
        //TODO
      case SEEK_END:
    }
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
