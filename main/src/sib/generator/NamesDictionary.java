package sib.generator;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class NamesDictionary {
	
	RandomAccessFile dictionary; 
	String dicFileName;

	HashMap<String, Integer> locationNames;
	
	Vector<Vector<String>> surNamesByLocations;
	Vector<Vector<String>> givenNamesByLocations;
	Random 		rand; 
	
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
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getRandomSurName(int locationId){
		String surName = ""; 
		int randomSurNameIdx = rand.nextInt(surNamesByLocations.get(locationId).size());
		surName = surNamesByLocations.get(locationId).get(randomSurNameIdx);
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
		return givenName;
	}	
}
