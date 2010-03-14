public class Directory {
  private static int maxChars = 30; // max characters of each file name

  // Directory entries
  private int fsizes[];        // each element stores a different file size.
  private char fnames[][];    // each element stores a different file name.

  public Directory(int maxInumber) { // directory constructor
    fsizes = new int[maxInumber];     // maxInumber = max files
    for ( int i = 0; i < maxInumber; i++ ) {
      fsizes[i] = 0;                 // all file size initialized to 0
    }
    fnames = new char[maxInumber][maxChars];
    String root = "/";                // entry(inode) 0 is "/"
    fsizes[0] = root.length( );        // fsizes[0] is the size of "/".
    root.getChars( 0, fsizes[0], fnames[0], 0 ); // fnames[0] includes "/"
  }

  // assumes data[] received directory information from disk
  // initializes the Directory instance with this data[]
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

  public byte[] directory2bytes() {
    // converts and return Directory information into a plain byte array
    // this byte array will be written back to disk
    // note: only meaningfull directory information should be converted
    // into bytes.
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

  //FIXME: this converts the integer index from fsizes to a short and is potentially dangerous
  public short ialloc(String filename) {
    // filename is the one of a file to be created.
    // allocates a new inode number for this filename
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

  //NOTE: assuming that we only return false if the inode refers to the
  //directory itself and otherwise returns true.
  public boolean ifree(short iNumber) {
    if (iNumber == 0) {
      return false;
    }
    // deallocates this inumber (inode number)
    // the corresponding file will be deleted.
    fsizes[iNumber] = 0;
    return true;
  }

  //NOTE: assuming that if the filename is not found that we are supposed to return -1
  public short namei(String filename) {
    // returns the inumber corresponding to this filename
    for (int i = 0; i < fsizes.length; i++) {
      String fname = new String(fnames[i], 0, fsizes[i]);
      if (filename.equals(fname)) {
        return (short)i;
      }
    }

    return -1;
  }

  private void writeFilename(String fname, int iNumber) {
    fname.getChars(0, fsizes[iNumber], fnames[iNumber], 0);
  }
}
