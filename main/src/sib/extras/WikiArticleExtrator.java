package sib.extras;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Vector;

public class WikiArticleExtrator {
	public static void main (String args[]){
		WikiArticleExtrator extractor = new WikiArticleExtrator(); 
		extractor.extract();
	}
	public void extract(){
		String strLine;
		String subject, predicate, object;
		String title; 
		String triples[];
		try {
			RandomAccessFile randFile = new RandomAccessFile("/export/scratch1/duc/work/SNBenchMark/dump34/longabstract_en.nt", "r");
			FileWriter writerAbstracts = new FileWriter("/export/scratch1/duc/work/SNBenchMark/dump34/abstractsFull.txt");
			FileWriter writerTags = new FileWriter("/export/scratch1/duc/work/SNBenchMark/dump34/tagsFull.txt");
			
			while ((strLine=randFile.readLine()) != null){
					triples = strLine.split(">", 3);
					subject = triples[0];
					title = subject.substring(subject.lastIndexOf("/"));
					//System.out.println("Title:  "  + title);
					Vector<String> tags = extractTags(title);
					Iterator<String> it = tags.iterator();
					while (it.hasNext()){
						//System.out.print(it.next() + " ");
						writerTags.write(it.next() + " ");
					}
					//System.out.println();
					writerTags.write("\n");
					
					predicate = triples[1];
					object = triples[2].trim();
					writerAbstracts.write(object);
					writerAbstracts.write("\n");
			}
			
			randFile.close();
			writerAbstracts.close();
			writerTags.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public Vector<String> extractTags(String rawStr){
		char c; 
		Vector<String> words = new Vector<String>(); 
		int idx = 0;
		boolean flagIsWord = false; 
		
		StringBuffer word = new StringBuffer();

		
		for(int i = 0; i < rawStr.length(); i ++){
			c = rawStr.charAt(i);
			if (isLetter(c)){
				if (flagIsWord) word.append(c);
				else{		// New word
					flagIsWord = true; 
					word = new StringBuffer();
					word.append(c);
				}
			}
			else{
				// End of a word
				if (flagIsWord){
					words.add(word.toString());
					flagIsWord = false;
				}
				else{
						
				}
			}
		}
		
		if (flagIsWord) words.add(word.toString());
		
		return words;
	}
	
	public Vector<String> extractTags(String rawStr, int minWordSize){
		char c; 
		Vector<String> words = new Vector<String>(); 
		int idx = 0;
		boolean flagIsWord = false; 
		
		StringBuffer word = new StringBuffer();

		
		for(int i = 0; i < rawStr.length(); i ++){
			c = rawStr.charAt(i);
			if (isLetter(c)){
				if (flagIsWord) word.append(c);
				else{		// New word
					flagIsWord = true; 
					word = new StringBuffer();
					word.append(c);
				}
			}
			else{
				// End of a word
				if (flagIsWord){
					if (word.toString().length() >= minWordSize)
						words.add(word.toString());
					
					flagIsWord = false;
				}
				else{
						
				}
			}
		}
		
		if (flagIsWord && (word.toString()).length() >= minWordSize) words.add(word.toString());
		
		return words;
	}
	
	private boolean isLetter(char c)
	{
		return Character.isLetter(c) || c=='-' || c == '.';
	}
	
	
}
