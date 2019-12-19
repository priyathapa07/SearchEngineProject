package SearchEngine;

import java.awt.EventQueue;
import java.awt.List;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JTextField;

import org.jsoup.Jsoup;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.awt.event.ActionEvent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;

import javax.swing.JPanel;
import java.awt.Font;

public class SearchEngineGUI {

	private static JFrame frame;
	private JTextField textField;
	
	private static JLabel[] doc_links;
	private static JLabel desc[];
	
	private static String search_text;
	private static int numOfLabels = 0;
	
	private static invertedIndex index = new invertedIndex();
	private static Set<String> results = new HashSet<String>();
	private JLabel numOfResults;
	
	private static JScrollPane scrollPane;
	private static JPanel panel;
	
	//Map to store term and its length
	private static HashMap<Integer, Integer> term_length;
	
	//Map to store position of each term
	private static HashMap<String, ArrayList<Integer>> terms_position;
	
	//to store number of files in the database. I am not hard-coding the number.
	//Instead I am looping over all the files in the database directory and counting the number of files
	private static double numOfFiles = 0;
	
	private static ArrayList<Integer> text_snippets;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SearchEngineGUI window = new SearchEngineGUI();
					window.frame.setVisible(true);
					
					//getting the list of files from the database directory
					String[] listOffiles = files();
					
