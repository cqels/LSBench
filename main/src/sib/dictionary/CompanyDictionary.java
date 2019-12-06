package sib.dictionary;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class CompanyDictionary {
	RandomAccessFile dictionary; 
	String dicFileName;

	HashMap<String, Integer> locationNames;
	
	Vector<Vector<String>> companiesByLocations;
	Random 		rand; 
	Random		randUnRelatedCompany;
	double		probUnCorrelatedCompany;
	Random		randUnRelatedLocation;
	
	public CompanyDictionary(String _dicFileName, HashMap<String, Integer> _locationNames, 
							long seedRandom, double _probUnCorrelatedCompany){
		this.locationNames = _locationNames; 
		this.dicFileName = _dicFileName;
		this.rand = new Random(seedRandom);
		this.randUnRelatedCompany = new Random(seedRandom);
		this.randUnRelatedLocation = new Random(seedRandom);
		
		this.probUnCorrelatedCompany = _probUnCorrelatedCompany;
	}
	public void init(){
		try {
			dictionary = new RandomAccessFile(dicFileName, "r");
			
			System.out.println("Building dictionary of companies (by locations)");
			
			companiesByLocations = new Vector<Vector<String>>(locationNames.size());
			for (int i = 0; i < locationNames.size(); i++){
				companiesByLocations.add(new Vector<String>());
			}
			
			extractCompanyNames();
			
			dictionary.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void extractCompanyNames(){
		//System.out.println("Extract companies by location ...");
		String line; 
		String locationName; 
		String companyName; 
		String lastLocationName = "";
		int curLocationId = -1; 
		int totalNumCompanies = 0;
		try {
			while ((line = dictionary.readLine()) != null){
				String infos[] = line.split("  ");
				locationName = infos[0];
				if (locationName.compareTo(lastLocationName) != 0){ 	// New location
					if (locationNames.containsKey(locationName)){		// Check whether it exists 
						lastLocationName = locationName;
						curLocationId = locationNames.get(locationName); 
						companyName = infos[1].trim();
						companiesByLocations.get(curLocationId).add(companyName);
						totalNumCompanies++;
					}
				}
				else{
					companyName = infos[1].trim();
					companiesByLocations.get(curLocationId).add(companyName);
					totalNumCompanies++;
				}

			}
			
			System.out.println("Done ... " + totalNumCompanies + " companies were extracted");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Check whether there is any location having no company
	public void checkCompleteness(){
		for (int i = 0; i  < locationNames.size(); i++){
			if (companiesByLocations.get(i).size() == 0){
				System.out.println("Location " + i + " has no company!");
			}
		}
		
		System.exit(-1);
	}
	
	// Get a random company from that location
	// if that location does not have any company, go to another location
	public String getRandomCompany(int _locationId){
		String company = ""; 
		int randomCompanyIdx;
		int locationId = _locationId;
		
		if (randUnRelatedCompany.nextDouble() > probUnCorrelatedCompany){
			while (companiesByLocations.get(locationId).size() == 0){
				locationId = randUnRelatedLocation.nextInt(locationNames.size());
			}
		
			randomCompanyIdx = rand.nextInt(companiesByLocations.get(locationId).size()); 
			company = companiesByLocations.get(locationId).get(randomCompanyIdx);
			return company;
		}
		else{		// Randomly select one company out of the location
			int uncorrelateLocationIdx = randUnRelatedLocation.nextInt(locationNames.size());
			while (companiesByLocations.get(uncorrelateLocationIdx).size() == 0){
				uncorrelateLocationIdx = randUnRelatedLocation.nextInt(locationNames.size());
			}
			
			
			randomCompanyIdx = rand.nextInt(companiesByLocations.get(uncorrelateLocationIdx).size()); 
			company = companiesByLocations.get(uncorrelateLocationIdx).get(randomCompanyIdx);
			
			return company;
		}
			
	}
}
