package sib.generator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import sib.extras.LocationCoef;
import sib.objects.Location;

/*
 * This class is used for generating the dictionaries. Thus, it needs to run only one time. 
 * The paths of the input files are the absoluted paths in Duc's computer 
 * */
public class DictionaryBuilder {
	
	String locationOriginalFile = "/export/scratch1/duc/work/virtuosoServer/virtuosoOPS/var/lib/virtuoso/db/allCountriesOriginal.txt";
	String notCountriesFile = "/export/scratch1/duc/work/virtuosoServer/virtuosoOPS/var/lib/virtuoso/db/notCountries.txt";
	String locationFile = "/export/scratch1/duc/work/virtuosoServer/virtuosoOPS/var/lib/virtuoso/db/allCountries.txt";
	String dicLocation = "/export/scratch1/duc/work/virtuosoServer/virtuosoOPS/var/lib/virtuoso/db/dicLocation.txt";
	//String interestFile = "/export/scratch1/duc/work/SIB/googleTrends/regionalDistribution.txt";
	String interestFile = "/export/scratch1/duc/work/SIB/googleTrends/regionalDistribution.txt.30";
	String locationInterestFile = "/export/scratch1/duc/work/SIB/googleTrends/locationInterestDist.txt"; 
	
	double exponentialCoef = 1.2;

	RandomAccessFile 	locationDic;
	RandomAccessFile 	interestDic;
	
	HashMap<String, Integer> locationNames; 
	HashMap<Integer, String> locationId_Names;
	Vector<Location> vLocations; 			// vector of location
	
	Vector<Vector<LocationCoef>> vvInterests; 	// vector of interests. Each interest has top-10 locations where it is popular 
											// each element stores the relative number of searches for this interest 
	HashMap<String, Integer> interestNames;
	HashMap<Integer, String> interestId_Names;
	
	Vector<Vector<Double>> coefInterestLocation;		// Distribution of each interest in corresponding to the locations 	
	Vector<Vector<Double>> coefLocationInterest;		// Distribution of each location in corresponding to the interests
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DictionaryBuilder builder = new DictionaryBuilder();

		// Run only one time
		//builder.extractOriginalCountries();
		builder.buildCummulativeDistByPopulation();
		
