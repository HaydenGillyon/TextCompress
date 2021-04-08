TextCompress
by Zagox11 (Student 690031300) - 2021

Program Information
--------------------------------

This program implements Huffman coding to allow the compression and decompression of text files to
reduce the space they take up on disk. This program's functions leave input files as is and the
output data is always written to a separate file.

IMPORTANT: Please see LICENSE.txt for the license that this program is distributed under.

DEPENDENCIES: Java 15.0.2

For more information on this program, check the documentation located in the '/doc' folder.

The TextCompress program is maintained in the following GitHub repository:
https://github.com/Zagox11/TextCompress


Program Functions
--------------------------------
This program has three main functions: compression, modified compression, and decompression.

NOTE:
Modified compression can be used for testing but it isn't recommended for routine use as the
reduction in file size is suboptimal compared to normal compression.
The difference between modified compression and normal compression is that instead of generating
the specific set of codewords for each character in the text (the encoding) from the input file
itself, you provide a second input file from which the encoding is created. This is then used on
the first input file to encode the text.

IMPORTANT:
The program files "TextCompress.class" and "TextCompress$Node.class" must be in the same directory
as each other for the program to run.

All file pathnames that are passed to the program can be either absolute (the full path including
everything from the drive letter to the name of the file) or relative. Relative pathnames
must be provided such that the path to access the files starts from the position of the program
files. Pathnames with spaces are acceptable as long as they are wrapped in quotation marks as shown:
"Path name with spaces.txt".


USAGE
--------------------------------
A video demonstration of compressing and then decompressing a text file on Windows 10 using Command
Prompt is provided in the '/res' folder called 'TextCompress_demonstration.mp4'.

To use this program you must first navigate to the directory containing the program files in a
console that can call Java. The two program files "TextCompress.class" and "TextCompress$Node.class"
can be found in the '/out' folder.

- COMPRESSION -
To compress a text file, type the following command:

java TextCompress -C INPUT_PATHNAME.txt OUTPUT_PATHNAME
OR
java TextCompress --compress INPUT_PATHNAME.txt OUTPUT_PATHNAME

INPUT_PATHNAME - The text file to be compressed which must end in '.txt'.
OUTPUT_PATHNAME - The pathname where the compressed file will be created. This output will always
end in '.bin' and if the name is not provided in this format then '_COMPRESSED.bin' will be
appended to the end. ('_COMPRESSED' will be inserted even if the filename ends in '.bin' already.)

- MODIFIED COMPRESSION -
To compress a text file using another text file's character frequencies for the encoding, type the
following command:

java TextCompress -M INPUT_PATHNAME.txt OUTPUT_PATHNAME ENCODING_SOURCE.txt
OR
java TextCompress --modified INPUT_PATHNAME.txt OUTPUT_PATHNAME ENCODING_SOURCE.txt

INPUT_PATHNAME - The text file to be compressed which must end in '.txt'.
OUTPUT_PATHNAME - The pathname where the compressed file will be created. This output will always
end in '.bin' and if the name is not provided in this format then '_COMPRESSED.bin' will be
appended to the end. ('_COMPRESSED' will be inserted even if the filename ends in '.bin' already.)
ENCODING_SOURCE - The text file to generate the encoding from which must end in '.txt'.

- DECOMPRESSION -
To decompress a file that has been compressed using TextCompress, type the following command:

java TextCompress -D INPUT_PATHNAME.bin OUTPUT_PATHNAME
OR
java TextCompress --decompress INPUT_PATHNAME.bin OUTPUT_PATHNAME

INPUT_PATHNAME - The compressed file to be decompressed which must end in '.bin'.
OUTPUT_PATHNAME - The pathname where the decompressed text file will be created. This output will
always end in '.txt' and if the name is not provided in this format then '_DECOMPRESSED.txt' will be
appended to the end. ('_DECOMPRESSED' will be inserted even if the filename ends in '.txt' already.)