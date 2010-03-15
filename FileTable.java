import java.util.*;
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

      iNumber = (filename.equals("/") ? 0 : dir.namei(filename));
      //FIXME: check logic here, example used an unmatched curly brace
      //FIXME: may have to change below to > 0, as 0 is the root directory
      if (iNumber >= 0) {//if the file exists
        inode = new Inode(iNumber);
        SysLib.cout("falloc loop entry with inode flag " + inode.flag + "\n");

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
            break;
          } else if (inode.flag == Inode.DELETE) {
            iNumber = -1; //no more open
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
            try { wait(); } catch (InterruptedException e) {}
            break;
          } else if (inode.flag == Inode.DELETE) { //the file has already been deleted
            iNumber = -1; //no more open
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
      
      if (e.inode.flag == Inode.READ || e.inode.flag == Inode.WRITE) {
        notify();
      }
      e.inode.toDisk(e.iNumber);

      //NOTE: this logic means that if this is the last file table entry that
      //needs this inode, if is no longer in read/write mode and should set it
      //to used. If the flag is DELETE, we want to 
      if (e.inode.count == 0) {
        e.inode.flag = Inode.USED;
      }
      return true;
    }
    return false;
  }

  public FileTableEntry getEntryAtInumber(int iNumber) {
    for (int i = 0; i < table.size(); i++) {
      FileTableEntry ftEnt = (FileTableEntry)table.elementAt(i);
      if (ftEnt.iNumber == iNumber) {
        return ftEnt;
      }
    }
    return null;
  }

  public synchronized boolean fempty() {
    return table.isEmpty();  // return if table is empty 
  }                           // should be called before starting a format
}
