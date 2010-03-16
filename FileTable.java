//Modified by:  Michael Xavier and Maridel Legaspi
//File		 :  FileTable.java
//Description:  File Table shared among all user threads

import java.util.*;

public class FileTable {
	private Vector table;         // the actual entity of this file table
	private Directory dir;        // the root directory 

	// constructor
	public FileTable(Directory directory) { 
		table = new Vector();     // instantiate a file table
		dir = directory;          // receive a reference to the Director
	}                             // from the file system


	// allocate a new file (structure) table entry for this file name
	// return a reference to this file (structure) table entry
	public synchronized FileTableEntry falloc(String filename, String mode) {
		short iNumber = -1;
		Inode inode = null;
		while (true) {
			iNumber = (filename.equals("/") ? 0 : dir.namei(filename));
			if (iNumber >= 0) {//if the file exists
				inode = new Inode(iNumber);
				if (mode.equals("r")) {
					if (inode.flag == Inode.UNUSED || inode.flag == Inode.USED
						|| inode.flag == Inode.READ){
						inode.flag = Inode.READ;
						// no need to wait
						break;
					} else if (inode.flag == Inode.WRITE) {
						// wait for a write to exit
						try { wait(); } catch (InterruptedException e) {}
						break;
					} else if (inode.flag == Inode.DELETE) {
						iNumber = -1; //no more open
						return null;
					}
				} else { //the mode is w, w+ or a
					if(inode.flag == Inode.UNUSED || inode.flag == Inode.USED){
						//the inode was never modified, set it to writer mode
						inode.flag = Inode.WRITE;
						// no need to wait
						break;
					} else if (inode.flag == Inode.WRITE || 
							   inode.flag == Inode.READ) {
						//cannot write to the file, wait to be woken up
						try { wait(); } catch (InterruptedException e) {}
						break;
					//the file has already been deleted
					} else if (inode.flag == Inode.DELETE) { 
						iNumber = -1;	//no more open
						return null;
					}
				}
			} else {      // the iNumber is negative so the file doesn't exist
				iNumber = dir.ialloc(filename);
				inode = new Inode();
				// we are creating a new inode to this file with the USED flag,
				//write it to the disk below
				break;
			}
		}
		inode.count++;
		inode.toDisk(iNumber);
		FileTableEntry e = new FileTableEntry(inode, iNumber, mode);
		table.addElement(e); // create a table entry and register it.
		return e;
	}

	// free this file table entry
	public synchronized boolean ffree(FileTableEntry e) {
		if (table.removeElement(e)) {
			e.inode.count--;
			if (e.inode.flag == Inode.READ || e.inode.flag == Inode.WRITE) {
				notify();
			}
			e.inode.toDisk(e.iNumber);
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
	}							// should be called before starting a format
}