		builder.init();
		builder.extractLocationInfo();
		builder.extractInterestInfo();
		builder.distributionGenerate();
		//builder.writeDistributionToFile();
		builder.writeDistributionWithCummulativeNormalization();
		
	}	
	public void init(){
		try {
			locationDic = new RandomAccessFile(locationFile,"r");
			interestDic = new RandomAccessFile(interestFile, "r");
			locationNames = new HashMap<String, Integer>();
			locationId_Names = new HashMap<Integer, String>();
			interestNames = new HashMap<String, Integer>(); 
			interestId_Names = new HashMap<Integer, String>();
			
			vLocations = new Vector<Location>(); 
			vvInterests = new Vector<Vector<LocationCoef>>();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void extractLocationInfo(){
		System.out.println("Start extracting location information ");
		try {
			int id = -1; 
			String line;
			while ((line = locationDic.readLine()) != null){
				String infos[] = line.split(" ");
				if (locationNames.containsKey(infos[0])){
					System.out.println("Check Input: A doubplication in location name");
				}
				else{
					id++;
					locationNames.put(infos[0], id);
					locationId_Names.put(id, infos[0]);
				
					//Put the simple name of the country by removing Republic_of_ from the fullName
					if (infos[0].contains("Republic_of_")){
						String realName = infos[0].replaceFirst("Republic_of_", "");	
						locationNames.put(realName, id);
						locationId_Names.put(id, realName);
					}
					else if (infos[0].contains("Kingdom_of_")){
						String realName = infos[0].replaceFirst("Kingdom_of_", "");	
						locationNames.put(realName, id);
						locationId_Names.put(id, realName);
					}
					
					Location location = new  Location(); 
					location.setName(infos[0]);
					location.setId(id);
					location.setLatt(Double.parseDouble(infos[1]));
					location.setLongt(Double.parseDouble(infos[2]));
					location.setPopulation(Integer.parseInt(infos[3]));
					
					vLocations.add(location);
				}
			}
			
			locationDic.close();
			System.out.println("Number of location extracted " + vLocations.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void extractInterestInfo(){
		System.out.println("Start extracting interests information ");
		try {
			String line; 
			int lineNo = 0;
			int interestIdx = -1;
			int noRegions = 0; 
			while ((line = interestDic.readLine()) != null){
				lineNo++;
				String infos[] = line.split(",");
				if (infos[0].compareTo("Region") == 0){		// new interest
					String interestName = infos[1].trim();
					if (interestNames.containsKey(interestName)) 
						System.out.println("Check input of interests: Duplication");
					else{
						interestIdx++;
						interestNames.put(interestName,interestIdx);
						interestId_Names.put(interestIdx, interestName);
						vvInterests.add(new Vector<LocationCoef>());
					}
				}
				else{
					String regionalName = infos[0].trim();
					regionalName = regionalName.replace(" ", "_");
					boolean isFound =  true; 
					//find the idx of the region in the location dictionary
					if (!locationNames.containsKey(regionalName)){
						//Try to check by removing some word such as _Federation or _Republic
						if (regionalName.contains("_Federation")){
							regionalName = regionalName.replace("_Federation", "");
							if (!locationNames.containsKey(regionalName)) isFound = false; 
						}
						else if (regionalName.contains("_Republic")){
							regionalName = regionalName.replace("_Republic", "");
							if (!locationNames.containsKey(regionalName)) isFound = false; 
						}
						else{
							isFound = false;
							System.out.println(regionalName + " does not appear");
						}
					}
					//Only add the region appeared in the locationDic
					if (isFound == true){
						LocationCoef locationCoef = new LocationCoef(); 
						locationCoef.setLocationIdx(locationNames.get(regionalName));
						locationCoef.setRate(Double.parseDouble(infos[1].trim()));

						vvInterests.get(interestIdx).add(locationCoef);
						noRegions++;
					}
				}
			}
			
			interestDic.close();
			System.out.println("Number of lines read: " + lineNo);
			
			System.out.println("Number of interests added: " + vvInterests.size() + " with " + noRegions +" regions");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	public void distributionGenerate(){
		System.out.println("Start generating the distribution ");
		
		coefInterestLocation = new Vector<Vector<Double>>(vvInterests.size());
		coefLocationInterest = new Vector<Vector<Double>>(vLocations.size());
		
		//init for coefLocationInterest
		for (int i = 0; i < vLocations.size(); i ++){
			coefLocationInterest.add(new Vector<Double>(vvInterests.size()));
		}
		

		int interestIdx = -1; 
		Iterator interestIterator = vvInterests.iterator();
		while (interestIterator.hasNext()){
			interestIdx++;
			coefInterestLocation.add(new Vector<Double>());
			Vector<LocationCoef> locationCoefSet = (Vector<LocationCoef>)interestIterator.next();
			
			for (int i=0; i < vLocations.size(); i++){
				
				// Check if the location is in the list of this interest
				int coefIdx = getCoefLocationIdx(vLocations.get(i).getId(),locationCoefSet); 
				if (coefIdx != -1){
					coefInterestLocation.lastElement().add(locationCoefSet.get(coefIdx).getRate());
					coefLocationInterest.get(i).add(locationCoefSet.get(coefIdx).getRate());
					//System.out.println("Coef of " +coefIdx+ " is: " + locationCoefSet.get(coefIdx).getRate());
				}
				else{	//Calculate by using the power-law function
					double distribution = getDistributionFromNearest(i, locationCoefSet);
					coefInterestLocation.lastElement().add(distribution);
					coefLocationInterest.get(i).add(distribution);
				}
			}
		}
		
		System.out.println("Finish generating the distribution for " + coefInterestLocation.size() + " interests");
	}
	
	public void writeDistributionToFile(){
		try {
			FileOutputStream ofLocationInterest = new FileOutputStream(locationInterestFile);
			OutputStreamWriter writer; 
			writer = new OutputStreamWriter(ofLocationInterest);
			
			Iterator iter = coefLocationInterest.iterator();
			int idx = -1; 
			while (iter.hasNext()){
				idx++;

				writer.write(locationId_Names.get(idx) + " (Location " + idx + ") \n");
				Vector<Double> vInterestDist = (Vector<Double>)iter.next();
				Iterator<Double> iterInterest = vInterestDist.iterator();
				int interestIdx = -1;
				while (iterInterest.hasNext()){
					interestIdx++;
					writer.write(interestId_Names.get(interestIdx) + "  " + iterInterest.next().toString() + "\n");
				}
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}

	public void writeDistributionWithCummulativeNormalization(){
		try {
			FileOutputStream ofLocationInterest = new FileOutputStream(locationInterestFile);
			OutputStreamWriter writer; 
			writer = new OutputStreamWriter(ofLocationInterest);
			
			Iterator iter = coefLocationInterest.iterator();
			int idx = -1; 
			while (iter.hasNext()){
				idx++;

				writer.write(locationId_Names.get(idx) + " (Location " + idx + ") \n");
				Vector<Double> vInterestDist = (Vector<Double>)iter.next();
				double normalize = getNormalizeCoef(vInterestDist);
				double cummulation = 0.0;
				Iterator<Double> iterInterest = vInterestDist.iterator();
				int interestIdx = -1;
				while (iterInterest.hasNext()){
					interestIdx++;
					cummulation = cummulation + iterInterest.next() * normalize;
					//writer.write(interestId_Names.get(interestIdx) + " " + cummulation + "\n");
					writer.write(cummulation + "\n");
				}
				
				//writer.write("\n");
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
	public double sumVector(Vector<Double> vInput){
		double sum = 0.0; 
		
		Iterator<Double> iter = vInput.iterator(); 
		while (iter.hasNext()){
			sum = sum + (double)iter.next(); 
		}
		
		return sum; 
	}
	public double getNormalizeCoef(Vector<Double> vInput){
		double normalize = 0.0;
		normalize = (double) 1.0 /  sumVector(vInput);
		
		return normalize;
	}

	
	public int getCoefLocationIdx(int locationIdx, Vector<LocationCoef> locationCoefSet){
		int returnIdx = -1;

		for (int i = 0; i < locationCoefSet.size(); i++){
			if (locationCoefSet.get(i).getLocationIdx() == locationIdx){
				returnIdx = i;
				break;
			}
		}

		return returnIdx;  
	}

	public double getDistributionFromNearest(int locationIdx, Vector<LocationCoef> locationCoefSet){
		double nearestDistance = 500;
		double distribute = 0.0; 
		double nearestInterestLocationRate = 0.0;
		
		Iterator iter = locationCoefSet.iterator();
		while (iter.hasNext()){
			LocationCoef locationCoef = (LocationCoef)iter.next();
			double distance = getEuclidDistance(locationCoef.getLocationIdx(), locationIdx);
			if (nearestDistance > distance) {
				nearestDistance = distance;
				nearestInterestLocationRate = locationCoef.getRate();
			}
		}

		distribute = getExponentialValue(nearestInterestLocationRate, nearestDistance);
		
		return distribute; 
	}
	
	// Since there can be duplications in the countryname lat long
	// when we run a sparql query for retrieving countries, 
	// we use this function in order to remove all the duplications,
	// and then write the compress list of countries in a new file
	
	public void extractOriginalCountries(){
		System.out.println("Start extracting location information ");
		try {
			String line;
			
			HashSet<String> setNotCountries = new HashSet<String>();
			
			RandomAccessFile notCountryDic = new RandomAccessFile(notCountriesFile, "r");;
			
			while((line = notCountryDic.readLine()) != null){
				String infos[] = line.split(" ");
				if (setNotCountries.contains(infos[0]) == false) 
					setNotCountries.add(infos[0]);
			}
			
			notCountryDic.close();
			
			
			RandomAccessFile 	locationOriginalDic;
			locationOriginalDic = new RandomAccessFile(locationOriginalFile, "r");
			FileOutputStream 	outputlocationDic;
			outputlocationDic = new FileOutputStream(locationFile);
			OutputStreamWriter writer; 
			writer = new OutputStreamWriter(outputlocationDic);

			
			String lastLocation = "xyz";
			while ((line = locationOriginalDic.readLine()) != null){	
				String infos[] = line.split("  ");
				if (infos[0].startsWith(lastLocation)==false){		//Remove duplication
					if (infos[0].contains("%") == false 
							&& infos[0].contains("Island")==false
							&& infos[0].contains("-")==false
							&& infos[0].contains("County")==false
							&& infos[0].contains("Duchy")==false
							&& infos[0].contains("City")==false
							&& infos[0].contains("History")==false	){
						
						lastLocation = infos[0];
						if (setNotCountries.contains(infos[0]) == false){
							long lat = Math.round(Double.parseDouble(infos[1])) ;
							long longt  = Math.round(Double.parseDouble(infos[2])) ;
							long population  = Math.round(Double.parseDouble(infos[3])) ;
							writer.write(infos[0] + " " + lat + " " + longt + " " + population + "\n");
						}
					}
				}
			}
			
			locationOriginalDic.close();
			
			writer.close();
			//outputlocationDic.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void buildCummulativeDistByPopulation(){
		long sumPopulation = 0; 
		try {
			FileOutputStream 	dicLocationFile;
			dicLocationFile = new FileOutputStream(dicLocation);
			OutputStreamWriter writer; 
			writer = new OutputStreamWriter(dicLocationFile);
			
			RandomAccessFile 	countriesFile;
			countriesFile = new RandomAccessFile(locationFile, "r");
			String line = "";
			while ((line = countriesFile.readLine()) != null){
				String infos[] = line.split(" ");
				sumPopulation = sumPopulation + Long.parseLong(infos[3]);
			}
			
			countriesFile.close(); 
			
			// Second round
			countriesFile = new RandomAccessFile(locationFile, "r");
			double cummulativeValue = 0.0;
			long currentSum = 0; 
			while ((line = countriesFile.readLine()) != null){
				String infos[] = line.split(" ");
				currentSum = currentSum +  + Long.parseLong(infos[3]);
				cummulativeValue = (double) currentSum/sumPopulation; 
				writer.write(infos[0] +" " + infos[1] + " " + infos[2] + " " + infos[3] + " " + cummulativeValue +"\n");
			}
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	// This functin returns the value of 
	// alpha * (exponentialCoef power -x)
	// Here alpha should the rate of appearing at one particular location for a interest
	public double getExponentialValue(double alpha, double x){
		double value = 0.0; 
		//value = alpha * Math.exp(x);
		value = alpha * Math.pow(exponentialCoef, (0-x));
		return value; 
	}
	
	public double getEuclidDistance(int locationIdx1, int locationIdx2){
		double distance; 
		Location location1 =  vLocations.get(locationIdx1);
		Location location2 = vLocations.get(locationIdx2);
		
		distance = Math.sqrt((location1.getLatt()  - location2.getLatt()) * (location1.getLatt() - location2.getLatt()) + 
					(location1.getLongt() - location2.getLongt()) * (location1.getLongt() - location2.getLongt()));

		//System.out.println("Distance: " + distance);
		
		return distance; 
	}
}
