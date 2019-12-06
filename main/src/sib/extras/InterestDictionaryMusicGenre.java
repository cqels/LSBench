package sib.extras;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.ObjectInputStream.GetField;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

public class InterestDictionaryMusicGenre {

	float 		cumGenreDistribution[];	// Store the number of singers in each music genre
	Vector<String> 		vecMusicGenres;			// Store the number of people in each location
	Vector<Vector<Integer>> vectorGenresSingle; // One genre contains many singers
	HashMap<String, Integer> 	singerNames; 		// Store the name of singers

	RandomAccessFile 	dictionary; 
	String 				dicFileName;
	boolean 			isCummulativeDist = false; 	// Store the vecInterestDistribution according to cumulative values
	
	Random 				randInterests; 	// For selecting some random interests from the singernames of a music genre 
	
	int 				latestLocationIdx = 0; 
	int					latestGenreIdx = 0;
	
	public static void main(String args[]){
		InterestDictionaryMusicGenre interestDic = new InterestDictionaryMusicGenre("/export/scratch1/duc/work/SIB/workspace/SocialGraph/Singers90sGroupBy.txt");
		interestDic.init();
		
	}
	public InterestDictionaryMusicGenre(String _dicFileName){
		this.dicFileName = _dicFileName; 
	}
	public InterestDictionaryMusicGenre(String _dicFileName, long randomInterestSeed){
		this.dicFileName = _dicFileName; 
		randInterests = new Random(randomInterestSeed);
	}
	public void init(){
		try {
			
			singerNames = new HashMap<String, Integer>();
			dictionary = new RandomAccessFile(dicFileName, "r");
			vecMusicGenres = new Vector<String>();
			vectorGenresSingle = new Vector<Vector<Integer>>();
			
			System.out.println("Extracting locations into a dictionary ");
			//extractLocations();
			extractInterestCummulative();
			
			dictionary.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public void extractInterestCummulative(){
		float cumdistribution;	//cumulative distribution value
		String line; 
		int index = 0; 
		
		isCummulativeDist = true; 
		
		String musicgenre = "";
		String lastmusicgenre = "";
		String singerName = "";
		
		int noOfGenre = 0; 
		int noOfSingers = 0; 
		
		try {
			while ((line = dictionary.readLine()) != null){
				//System.out.println("Line --> " + line);
				//System.out.println("[0]: " + line.split(" ")[0]);
				musicgenre = line.split(" ")[0];
				
				if(lastmusicgenre.compareTo(musicgenre) != 0){	// --> New music genre
					noOfGenre++;
					lastmusicgenre = musicgenre;
					vecMusicGenres.add(musicgenre);
					vectorGenresSingle.add(new Vector<Integer>());
				} 	
				
				singerName = line.split(" ")[1];
				if (singerNames.containsKey(singerName)){
					(vectorGenresSingle.get(noOfGenre-1)).add(singerNames.get(singerName));
				}
				else{
					noOfSingers++;
					//System.out.println(singerName);
					singerNames.put(singerName, noOfSingers);
					(vectorGenresSingle.get(noOfGenre-1)).add(noOfSingers);
				}
			}
			
			System.out.println("Number of genres in the dictionary is " + noOfGenre);
			System.out.println("Number of singers in the dictionary is " + noOfSingers);
			
			// Canculating the cumulative distribution for each genre
			int totalSinglesInGenres = 0; 
			for (int i = 0; i < vectorGenresSingle.size() ; i++){
				totalSinglesInGenres = totalSinglesInGenres + (vectorGenresSingle.get(i)).size();
			}
			
			System.out.println("Total number of singers in all Genres " + totalSinglesInGenres);
			int cumulation = 0;
			cumGenreDistribution = new float[noOfGenre];
			for (int i = 0; i < noOfGenre ; i++){
				cumulation = cumulation + (vectorGenresSingle.get(i)).size();
				cumGenreDistribution[i] = (float) cumulation/totalSinglesInGenres; 
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/*
	 * Based on the number of users for each location 
	 * and the user index, find the genre of music that a user has.
	 * Then, randomly select singers from that genre of musics as the user's interests
	 * */
	
	public HashSet<Integer> getInterests(int userIdx, int locationIdx, Vector<Integer> vecLocationDistribution, int _noInterests){

		HashSet<Integer> setOfInterests = new HashSet<Integer>(_noInterests);
		
		int numUsersInCurrentLocation;
		int userIdxInCurrentLocation;
		
		if (locationIdx !=0){
			numUsersInCurrentLocation = vecLocationDistribution.get(locationIdx) - vecLocationDistribution.get(locationIdx-1);
			userIdxInCurrentLocation = userIdx - vecLocationDistribution.get(locationIdx-1);
		}
		else{
			numUsersInCurrentLocation = vecLocationDistribution.get(0);
			userIdxInCurrentLocation = userIdx;
		}
		
		
		 
		
		while (userIdxInCurrentLocation > Math.round(cumGenreDistribution[latestGenreIdx] * (float)numUsersInCurrentLocation) ){
			latestGenreIdx++;
			
			if (latestGenreIdx >= cumGenreDistribution.length) latestGenreIdx=0;
		}
		
		int selectedGenreIdx = latestGenreIdx; 
		int sizeOfInterst = vectorGenresSingle.get(selectedGenreIdx).size();
		int singerIdx; 
		// Randomly select noInterests from vectorGenresSingle.get(selectedGenreIdx)
		if (_noInterests >= sizeOfInterst){
			// Add all the singers if the number of intersts is greater than the number of singers
			for(int i = 0; i < sizeOfInterst; i++){
				setOfInterests.add(vectorGenresSingle.get(selectedGenreIdx).get(i));
			}
		}
		else{	// Run noInterests random
			for (int i=0; i < _noInterests; i++){
				singerIdx = randInterests.nextInt(_noInterests);
				if (!setOfInterests.contains(singerIdx)) setOfInterests.add(singerIdx);
			}
		}
		
		return setOfInterests;
	} 
	
	// The similarity score between two users' interests is calculated based on the 
	// DICE similarity 
	// SIM(A,B) = 2 |A intersects B| / (|A|+|B|)
	public double getInterestSimilarityScore(HashSet<Integer> interestSet1,HashSet<Integer> interestSet2){
		int numSameInterests = 0; 
		Iterator<Integer> it = interestSet1.iterator();
		while (it.hasNext()){
			if (interestSet2.contains(it.next()))	numSameInterests++;
		}
		
		return (double)(2*numSameInterests)/(interestSet1.size()+interestSet2.size());
	}

	public float[] getCumGenreDistribution() {
		return cumGenreDistribution;
	}
	public void setCumGenreDistribution(float[] cumGenreDistribution) {
		this.cumGenreDistribution = cumGenreDistribution;
	}
	public Vector<String> getVecMusicGenres() {
		return vecMusicGenres;
	}
	public void setVecMusicGenres(Vector<String> vecMusicGenres) {
		this.vecMusicGenres = vecMusicGenres;
	}
	public Vector<Vector<Integer>> getVectorGenresSingle() {
		return vectorGenresSingle;
	}
	public void setVectorGenresSingle(Vector<Vector<Integer>> vectorGenresSingle) {
		this.vectorGenresSingle = vectorGenresSingle;
	}
	public HashMap<String, Integer> getSingerNames() {
		return singerNames;
	}
	public void setSingerNames(HashMap<String, Integer> singerNames) {
		this.singerNames = singerNames;
	}	
	
}
