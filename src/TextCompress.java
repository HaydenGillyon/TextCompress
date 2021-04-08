import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;

/**
 * Main class of this program which allows the compression of text files using
 * Huffman coding.
 * @author Zagox11 (Student 690031300)
 * @version 1.0
 */
public class TextCompress {
    /**
     * Represents the nodes of a Huffman tree. This includes leaf nodes which
     * store character values, and internal nodes which use the default
     * character value.
     */
    static class Node {
        // Attributes
        char character = '\u0000';
        int weight;
        Node left;
        Node right;

        /**
         * Returns this node's weight.
         * @return node weight
         */
        public int getWeight() {
            return weight;
        }
    }

    /**
     * Counts the frequency of all the characters in the input file and creates
     * leaf nodes for them which each hold a character and it's count which is
     * stored in the weight attribute.
     * @param file input file to get characters and their weights from
     * @param modified true if modified compression is being used and forces
     *                 the generation of a set of unicode character's leaf
     *                 nodes, whether they are in the input or not, so that the
     *                 resulting encoding can be used more universally across
     *                 other datasets
     * @return list of leaf nodes representing characters' frequencies
     * @throws IOException
     */
    private static LinkedList<Node> getLeafNodes(File file, boolean modified)
            throws IOException {
        // Get frequencies of characters from file and add them to a hashmap
        LinkedHashMap<Character, Integer> frequencies = new LinkedHashMap<>();
        try (BufferedReader reader
                     = new BufferedReader(new FileReader(file))) {
            int nextCharCode;
            char nextChar;
            while ((nextCharCode = reader.read()) != -1) {
                nextChar = (char)nextCharCode;
                // Create character entry or increment entry value by 1
                frequencies.merge(nextChar, 1, Integer::sum);
            }
        } catch (IOException e) {
            throw new IOException("An IO exception occurred when trying to make"
                    + " the character frequency map.");
        }

        /*
        Ensure some unicode characters are always included when performing
        modified encoding to avoid unrepresented characters when decoding.
         */
        if (modified) {
            int n = 32;
            while (true) {
                // Convert decimal character code to a char and put it if absent
                frequencies.putIfAbsent((char) n, 1);
                n++;
                if (n > 250) break; // End of needed characters
                if (n > 126 && n < 160) n = 160; // Skip unneeded characters
            }
        }

        /*
        Create leaf nodes and add them into the list, then sort the list by
        the node weights (character frequencies).
         */
        LinkedList<Node> leafNodes = new LinkedList<>();
        Node node;
        for (Entry<Character, Integer> entry : frequencies.entrySet()) {
            node = new Node();
            node.character = entry.getKey();
            node.weight = entry.getValue();
            leafNodes.add(node);
        }
        leafNodes.sort(Comparator.comparing(Node::getWeight));
        return leafNodes;
    }

    /**
     * Recursively searches the provided Huffman tree, assigning each leaf node
     * a binary codeword and entering the character-codeword pair into the given
     * encodingMap. Defines each codeword based on the leaf node's position in
     * the tree such that, from the root, each move to the left appends a 0 and
     * each move to the right appends a 1.
     * @param encodingMap encoding that stores mappings between characters and
     *                    their assigned codewords
     * @param node the node to be checked
     * @param code the binary string that should be assigned as the codeword
     *             should this node be a leaf
     */
    private static void findHuffmanCodes(
            LinkedHashMap<Character, String> encodingMap, Node node,
            StringBuilder code) {
        StringBuilder thisCode = new StringBuilder(code);
        // BASE: Check if node is a leaf node (has non-default character value)
        if (node.character != '\u0000') {
            encodingMap.put(node.character, thisCode.toString());
            return;
        }

        // Traverse left branch
        findHuffmanCodes(encodingMap, node.left, thisCode.append('0'));

        // Reset thisCode by removing the last appended 0
        thisCode.deleteCharAt(thisCode.length() - 1);

        // Traverse right branch
        findHuffmanCodes(encodingMap, node.right, thisCode.append('1'));
    }

