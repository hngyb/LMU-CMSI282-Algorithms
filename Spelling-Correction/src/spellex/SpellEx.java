package spellex;

import java.util.*;

public class SpellEx {

	// Note: Not quite as space-conscious as a Bloom Filter,
	// nor a Trie, but since those aren't in the JCF, this map
	// will get the job done for simplicity of the assignment
	private Map<String, Integer> dict;

	// For your convenience, you might need this array of the
	// alphabet's letters for a method
	private static final char[] LETTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();

	/**
	 * Constructs a new SpellEx spelling corrector from a given "dictionary" of
	 * words mapped to their frequencies found in some corpus (with the higher
	 * counts being the more prevalent, and thus, the more likely to be suggested)
	 * 
	 * @param words The map of words to their frequencies
	 */
	SpellEx(Map<String, Integer> words) {
		dict = new HashMap<>(words);
	}

	/**
	 * Returns the edit distance between the two input Strings s0 and s1 based on
	 * the minimal number of insertions, deletions, replacements, and transpositions
	 * required to transform s0 into s1
	 * 
	 * @param s0 A "start" String
	 * @param s1 A "destination" String
	 * @return The minimal edit distance between s0 and s1
	 */
	public static int editDistance(String s0, String s1) {
		int len1 = s0.length();
		int len2 = s1.length();

		int[][] d = new int[len1 + 1][len2 + 1];

		for (int i = 0; i <= len1; i++) {
			d[i][0] = i;
		}
		for (int j = 0; j <= len2; j++) {
			d[0][j] = j;
		}

		for (int i = 1; i <= len1; i++) {
			for (int j = 1; j <= len2; j++) {
				if (s0.charAt(i - 1) == s1.charAt(j - 1)) {
					d[i][j] = d[i - 1][j - 1];
				} else {
					if (i >= 2 && j >= 2 && s0.charAt(i - 1) == s1.charAt(j - 2)
							&& s1.charAt(j - 1) == s0.charAt(i - 2)) {
						d[i][j] = d[i - 2][j - 2] + 1;
					} else {
						d[i][j] = Math.min(d[i - 1][j - 1], Math.min(d[i - 1][j], d[i][j - 1])) + 1;
					}

				}
			}
		}

		return d[len1][len2];

	}

	/**
	 * Returns the n closest words in the dictionary to the given word, where
	 * "closest" is defined by:
	 * <ul>
	 * <li>Minimal edit distance (with ties broken by:)
	 * <li>Largest count / frequency in the dictionary (with ties broken by:)
	 * <li>Ascending alphabetic order
	 * </ul>
	 * 
	 * @param word The word we are comparing against the closest in the dictionary
	 * @param n    The number of least-distant suggestions desired
	 * @return A set of up to n suggestions closest to the given word
	 */
	public Set<String> getNLeastDistant(String word, int n) {
		Set<String> ks = dict.keySet();
		List<Suggestions> list = new ArrayList<>();
		Suggestions[] sg = new Suggestions[dict.size()];
		Set<String> candidates = new HashSet<>();
		int i = 0;

		for (String s : ks) {
			sg[i] = new Suggestions(s, dict.get(s), editDistance(word, s));
			list.add(sg[i]);
			i++;
		}

		Collections.sort(list, new SuggestionsCompare());

		for (i = 0; i < n; i++) {
			candidates.add(list.get(i).word);
		}

		return candidates;

	}

