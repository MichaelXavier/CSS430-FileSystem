//Modified by:  Michael Xavier and Maridel Legaspi
//File		 :  FileSystem.java
//Description:  Uses eight system calls : open, read, write, close, seek, 
//				format, delete, fsize

import java.util.*;
public class FileSystem extends Thread{
	private SuperBlock superblock;
	private Directory directory;
	private FileTable filetable;

	public static final int SEEK_SET = 0;
	public static final int SEEK_CUR = 1;
	public static final int SEEK_END = 2;

	// constructor
	public FileSystem(int diskBlocks) {
		superblock = new SuperBlock(diskBlocks);
		directory = new Directory(superblock.totalInodes);
		filetable = new FileTable(directory);

		//read the "/" file from disk
		FileTableEntry dirEnt = open("/", "r");
		int dirSize = fsize(dirEnt);
		if (dirSize > 0) {
			// the directory has some data.
			byte[] dirData = new byte[dirSize];
			read(dirEnt, dirData);
			directory.bytes2directory(dirData);
		}
		close(dirEnt);
	}

	public FileTableEntry open(String filename, String mode) {
		//Check if it is a new file before falloc because falloc will create a
		//new file if one does not exist
		boolean new_file = directory.namei(filename) == -1;
		FileTableEntry ftEnt = filetable.falloc(filename, mode);
		short flag;
		if (mode.equals("a")) {
			seek(ftEnt, 0, SEEK_END);
			flag = Inode.WRITE;
		} else if (mode.equals("w")) {	//truncate the file to 0
			deallocAllBlocks(ftEnt);
			new_file = true;
			flag = Inode.WRITE;
		} else if (mode.equals("w+")) { //w+ sets the seek at beginning of file
			flag = Inode.WRITE;
		} else {			 //mode is read, sets seek to beginning of the file
			if (new_file) 
				return null;  
			flag = Inode.READ;
		}
		//We only want to set the flag if we are the first one in
		if (ftEnt.count == 1) {
			ftEnt.inode.flag = flag;
		}
		//Allocate hard drive space for the file if its new
		if (new_file) {
			//assign a direct block to it
			short direct_block = superblock.getFreeBlock();
			if (direct_block == -1) {
				return null; //Not enough space for a direct block
			}
			ftEnt.inode.setNextBlockNumber(direct_block);
			ftEnt.inode.toDisk(ftEnt.iNumber); 
		}
		return ftEnt;
	}

	// reads the disk and returns number of bytes that
	// have been read, or a negative value upon an error.
	public synchronized int read(FileTableEntry ftEnt, byte[] buffer) {
		int bytes_read = 0;
		int read_length = 0;
		while (true) {
		  switch(ftEnt.inode.flag) {
			case Inode.WRITE:
				try { wait(); } catch (InterruptedException e) {}
				break;
			case Inode.DELETE:
				return -1;
			default: //mark inode as being in read state. this SHOULD prevent 
					 //writer threads from interfering even if this method's 
					 //execution gets preempted
				ftEnt.inode.flag = Inode.READ;
				//read a block at a time
				byte[] temp_block = new byte[Disk.blockSize];
				int bufsize = 0;
				while (bytes_read < buffer.length) {
					int block_num = seek2block(ftEnt.seekPtr, ftEnt.inode);
					// error check
					if (block_num == -1) {
						return -1;
					}
					//Read from disk
					if (SysLib.rawread(block_num, temp_block) == -1) {
						return -1;
					}
					//If on last block and doesn't use all space, dont read all of it
					boolean last_block = (ftEnt.inode.length - ftEnt.seekPtr) <
					Disk.blockSize || (ftEnt.inode.length - ftEnt.seekPtr) ==0;
					read_length = (last_block ? 
						(ftEnt.inode.length - ftEnt.seekPtr) : Disk.blockSize);
					//in one disk block
					if (buffer.length < (512 - ftEnt.seekPtr)){
					System.arraycopy(temp_block, ftEnt.seekPtr, buffer, 0,buffer.length);
					bytes_read = buffer.length;
					} else{  // data in multiple blocks
					System.arraycopy(temp_block, 0, buffer, bufsize, read_length);
					bytes_read += read_length;
					}
					bufsize = bufsize + read_length - 1;
					seek(ftEnt, read_length, SEEK_CUR);
				}  //end default switch case

			if (ftEnt.count > 0) {
				ftEnt.count--;
			}
			//If there's another thread waiting, wake it up
			if (ftEnt.count > 0) { 
				notifyAll();
			} else {
				//This inode is no longer in a read state.
				ftEnt.inode.flag = Inode.USED;
			}
		    return bytes_read;
          } // end of switch 
	   } // end of while
	} // end of read method

