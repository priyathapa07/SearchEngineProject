package SearchEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.swing.text.Document;

import org.jsoup.Jsoup;

public class main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Scanner sc = new Scanner(System.in);
		
		invertedIndex index = new invertedIndex();
		//String[] files = new String[]{"C:\\ir\\file1.txt", "C:\\ir\\file2.txt", "C:\\ir\\file3.txt"};
		String[] files = new String[]{"C:/ir/file1.html", "C:/ir/file2.html", "C:/ir/file3.html"};
		
		//index.indexHtmlFiles(files);
		
		System.out.println("Enter your search query: ");
		String query = sc.nextLine();
	
		Set<String> results = new HashSet<String>();
		//results = index.searchQuery(query);
		
		for(String s: results) {
			System.out.print("  " + s);
		}
		int a = 1;
		//System.out.println(System.getProperty("user.dir"));
		if(query.contains(a+"he")) System.out.println(System.getProperty("user.dir"));
		//PorterStemmer ps = new PorterStemmer();
		//System.out.println(ps.stem(query));
		
		
		
		
	}

}