    /**
     * Generates the Huffman encoding, a LinkedHashMap of characters and their
     * binary codewords, for the specified file and returns it.
     * @param file the file to generate the encoding for
     * @param modified if true, signals that the file being passed is not the
     *                 one that the encoding will be used to compress and so
     *                 a basic set of characters will always be included
     *                 regardless of whether they are inside the passed file or
     *                 not
     * @return the Huffman encoding generated from the passed file
     * @throws IOException
     */
    private static LinkedHashMap<Character, String>
            getHuffmanEncoding(File file, boolean modified) throws IOException {

        Queue<Node> leafNodeQueue = getLeafNodes(file, modified);
        Queue<Node> internalNodeQueue = new LinkedList<>();
        LinkedHashMap<Character, String> encodingMap = new LinkedHashMap<>();

        /*
        If file contains only 1 type of character then assign 1 as it's code and
        return it inside encodingMap.
         */
        if (leafNodeQueue.size() == 1) {
            encodingMap.put(leafNodeQueue.remove().character, "1");
            return encodingMap;
        }

        // Create Huffman tree
        Node internalNode;
        Node leftNode;
        Node rightNode;
        while (leafNodeQueue.size() + internalNodeQueue.size() > 1) {
            // Left Node
            leftNode = getNextNode(leafNodeQueue, internalNodeQueue);

            // Right Node
            rightNode = getNextNode(leafNodeQueue, internalNodeQueue);

            internalNode = new Node();
            internalNode.weight = leftNode.weight + rightNode.weight;
            internalNode.left = leftNode;
            internalNode.right = rightNode;
            internalNodeQueue.add(internalNode);
        }

        // Place the last node in a variable as it is now the root of the tree
        Node huffmanTree = internalNodeQueue.remove();

        // Traverse Huffman tree to find character codes and fill encodingMap
        findHuffmanCodes(encodingMap, huffmanTree, new StringBuilder());

        return encodingMap;
    }

    /**
     * Returns the next node to add into the Huffman tree which is the one that
     * has the smallest weight attribute out of all nodes in leafNodeQueue and
     * internalNodeQueue.
     * @param leafNodeQueue queue of leaf nodes which have yet to be attached
     *                      to any other nodes in the Huffman tree
     * @param internalNodeQueue queue of nodes that don't represent characters
     *                          and link to other nodes to the left and right in
     *                          the Huffman tree
     * @return the smallest node out of both leafNodeQueue and internalNodeQueue
     */
    private static Node getNextNode(Queue<Node> leafNodeQueue,
                                    Queue<Node> internalNodeQueue) {
        Node nextNode;
        Node nextLeafNode = leafNodeQueue.peek();
        Node nextInternalNode = internalNodeQueue.peek();
        if (!(nextLeafNode == null) && !(nextInternalNode == null)) {
            nextNode = (leafNodeQueue.peek().getWeight()
                    > internalNodeQueue.peek().getWeight())
                    ? internalNodeQueue.remove() : leafNodeQueue.remove();
        } else if (nextInternalNode == null) {
            nextNode = leafNodeQueue.remove();
        } else {
            nextNode = internalNodeQueue.remove();
        }
        return nextNode;
    }

    /**
     * Takes a string storing a sequence of binary digits and converts it into,
     * and then returns, an array of bytes. Deals with sequences that don't fit
     * exactly into bytes by appending as many 0s to the end of the sequence as
     * necessary to make the sequence divisible into 8-bit bytes before
     * converting.
     * @param binaryString the binary string to be converted
     * @return a byte array with the order of bits preserved from binaryString
     */
    private static byte[] getBinary(String binaryString) {
        StringBuilder sb = new StringBuilder(binaryString);
        while (sb.length() % 8 != 0) {
            sb.append('0');
        }
        binaryString = sb.toString();

        byte[] data = new byte[binaryString.length() / 8];

        for (int i = 0; i < binaryString.length(); i++) {
            char c = binaryString.charAt(i);
            if (c == '1') {
                data[i >> 3] |= 0x80 >> (i & 0x7);
            }
        }
        return data;
    }

