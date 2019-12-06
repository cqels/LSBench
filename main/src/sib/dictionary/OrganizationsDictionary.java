package sib.dictionary;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class OrganizationsDictionary {
	RandomAccessFile dicAllInstitutes; 
	String dicFileName;

	HashMap<String, Integer> locationNames;
	
	Vector<Vector<String>> organizationsByLocations;
	Random 		rand;
	
	Random		randUnRelatedOrganization;
	double		probUnCorrelatedOrganization;
	Random		randUnRelatedLocation;

	// For top institutes
	Vector<Vector<String>>	topInstitutesByLocation;
	Vector<String>	allTopInstitutes;
	String		topInstitutesFileName;
	RandomAccessFile dicTopInstitutes; 
	Random		randTopUniv;
	double 		probTopUniv; 
	
	public OrganizationsDictionary(String _dicFileName, HashMap<String, Integer> _locationNames, 
									long seedRandom, double _probUnCorrelatedOrganization, 
									String _topInstitutesFileName, long _seedTopUni, double _probTopUni){
		this.locationNames = _locationNames; 
		this.dicFileName = _dicFileName;
		this.rand = new Random(seedRandom);
		this.randUnRelatedLocation = new Random(seedRandom);
		this.randUnRelatedOrganization = new Random(seedRandom);
		this.probUnCorrelatedOrganization = _probUnCorrelatedOrganization;
		this.topInstitutesFileName = _topInstitutesFileName;
		this.randTopUniv = new Random(_seedTopUni);
		this.probTopUniv = _probTopUni;
	}
	public void init(){
		try {
			dicAllInstitutes = new RandomAccessFile(dicFileName, "r");
			
			System.out.println("Building dictionary of organizations (by locations)");
			
			organizationsByLocations = new Vector<Vector<String>>(locationNames.size());
			for (int i = 0; i < locationNames.size(); i++){
				organizationsByLocations.add(new Vector<String>());
			}
			
			extractOrganizationNames();
			
			dicAllInstitutes.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		extractTopInstitutes();
	}
	
	public void extractTopInstitutes(){
		try {
			dicTopInstitutes = new RandomAccessFile(topInstitutesFileName, "r");
			
			// Top universities is stored in two ways (i) according to each location (ii) the whole set  
			allTopInstitutes = new Vector<String>();
			topInstitutesByLocation = new Vector<Vector<String>>(locationNames.size());
			
			for (int i = 0; i < locationNames.size(); i++){
				topInstitutesByLocation.add(new Vector<String>());
			}
			System.out.println("Get list of top institutes ...");
			
			
			String line; 
			String locationName; 
			String instituteName; 
			int totalNumTopInstitutes = 0;
			int locationId;

			while ((line = dicTopInstitutes.readLine()) != null){
				String infos[] = line.split("  ");
				instituteName = infos[0];
				locationName = infos[1];
				if (locationNames.containsKey(locationName)){		// Check whether it exists 
					locationId = locationNames.get(locationName); 
					topInstitutesByLocation.get(locationId).add(instituteName);
					allTopInstitutes.add(instituteName);
					totalNumTopInstitutes++;
				}
				else
					System.out.println("[DEBUG] The location " +locationName + " for top institute is not available ");
				
			}
			
			System.out.println("Done ... " + totalNumTopInstitutes + " top universities were extracted");
				
			
			
			dicTopInstitutes.close();
			
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
			while ((line = dicAllInstitutes.readLine()) != null){
				//System.out.println("Line --> " + line);
				//System.out.println("[0]: " + line.split(" ")[0]);
				String infos[] = line.split("  ");
				locationName = infos[0];
				//System.out.println("Line in names = " + line); 
				if (locationName.compareTo(lastLocationName) != 0){ 	// New location
					if (locationNames.containsKey(locationName)){		// Check whether it exists
						lastLocationName = locationName;
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
	
	// Check whether there is any location having no institute
	public void checkCompleteness(){
		for (int i = 0; i  < locationNames.size(); i++){
			if (organizationsByLocations.get(i).size() == 0){
				System.out.println("Location " + i + " has no institute!");
			}
		}
		
		System.exit(-1);
	}
	
	public String getRandomOrganization(int _locationId){
		String organization = ""; 
		int randomOrganizationIdx;
		int locationId = _locationId;
		
		if (randUnRelatedOrganization.nextDouble() > probUnCorrelatedOrganization){
			while (organizationsByLocations.get(locationId).size() == 0){
				locationId = randUnRelatedLocation.nextInt(locationNames.size());
			}
		
			randomOrganizationIdx = rand.nextInt(organizationsByLocations.get(locationId).size()); 
			organization = organizationsByLocations.get(locationId).get(randomOrganizationIdx);
			return organization;
		}
		else{		// Randomly select one institute out of the location
			int uncorrelateLocationIdx = randUnRelatedLocation.nextInt(locationNames.size());
			while (organizationsByLocations.get(uncorrelateLocationIdx).size() == 0){
				uncorrelateLocationIdx = randUnRelatedLocation.nextInt(locationNames.size());
			}
			
			
			randomOrganizationIdx = rand.nextInt(organizationsByLocations.get(uncorrelateLocationIdx).size()); 
			organization = organizationsByLocations.get(uncorrelateLocationIdx).get(randomOrganizationIdx);
			
			return organization;
		}
	}
	
	// User having more friends have high probability of studying at the top university
	// or in other meaning, users from top universities usually have many friends
	public String getRandomOrganization(int _locationId, boolean isPopularUser){
		String organization = ""; 
		int randomOrganizationIdx;
		int locationId = _locationId;
		
		// Check whether the user studies in a top university
		if (isPopularUser){
			if (randTopUniv.nextDouble() < probTopUniv){	// User studies in a top university
				if (topInstitutesByLocation.get(locationId).size() == 0)
					return getRandomTopUniversity();
				else{
					if (randUnRelatedOrganization.nextDouble() > probUnCorrelatedOrganization)
						return getRandomTopUniversity();
					else
						return getTopUniversityByLocation(locationId);
							
				}
			}
		}
		
		return getRandomOrganization(locationId);
	}
	
	public String getRandomTopUniversity(){
		int randomOrganizationIdx = rand.nextInt(allTopInstitutes.size());
		return allTopInstitutes.get(randomOrganizationIdx);
	}
	public String getTopUniversityByLocation(int locationId){
		int randomOrganizationIdx = rand.nextInt(allTopInstitutes.size());
		return allTopInstitutes.get(randomOrganizationIdx);
	}
}
