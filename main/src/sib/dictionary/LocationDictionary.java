package sib.dictionary;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import sib.dictionary.NamesDictionary.NameFreq;
import sib.objects.Location;
import sib.util.ZOrder;

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
	public double getLatt(int locationIdx){
		return vecLocations.get(locationIdx).getLatt();
	}
	public double getLongt(int locationIdx){
		return vecLocations.get(locationIdx).getLongt();
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
			
			orderByZ();
			
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
	
	public void orderByZ(){
		LocationZorder[] sortLocation = new LocationZorder[vecLocations.size()];
		ZOrder zorder = new ZOrder(8);
		
		for (int i = 0; i < vecLocations.size(); i++){
			Location loc = vecLocations.get(i);
			int zvalue = zorder.getZValue(((int)Math.round(loc.getLongt()) + 180)/2, ((int)Math.round(loc.getLatt()) + 180)/2);
			sortLocation[i] = new LocationZorder(loc.getId(),zvalue);
		}
		
		Arrays.sort(sortLocation);
		
		System.out.println("Sorted location according to their z-value ");
		
		for (int i = 0; i < sortLocation.length; i ++){
			//sortLocation[i].print();
			//System.out.println(sortLocation[i].id + "   " + vecLocations.get(sortLocation[i].id).getName() + "   "  + sortLocation[i].zvalue);
			vecLocations.get(sortLocation[i].id).setzId(i);
		}
	}
	
	public int getZorderID(int _locationId){
		return vecLocations.get(_locationId).getzId();
	}
	class LocationZorder implements Comparable{
		int id;
		int zvalue; 
		public LocationZorder(int _id, int _zvalue){
			this.id = _id; 
			this.zvalue = _zvalue; 
		}
		public int compareTo(Object obj)
		{
			LocationZorder tmp = (LocationZorder)obj;
			if(this.zvalue < tmp.zvalue)
			{	
				return -1;
			}
			else if(this.zvalue > tmp.zvalue)
			{
				return 1;
			}
			return 0;
		}
		public void print(){
			System.out.println(id + "  " + zvalue);
		}
	}
	
	
}
