package sib.dictionary;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import sib.objects.PopularPlace;

public class PopularPlacesDictionary {

	RandomAccessFile dicPopularPlace; 
	String dicFileName;

	HashMap<String, Integer> locationNames;
	
	Vector<Vector<PopularPlace>> popularPlacesByLocations;		//Popular places in each country
	Random 		randPopularPlaceId;
	
	int numLocations; 

	public PopularPlacesDictionary(String _dicFileName, HashMap<String, Integer> _locationNames, 
			long seedRandom){
		
		this.dicFileName = _dicFileName; 
		this.locationNames = _locationNames; 
		this.randPopularPlaceId = new Random(seedRandom);
	}
	public void init(){
		try {
			dicPopularPlace = new RandomAccessFile(dicFileName, "r");
			
			System.out.println("Building dictionary of popular places (by countries)");
			
			numLocations = locationNames.size(); 
			
			popularPlacesByLocations = new Vector<Vector<PopularPlace>>(numLocations);
			for (int i = 0; i < locationNames.size(); i++){
				popularPlacesByLocations.add(new Vector<PopularPlace>());
			}
			
			//removePopularPlacesDuplication(); // Run only one time
			
			extractPopularPlaces();
			
			//checkCompleteness();
			
			dicPopularPlace.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void extractPopularPlaces(){
		//System.out.println("Extract organizations by location ...");
		String line; 
		String locationName; 
		String popularPlaceName; 
		String lastLocationName = "";
		int curLocationId = -1; 
		int totalNumPopularPlaces = 0;
		
		String label; 
		double latt; 
		double longt; 
		try {
			while ((line = dicPopularPlace.readLine()) != null){
					
				String infos[] = line.split("  ");	//country  Name  Label  Lat  Long
				locationName = infos[0];
				//System.out.println("Line in names = " + line); 
				if (locationName.compareTo(lastLocationName) != 0){ 	// New location
					if (locationNames.containsKey(locationName)){		// Check whether it exists
						lastLocationName = locationName;
						curLocationId = locationNames.get(locationName); 
						popularPlaceName = infos[1];
						label = infos[2];
						latt = Double.parseDouble(infos[3]);
						longt = Double.parseDouble(infos[4]);
						popularPlacesByLocations.get(curLocationId).add(new PopularPlace(label, latt, longt));
						
						totalNumPopularPlaces++;
					}
						
				}
				else{
					popularPlaceName = infos[1];
					label = infos[2];
					latt = Double.parseDouble(infos[3]);
					longt = Double.parseDouble(infos[4]);
					popularPlacesByLocations.get(curLocationId).add(new PopularPlace(label, latt, longt));
					totalNumPopularPlaces++;
				}

			}
			
			System.out.println("Done ... " + totalNumPopularPlaces + " popular places were extracted");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Check whether there is any location having no institute
	public void checkCompleteness(){
		for (int i = 0; i  < locationNames.size(); i++){
			if (popularPlacesByLocations.get(i).size() == 0){
				System.out.println("Location " + i + " has no popular place!");
			}
		}
		
		System.exit(-1);
	}
	
	public short getPopularPlace(int locationidx){
		if (popularPlacesByLocations.get(locationidx).size() == 0) return -1;
		
		return (short) randPopularPlaceId.nextInt(popularPlacesByLocations.get(locationidx).size());
	}
	
	
	public int getPopularPlaceNoCheck(int locationIdx){
		return randPopularPlaceId.nextInt(popularPlacesByLocations.get(locationIdx).size()); 
	}
	
	public PopularPlace getPopularPlace(int locationIdx, int placeId){
		return popularPlacesByLocations.get(locationIdx).get(placeId);
	}
	public int getNumPopularPlaces(int locationIdx){
		return popularPlacesByLocations.get(locationIdx).size();
	}
	
	public int getNumLocations() {
		return numLocations;
	}
	public void setNumLocations(int numLocations) {
		this.numLocations = numLocations;
	}
	
	public void removePopularPlacesDuplication(){
		//System.out.println("Extract organizations by location ...");
		String dicPopularPlacesOriginal = "/export/scratch1/duc/work/virtuosoServer/virtuosoOPS/var/lib/virtuoso/db/popularPlacesByCountry.txt.original";
		String dicPopularPlaces = "/export/scratch1/duc/work/virtuosoServer/virtuosoOPS/var/lib/virtuoso/db/popularPlacesByCountry.txt";

		String line; 
		String locationName; 
		String popularPlaceName; 
		String lastLocationName = "";
		int curLocationId = -1; 
		int totalNumPopularPlaces = 0;
		String lastAddedPopularName = "";
		
		
		try {
			FileOutputStream 	dicPopularPlaceFile;
			dicPopularPlaceFile = new FileOutputStream(dicPopularPlaces);
			OutputStreamWriter writer; 
			writer = new OutputStreamWriter(dicPopularPlaceFile);
			
			RandomAccessFile dicPopularPlace = new RandomAccessFile(dicPopularPlacesOriginal, "r");

			while ((line = dicPopularPlace.readLine()) != null){
					
				String infos[] = line.split("  ");	//country  Name  Label  Lat  Long
				locationName = infos[0];
				//System.out.println("Line in names = " + line); 
				if (locationName.compareTo(lastLocationName) != 0){ 	// New location
					if (locationNames.containsKey(locationName)){		// Check whether it exists
						lastLocationName = locationName;
						curLocationId = locationNames.get(locationName); 
						popularPlaceName = infos[1].trim();
						if (popularPlaceName.compareTo(lastAddedPopularName) != 0){
							writer.write(line + "\n");
							lastAddedPopularName = popularPlaceName;
							totalNumPopularPlaces++;
						}
					}
						
				}
				else{
					popularPlaceName = infos[1].trim();
					if (popularPlaceName.compareTo(lastAddedPopularName) != 0){
						writer.write(line + "\n");
						lastAddedPopularName = popularPlaceName;
						totalNumPopularPlaces++;
					}
					
				}

			}
			
			writer.close();
			
			System.out.println("Done ... " + totalNumPopularPlaces + " organizations were extracted");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
