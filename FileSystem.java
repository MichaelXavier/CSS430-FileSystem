public class FileSystem {
  private SuperBlock superblock;
  private Directory directory;
  private FileStructureTable filetable;

  public FileSystem(int diskBlocks) {
    superblock = new SuperBlock(diskBlocks);
    directory = new Directory(superblock.totalInodes);
    filetable = new FileStructureTable(directory);

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
    //TODO
  }

  public int write(FileTableEntry ftEnt, byte[] buffer) {
    //TODO
  }

  public int fsize(FileTableEntry ftEnt) {
    //TODO
  }


  //TODO more

  //FIXME: should this be private?
  private boolean deallocAllBlocks(FileTableEntry ftEnt) {
    //TODO
  }
}
