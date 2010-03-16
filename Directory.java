//Modified by:  Michael Xavier and Maridel Legaspi
//File		 :  Directory.java
//Description:  The root '/' directory is the only one predefined by this file
//				system.  All files are maintained in the root directory.  It
//				contains it file name and the corresponding inode number.

public class Directory {
	private static int maxChars = 30; // max characters of each file name

	// Directory entries
	private int fsizes[];		// each element stores a different file size.
	private char fnames[][];	// each element stores a different file name.

	// directory constructor
	public Directory(int maxInumber) { 
		fsizes = new int[maxInumber];		// maxInumber = max files
		for ( int i = 0; i < maxInumber; i++ ) {
			fsizes[i] = 0;					// all file size initialized to 0
		}
		fnames = new char[maxInumber][maxChars];
		String root = "/";					// entry(inode) 0 is "/"
		fsizes[0] = root.length( );			// fsizes[0] is the size of "/".
		root.getChars( 0, fsizes[0], fnames[0], 0 ); // fnames[0] includes "/"
	}

	// initializes the Directory instance with this data[]
	// assumes data[] received directory information from disk
	public void bytes2directory( byte data[] ) {
		int offset = 0;
		for (int i = 0; i < fsizes.length; i++, offset += 4) {
			fsizes[i] = SysLib.bytes2int(data, offset);
		}

		for (int i = 0; i < fnames.length; i++, offset += maxChars * 2) {
			String fname = new String(data, offset, maxChars * 2);
			writeFilename(fname, i);
		}
	}

	// converts and return Directory information into a plain byte array
	// this byte array will be written back to disk
	public byte[] directory2bytes() {
		byte[] ret = new byte[(4 * fsizes.length) + (fsizes.length * maxChars * 2)];
		int offset = 0;
		for (int i = 0; i < fsizes.length; i++, offset += 4) {
			SysLib.int2bytes(fsizes[i], ret, offset);
		}

		for (int i = 0; i < fnames.length; i++, offset += maxChars * 2) {
			String fname = new String(fnames[i], 0, fsizes[i]);
			byte[] str_bytes = fname.getBytes();
			System.arraycopy(str_bytes, 0, ret, offset, str_bytes.length);
		}
		return ret;
	}

	// allocates a new inode number for this filename
	// filename is the one of a file to be created.
	public short ialloc(String filename){
		short ret = 0;
		for (int i = 0; i < fsizes.length; i++) {
			if (fsizes[i] == 0) {
				ret = (short)i;
				fsizes[i] = filename.length();
				writeFilename(filename, i);
				break;
			}
		}
		return ret;
	}

	// deallocates this inumber (inode number)
	// the corresponding file will be deleted.
	public boolean ifree(short iNumber){
		if (iNumber <= 0) {
			return false;
		}
		fsizes[iNumber] = 0;
		//copy over the filename 
		fnames[iNumber] = new char[maxChars];
		return true;
	}

	// returns the inumber corresponding to this filename
	public short namei(String filename) {
		for (int i = 0; i < fsizes.length; i++) {
			String fname = new String(fnames[i], 0, fsizes[i]);
			if (fsizes[i] > 0 && filename.equals(fname)) {
				return (short)i;
		}
	}

	return -1;
	}

	// writes a String fname into its corresponding array (inode) number
	private void writeFilename(String fname, int iNumber) {
		fname.getChars(0, fsizes[iNumber], fnames[iNumber], 0);
	}
}
