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

  //FIXME: i have 2 definitions which are quite different of FileTable's falloc method. the other one use dir.ialloc
  public synchronized FileTableEntry falloc(String filename, String mode) {
    //FIXME: check logic here, writing these from the notes, he seems to have
    //remove the while loop which is a bit unsettling
    Inode inode = null;
    short iNumber = (fnames.equals("/") ? 0 : dir.namei(filename));

    if (iNumber >= 0) { // File exists
      inode = new Inode(iNumber);
    }

    //FIXME; his line was originally iNumber = -1; check logic
    inode.iNumber = -1; // This seemed to make more sense?
    //FIXME: should we call inode.toDisk ??
    return null;

    //FIXME: should we still be looping
    while (true) {

      if (mode.equals("r")) {
        if (inode.flag == Inode.UNUSED || inode.flag == Inode.READ) {
          inode.flag = Inode.flag = Inode.READ; // if unused or read, just set it to read
          break;//FIXME: check logic
        } else { // inode must be in a writing operation so we have to wait
          try { wait() } catch (InterruptedException e) {}
        }
      } else { // mode is either "w", "w+" or "a"
        if (inode.flag == Inode.READ || inode.flag == Inode.WRITE || inode.flag == Inode.READ_WRITE) {
          //FIXME check logic but we can only have one writer so all these
          //conditions should cause a wait i think.
          try { wait() } catch (InterruptedException e) {}
        } else if (mode.equals("w")) {
          inode.flag = Inode.WRITE;
          break;//FIXME: check logic
        } else if (mode.equals("w+")) {
          inode.flag = Inode.READ_WRITE;
          break;//FIXME: check logic
        } else {//mode must be "a"
          inode.flag = Inode.READ;
          break;//FIXME: check logic
        }
      }

    }


    inode.count++;
    inode.toDisk(iNumber);
    FileTableEntry e = new FileTableEntry(inode, iNumber, mode);
    table.addElement(e); // create a table entry and register it.
    return e;

    /*short iNumber = -1;
    Inode inode = null;

    //FIXME!!!!!!!!!! update with notes from 03-09-2010
    while (true) {
      iNumber = (fnames.equals("/") ? 0 : dir.namei(filename));
      //FIXME: check logic here, example used an unmatched curly brace
      if (iNumber < 0) {
        //the file was not found so we cannot allocate it to the filetable
        //FIXME: doublecheck logic
        return null;
      }

      inode = new Inode(iNumber);
      if (iNumber <= Inode.READ) {
        break;
      }

      if (mode.compareTo("r")) {
        //FIXME: the "is" here appears to be psuedocode
        if (inode.flag == Inode.READ) {
          // no need to wait
          break;
        //FIXME: the "is" here appears to be psuedocode
        } else if (inode.flag == Inode.WRITE) {
          // wait for a write to exit
          //FIXME: example has unmatched brace here, is something else supposed to happen here?
          try { wait() } catch (InterruptedException e) {}
        } else if (inode.flag == Inode.DELETE) {
          inumber = -1; //no more open
          return null;
        }
      } else if (mode.compareTo("w")) {
        //FIXME: ensure that WRITE and READ should both wait in this case when intending to write to this file
        if (inode.flag == Inode.WRITE || inode.flag == Inode.READ) {
          // wait for a write to exit
          //FIXME: example has unmatched brace here, is something else supposed to happen here?
          try { wait() } catch (InterruptedException e) {}
        } else if (inode.flag == Inode.DELETE) {
          inumber = -1; //no more open
          return null;
        }
      }
    }

    inode.count++;
    inode.toDisk(iNumber);
    FileTableEntry e = new FileTableEntry(inode, iNumber, mode);
    table.addElement(e); // create a table entry and register it.
    return e;
    */
  }

  public synchronized boolean ffree(FileTableEntry e) {
    //FIXME: check logic here, this is the code that munehiro gave. it is much
    //shorter but seems to not affect the count
    //FIXME!!! could be bugged
    if (table.removeElement(e)) {
      e.inode.count--;

      //write the Inode to the disk first because we may potentially give up
      //control to another thread in the notify
      inode.toDisk();

      //if there's another thread waiting on this file table entry, decrement
      //and wake a thread up
      if (e.count > 1) {
        e.count--;
        notify();
      }

      return true;
    } else {
      return false;
    }


    //NOTE: we assume that we should check to see that the file table entry belongs to this filetable beforem modifying its inode
    /*int index_of = table.indexOf(e);

    if (index_of == -1) {
      return false;
    }

    Inode inode = e.inode;

    // save the corresponding inode to the disk
    inode.toDisk();

    // free this file table entry.
    //FIXME: we interpret "free" as decrementing the count, if the count is
    //then 0, we reset the seek pointer and set it to USED which means the
    //next thread to try to read/write from it will not have to wait
    if (e.count > 0) {
      e.count--;
      if (e.count == 0) { // this is the last file table entry that is using this file

        //Don't think this would ever come up
        //seekPtr = 0; // put the seek back at the beginning of the file for the next thread that wants it
        //FIXME: check logic, what exactly does "USED" even mean
        e.inode.flag = Inode.USED;
        e.inode.count--;

        //FIXME: check logic, we assume that if the count for this file table
        //entry is now at 0 threads, no other threads depend on it and we can
        //delete it from our vector.
        table.removeElementAt(index_of);
      }
    }

    return true;*/
  }

  public synchronized boolean fempty() {
    return table.isEmpty();  // return if table is empty 
  }                           // should be called before starting a format
}