					//indexing all the files using the function of invertedIndex class
					index.indexHtmlFiles(listOffiles);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SearchEngineGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(Color.WHITE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		textField = new JTextField();
		textField.setFont(new Font("Calibri", Font.PLAIN, 16));
		textField.setBounds(54, 28, 455, 39);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		numOfResults = new JLabel("");
		numOfResults.setBounds(64, 80, 327, 33);
		frame.getContentPane().add(numOfResults);
		
		JButton btnSearch = new JButton("Search");
		btnSearch.setFont(new Font("Calibri", Font.BOLD, 16));
		btnSearch.setForeground(Color.WHITE);
		btnSearch.setBackground(new Color(65, 105, 225));
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				//getting the search query
				search_text = textField.getText();
				search_text = search_text.trim().replaceAll("\\p{Punct}", "").toLowerCase();
				removeAllResults();
				
				//calling the function of invertedIndex class to get the appropriate documents returned by the search query
				results = index.searchQuery(search_text);
				numOfLabels = results.size();
				
				
				
				if(search_text.equals("")) {
					numOfResults.setText("Showing 0 results for " + "\"" + search_text + "\"" );
					removeAllResults();
				}else {
					numOfResults.setText("Showing " + numOfLabels + " results for " + "\"" + search_text + "\"" );
					makeJlabels(results);
				}
				
				
			}
		});
		btnSearch.setBounds(530, 28, 139, 39);
		frame.getContentPane().add(btnSearch);
		
		panel = new JPanel();
		panel.setBorder(null);
		panel.setBackground(Color.WHITE);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(60, 141, (int)screenSize.getWidth()-60, (int)screenSize.getHeight()-200);
		scrollPane.setBorder(null);
		frame.getContentPane().add(scrollPane);
				
	}
	
	public static void removeAllResults() {		
		panel.removeAll();
		panel.validate();
		panel.repaint();
	}
	
	public static void makeJlabels(Set<String> query_results) {
		int len = query_results.size();
		doc_links = new JLabel[len];
		desc = new JLabel[len];
		
		
		
		String descp = null;
		
		int i = 0;
		
		writeFile("Query: " + search_text + "\n\n" + "Results: " + "\n");
		int tp1 = 0, fp1 = 0, tp2 = 0, fp2 = 0, tp3 = 0, fp3 = 0, tp4 = 0, fp4 = 0, tp5 = 0, fp5 = 0
				, tp6 = 0, fp6 = 0, tp7 = 0, fp7 = 0, tp8 = 0, fp8 = 0, tp9 = 0, fp9 = 0, tp10 = 0, fp10 = 0;
		
		double[] tp = new double[11];
		double[] fp = new double[11];
		
		for(int b=0; b<=10; b++) {
			tp[b] = 0;
			fp[b] = 0;
		}
		
		//for every documents returned, presenting them in a user friendly way listing the document links and text snippet
		for(String res: query_results) {
			
			doc_links[i] = new JLabel("");
			doc_links[i].setForeground(Color.BLUE.darker());
			doc_links[i].setBackground(Color.LIGHT_GRAY);
			doc_links[i].setFont(new Font("Calibri", Font.BOLD, 17));
			
			File html = new File(res);
			org.jsoup.nodes.Document doc = null;
			try {
				doc = Jsoup.parse(html, "UTF-8");
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			String snippet = null;
			
			desc[i] = new JLabel("");
			desc[i].setFont(new Font("Calibri", Font.PLAIN, 16));
			desc[i].setText("<html><br>");
			
			descp = doc.body().text();
			
			//getting the position of each search query term in the returned document
			ArrayList<Integer> positions = getWordPositions(descp);
			
			int num_of_char = 0, count = 0;
			//displaying the text snippet for each document returned
			for(int k=0; k<positions.size(); k++) {
				
				if(num_of_char >= 90) {
					desc[i].setText(desc[i].getText()+ "<br>");
					num_of_char = 0;
				}
				
				if(descp.length() - positions.get(k) > 60) {
					desc[i].setText(desc[i].getText() + "<font color=green>"+
							descp.substring(positions.get(k), positions.get(k) + term_length.get(positions.get(k)))+ "</font>"+
							descp.substring(positions.get(k) + term_length.get(positions.get(k)), positions.get(k)+60) + " ... ");
					num_of_char += 60;
					count++;
					snippet += descp.substring(positions.get(k), positions.get(k) + term_length.get(positions.get(k))) 
								+ descp.substring(positions.get(k) + term_length.get(positions.get(k)), positions.get(k)+60) + " ... ";
					
				}else {
					desc[i].setText(desc[i].getText() + "<font color=green>"+
							descp.substring(positions.get(k), positions.get(k) + term_length.get(positions.get(k)) -1)+ "</font>"+
							descp.substring(positions.get(k) + term_length.get(positions.get(k)) -1, descp.length()) + " ... ");
					num_of_char += descp.length() - positions.get(k);
					count++;
					snippet += descp.substring(positions.get(k), positions.get(k) + term_length.get(positions.get(k)) -1) + 
							descp.substring(positions.get(k) + term_length.get(positions.get(k)) -1, descp.length()) + " ... ";
					
				}
				
				if (count > 6) {
					break;
				}
				
			}
			
			if(count<=6) {
				for(int j=0; j<text_snippets.size(); j++) {
					
					if(num_of_char >= 90) {
						desc[i].setText(desc[i].getText()+ "<br>");
						num_of_char = 0;
					}
					
					if(descp.length() - text_snippets.get(j) > 60) {
						desc[i].setText(desc[i].getText() + "<font color=green>"+
								descp.substring(text_snippets.get(j), text_snippets.get(j) + term_length.get(text_snippets.get(j)))+ "</font>"+
								descp.substring(text_snippets.get(j) + term_length.get(text_snippets.get(j)), text_snippets.get(j)+60) + " ... ");
						num_of_char += 60;
						count++;
						snippet += descp.substring(text_snippets.get(j), text_snippets.get(j) + term_length.get(text_snippets.get(j)))+
								descp.substring(text_snippets.get(j) + term_length.get(text_snippets.get(j)), text_snippets.get(j)+60) + " ... ";
						
					}else {
						desc[i].setText(desc[i].getText() + "<font color=green>"+
								descp.substring(text_snippets.get(j), text_snippets.get(j) + term_length.get(text_snippets.get(j)) -1)+ "</font>"+
								descp.substring(text_snippets.get(j) + term_length.get(text_snippets.get(j)) -1, descp.length()) + " ... ");
						num_of_char += descp.length() - text_snippets.get(j);
						count++;
						snippet += descp.substring(text_snippets.get(j), text_snippets.get(j) + term_length.get(text_snippets.get(j)) -1)+ 
								descp.substring(text_snippets.get(j) + term_length.get(text_snippets.get(j)) -1, descp.length()) + " ... ";
						
					}
					
					if (count > 6) {
						break;
					}
					
				}
			}
			
			
			//systematically displaying the text snippet
			desc[i].setText(desc[i].getText() + "<html>");
			
			if(snippet == null) {
				desc[i].setText("<html><br>" + descp.substring(0,50) + "<html>");
				snippet = descp.substring(0,50);
			}
			
					
			//displaying the document link/title
			doc_links[i].setText(doc.title());
			doc_links[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			
			
			//writing the result in output file
			writeFile((i+1) + ". " + doc_links[i].getText() + "\n\n");
			writeFile("Snippet Text: " + snippet + "\n\n\n");
			
			
			panel.add(doc_links[i]);
			panel.add(desc[i]);
			panel.add(Box.createRigidArea(new Dimension(3,30)));
			
			
			//handling the user clicks in the title.
			//if clicked it will open the corresponding document in default browser
			doc_links[i].addMouseListener(new MouseAdapter() {
				 @Override
				    public void mouseClicked(MouseEvent e) {
				       
					 	try {
					 		File htmlFile = new File(res); 
							Desktop.getDesktop().browse(htmlFile.toURI());
						} catch (MalformedURLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				    }
				 
				    @Override
				    public void mouseEntered(MouseEvent e) {
				        // the mouse has entered the label
				    }
				 
				    @Override
				    public void mouseExited(MouseEvent e) {
				        // the mouse has exited the label
				    }
			});

			i++;
			
			for(int b = 1; b<=10; b++) {
				if(res.contains(b+ "_doc")) tp[b]++; 
				else fp[b]++;					
			}
			
		}
		
		//calculating precision and recall
		double recall = numOfLabels/18.0;
		double precision = numOfLabels/numOfLabels;
		int query_used = queryUsed();
		
		
		for(int b=1; b<=10; b++) {
			if(query_used == b) {
				recall = calculateRecall(tp[b], b);
				precision = calculatePrecision(tp[b], fp[b]);
				
			}
		}
		
		writeFile("\n");
		writeFile("Precision = " + precision +"\nRecall = " + recall);
		writeFile("\n\n----------------------------------------------------------------------------------------------\n\n\n\n");
		
		BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(boxLayout);

	}
	
	//determining which query of the original 10 queries is being used
	public static int queryUsed() {
		String s = search_text.toLowerCase(); 
		
		if(s.contains("rock and roll") || s.contains("music")) return 1;
		else if(s.contains("soccer")) return 2;
		else if(s.contains("big bang")) return 3;
		else if(s.contains("guitar")) return 4;
		else if(s.contains("writing") || s.contains("novel")) return 5;
		else if(s.contains("college")) return 6;
		else if(s.contains("freewill") || s.contains("destiny")) return 7;
		else if(s.contains("enlightened")) return 8;
		else if(s.contains("joker")) return 9;
		else if(s.contains("survive") || s.contains("antarctica")) return 10;
		else return -1;
		
	}
	
	public static double calculateRecall(double tp, int q) {
		
		int fn = 0;
		if(q==1) fn = 18;
		if(q==2) fn = 22;
		if(q==3) fn = 23;
		if(q==4) fn = 21;
		if(q==5) fn = 24;
		if(q==6) fn = 18;
		if(q==7) fn = 20;
		if(q==8) fn = 19;
		if(q==9) fn = 19;
		if(q==10) fn = 20;
		
		return (tp/fn);
	}
	
	public static double calculatePrecision(double tp, double fp) {
		return (tp/(tp+fp)) ;
	}
	
	
	
	//this function returns the arraylist of positions where the search term appears in the corresponding document
	//this is done to display the text snippet of where the term appear following each document title 
	
	public static ArrayList<Integer> getWordPositions(String text){
		ArrayList<Integer> positions = new ArrayList<Integer>();
		
		
		//String s = search_text.replaceAll("[^a-zA-Z ]", "").toLowerCase();
		
		term_length = new HashMap<Integer, Integer>();
		text_snippets = new ArrayList<Integer>();
		
		String[] query_terms = search_text.split("\\s");
		String[] text_terms = text.split("\\s");
		
		terms_position = new HashMap<String, ArrayList<Integer>>();
		
		int pos = 0;
		for(String term: text_terms) {
			ArrayList<Integer> p = terms_position.get(term);
			if(p == null) {
				term = term.toLowerCase();
				p = new ArrayList<Integer>();
				terms_position.put(term, p);
				//term_length.put(pos, term.length() + 1);
			}
			p.add(pos);
			term_length.put(pos, term.length() + 1);
			pos = pos + term.length() + 1;
		}
				
		for(String word: query_terms) {
			if(!isStopWord(word)) {
				word = word.toLowerCase();
				
				ArrayList<Integer> position = terms_position.get(word);
				
				if(position != null) {
					int x=0;
					for(Integer in: position) {
						if(x==0) positions.add(position.get(0));
						else text_snippets.add(in);
						x++;
					}
				}
			}
			
		}
		
		return positions;
	}
	
	//a function to return true/false if a word is stopword or not
	public static boolean isStopWord(String str) {
		
		String[] stopwords = new String[]{"a", "able", "about",
			"across", "after", "all", "almost", "also", "am", "among", "an",
			"and", "any", "are", "as", "at", "be", "because", "been", "but",
			"by", "can", "cannot", "could", "dear", "did", "do", "does",
			"either", "else", "ever", "every", "for", "from", "get", "got",
			"had", "has", "have", "he", "her", "hers", "him", "his", "how",
			"however", "i", "if", "in", "into", "is", "it", "its", "just",
			"least", "let", "likely", "may", "me", "might", "most",
			"must", "my", "neither", "no", "nor", "not", "of", "off", "often",
			"on", "only", "or", "other", "our", "own", "rather", "said", "say",
			"says", "she", "should", "since", "so", "some", "than", "that",
			"the", "their", "them", "then", "there", "these", "they", "this",
			"tis", "to", "too", "twas", "us", "wants", "was", "we", "were",
			"what", "when", "where", "which", "while", "who", "whom", "why",
			"will", "with", "would", "yet", "you", "your"};
		
	
		
		if(arrayContains(stopwords, str)) {
			return true;
		}
		
		return false;
	}
	
	public static boolean arrayContains(String[] stopwords, String s) {
		
		for (int i=0; i<stopwords.length; i++) {
			if(stopwords[i].equals(s)) {
				return true;
			}
		}
		return false;
	}
	
	
	//function that is looping through all the files in the directory where the corpus is stored
	//here we can change the path of the directory where the corpus is stored
	
	//this is very important because while submitting the project, the directory will be my usb-drive directory
	//and we will have to change it for the program to work.
	//usb-drive directory is showing as E in my laptop, which might not be the same in other computers.
	public static String[] files() {
		
		//String path = "C:/Search Engine Project/corpus/";
		String path = System.getProperty("user.dir") + "/corpus/";
		
		File dir = new File(path);
		
		File[] file_list = dir.listFiles();
		if (file_list != null) {
			int i=0;
			for (File child : file_list) {
		      //System.out.println(child);
		      numOfFiles++;
		    }
		} 
		String files[] = new String[(int) numOfFiles];
		if (file_list != null) {
			int i=0;
			for (File child : file_list) {
		      //System.out.println(child);
				files[i] = child.toString();
				i++;
		    }
		} 
		
		return files;
	}

	public static double numOfFiles() {
		return numOfFiles;
	}
	
	
	//function to write result into the output files 
	public static void writeFile(String str) {
		//String path = "C:/Search Engine Project/query_result_output.txt";
		String path = System.getProperty("user.dir") + "/query_result_output.txt/";
		try {
			OutputStream os = new FileOutputStream(new File(path), true);
			os.write(str.getBytes());
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//calculating precision and recall
	public static void precision() {
		
	}
	
}