    //writes the contents of buffer to the file indicated by fd starting at the
	//position indicated by the seek pointer. The operation may overwrite 
	//existing data in the file and/or append to the end of the file. 
	//SysLib.write increments the seek pointer by the number of bytes to have 
	//been written. The return value is the number of bytes that have been 
	//written, or a negative value upon an error.
	public synchronized int write(FileTableEntry ftEnt, byte[] buffer) {
		if (ftEnt == null) {
			return -1;
		}
		short block_num = seek2block(ftEnt.seekPtr, ftEnt.inode);
		int bytes_written = 0;
		int offset_in_block = ftEnt.seekPtr % Disk.blockSize;
		while (true) {
		switch(ftEnt.inode.flag) {
			case Inode.WRITE:
			case Inode.READ:
			//We cannot write if something is writing or reading
				if (ftEnt.count > 1) {//DEBUG
					try { wait(); } catch (InterruptedException e){}
				} else {
					ftEnt.inode.flag = Inode.USED;
				}
				break;
			case Inode.DELETE:
				return -1;
			default:
				//First we mark the inode as being in a write state. 
				//this SHOULD prevent reader/writer threads from interfering 
				//even if this method's execution gets preempted
				ftEnt.inode.flag = Inode.WRITE;
				//read a block at a time
				byte[] temp_block = new byte[Disk.blockSize];
				short inode_offset = seek2offset(ftEnt.seekPtr);
				while (bytes_written < buffer.length) {
					inode_offset = seek2offset(ftEnt.seekPtr);
					if (inode_offset >= Inode.directSize - 1 && 
						ftEnt.inode.getIndexBlockNumber() <= 0) {
						//allocate an index block to it
						short index_block = superblock.getFreeBlock();
						if (index_block == -1) {
							return -1;//no space for an indirect block
						}
						//otherwise set the indirect block. save  inode to disk
						ftEnt.inode.setIndexBlock(index_block);
						//save the inode to disk immediately
						ftEnt.inode.toDisk(ftEnt.iNumber);
					}
					int bytes_left_in_buffer = buffer.length - bytes_written;
					//The block in question is not available yet, reserve it
					if (block_num == -1 || (bytes_written % Disk.blockSize >
						0 && bytes_left_in_buffer > 0)) {
						
						block_num = superblock.getFreeBlock();
						//Out of space, cant do it.
						if (block_num == -1) {
							return -1;
						}
						//Set this new block in the inode so it knows 
						//where the rest of the file will go
						if (!(ftEnt.inode.setNextBlockNumber(block_num))) {
							return -1;
						}
						//write the inode to disk immediately FIXME check logic
						ftEnt.inode.toDisk(ftEnt.iNumber);
					}
            
					//Now we have the block number, read it from the disk
					SysLib.rawread(block_num, temp_block);
					//If there is more than a block left in the buffer, write a block
					//and then reenter the loop, otherwise, write until the buffer is
					//empty the maximum we could write is the difference between 
					//the block size and how far in we are for that block
					int bytes_to_write = ((bytes_left_in_buffer <
					  (Disk.blockSize - offset_in_block)) ?bytes_left_in_buffer
					  : (Disk.blockSize - offset_in_block));
					System.arraycopy(buffer, bytes_written, temp_block,
						offset_in_block, bytes_to_write);
					//Write the data to the block
					SysLib.rawwrite(block_num, temp_block);
					//increment to the next block
					block_num++;
					bytes_written += bytes_to_write;
					ftEnt.seekPtr += bytes_to_write;
					//starting on a new block, offset in block will always be 0
					offset_in_block = 0; 
				} //end default switch case
				ftEnt.count--;

				//if the seek pointer is beyond the length of the file, 
				//the file has grown so update the length
				if (ftEnt.seekPtr >= ftEnt.inode.length) {
					//grow the inode length by the difference and write the inode
					ftEnt.inode.length += (ftEnt.seekPtr - ftEnt.inode.length);
					ftEnt.inode.toDisk(ftEnt.iNumber);
				}
				if (ftEnt.count > 0) {
					notifyAll();
				} else {
					//This inode is no longer in a read state.
					ftEnt.inode.flag = Inode.USED;
				}
				return bytes_written;
		}// end of switch
		}// end of while
	}// end of write method

