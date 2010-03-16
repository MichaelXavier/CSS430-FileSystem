//Coded by	 :  Michael Xavier and Maridel Legaspi
//File		 :  SuperBlock.java
//Description:  The disk block 0 is the Superblock.  It is used to describe the
//				number of disk blocks, the number of inodes and the block 
//				number of the head of the free list.

import java.util.*;
public class SuperBlock
{
	private final int defaultInodeBlocks = 64;
	public int totalBlocks;			// the number of disk blocks
	public int totalInodes;			// the number of inodes
	public int freeList;			// the block number of the free list's head

	// constructor
	public SuperBlock(int diskSize)
	{
		//read the superblock from disk
		byte[] superBlock = new byte[Disk.blockSize];
		SysLib.rawread(0, superBlock);
		totalBlocks = SysLib.bytes2int(superBlock, 0);
		totalInodes = SysLib.bytes2int(superBlock, 4);
		freeList = SysLib.bytes2int(superBlock, 8);

		// disk contents are valid
		if (totalBlocks == diskSize && totalInodes > 0 && freeList >= 2)
		{
			return;
		}
		else
		{   // need to format disk
			totalBlocks = diskSize;
			SysLib.cout("default format ( " + defaultInodeBlocks + " )\n");
			format(defaultInodeBlocks);
		}
	}

	// sets up disk block 0 to be the superblock, appropriate disk blocks for
	// the inodes and sets up the freelist to take the first two bytes for
	// each remaining disk blocks
	public synchronized void format(int files)
	{
		byte[] superBlock = new byte[512];
		totalBlocks = 1000;
		totalInodes = files;
		freeList = (files % 16) == 0 ? files / 16 + 1 : files / 16 + 2;
		// sets a byte[] to contain the superblock information
		SysLib.int2bytes(totalBlocks, superBlock, 0);
		SysLib.int2bytes(totalInodes, superBlock, 4);
		SysLib.int2bytes(freeList, superBlock, 8);
		// write the superblock contents to the disk
		SysLib.rawwrite(0, superBlock);

		byte[] data = new byte[512];
		for (short i = (short)freeList; i < totalBlocks; i++)
		{
			//zero out the block and fill the rest of the block with 0s
			for (int j = 0; j < Disk.blockSize; j++)
			{
				data[j] = (byte)0;
			}
			//calculate the next freeblock
			//if at the end, the next block in the free list is invalid (0)
			short next_block = (short)((i == totalBlocks - 1) ? 0 : i + 1);
			SysLib.short2bytes(next_block, data, 0);
			//Save back to the disk
			SysLib.rawwrite(i, data);
		}
	}

	// Write back totalBlocks, inodeBlocks, and freeList to disk.
	public void sync()
	{
		byte[] superBlock = new byte[Disk.blockSize];
		SysLib.rawread(0, superBlock);
		SysLib.int2bytes(totalBlocks, superBlock, 0);
		SysLib.int2bytes(totalInodes, superBlock, 4);
		SysLib.int2bytes(freeList, superBlock, 8);
		SysLib.rawwrite(0, superBlock);
	}

	// Dequeue the top block from the free list
	public short getFreeBlock() {
		// save a temp copy of the current head of our "linked list"
		short ret = (short)freeList;
		// read the first free block
		byte[] data = new byte[Disk.blockSize];
		SysLib.rawread(freeList, data);
		// get the next free block from the one to be removed
		freeList = (int)SysLib.bytes2short(data, 0);

		//overwrite first 2 bytes in the block we just pulled off the free list
		SysLib.rawread(ret, data);
		SysLib.short2bytes((short)0, data, 0);
		//write back to disk
		SysLib.rawwrite(ret, data);

		return ret;
	}

	//Enqueue a given block to the end of the free list
	public void returnBlock(short blockNumber) {
		short last_free = (short)freeList; // start at freelist disk block
		short next_free = 0;
		byte[] current_end = null;  
		byte[] new_end = null;  

		SysLib.rawread(blockNumber, new_end);
		SysLib.short2bytes((short)0, new_end, 0);
		SysLib.rawwrite(blockNumber, new_end);

		while (last_free < totalBlocks){ 
		// read the next block in the free list
		SysLib.rawread(last_free, current_end);
		next_free = SysLib.bytes2short(current_end, 0);
		if (next_free == 0) {
			//set block's next free block to the given argument blockNumber
			SysLib.short2bytes(blockNumber, current_end, 0);
			SysLib.rawwrite(last_free, current_end);
			return;
		}
		// we haven't found it yet, keep traversing
		last_free = next_free;
		}
	}
}
