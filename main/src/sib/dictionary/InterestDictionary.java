package sib.dictionary;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

public class InterestDictionary {

	Vector<Vector<Double>> vLocationInterest; 
	HashMap<String, Integer> 	interestNames; 		// Store the name of singers
	HashMap<Integer, String> 	interestdsNamesMapping;

	RandomAccessFile 	dictionary; 
	RandomAccessFile 	interestFile;
	String 				distributionFileName;
	String				interestFileName; 
	
	Random 				randInterests; 	// For selecting random interests from the singernames
	int 				numOfIntersts;
	
	public static void main(String args[]){
		InterestDictionary interestDic = new InterestDictionary("/export/scratch1/duc/work/SIB/workspace/SocialGraph/locationInterestDist.txt",
																"/export/scratch1/duc/work/SIB/workspace/SocialGraph/singerNames.txt",
																43643564);
		interestDic.init();
	}
	
	public InterestDictionary(String _distributionFileName){
		this.distributionFileName = _distributionFileName; 
	}
	
	public InterestDictionary(String _distributionFileName, String _interestFileName, long randomInterestSeed){
		this.distributionFileName = _distributionFileName;
		this.interestFileName = _interestFileName; 
		randInterests = new Random(randomInterestSeed);
	}
	public void init(){
		try {
			
			interestNames = new HashMap<String, Integer>();
			interestdsNamesMapping = new HashMap<Integer, String>();
			dictionary = new RandomAccessFile(distributionFileName, "r");
			interestFile = new RandomAccessFile(interestFileName, "r");
			vLocationInterest = new Vector<Vector<Double>>();
			
			//System.out.println("Extracting locations into a dictionary ");
			
			extractInterests();
			
			extractInterestCummulative();
			
			System.out.println("Done ... " + interestNames.size() + " interests were extracted ");
			
			dictionary.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public void extractInterests(){
		try {
			String line; 
			int idx  = -1; 
			while ((line = interestFile.readLine()) != null){
				idx++;
				interestNames.put(line.trim().toLowerCase(),idx);
				interestdsNamesMapping.put(idx,line.trim());
			}
			
			numOfIntersts = interestNames.size();
			interestFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void extractInterestCummulative(){
		double cumdistribution;	//cumulative distribution value
		String line; 
		
		try {
			while ((line = dictionary.readLine()) != null){
				Vector<Double> vInterestDist = new Vector<Double>(interestNames.size());
				for (int i = 0; i < numOfIntersts; i++){
					line = dictionary.readLine();	// This line is for location name
					cumdistribution = Double.parseDouble(line.trim());
					vInterestDist.add(cumdistribution);
				}
				vLocationInterest.add(vInterestDist);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public HashSet<Integer> getInterests(int locationIdx, int _noInterests){
		HashSet<Integer> setOfInterests = new HashSet<Integer>(); 
		
		// Randomly select noInterests from vectorGenresSingle.get(selectedGenreIdx)
		int singerIdx; 
		for (int i=0; i < _noInterests; i++){
			double randDis = randInterests.nextDouble();
			singerIdx = getInterestIdxFromLocation(randDis, locationIdx);
			if (!setOfInterests.contains(singerIdx)) setOfInterests.add(singerIdx);
		}
		
		return setOfInterests;
	} 
	
	public int getInterestIdxFromLocation(double randomDis, int locationidx){
		Vector<Double> vInterestDis = vLocationInterest.get(locationidx);
		
		int lowerBound = 0;
		int upperBound = numOfIntersts - 1; 
		
		int curIdx = (upperBound + lowerBound)  / 2;
		
		while (upperBound > (lowerBound+1)){
			if (vInterestDis.get(curIdx) > randomDis ){
				upperBound = curIdx;
			}
			else{
				lowerBound = curIdx; 
			}
			curIdx = (upperBound + lowerBound)  / 2;
		}
		
		return curIdx; 
	}
	
	/*
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
	*/

	// We use a higher similarity score for two users' interests
	// SIM(A,B) = |A intersects B| / (min(|A|,|B|))
	public double getInterestSimilarityScore(HashSet<Integer> interestSet1,HashSet<Integer> interestSet2){
		int numSameInterests = 0; 
		Iterator<Integer> it = interestSet1.iterator();
		while (it.hasNext()){
			if (interestSet2.contains(it.next()))	numSameInterests++;
		}
		
		return (double)(numSameInterests)/ Math.min(interestSet1.size(),interestSet2.size());
	}
	
	public HashMap<String, Integer> getInterestNames() {
		return interestNames;
	}
	public void setInterestNames(HashMap<String, Integer> interestNames) {
		this.interestNames = interestNames;
	}
    public HashMap<Integer, String> getInterestdsNamesMapping() {
        return interestdsNamesMapping;
    }
}
