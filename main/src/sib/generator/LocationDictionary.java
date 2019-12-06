package sib.generator;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Vector;

import sib.objects.Location;

public class LocationDictionary {

	int numberOfUsers; 
	Vector<Integer> vecLocationDistribution;	// Store the number of people in each location

	Vector<Location> vecLocations;
	
	RandomAccessFile dictionary; 
	String dicFileName;
	HashMap<String, Integer>	locationNameMapping; 	//Mapping from a location name to a id 
	
	boolean isCummulativeDist = false; 	// Store the vecLocationDistribution according to cumulative values
	int countNumOfSameLocation = 0; 
	int curLocationIdx = 0;

	public LocationDictionary(int _numberOfUsers, String dicFileName){
		this.numberOfUsers = _numberOfUsers; 
		this.dicFileName = dicFileName;
	}
	
	public HashMap<String, Integer> getLocationNameMapping() {
		return locationNameMapping;
	}
	public void setLocationNameMapping(HashMap<String, Integer> locationNameMapping) {
		this.locationNameMapping = locationNameMapping;
	}
	
	public Vector<Integer> getVecLocationDistribution() {
		return vecLocationDistribution;
	}
	public void setVecLocationDistribution(Vector<Integer> vecLocationDistribution) {
		this.vecLocationDistribution = vecLocationDistribution;
	}

	public Vector<Location> getVecLocations() {
		return vecLocations;
	}

	public void setVecLocations(Vector<Location> vecLocations) {
		this.vecLocations = vecLocations;
	}

	public String getLocatioName(int locationIdx){
		return vecLocations.get(locationIdx).getName();
	}
	public void init(){
		try {
			dictionary = new RandomAccessFile(dicFileName, "r");
			vecLocationDistribution = new Vector<Integer>();
			vecLocations = new Vector<Location>();
			locationNameMapping = new HashMap<String, Integer>();
			
			//System.out.println("Extracting locations into a dictionary ");
			//extractLocations();
			extractLocationsCummulative();
			
			dictionary.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void extractLocationsCummulative(){
		String locationName; 
		float cumdistribution;	//cummulative distribution value
		String line; 
		
		isCummulativeDist = true; 
		
		try {
			while ((line = dictionary.readLine()) != null){
				String infos[] = line.split(" ");
				locationName = infos[0];

				locationNameMapping.put(locationName,vecLocations.size());	
				
				Location location = new Location(); 
				location.setId(vecLocations.size());
				location.setName(locationName);
				location.setLatt(Double.parseDouble(infos[1]));
				location.setLongt(Double.parseDouble(infos[2]));
				location.setPopulation(Integer.parseInt(infos[3]));
				
				vecLocations.add(location);
				
				cumdistribution = Float.parseFloat(infos[4]);
				vecLocationDistribution.add(Math.round(cumdistribution*(float)numberOfUsers));
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("Done ... " + vecLocationDistribution.size() + " locations were extracted");
		//Recalculate the number of people for each locations
	}
	public void extractLocations(){
		String locationName; 
		float cumdistribution;	//cummulative distribution value
		String line; 
		int total = 0;
		int lasttotal = 0; 
		
		isCummulativeDist = false; 
		
		try {
			while ((line = dictionary.readLine()) != null){
				String infos[] = line.split(" ");
				locationName = infos[0];
				cumdistribution = Integer.parseInt(infos[4]);
				
				Location location = new Location(); 
				location.setId(vecLocations.size());
				location.setName(locationName);
				location.setLatt(Double.parseDouble(infos[1]));
				location.setLongt(Double.parseDouble(infos[2]));
				location.setPopulation(Integer.parseInt(infos[3]));
				
				vecLocations.add(location);
				
				total = Math.round(cumdistribution*(float)numberOfUsers);
				vecLocationDistribution.add(total - lasttotal);
				lasttotal = total;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println(vecLocationDistribution.size() + " locations were extracted");
		//Recalculate the number of people for each locations
	}
	
	public int getLocation(int userIdx){
		if (isCummulativeDist){
			if (userIdx < vecLocationDistribution.get(curLocationIdx))	return curLocationIdx;
			else
			{
				curLocationIdx++;
				return curLocationIdx;
			}
					
		}
		else{
			if (countNumOfSameLocation < vecLocationDistribution.get(curLocationIdx)){
				countNumOfSameLocation++;
				return curLocationIdx;
			}
			else{
				countNumOfSameLocation = 0;
				curLocationIdx++;
				return curLocationIdx;
			}
		}
	}
	
}
