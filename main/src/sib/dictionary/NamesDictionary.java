package sib.dictionary;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class NamesDictionary {
	
	RandomAccessFile dictionary; 
	String dicFileName;

	HashMap<String, Integer> locationNames;
	
	Vector<Vector<String>> surNamesByLocations;
	Vector<Vector<String>> givenNamesByLocations;
	Random 		rand;
	
	// Store the statistic for testdriver
	int[][] countBySurNames; 		
	int[][] countByGivenNames; 
	
	public NamesDictionary(String _dicFileName, HashMap<String, Integer> _locationNames, long seedRandom){
		this.locationNames = _locationNames; 
		this.dicFileName = _dicFileName;
		this.rand = new Random(seedRandom);
	}
	public void init(){
		try {
			dictionary = new RandomAccessFile(dicFileName, "r");
			
			//System.out.println("Extracting names into a dictionary ");
			
			surNamesByLocations = new Vector<Vector<String>>(locationNames.size());
			givenNamesByLocations = new Vector<Vector<String>>(locationNames.size());
			for (int i = 0; i < locationNames.size(); i++){
				surNamesByLocations.add(new Vector<String>());
				givenNamesByLocations.add(new Vector<String>());
			}
			
			extractNames();
			
			dictionary.close();
			
			//System.out.println("Sort popular names in Germany");
			//getFrequency(89);
			//getFrequency(69);
			//System.exit(-1);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void extractNames(){
		System.out.println("Extract surnames/givenNames by locations ...");
		String line; 
		String locationName; 
		String surName; 
		String givenName; 
		String lastLocationName = "";
		int curLocationId = -1; 
		int totalNumNames = 0;
		try {
			while ((line = dictionary.readLine()) != null){
				//System.out.println("Line --> " + line);
				//System.out.println("[0]: " + line.split(" ")[0]);
				String infos[] = line.split("  ");
				locationName = infos[0];
				//System.out.println("Line in names = " + line); 
				if (locationName.compareTo(lastLocationName) != 0){ 	// New location
					//Debug
					//if (curLocationId == 4)
					//	System.out.println("Size of create givennames for location " + curLocationId + " is " + givenNamesByLocations.get(curLocationId).size());

					if (locationNames.containsKey(locationName)){		// Check whether it exists
						curLocationId = locationNames.get(locationName);
						surName = infos[1].trim();
						surNamesByLocations.get(curLocationId).add(surName);
						givenName = infos[2].trim();
						givenNamesByLocations.get(curLocationId).add(givenName);
						totalNumNames++;
					}
				}
				else{
					surName = infos[1].trim();
					surNamesByLocations.get(curLocationId).add(surName);
					givenName = infos[2].trim();
					givenNamesByLocations.get(curLocationId).add(givenName);
					totalNumNames++;
				}

			}
			
			System.out.println("Done ... " + totalNumNames + " names were extracted ");
			
			// For statictic of the testdriver
			//countBySurNames = new int[locationNames.size()][totalNumNames];
			//countByGivenNames = new int[locationNames.size()][totalNumNames];
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getFrequency(int index){
		// Sort
		
		Vector<String> names = surNamesByLocations.get(index);
		 
		Vector<NameFreq> nameFrequency = new Vector<NameFreq>();
		
		Collections.sort(names);
		String preName = "";
		int count =0; 
		int totalCount = 0; 
		
		for (int i=0; i < names.size(); i++){
			
			if (names.get(i).compareTo(preName) != 0){
				//System.out.println(" " + preName + " :  " + count);
				nameFrequency.add(new NameFreq(preName, count));
				preName = names.get(i); 
				count = 0;
			}
			count++;
			totalCount++;
		}
		
		NameFreq[] sortNameFreq = new NameFreq[nameFrequency.size()];
		for (int i = 0; i <  nameFrequency.size(); i ++){
			sortNameFreq[i] = new NameFreq(nameFrequency.get(i).name, nameFrequency.get(i).freq);
		}
		
		System.out.println("Number of names " + sortNameFreq.length);
		Arrays.sort(sortNameFreq);
		for (int i = 0; i < sortNameFreq.length; i ++){
			sortNameFreq[i].printPercent(totalCount);
		}
		
	}
	public String getRandomSurName(int locationId){
		String surName = ""; 
		int randomSurNameIdx = rand.nextInt(surNamesByLocations.get(locationId).size());
		surName = surNamesByLocations.get(locationId).get(randomSurNameIdx);
		
		// For statistic of the test driver 
		//countBySurNames[locationId][randomSurNameIdx]++;
		
		return surName;
	}
	public String getRandomGivenName(int locationId){
		String givenName = "";
		int randomGivenNameIdx =-1;
		try {
			randomGivenNameIdx = rand.nextInt(givenNamesByLocations.get(locationId).size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Location ID = " + locationId);
			e.printStackTrace();
			System.exit(-1);
		}
		givenName = givenNamesByLocations.get(locationId).get(randomGivenNameIdx);
		
		// For statistic of the test driver
		//countByGivenNames[locationId][randomGivenNameIdx]++;
		
		return givenName;
	}

	public Vector<Vector<String>> getSurNamesByLocations() {
		return surNamesByLocations;
	}
	public void setSurNamesByLocations(Vector<Vector<String>> surNamesByLocations) {
		this.surNamesByLocations = surNamesByLocations;
	}
	public Vector<Vector<String>> getGivenNamesByLocations() {
		return givenNamesByLocations;
	}
	public void setGivenNamesByLocations(
			Vector<Vector<String>> givenNamesByLocations) {
		this.givenNamesByLocations = givenNamesByLocations;
	}
	public int[][] getCountBySurNames() {
		return countBySurNames;
	}
	public void setCountBySurNames(int[][] countBySurNames) {
		this.countBySurNames = countBySurNames;
	}
	public int[][] getCountByGivenNames() {
		return countByGivenNames;
	}
	public void setCountByGivenNames(int[][] countByGivenNames) {
		this.countByGivenNames = countByGivenNames;
	}
	
	class NameFreq implements Comparable{
		String name;
		int freq; 
		public NameFreq(String _name, int _freq){
			this.name = _name; 
			this.freq = _freq; 
		}
		public int compareTo(Object obj)
		{
			NameFreq tmp = (NameFreq)obj;
			if(this.freq < tmp.freq)
			{	
				return -1;
			}
			else if(this.freq > tmp.freq)
			{
				return 1;
			}
			return 0;
		}
		public void print(){
			System.out.println(name + "  " + freq);
		}
		public void printPercent(int total){
			System.out.println(name + "  " + (double)100*freq/total);
		}
	}
}

