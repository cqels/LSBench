package sib.generator;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class OrganizationsDictionary {
	RandomAccessFile dictionary; 
	String dicFileName;

	HashMap<String, Integer> locationNames;
	
	Vector<Vector<String>> organizationsByLocations;
	Random 		rand; 
	
	public OrganizationsDictionary(String _dicFileName, HashMap<String, Integer> _locationNames, long seedRandom){
		this.locationNames = _locationNames; 
		this.dicFileName = _dicFileName;
		this.rand = new Random(seedRandom);
	}
	public void init(){
		try {
			dictionary = new RandomAccessFile(dicFileName, "r");
			
			System.out.println("Building dictionary of organizations (by locations)");
			
			organizationsByLocations = new Vector<Vector<String>>(locationNames.size());
			for (int i = 0; i < locationNames.size(); i++){
				organizationsByLocations.add(new Vector<String>());
			}
			
			extractOrganizationNames();
			
			dictionary.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void extractOrganizationNames(){
		//System.out.println("Extract organizations by location ...");
		String line; 
		String locationName; 
		String organizationName; 
		String lastLocationName = "";
		int curLocationId = -1; 
		int totalNumOrganizations = 0;
		try {
			while ((line = dictionary.readLine()) != null){
				//System.out.println("Line --> " + line);
				//System.out.println("[0]: " + line.split(" ")[0]);
				String infos[] = line.split("  ");
				locationName = infos[0];
				//System.out.println("Line in names = " + line); 
				if (locationName.compareTo(lastLocationName) != 0){ 	// New location
					if (locationNames.containsKey(locationName)){		// Check whether it exists 
						curLocationId = locationNames.get(locationName); 
						organizationName = infos[1].trim();
						organizationsByLocations.get(curLocationId).add(organizationName);
						totalNumOrganizations++;
					}
				}
				else{
					organizationName = infos[1].trim();
					organizationsByLocations.get(curLocationId).add(organizationName);
					totalNumOrganizations++;
				}

			}
			
			System.out.println("Done ... " + totalNumOrganizations + " organizations were extracted");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getRandomOrganization(int locationId){
		String organization = ""; 
		
		if (organizationsByLocations.get(locationId).size() == 0) return "Organization in Location " + locationId;
		
		int randomOrganizationIdx = rand.nextInt(organizationsByLocations.get(locationId).size()); 
		organization = organizationsByLocations.get(locationId).get(randomOrganizationIdx);
		return organization;
	}
}
