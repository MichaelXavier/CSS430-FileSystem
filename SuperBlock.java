public class SuperBlock {
  private final int defaultInodeBlocks = 64;
  public int totalBLocks;
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
      totalBLocks = diskSize;
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
    // Write back totalBLocks, inodeBlocks, and freeList to disk.
  }

  //FIXME: assuming int return value (of the free block's number) and no arguments
  //FIXME: should this return a block instead?
  public void getFreeBlock() {
    // Dequeue the top block from the free list
  }

  //FIXME: assuming void return value
  public void returnBlock(int blockNumber) {
    //Enqueue a given block to the end of the free list
  }
}