    /**
     * First, creates a LinkedHashMap from encodingMap with the key-value pairs
     * flipped so that the compressed file can be decoded later, serialising it
     * and writing the resulting bytes to the output file. Next, reads and
     * encodes the data from inputFile by replacing each character with its
     * corresponding binary codeword from encodingMap.
     * @param inputFile file to be compressed
     * @param outputFile file to write the flipped encoding map and encoded data
     *                   to
     * @param encodingMap map that links characters to their binary string
     *                    codewords
     * @throws IOException
     */
    private static void encodeAndWrite(File inputFile,
                                       File outputFile,
                                       LinkedHashMap<Character, String>
                                               encodingMap) throws IOException {

        /*
        Create LinkedHashMap with flipped key-value pairs for storing in the
        output file.
         */
        LinkedHashMap<String, Character> flippedEncodingMap
                = new LinkedHashMap<>();
        for (Entry<Character, String> entry : encodingMap.entrySet()) {
            flippedEncodingMap.put(entry.getValue(), entry.getKey());
        }

        try (BufferedReader in = new BufferedReader(new FileReader(inputFile));
             DataOutputStream out
                     = new DataOutputStream(new BufferedOutputStream(
                     new FileOutputStream(outputFile)));
             ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
             ObjectOutputStream objectByteArrayOut
                     = new ObjectOutputStream(byteArrayOut)) {
            /*
            Serialise flippedEncodingMap to a byte array and measure it's size.
            Write the size to the output stream in 32 bits (as an int) and then
            also write the byte array itself to the output stream.
             */
            objectByteArrayOut.writeObject(flippedEncodingMap);
            objectByteArrayOut.flush();

            int encodingSize = byteArrayOut.size();
            byte[] serialisedMap = byteArrayOut.toByteArray();

            out.writeInt(encodingSize);
            out.write(serialisedMap);

            // For each character in input, write it's matching code to output
            int nextCharCode;
            char nextChar;
            StringBuilder writeBuffer = new StringBuilder();
            String code;
            while ((nextCharCode = in.read()) != -1) {
                nextChar = (char)nextCharCode;
                code = encodingMap.get(nextChar);
                writeBuffer.append(code);
            }

            byte[] allData = getBinary(writeBuffer.toString());
            out.write(allData);
            out.flush();
        } catch (IOException e) {
            throw new IOException("An IO exception occurred when trying to"
                    + " compress the input file's contents into the output"
                    + " file.");
        }
    }

