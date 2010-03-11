public class Inode {
  private final static int iNodeSize = 32;       // fix to 32 bytes
  private final static int directSize = 11;      // # direct pointers

  public static int UNUSED = 0;
  public static int USED = 1;
  public static int READ = 2;
  public static int WRITE = 3;
  public static int DELETE = 4;

  public int length;                             // file size in bytes
  public short count;                            // # file-table entries pointing to this
  public short flag;                             // 0 = unused, 1 = used, ...
  public short direct[] = new short[directSize]; // direct pointers
  public short indirect;                         // a indirect pointer

  Inode() {                                     // a default constructor
    length = 0;
    count = 0;
    flag = USED;
    for ( int i = 0; i < directSize; i++ ) {
      direct[i] = -1;
    }
    indirect = -1;
  }

  Inode(int iNumber) {                         // retrieving inode from disk
    int blockNumber = getBlockNumber();
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

  public void toDisk(int iNumber) {                   // save to disk as the i-th inode
    // design it by yourself.
    //FIXME: inefficient implementation
    int blockNumber = getBlockNumber();
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
      SysLib.short2bytes(direct[i], data, iNumber * iNodeSize + inode_offset);
    }
    
    SysLib.short2bytes(indirect, data, iNumber * iNodeSize + inode_offset);

    SysLib.rawwrite(blockNumber, data);
  }

  short getIndexBlockNumber() {
    return indirect;
  }

  boolean setIndexBlock(short indexBlockNumber) {
    indirect = indexBlockNumber;
  }

  //FIXME: assuming this method is supposed to do an offset inside the direct array
  short findTargetBlock(int offset) {
    if (offset < 0) {
      return -1;
    } else if (offset < directSize) {
      return direct[offset]; 
    }

    //get the index within the indirect block (which is treated like an array)
    int indirect_offset = offset - directSize;
    byte[] indirect_block = new byte[Disk.blockSize];

    //read the entire indirect block
    SysLib.rawread(indirect, indirect_block);

    //read from that indirect block the short at the indirect_offset
    return SysLib.bytes2short(indirect_block, indirect_offset);
  }

  //TODO: more methods, not described in the pdf

  private int getBlockNumber(int iNumber) {
    return 1 + iNumber / 16;
  }
}
