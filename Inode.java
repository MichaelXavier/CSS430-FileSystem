//Modified by:  Michael Xavier and Maridel Legaspi
//File		 :  Inode.java
//Description:  An inode describes a file.  It is stored in the disk after the 
//				superblock.  Each diskblock takes up to 16 inodes.  Each inode
//				stores the length of the corresponding file, the number of file
//				table entries that points to this inode, the inode flag, and
//				12 pointers of the index block

import java.util.Vector;

public class Inode {
	public final static int directSize = 11;       // # direct pointers
	private final static int iNodeSize = 32;       // fix to 32 bytes
	//block numbers are shorts, 2 bytes
	private final static int indirectSize = Disk.blockSize / 2; 
	// inode flags
	public final static short UNUSED = 0;
	public final static short USED = 1;
	public final static short READ = 2;
	public final static short WRITE = 3;
	public final static short DELETE = 4;

	public int length;                 // file size in bytes
	public short count;                // # file-table entries pointing to this
	public short flag;                 // 0 = unused, 1 = used, ...
	public short direct[] = new short[directSize]; // direct pointers
	public short indirect;                         // a indirect pointer

	// constructors
	Inode() {                          // a default constructor
		length = 0;
		count = 0;
		flag = USED;
		for ( int i = 0; i < directSize; i++ ) {
			direct[i] = -1;
		}
		indirect = -1;
	}

	Inode(int iNumber) {              // retrieving inode from disk
		int blockNumber = getBlockNumber(iNumber);
		byte[] data = new byte[Disk.blockSize];
		SysLib.rawread(blockNumber, data);
		int offset = (iNumber % 16) * 32;

		length = SysLib.bytes2int(data, offset);
		offset += 4;
		count = SysLib.bytes2short(data, offset);
		offset += 2;
		flag = SysLib.bytes2short(data, offset);
		offset += 2;

		for (int i = 0; i < directSize; i++, offset += 2) {
			direct[i] = SysLib.bytes2short(data, offset);
		}
		indirect = SysLib.bytes2short(data, offset);
	}

	// save to disk as the i-th inode
	public void toDisk(int iNumber) {                   
		int blockNumber = getBlockNumber(iNumber);
		//read the whole block from the disk
		byte[] data = new byte[Disk.blockSize];
		SysLib.rawread(blockNumber, data);

		int inode_offset = 0;
		// offset to the correct inode
		SysLib.int2bytes(length, data, iNumber * iNodeSize + inode_offset);
		inode_offset += 4;
		SysLib.short2bytes(count, data, iNumber * iNodeSize + inode_offset);
		inode_offset += 2;
		SysLib.short2bytes(flag, data, iNumber * iNodeSize + inode_offset);
		inode_offset += 2;
		for (int i = 0; i < directSize; i++, inode_offset += 2) {
			SysLib.short2bytes(direct[i], data, iNumber * iNodeSize 
				+ inode_offset);
		}
		SysLib.short2bytes(indirect, data, iNumber * iNodeSize + inode_offset);
		SysLib.rawwrite(blockNumber, data);
	}

	// returns the indirect pointer
	public short getIndexBlockNumber() {
		return indirect;
	}

	// sets the index block number
	public boolean setIndexBlock(short indexBlockNumber) {
		indirect = indexBlockNumber;
		return true; 
	}

	public short findTargetBlock(int offset) {
		if (offset < 0) {
			return -1;
		}
		else if (offset < directSize){
			return direct[offset]; 
		}
		//get the index within the indirect block (treated like an array)
		int indirect_offset = offset - directSize;

		//read from that indirect block the short at the indirect_offset
		return SysLib.bytes2short(readIndirectBlock(), indirect_offset);
	}


	public boolean setNextBlockNumber(short blockNumber) {
		//Check direct first
		for (int i = 0; i < directSize; i++) {
			if (direct[i] <= 0) {
				direct[i] = blockNumber;
				return true;
			}
		}
		//Check indirect
		byte[] indirect_block = readIndirectBlock();
		for(short offset_in_indirect = 0; offset_in_indirect < indirectSize *2;
			offset_in_indirect += 2) {
			//The next free indirect will be -1
			if (SysLib.bytes2short(indirect_block, offset_in_indirect) <= 0) {
				//write the block number to the byte array
				SysLib.short2bytes(blockNumber, indirect_block, 
					offset_in_indirect);
				//write the block back to disk return success condition on disk
				return SysLib.rawwrite(indirect, indirect_block) != -1;
			}
		}
		return false;
	}

	public Vector<Short> deallocAllBlocks(int iNumber) {
		Vector blocks_freed = new Vector<Short>();
		//clear the directs
		for (int i = 0; i < directSize; i++) {
			if (direct[i] > 0) {
				blocks_freed.add(direct[i]);
				direct[i] = -1;
			}
		}
		byte[] indirect_block = readIndirectBlock();
		//go through the index block
		for (int i = 0; i < Disk.blockSize / 2; i++) {
			short indirect_value = SysLib.bytes2short(indirect_block, i);
			//If its a valid block, reset it and add it to the return vector
			if (indirect_value > 0) {
				//write 0 to the index block at this pos to invalidate it
				SysLib.short2bytes((short)0, indirect_block, i);
				//save it to the return vector
				blocks_freed.add(new Short(indirect_value));
			}
		}
		//Write the now zeroed indirect block back to disk
		SysLib.rawwrite(indirect, indirect_block);
		toDisk(iNumber);
		return blocks_freed;
	}

	private int getBlockNumber(int iNumber) {
		return 1 + iNumber / 16;
	}

	private byte[] readIndirectBlock() {
		byte[] indirect_block = new byte[Disk.blockSize];
		//read the entire indirect block
		SysLib.rawread(indirect, indirect_block);
		return indirect_block;
	}
}
