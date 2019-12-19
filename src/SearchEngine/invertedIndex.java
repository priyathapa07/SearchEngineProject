package SearchEngine;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;

public class invertedIndex {

	//storing FILE ID as key and file path as value
	static Map<Integer, String> source;
	
	//storing each term in the file as key and set of FILE ID's in which the term appears as value
	static HashMap<String, Set<Integer>> index;
	
	static PorterStemmer ps = new PorterStemmer();
	
	//hashmap to store term frequency for each individual document
	static Map<Integer, Map<String, Integer>> tfMap ;
	
	//storing number of terms for each document. This is later used to calculate tf-idf.
	static Map<Integer, Double> doc_terms = new HashMap<>();;
	
	invertedIndex(){
		source = new HashMap<Integer, String>();
		index = new HashMap<String, Set<Integer>>();
		
	}
	
	//function to index files. this function maps each term with file id's in which the term appears. 
	//for example term cat can appear in file id 1, 5, and 17.
	public void indexHtmlFiles(String[] files) {
		
		//hashmap to store term frequency for each term in each file
		tfMap = new HashMap<Integer, Map<String, Integer>>();
		int file_no = 1;
		
		//for all files in the directory
		for(String file: files) {
			
			try {
				//extracting the text of the html file
				File html = new File(file);
				org.jsoup.nodes.Document doc= Jsoup.parse(html, "UTF-8");
				source.put(file_no, file);
				String text = doc.body().text();
				
				text = text.replaceAll("\\p{Punct}", "").toLowerCase();
				String[] words = text.split("\\s");
				doc_terms.put(file_no, (double)words.length);
				
				//indexing each term in the file
				int pos = 0;
				for(String word: words) {
					word = word.toLowerCase();
					word = ps.stem(word);
					
					if(SearchEngineGUI.isStopWord(word)) {
						continue;
					}
					
					//indexing each term
					Set<Integer> doc_id = index.get(word);
					if(doc_id == null) {
						doc_id = new HashSet<Integer>();
						index.put(word, doc_id);
					}
					doc_id.add(file_no);
					
					
					//calculating term frequency 
					 Map<String, Integer> table = tfMap.get(file_no);
					 Integer n = null;
					 if(table != null) {
						 n = table.get(word);
					 }else {
						 table = new HashMap<String, Integer>();
					 }
					 
					 
					 if(n==null) {
						 n = 1;
					 }else {
						 n++;
					 }
					 
					 table.put(word,n);
					 tfMap.put(file_no,table);
					 
					 
				}
				
			} catch (IOException e) {
				System.out.println("File" + file + "not found.");
			}
			
			file_no++;
		}
	}
	
	//this function returns the appropriate documents for the corresponding search query
	public Set<String> searchQuery(String query) {
		
		String s = query.replaceAll("\\p{Punct}", "").toLowerCase();
		String[] words = s.split("\\s");
		
		//storing the document path
		Set<String> result = new HashSet<String>();
		
		//helper data structure to store tf-idf for each document
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		
		//later used to calculate average
		double total_tfIdf = 0;
		
		
		//for all words in the search query 
		for(String word: words) {
			
			word = word.toLowerCase();
			word = ps.stem(word);
			
			//getting the list of doc id's in which a particular term in the query appears. 
			Set<Integer> doc_id = index.get(word);
			
			if(doc_id != null) {
				
				for(Integer i: doc_id) {
					
					//term-frequency table for each individual documents
					Map<String, Integer> table = tfMap.get(i);
					
					//term frequency for a word in the search query
					int tf = table.get(word);
					
					
					//calculating tf-idf for each term and mapping it to the corresponding document
					//this is the most important part of the entire coding which greatly helps in the precision.
					
					//total number of documents
					double N = SearchEngineGUI.numOfFiles();
					
					//term-frequency normalized
					double tf_d = (tf/doc_terms.get(i));
					
					//inverse document frequency
					double idf = Math.log(N/doc_id.size());
					
					//calculating tf-idf
					double tf_idf = tf_d * idf;
					
					//adding all the tf-idf for calculating average
					total_tfIdf += tf_idf;
					
					//System.out.println(word + " -- " + source.get(i) + " -- " + tf_idf + " ---> " + idf +", "+ tf_d + ", " + N + ", " + doc_id.size() );
					
					//checking if a tf-idf has been mapped to a document
					//if no map it, otherwise add to existing value
					Double x = map.get(i);
					if(x == null) {
						map.put(i, tf_idf);
					}else {
						map.put(i, x+tf_idf);
					}
					
				}
				
			} 
			
		}
		
		
		//sorting the hashmap of doc id to tf-idf based on decreasing tf-idf
		Map<Integer, Double> sorted = map.entrySet( ).stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));


		//System.out.println(sorted);
		
		//calculating the average of tf-idf, which we will use to control precision, in other words terminate the adding of documents in result
		double a = Math.round((total_tfIdf/map.size()) * 1000.0) / 1000.0;
		
		
		//adding relevant documents to the result set based on the tf-idf value
		//the more the tf-idf score, the higher importance of that document. 
		//so i am only adding documents that has relatively higher value of tf-idf
		for (Map.Entry<Integer, Double> entry : sorted.entrySet()) {
			
			//if tf-idf is less than average than break
			//this controls precision
			if(entry.getValue() < a) break;
			result.add(source.get(entry.getKey()));
		}
		
		return result;
	}
	
}
