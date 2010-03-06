public class FileTable {
  private Vector table;         // the actual entity of this file table
  private Directory dir;        // the root directory 

  public FileTable(Directory directory) { // constructor
    table = new Vector();     // instantiate a file (structure) table
    dir = directory;           // receive a reference to the Director
  }                             // from the file system

  // major public methods
  // allocate a new file (structure) table entry for this file name
  // allocate/retrieve and register the corresponding inode using dir
  // increment this inode's count
  // immediately write back this inode to the disk
  // return a reference to this file (structure) table entry
  public synchronized FileTableEntry falloc(String filename, String mode) {
    short iNumber = -1;
    Inode inode = null;

    while (true) {
      iNumber = (fnames.equals("/") ? 0 : dir.namei(fname));
      //FIXME: check logic here, example used an unmatched curly brace
      if (iNumber >= 0) {
        inode = new Inode(iNumber);
      }

      if (mode.compareTo("r")) {
        //FIXME: the "is" here appears to be psuedocode
        if (inode.flag is "read") {
          // no need to wait
          break;
        //FIXME: the "is" here appears to be psuedocode
        } else if (inode.flag is "read") {
          // wait for a write to exit
          //FIXME: example has unmatched brace here, is something else supposed to happen here?
          try { wait() } catch (InterruptedException e) {}
        } else if (inode.flag is "to be deleted") {
          inumber = -1; //no more open
          return null;
        }
      } else if (mode.compareTo("w")) {
        //TODO ...
      }
    }

    inode.count++;
    inode.toDisk(iNumber);
    FileTableEntry e = new FileTableEntry(inode, iNumber, mode);
    table.addElement(e); // create a table entry and register it.
    return e;
  }

  public synchronized boolean ffree(FileTableEntry e) {
    // receive a file table entry reference
    // save the corresponding inode to the disk
    // free this file table entry.
    // return true if this file table entry found in my table
  }

  public synchronized boolean fempty() {
    return table.isEmpty();  // return if table is empty 
  }                           // should be called before starting a format
}
