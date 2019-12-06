package sib.generator;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.Vector;

public class RandomLocationGenerator {
	String dicFileName; 
	Vector<String> vecLocations; 		// vector for storing all the locations URI
	RandomAccessFile dictionary; 
	Random rand; 
	private int locationIdx; 
	
	public int getLocationIdx() {
		return locationIdx;
	}

	public void setLocationIdx(int locationIdx) {
		this.locationIdx = locationIdx;
	}

	public RandomLocationGenerator(String fileName){
		rand = new Random(); 
		dicFileName = fileName; 
		init();
	}
	
	public RandomLocationGenerator(String fileName, long seed){
		dicFileName = fileName;
		rand = new Random(seed);
		init();
	}
	
	public void init(){
		try {
			dictionary = new RandomAccessFile(dicFileName, "r");
			vecLocations = new Vector<String>();
			
			System.out.println("Extracting locations into a dictionary ");
			extractLocations();
			
			dictionary.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void extractLocations(){
		String location; 
		try {
			while ((location = dictionary.readLine()) != null){
				vecLocations.add(location);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(vecLocations.size() + " locations were extracted");
		
	} 
	
	public String getRandomLocation(){
		locationIdx = rand.nextInt(vecLocations.size()-1);
		return vecLocations.elementAt(locationIdx) ;
	}
	
	public String getCorrelatedLocation(double probability, int correlatedIdx){

		double randProb = rand.nextDouble();
		if (randProb < probability){
			locationIdx = correlatedIdx;
			return vecLocations.elementAt(locationIdx) ;
		}
		
		locationIdx = rand.nextInt(vecLocations.size()-1);
		return vecLocations.elementAt(locationIdx) ;
	}
}
