all: Superblock.class TCB.class FileTableEntry.class Inode.class Directory.class 

Superblock.class: Superblock.java
	javac -classpath ../../ThreadOS Superblock.java
	rm -f ../../ThreadOS/Superblock.class
	ln Superblock.class ../../ThreadOS/Superblock.class

TCB.class: TCB.java
	javac -classpath ../../ThreadOS TCB.java
	rm -f ../../ThreadOS/TCB.class
	ln TCB.class ../../ThreadOS/TCB.class

FileTableEntry.class: FileTableEntry.java
	javac -classpath ../../ThreadOS FileTableEntry.java
	rm -f ../../ThreadOS/FileTableEntry.class
	ln FileTableEntry.class ../../ThreadOS/FileTableEntry.class

Inode.class: Inode.java
	javac -classpath ../../ThreadOS Inode.java
	rm -f ../../ThreadOS/Inode.class
	ln Inode.class ../../ThreadOS/Inode.class

Directory.class: Directory.java
	javac -classpath ../../ThreadOS Directory.java
	rm -f ../../ThreadOS/Directory.class
	ln Directory.class ../../ThreadOS/Directory.class

clean:
	rm -f *.class
