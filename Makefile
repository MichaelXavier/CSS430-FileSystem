CC 		 = javac
CFLAGS = -Wno-all -classpath ../ThreadOS

all: SuperBlock.class TCB.class FileTableEntry.class FileTable.class Inode.class Directory.class FileSystem.class Kernel.class SysLib.class Test5.class

SuperBlock.class: SuperBlock.java
	$(CC) $(CFLAGS) SuperBlock.java
	rm -f ../ThreadOS/SuperBlock.class
	ln SuperBlock.class ../ThreadOS/SuperBlock.class

TCB.class: TCB.java
	$(CC) $(CFLAGS) TCB.java
	rm -f ../ThreadOS/TCB.class
	ln TCB.class ../ThreadOS/TCB.class

FileTableEntry.class: FileTableEntry.java
	$(CC) $(CFLAGS) FileTableEntry.java
	rm -f ../ThreadOS/FileTableEntry.class
	ln FileTableEntry.class ../ThreadOS/FileTableEntry.class

FileTable.class: FileTable.java
	$(CC) $(CFLAGS) FileTable.java
	rm -f ../ThreadOS/FileTable.class
	ln FileTable.class ../ThreadOS/FileTable.class

Inode.class: Inode.java
	$(CC) $(CFLAGS) Inode.java
	rm -f ../ThreadOS/Inode.class
	ln Inode.class ../ThreadOS/Inode.class

Directory.class: Directory.java
	$(CC) $(CFLAGS) Directory.java
	rm -f ../ThreadOS/Directory.class
	ln Directory.class ../ThreadOS/Directory.class

FileSystem.class: FileSystem.java
	$(CC) $(CFLAGS) FileSystem.java
	rm -f ../ThreadOS/FileSystem.class
	ln FileSystem.class ../ThreadOS/FileSystem.class

Kernel.class: Kernel.java
	$(CC) $(CFLAGS) Kernel.java
	rm -f ../ThreadOS/Kernel.class
	ln Kernel.class ../ThreadOS/Kernel.class

SysLib.class: SysLib.java
	$(CC) $(CFLAGS) SysLib.java
	rm -f ../ThreadOS/SysLib.class
	ln SysLib.class ../ThreadOS/SysLib.class

Test5.class: Test5.java
	$(CC) $(CFLAGS) Test5.java
	rm -f ../ThreadOS/Test5.class
	ln Test5.class ../ThreadOS/Test5.class

clean:
	rm -f *.class
