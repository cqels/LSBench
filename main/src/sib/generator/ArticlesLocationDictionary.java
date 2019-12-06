package sib.generator;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Vector;

public class ArticlesLocationDictionary {
	
	RandomAccessFile dictionary; 
	String dicFileName;
	HashMap<String, Integer>	locationNameMapping; 	//Mapping from a location name to a id
	int totalNumberOfArticles; 
	
	public ArticlesLocationDictionary(String dicFileName, HashMap<String, Integer> _locationNameMapping){
		this.dicFileName = dicFileName;
		this.locationNameMapping = _locationNameMapping; 
	}
	
	Vector<Vector<String>>		locationArticles; 			// 

	public HashMap<String, Integer> getLocationNameMapping() {
		return locationNameMapping;
	}
	public void setLocationNameMapping(HashMap<String, Integer> locationNameMapping) {
		this.locationNameMapping = locationNameMapping;
	}

	public void init(){
		try {
			dictionary = new RandomAccessFile(dicFileName, "r");
			
			locationArticles = new Vector<Vector<String>>(locationNameMapping.size()); 
			for (int i = 0; i < locationArticles.capacity(); i ++){
				locationArticles.add(new Vector<String>());
			}
			
			System.out.println("Extracting articles with correlated locations ");
			extractArticles();
			
			dictionary.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void extractArticles(){
		String location; 

		String line;
		String content; 
		
		try {
			while ((line = dictionary.readLine()) != null){
				location = line.split(" ")[0];
				content = line.substring(location.length() + 1);
				content = content.trim();
				// Add to the vector of articles
				if (locationNameMapping.containsKey(location)){
					locationArticles.get(locationNameMapping.get(location)).add(content); 
					totalNumberOfArticles++;
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		System.out.println(totalNumberOfArticles + " articles have been collected");
	}

}
