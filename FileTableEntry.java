//Coded by	 :  Michael Xavier and Maridel Legaspi
//File		 :  FileTableEntry.java

public class FileTableEntry
{
	public int seekPtr;                 //    a file seek pointer
	public Inode inode;					//    a reference to its inode
	public final short iNumber;         //    this inode number
	public int count;                   //    # threads sharing this entry
	public final String mode;           //    "r", "w", "w+", or "a"

	//constructor
	public FileTableEntry(Inode i, short inumber, String m)
	{
		seekPtr = 0;             // the seek pointer is set to the file top
		inode = i;
		iNumber = inumber;
		count = 1;               // at least one thread is using this entry
		mode = m;                // once access mode is set, it never changes
		if (mode.compareTo("a") == 0)
		{		// if mode is append,
			seekPtr = inode.length;			// seekPtr points to end of file
		}
	}
}