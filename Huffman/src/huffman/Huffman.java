package huffman;

import java.util.*;
import java.io.*;

/**
 * Huffman instances provide reusable Huffman Encoding Maps for compressing and
 * decompressing text corpi with comparable distributions of characters.
 */
public class Huffman {

	// -----------------------------------------------
	// Construction
	// -----------------------------------------------

	private HuffNode trieRoot;
	private TreeMap<Character, String> encodingMap = new TreeMap<>();

	/**
	 * Creates the Huffman Trie and Encoding Map using the character distributions
	 * in the given text corpus
	 * 
	 * @param corpus A String representing a message / document corpus with
	 *               distributions over characters that are implicitly used
	 *               throughout the methods that follow. Note: this corpus ONLY
	 *               establishes the Encoding Map; later compressed corpi may
	 *               differ.
	 */
	Huffman(String corpus) {
		// Finds the distribution of characters in the given corpus
		HashMap<Character, Integer> distributions = new HashMap<>();
		for (int i = 0; i < corpus.length(); i++) {
			char letter = corpus.charAt(i);
			if (distributions.containsKey(letter)) {
				distributions.put(letter, distributions.get(letter) + 1);
			} else {
				distributions.put(letter, 1);
			}
		}

		// Builds the Huffman Trie
		PriorityQueue<HuffNode> characters = new PriorityQueue<>();
		for (char key : distributions.keySet()) {
			characters.add(new HuffNode(key, distributions.get(key)));
		}
		// Sets the trieRoot
		HuffNode parent = new HuffNode('\0', 0);
		while (characters.size() != 1) {
			HuffNode right = characters.poll();
			HuffNode left = characters.poll();
			parent = new HuffNode('\0', right.count + left.count);
			parent.right = right;
			parent.left = left;
			characters.add(parent);
		}

		trieRoot = parent;

		// Creates the Encoding Map
		createEncodingMap(trieRoot, "");
	}

	// Create the Encoding Map recursively
	public void createEncodingMap(HuffNode node, String encoding) {
		if (node.isLeaf()) {
			encodingMap.put(node.character, encoding);
			return;
		}

		createEncodingMap(node.left, encoding + "1");
		createEncodingMap(node.right, encoding + "0");

	}

	// -----------------------------------------------
	// Compression
	// -----------------------------------------------

	/**
	 * Compresses the given String message / text corpus into its Huffman coded
	 * bitstring, as represented by an array of bytes. Uses the encodingMap field
	 * generated during construction for this purpose.
	 * 
	 * @param message String representing the corpus to compress.
	 * @return {@code byte[]} representing the compressed corpus with the Huffman
	 *         coded bytecode. Formatted as 3 components: (1) the first byte
	 *         contains the number of characters in the message, (2) the bitstring
	 *         containing the message itself, (3) possible 0-padding on the final
	 *         byte.
	 */
	public byte[] compress(String message) {
		ByteArrayOutputStream compressed = new ByteArrayOutputStream();
		
		// Writes Message Length
		compressed.write(message.length());

		// Writes Message Content
		String content = "";
		for (int i = 0; i < message.length(); i++) {
			content += encodingMap.get(message.charAt(i));
		}

		while (content.length() >= 8) {
			int parsed = Integer.parseInt(content.substring(0, 8), 2);
			compressed.write((byte) parsed);
			content = content.substring(8);
		}

		// Adds Padding
		int checkPadd = content.length() % 8;
		if (checkPadd != 0) {
			int padding = 8 - checkPadd;
			for (int i = 0; i < padding; i++) {
				content += "0";
			}
		}
		compressed.write((byte) Integer.parseInt(content, 2));

		return compressed.toByteArray();

	}

	// -----------------------------------------------
	// Decompression
	// -----------------------------------------------

	/**
	 * Decompresses the given compressed array of bytes into their original, String
	 * representation. Uses the trieRoot field (the Huffman Trie) that generated the
	 * compressed message during decoding.
	 * 
	 * @param compressedMsg {@code byte[]} representing the compressed corpus with
	 *                      the Huffman coded bytecode. Formatted as 3 components:
	 *                      (1) the first byte contains the number of characters in
	 *                      the message, (2) the bitstring containing the message
	 *                      itself, (3) possible 0-padding on the final byte.
	 * @return Decompressed String representation of the compressed bytecode
	 *         message.
	 */
	public String decompress(byte[] compressedMsg) {
		String decompressed = "";
		int msgLength = compressedMsg[0];
		
		// bitstring representation
		for (int i = 1; i < compressedMsg.length; i++) {
			String temp = "";
			if (compressedMsg[i] < 0) {
				compressedMsg[i] += 128;
				temp = String.format("%7s", Integer.toBinaryString(compressedMsg[i])).replace(' ', '0');
				temp = "1" + temp;
			} else {
				temp = String.format("%8s", Integer.toBinaryString(compressedMsg[i])).replace(' ', '0');
			}

			decompressed += temp;
		}

		return decode(decompressed, msgLength);

	}
	
	// String representation
	public String decode(String decompressed, int msgLength) {
		String result = "";
		int startIndex = 0;
		int endIndex = startIndex + 1;

		Map<String, Character> reverseMap = new HashMap<>();
		for (Character key : encodingMap.keySet()) {
			reverseMap.put(encodingMap.get(key), key);
		}

		while(result.length()<msgLength){
			String letter = decompressed.substring(startIndex, endIndex);
			if (reverseMap.containsKey(letter)) {
				result += reverseMap.get(letter);
				startIndex = endIndex;
			}
			endIndex++;
		}
		return result;
	}

	// -----------------------------------------------
	// Huffman Trie
	// -----------------------------------------------

	/**
	 * Huffman Trie Node class used in construction of the Huffman Trie. Each node
	 * is a binary (having at most a left and right child), contains a character
	 * field that it represents (in the case of a leaf, otherwise the null character
	 * \0), and a count field that holds the number of times the node's character
	 * (or those in its subtrees, in the case of inner nodes) appear in the corpus.
	 */
	private static class HuffNode implements Comparable<HuffNode> {

		HuffNode left, right;
		char character;
		int count;

		HuffNode(char character, int count) {
			this.count = count;
			this.character = character;
		}

		public boolean isLeaf() {
			return left == null && right == null;
		}

		public int compareTo(HuffNode other) {
			return this.count - other.count;
		}

	}

}