	/**
	 * Returns the set of n most frequent words in the dictionary to occur with edit
	 * distance distMax or less compared to the given word. Ties in max frequency
	 * are broken with ascending alphabetic order.
	 * 
	 * @param word    The word to compare to those in the dictionary
	 * @param n       The number of suggested words to return
	 * @param distMax The maximum edit distance (inclusive) that suggested /
	 *                returned words from the dictionary can stray from the given
	 *                word
	 * @return The set of n suggested words from the dictionary with edit distance
	 *         distMax or less that have the highest frequency.
	 */
	public Set<String> getNBestUnderDistance(String word, int n, int distMax) {
		Set<String> Set = new HashSet<>();
		int len = word.length();
		List<int[]> comb = generate(len, 2);
		Set<String> ks = dict.keySet();

		for (int i = 0; i <= distMax; i++) {
			if (i == 0) {// editDistance == 0
				Set.add(word);
			}

			else if (i == 1) {// editDistance == 1
				// Insertion
				for (int j = 0; j < LETTERS.length; j++) {
					for (int k = 0; k < len + 1; k++) {
						StringBuffer Word = new StringBuffer(word);
						Word.insert(k, LETTERS[j]);
						String str = Word.toString();
						Set.add(str);
						Word.deleteCharAt(k);
					}
				}
				// Deletion
				for (int j = 0; j < len; j++) {
					StringBuffer Word = new StringBuffer(word);
					Word.deleteCharAt(j);
					String str = Word.toString();
					Set.add(str);
				}
				// Replacement
				for (int j = 0; j < len; j++) {
					for (int k = 0; k < LETTERS.length; k++) {
						StringBuffer Word = new StringBuffer(word);
						Word.setCharAt(j, LETTERS[k]);
						String str = Word.toString();
						Set.add(str);
					}
				}
				// Transportation
				if (len > 1) {
					for (int j = 0; j < comb.size(); j++) {
						int[] temp = comb.get(j);
						StringBuffer Word = new StringBuffer(word);
						char ch1 = Word.charAt(temp[0]);
						char ch2 = Word.charAt(temp[1]);
						Word.setCharAt(temp[0], ch2);
						Word.setCharAt(temp[1], ch1);
						String str = Word.toString();
						Set.add(str);
					}
				}
			} else {// editDistance == 2
					// Insertion
				for (int j = 0; j < len + 1; j++) {
					StringBuffer Word = new StringBuffer(word);
					for (int k = 0; k < LETTERS.length; k++) {
						Word.insert(j, LETTERS[k]);
						for (int l = 0; l < len + 2; l++) {
							for (int m = 0; m < LETTERS.length; m++) {
								Word.insert(l, LETTERS[m]);
								String str = Word.toString();
								Set.add(str);
								Word.deleteCharAt(l);
							}
						}
						Word.deleteCharAt(j);
					}
				}
				// Deletion
				for (int j = 0; j < comb.size(); j++) {
					int[] temp = comb.get(j);
					StringBuffer Word = new StringBuffer(word);
					Word.deleteCharAt(temp[1]);
					Word.deleteCharAt(temp[0]);
					String str = Word.toString();
					Set.add(str);
				}
				// Replacement
				for (int j = 0; j < len; j++) {
					StringBuffer Word = new StringBuffer(word);
					char ch1 = Word.charAt(j);
					for (int k = 0; k < LETTERS.length; k++) {
						Word.setCharAt(j, LETTERS[k]);
						for (int l = 0; l < len; l++) {
							char ch2 = Word.charAt(l);
							for (int m = 0; m < LETTERS.length; m++) {
								Word.setCharAt(l, LETTERS[m]);
								String str = Word.toString();
								Set.add(str);
							}
							Word.setCharAt(l, ch2);
						}
					}
					Word.setCharAt(j, ch1);
				}
				// Transportation
				if (len > 1) {
					for (int j = 0; j < comb.size(); j++) {
						StringBuffer Word = new StringBuffer(word);
						int[] temp = comb.get(j);
						char ch1 = Word.charAt(temp[0]);
						char ch2 = Word.charAt(temp[1]);
						Word.setCharAt(temp[0], ch2);
						Word.setCharAt(temp[1], ch1);
						for (int k = 0; k < comb.size(); k++) {
							temp = comb.get(k);
							char ch3 = Word.charAt(temp[0]);
							char ch4 = Word.charAt(temp[1]);
							Word.setCharAt(temp[0], ch4);
							Word.setCharAt(temp[1], ch3);
							String str = Word.toString();
							Set.add(str);
							Word.setCharAt(temp[0], ch3);
							Word.setCharAt(temp[1], ch4);
						}
						Word.setCharAt(temp[0], ch1);
						Word.setCharAt(temp[1], ch2);
					}
				}

			}
		}
		// distill Set down to only those that appear in the dictionary
		Set.retainAll(ks);

		// return the n most frequent
		List<Suggestions> list = new ArrayList<>();
		Suggestions[] sg = new Suggestions[Set.size()];
		Set<String> candidates = new HashSet<>();
		int i = 0;
		Iterator<String> iterator = Set.iterator();

		while (iterator.hasNext()) {
			String tempWord = iterator.next();
			sg[i] = new Suggestions(tempWord, dict.get(tempWord), 0);
			list.add(sg[i]);
			i++;

		}
		Collections.sort(list, new SuggestionsCompare());

		for (i = 0; i < n; i++) {
			if (i > list.size() - 1)
				break;
			candidates.add(list.get(i).word);
		}

		return candidates;
	}

	class Suggestions {
		String word;
		int frequency;
		int editDistance;

		public Suggestions(String word, int frequency, int editDistance) {
			this.word = word;
			this.frequency = frequency;
			this.editDistance = editDistance;
		}
	}

	class SuggestionsCompare implements Comparator<Suggestions> {
		int ret = 0;

		@Override
		public int compare(Suggestions s1, Suggestions s2) {
			if (s1.editDistance < s2.editDistance) {
				ret = -1;
			}
			if (s1.editDistance == s2.editDistance) {
				if (s1.frequency == s2.frequency) {
					if (s1.word.compareTo(s2.word) < 0) {
						ret = -1;
					} else if (s1.word.compareTo(s2.word) == 0) {
						ret = 0;
					} else if (s1.word.compareTo(s2.word) > 0) {
						ret = 1;
					}
				} else if (s1.frequency > s2.frequency) {
					ret = -1;
				} else if (s1.frequency < s2.frequency) {
					ret = 1;
				}
			}
			if (s1.editDistance > s2.editDistance) {
				ret = 1;
			}

			return ret;
		}
	}

	private void Combination(List<int[]> combinations, int[] data, int start, int end, int index) {
		if (index == data.length) {
			int[] combination = data.clone();
			combinations.add(combination);
		} else if (start <= end) {
			data[index] = start;
			Combination(combinations, data, start + 1, end, index + 1);
			Combination(combinations, data, start + 1, end, index);
		}
	}

	public List<int[]> generate(int n, int r) {
		List<int[]> combinations = new ArrayList<>();
		Combination(combinations, new int[r], 0, n - 1, 0);
		return combinations;
	}
}
