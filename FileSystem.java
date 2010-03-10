public class FileSystem {
  private SuperBlock superblock;
  private Directory directory;
  private FileTable filetable;

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
  public int seek(FileTableEntry ftEnt, int offset, int whence) {
    //TODO
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
