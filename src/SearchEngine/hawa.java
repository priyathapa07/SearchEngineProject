package SearchEngine;


import java.io.*;
import java.util.*;

public class hawa {

	Map<Integer, String> source;
	HashMap<String, HashSet<Integer>> index;
	
	List<String> stopwords = Arrays.asList("a", "able", "about",
			"across", "after", "all", "almost", "also", "am", "among", "an",
			"and", "any", "are", "as", "at", "be", "because", "been", "but",
			"by", "can", "cannot", "could", "dear", "did", "do", "does",
			"either", "else", "ever", "every", "for", "from", "get", "got",
			"had", "has", "have", "he", "her", "hers", "him", "his", "how",
			"however", "i", "if", "in", "into", "is", "it", "its", "just",
			"least", "let", "like", "likely", "may", "me", "might", "most",
			"must", "my", "neither", "no", "nor", "not", "of", "off", "often",
			"on", "only", "or", "other", "our", "own", "rather", "said", "say",
			"says", "she", "should", "since", "so", "some", "than", "that",
			"the", "their", "them", "then", "there", "these", "they", "this",
			"tis", "to", "too", "twas", "us", "wants", "was", "we", "were",
			"what", "when", "where", "which", "while", "who", "whom", "why",
			"will", "with", "would", "yet", "you", "your");
	
	hawa(){
		source = new HashMap<Integer, String>();
		index = new HashMap<String, HashSet<Integer>>();
		
	}
	
	
	public void indexFiles(String[] files) {
		
		int pos = 0;
		
		for(String file: files) {
			
			try {
				BufferedReader read_file = new BufferedReader(new FileReader(file));
				
				source.put(pos, file);
				String line;
				
				while((line = read_file.readLine()) != null) {
					
					String[] words = line.split("\\s");
					for(String word: words) {
						word = word.toLowerCase();
						
						if(stopwords.contains(word)) {
							
							continue;
						}
						
						if(!index.containsKey(word)) {
							index.put(word, new HashSet<Integer>());
						}
						
						index.get(word).add(pos);
					}
				}
				
			} catch (IOException e) {
				System.out.println("File" + file + "not found.");
			}
			
			pos++;
		}
	}
	
	
	public void searchQuery(String query) {
		
		String[] words = query.split("\\s");
		HashSet<Integer> result = new HashSet<Integer>(index.get(words[0].toLowerCase()));
		
		for(String word: words) {
			result.retainAll(index.get(word));
		}
		
		if(result.size() == 0) {
			System.out.println("Not Found!!");
			return;
		}
		
		System.out.println("Found in: ");
		for(int i: result) {
			System.out.println("\t" + source.get(i));
		}
	}
	
}
