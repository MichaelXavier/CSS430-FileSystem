//FIXME: this class is used NOWHERE in the program so it is ambiguous as to
//what caller is using these methods, etc.
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
      iNumber = (fnames.equals("/") ? 0 : dir.namei(filename));
      //FIXME: check logic here, example used an unmatched curly brace
      if (iNumber >= 0) {
        inode = new Inode(iNumber);
        if (iNumber <= Inode.USED) {
          break;
        }

        if (mode.equals("r")) {
          //FIXME: the "is" here appears to be psuedocode
          if (inode.flag == Inode.UNUSED || inode.flag == Inode.USED || inode.flag == Inode.READ) {
            inode.flag = Inode.READ;
            // no need to wait
            break;
          //FIXME: the "is" here appears to be psuedocode
          } else if (inode.flag == Inode.WRITE) {
            // wait for a write to exit
            //FIXME: example has unmatched brace here, is something else supposed to happen here?
            try { wait(); } catch (InterruptedException e) {}
          } else if (inode.flag == Inode.DELETE) {
            inumber = -1; //no more open
            return null;
          }
        } else { //the mode is w, w+ or a
          //FIXME: ensure that WRITE and READ should both wait in this case when intending to write to this file
          if (inode.flag == Inode.UNUSED || inode.flag == Inode.USED) {
            //the inode has never been modified so we will set it to writer mode and we're done
            inode.flag = Inode.WRITE;
            // no need to wait
            break;
          } else if (inode.flag == Inode.WRITE || inode.flag == Inode.READ) {//cannot write to the file, wait to be woken up
            try { wait() } catch (InterruptedException e) {}
          } else if (inode.flag == Inode.DELETE) { //the file has already been deleted
            inumber = -1; //no more open
            return null;
          }
        }
      } else { // the iNumber is negative so the file doesn't exist
        iNumber = dir.ialloc(filename);
        inode = new Inode();
        // we are creating a new inode to this file with the USED flag, write
        // it to the disk below
        break;
      }
    }

    inode.count++;
    inode.toDisk(iNumber);
    FileTableEntry e = new FileTableEntry(inode, iNumber, mode);
    table.addElement(e); // create a table entry and register it.
    return e;
  }

  //NOTE: filesystem should check ahead before calling this to make sure no
  //other threads are using it so one thread doesn't delete another thread's
  //file
  public synchronized boolean ffree(FileTableEntry e) {
    if (table.removeElement(e)) {
      e.inode.count--;
      
      if (inode.flag == Inode.READ || inode.flag == Inode.WRITE) {
        notify();
      }
      e.inode.toDisk(e.iNumber);
      return true;
    }
    return false;
  }

  public synchronized boolean fempty() {
    return table.isEmpty();  // return if table is empty 
  }                           // should be called before starting a format
}