    /**
     * Carries out the compression function of this program by checking that the
     * provided filenames are valid, creating the output file, and then
     * beginning compression of the input file's data, writing it to the output.
     * This data is prepended by an encoding that is used by the decompression
     * function of this program to decode the text again.
     * @param inputFilename pathname of the input file which can be either
     *                      absolute or relative to the location of these
     *                      program files
     * @param outputFilename pathname of the output file to be created which can
     *                       be either absolute or relative to the location of
     *                       these program files
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private static void compress(String inputFilename,
                                 String outputFilename)
            throws IOException, IllegalArgumentException {
        
        if (!inputFilename.endsWith(".txt")) {
            throw new IllegalArgumentException("File to be"
                    + " compressed must end in '.txt'.");
        }

        // Format output filename
        if (!outputFilename.endsWith(".bin")) {
            outputFilename += "_COMPRESSED.bin";
        } else {
            outputFilename = outputFilename.substring(0,
                    outputFilename.length() - 4)
                    + "_COMPRESSED"
                    + outputFilename.substring(outputFilename.length() - 4);
        }

        // Create file objects
        File inputFile = new File(inputFilename);
        File outputFile = new File(outputFilename);

        if (!inputFile.exists()) {
            throw new IllegalArgumentException("File to be compressed was"
                    + " not found in the specified location.");
        } else if (inputFile.length() < 4) {
            throw new IllegalArgumentException("File to be compressed must not"
                    + " be less than 4 bytes in length.");
        }

        // Create output file
        try {
            if (!outputFile.createNewFile()) {
                throw new IllegalArgumentException("A compressed file already"
                        + " exists in the given location with that name!");
            }
        } catch (IOException e) {
            throw new IOException("An IO exception occurred when trying to"
                    + " create the output file.");
        }

        // Generate Huffman encoding for input file
        LinkedHashMap<Character, String> encodingMap
                = getHuffmanEncoding(inputFile, false);

        /*
        Encode the contents of inputFile by replacing characters with codewords
        from encodingMap and then write the result to outputFile.
         */
        encodeAndWrite(inputFile, outputFile, encodingMap);
    }

    /**
     * Carries out the modified compression function of this program by checking
     * that the provided filenames are valid, creating the output file,
     * generating the encoding from the provided encoding source file, and then
     * beginning compression of the input file's data using it, writing the
     * result to the output. This data is prepended by the encoding which is
     * used by the decompression function of this program to decode the text
     * again.
     * @param inputFilename pathname of the input file which can be either
     *                      absolute or relative to the location of these
     *                      program files
     * @param outputFilename pathname of the output file to be created which can
     *                       be either absolute or relative to the location of
     *                       these program files
     * @param encodingSourceFilename pathname of the file to generate the
     *                               Huffman encoding from which can be either
     *                               absolute or relative to the location of
     *                               these program files
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private static void compressModified(String inputFilename,
                                         String outputFilename,
                                         String encodingSourceFilename)
            throws IOException, IllegalArgumentException {

        if (encodingSourceFilename == null) {
            throw new IllegalArgumentException("Not enough arguments for"
                    + " modified compression. Please provide a file to generate"
                    + " the Huffman encoding from.");
        }

        if (!inputFilename.endsWith(".txt")) {
            throw new IllegalArgumentException("File to be"
                    + " compressed must end in '.txt'.");
        } else if (!encodingSourceFilename.endsWith(".txt")) {
            throw new IllegalArgumentException("Text to generate Huffman"
                    + " encoding from must end in '.txt'.");
        }

        // Format output filename
        if (!outputFilename.endsWith(".bin")) {
            outputFilename += "_COMPRESSED.bin";
        } else {
            outputFilename = outputFilename.substring(0,
                    outputFilename.length() - 4)
                    + "_COMPRESSED"
                    + outputFilename.substring(outputFilename.length() - 4);
        }

        // Create file objects
        File inputFile = new File(inputFilename);
        File outputFile = new File(outputFilename);
        File encodingSource = new File(encodingSourceFilename);

        if (!inputFile.exists()) {
            throw new IllegalArgumentException("File to be compressed was"
                    + " not found in the specified location.");
        } else if (!encodingSource.exists()) {
            throw new IllegalArgumentException("File to generate Huffman"
                    + " encoding from was not found in the specified"
                    + " location.");
        } else if (inputFile.length() < 4 || encodingSource.length() < 4) {
            throw new IllegalArgumentException("File to be compressed and the"
                    + "file to generate Huffman encoding from must not be less"
                    + " than 4 bytes in length each.");
        }

        // Create output file
        try {
            if (!outputFile.createNewFile()) {
                throw new IllegalArgumentException("A compressed file already"
                        + " exists in the given location with that name!");
            }
        } catch (IOException e) {
            throw new IOException("An IO exception occurred when trying to"
                    + " create the output file.");
        }

        // Generate Huffman encoding for encoding source file
        LinkedHashMap<Character, String> encodingMap
                = getHuffmanEncoding(encodingSource, true);

        /*
        Encode the contents of inputFile by replacing characters with codewords
        from encodingMap and then write the result to outputFile.
         */
        encodeAndWrite(inputFile, outputFile, encodingMap);
    }

    /**
     * Carries out the decompression function of this program by first checking
     * that the provided filenames are valid and creating the output file.
     * Then, reads through the compressed input file and decompresses the
     * contents, writing the result to the output file. The encoding stored at
     * the start of the input file is first deserialised and then used to find
     * the characters that should be written to the output for each codeword in
     * the input.
     * @param inputFilename pathname of the compressed input file which can be
     *                      either absolute or relative to the location of these
     *                      program files
     * @param outputFilename pathname of the output file to be created that the
     *                       decompressed data will be written to which can be
     *                       either absolute or relative to the location of
     *                       these program files
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private static void decompress(String inputFilename,
                                   String outputFilename)
            throws IOException, IllegalArgumentException {

        if (!inputFilename.endsWith(".bin")) {
            throw new IllegalArgumentException("File to be"
                    + " decompressed must end in '.bin'.");
        }

        // Format output filename
        if (!outputFilename.endsWith(".txt")) {
            outputFilename += "_DECOMPRESSED.txt";
        } else {
            outputFilename = outputFilename.substring(0,
                    outputFilename.length() - 4)
                    + "_DECOMPRESSED"
                    + outputFilename.substring(outputFilename.length() - 4);
        }

        // Create file objects
        File inputFile = new File(inputFilename);
        File outputFile = new File(outputFilename);

        if (!inputFile.exists()) {
            throw new IllegalArgumentException("File to be decompressed was"
                    + " not found in the specified location.");
        } else if (inputFile.length() < 50) {
            throw new IllegalArgumentException("Compressed file to be"
                    + " decompressed cannot be less than 50 bytes in length.");
        }

        // Create output file
        try {
            if (!outputFile.createNewFile()) {
                throw new IllegalArgumentException("A decompressed file already"
                        + " exists in the given location with that name!");
            }
        } catch (IOException e) {
            throw new IOException("An IO exception occurred when trying to"
                    + " create the output file.");
        }

        // Decode input file contents and write result to output file
        try (DataInputStream in
                     = new DataInputStream(new BufferedInputStream(
                new FileInputStream(inputFile)));
             BufferedWriter out
                     = new BufferedWriter(new FileWriter(outputFile))
             ) {
            // Deserialise character encoding from input stream
            int encodingSize = in.readInt();
            byte[] serialisedMap = in.readNBytes(encodingSize);
            LinkedHashMap<String, Character> characterEncoding;

            try (ByteArrayInputStream byteArrayIn
                         = new ByteArrayInputStream(serialisedMap);
                    ObjectInputStream objectByteArrayIn
                            = new ObjectInputStream(byteArrayIn)) {

                characterEncoding = (LinkedHashMap<String, Character>)
                        objectByteArrayIn.readObject();
            }

            // Use the character encoding to decode the rest of the input data
            StringBuilder readBuffer = new StringBuilder();
            int nextByte;
            int checked = 0;
            Character checkCharacter;
            while ((nextByte = in.read()) != -1) {
                /*
                Convert next byte to binary strings with leading 0s and append
                to readBuffer.
                 */
                readBuffer.append(
                        String.format("%8s",
                                Integer.toBinaryString(nextByte))
                                .replaceAll(" ", "0"));

                /*
                Check a portion of bits going from the start of readBuffer up to
                it's length for a matching codeword and continue doing this with
                the portion being 1 bit more each time until a match is found.
                Then, write the corresponding character to the output stream and
                remove the matching segment of bits from the start of
                readBuffer.
                 */
                for (int bits = checked; bits < readBuffer.length(); bits++) {
                    if ((checkCharacter
                            = characterEncoding.get(
                                    readBuffer.substring(0, bits+1))) != null) {
                        out.write(checkCharacter);
                        readBuffer.delete(0, bits+1);
                        checked = 0;
                        bits = -1;  // So that bits = 0 once incremented by loop
                    } else if (bits == readBuffer.length() - 1) {
                        checked = bits + 1;
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("An IO exception occurred when trying to"
                    + " decompress the input file's contents into the output"
                    + " file.");
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IllegalArgumentException("The provided input file cannot"
                    + " be decoded as it does not contain a correctly formatted"
                    + " character encoding object.");
        }
    }

    /**
     * Main method where the program is run from. Takes up to 4 argument strings
     * from the standard input (passed when the program is called) which control
     * how the application functions.
     * @param args passed strings to control the program
     */
    public static void main(String[] args) {
        try {
            if (args.length < 3) {
                throw new IllegalArgumentException("Not enough arguments.");
            }
            String option = args[0];
            String filename1 = args[1];
            String filename2 = args[2];
            String filename3;
            if (args.length >= 4) {
                filename3 = args[3];
            } else {
                filename3 = null;
            }
            switch (option) {
                case "-C", "--compress" -> compress(filename1, filename2);
                case "-M", "--modified" -> compressModified(filename1,
                        filename2, filename3);
                case "-D", "--decompress" -> decompress(filename1, filename2);
                default -> throw new IllegalArgumentException("Option argument"
                        + " not recognised.");
            }
        } catch (IllegalArgumentException | IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