	//returns the size in bytes of the file
	public int fsize(FileTableEntry ftEnt) {
		return ftEnt.inode.length;
	}


	//close should wait for all threads to finish with the file before actually
	//closing it 
	public synchronized int close(FileTableEntry ftEnt) {
		if (ftEnt == null) {
		    return -1;
		}
		if (ftEnt.count > 0) {//DEBUG shouldnt have to do this
		    ftEnt.count--;
		}
		//write the inode state back to the disk, only if last thread
		if (ftEnt.count == 0) {
			//Only set the flag if we are the last one out
			ftEnt.inode.flag = Inode.USED;
			ftEnt.inode.toDisk(ftEnt.iNumber);
			return filetable.ffree(ftEnt) ? 0 : -1;
		}
		return 0;
	}

	//sets the seek pointer
	public int seek(FileTableEntry ftEnt, int offset, int whence) {
		int temp_seek = ftEnt.seekPtr;
		switch(whence) {
			case SEEK_SET: 
				temp_seek = offset;
				break;
			case SEEK_CUR: 
				temp_seek += offset;
				break;
			case SEEK_END: 
				temp_seek = fsize(ftEnt) + offset;
				break;
			default:
				return -1;
		}
		if (temp_seek < 0) {
			return -1;
		}
		//set the seek pointer in the entry
		ftEnt.seekPtr = temp_seek;
		return ftEnt.seekPtr;
	}

	public int format(int files) {
		if (files > 0){
			superblock.format(files);
			//directory = new Directory(files);
			//filetable = new FileTable(directory);
			return 0;
		}
		return -1;
	}

	public synchronized int delete(String filename) {
		int iNumber = directory.namei(filename);
		if (iNumber == -1) {
			return -1;
		}
		Inode inode = new Inode(iNumber);
		//no one else is using it so set the delete block
		inode.flag = Inode.DELETE;
		if (!directory.ifree((short)iNumber)) {
			return -1;
		}
		inode.count = 0;
		inode.flag = Inode.USED;
		inode.toDisk(iNumber);
		return 0;
	}

	//Empty out the inode, delete any blocks it frees in the process
	private boolean deallocAllBlocks(FileTableEntry ftEnt) {
		Vector<Short> blocks_freed = ftEnt.inode.deallocAllBlocks(ftEnt.iNumber);
		for (int i = 0; i < blocks_freed.size(); i++) {
			Short block = (Short)blocks_freed.elementAt(i);
			superblock.returnBlock((short)block);
		}
		return true;
	}

	//Given the seek pointer for a file, returns the block number where it can
	//be found, -1 otherwise
	private short seek2block(int f_pos, Inode inode) {
		return inode.findTargetBlock(seek2offset(f_pos));
	}

	private short seek2offset(int f_pos) {
		return (short)(f_pos / Disk.blockSize);
	}
}
