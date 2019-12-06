package sib.generator;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.Vector;

public class RandomNamesGenerator {
	String dicFileName; 
	Vector<String> vecNames; 		// vector for storing all the names
	RandomAccessFile dictionary; 
	Random rand; 
	
	public RandomNamesGenerator(String fileName){
		rand = new Random(); 
		dicFileName = fileName; 
		init();
	}
	
	public RandomNamesGenerator(String fileName, long seed){
		dicFileName = fileName;
		rand = new Random(seed);
		init();
	}
	
	public void init(){
		try {
			dictionary = new RandomAccessFile(dicFileName, "r");
			vecNames = new Vector<String>();
			
			System.out.println("Extracting names into a dictionary ");
			extractNames();
			
			dictionary.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void extractNames(){
		String name; 
		try {
			while ((name = dictionary.readLine()) != null){
				vecNames.add(name);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(vecNames.size() + " names were extracted");
		
	} 
	
	public String getRandomFirstName(){
		int idx = rand.nextInt(vecNames.size()-1);
		return vecNames.elementAt(idx) ;
	}
	
	public String getRandomLastName(){
		int idx = rand.nextInt(vecNames.size()-1);
		return vecNames.elementAt(idx) ;
	}

}

