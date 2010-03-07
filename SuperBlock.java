public class SuperBlock {
  private final int defaultInodeBlocks = 64;
  public int totalBlocks;
  public int totalInodes;
  public int freeList;

  public SuperBlock(int diskSize) {
    // read the superblock from disk
    byte[] superBlock = new byte[Disk.blockSize];
    SysLib.rawread(0, superBlock);
    totalBlocks = SysLib.bytes2int(superblock, 0);
    totalInodes = SysLib.bytes2int(superblock, 4);
    freeList = SysLib.bytes2int(superblock, 8);
    
    if (totalBlocks == diskSize && totalInodes > 0 && freeList >= 2) {
      // disk contents are valid
    } else {
      // need to format disk
      totalBlocks = diskSize;
      format(defaultInodeBlocks);
    }
  }

  //FIXME: should any of the below be synchronized?

  //FIXME: I'm *assuming* this method is here from the constructor but it is
  // not mentioned explicitly anywhere else in the project documentation. Is
  // the return type still int?
  public int format(int files) {
    //TODO
  }

  //FIXME: assuming void return value and no arguments
  public void sync() {
    // Write back totalBlocks, inodeBlocks, and freeList to disk.
    int offset = 0;
    byte[] superBlock = new byte[Disk.blockSize];

    SysLib.int2bytes(totalBlocks, superBlock, 0);
    SysLib.int2bytes(totalInodes, superBlock, 4);
    SysLib.int2bytes(freeList, superBlock, 8);

    SysLib.rawwrite(0, superBlock);
  }

  //FIXME: assuming int return value (of the free block's number) and no arguments
  //FIXME: should this return a block instead?
  public int getFreeBlock() {
    // Dequeue the top block from the free list
    // save a temp copy of the current head of our "linked list"
    int ret = freeList;
    //NOTE: assuming the first 2 (not 4) bytes of a block is the next free number 
    // read the first free block
    byte[] data = new byte[Disk.blockSize];
    SysLib.rawread(freeList, data);
    // get the next free block from the one to be removed
    //NOTE: our notes say the first 2 characters of the block are the next t free block but that cannot be possible if it's an integer, which occupies 4 bytes
    int next_free = SysLib.bytes2int(data, 0);

    freeList = next_free;

    return ret;
  }

  //FIXME: assuming void return value
  //NOTE: assuming that once we reach the end of the free blocks, the last one's "next block" will be -1
  //FIXME: this algorithm requires us to traverse
  public void returnBlock(int blockNumber) {
    //Enqueue a given block to the end of the free list
    
    // first traverse the free list until we get to the end
    int last_free = freeList;
    int next_free = -1;
    byte[] current_end;  
    byte[] new_end;  
    
    //Set the new end up so it has a -1 as it's next pointer.
    SysLib.rawread(blockNumber, new_end);
    SysLib.int2bytes(-1, new_end, 0);
    SysLib.rawwrite(blockNumber, new_end);
    
    while (current_end < totalBlocks) {
      // read the next block in the free list
      SysLib.rawread(last_free, current_end);
      //if the first 4 bytes are -1 then we have hit the end
      next_free = SysLib.bytes2int(current_end, 0);
      if (next_free == -1) {
        //set that block's next free block to the given argument blockNumber
        SysLib.int2bytes(blockNumber, current_end, 0);

        SysLib.rawwrite(last_free, current_end);

        //set the given block's next pointer to be -1, indicating it is at
        //the end of the list
        return;
      }

      // we haven't found it yet, keep traversing
      last_free = next_free;
    }
  }
}
