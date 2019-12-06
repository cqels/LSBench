package sib.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;

import sib.dictionary.LocationDictionary;
import sib.dictionary.NamesDictionary;

public class OutputDataWriter {
	// For data of test driver
	private static String 	outputDirectory = "td_data";
	private static String 	experimentDirectory = "experiment";
	private static String 	groupDataFilename = "gr.dat";
	private static String 	generalDataFilename = "general.dat";
	private static String 	userNameDataFilename = "names.dat";
	private static String 	locationDataFilename = "loc.dat";
	private static String 	userDataFilename = "users.dat";
	private static String 	socialDegreeFileName = "socialDegree";
	private static String 	clustCoefficientFileName = "clusteringCoef";
	
	ObjectOutputStream 		userDataOutput;
	File 					outputDir;
	File 					experimentOutputDir;
	//private int userData[]; 		// Store the number of friends per user 		 
	
	public OutputDataWriter(){
		outputDir = new File(outputDirectory);
		outputDir.mkdirs();
		experimentOutputDir = new File(experimentDirectory);
		experimentOutputDir.mkdirs();
	}
	protected void initWritingUserData(){
		// Write user info
		File userDataFile = new File(outputDir, userDataFilename);
		try {
			userDataOutput = new ObjectOutputStream(new FileOutputStream(userDataFile, false));
		} catch(IOException e) {
			System.err.println("Could not open or create file " + userDataFile.getAbsolutePath());
			System.err.println(e.getMessage());
			System.exit(-1);
		}

	}
	protected void writeUserData(int userId, int numOfFriend){
		try {
			userDataOutput.writeInt(userId);
			userDataOutput.writeInt(numOfFriend);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	protected void finishWritingUserData(){
		try {
			userDataOutput.writeInt(-1);			// End of file
			userDataOutput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void writeGeneralDataForTestDriver(int numtotalUser, DateGenerator dateTimeGenerator){
		// Write user info
		File generalDataFile = new File(outputDir, generalDataFilename);
		ObjectOutputStream generalDataOutput;
		try {
			generalDataFile.createNewFile();
			generalDataOutput = new ObjectOutputStream(new FileOutputStream(generalDataFile, false));
			generalDataOutput.writeInt(numtotalUser);
			generalDataOutput.writeLong(dateTimeGenerator.getCurrentDateTime());
			generalDataOutput.close();
		} catch(IOException e) {
			System.err.println("Could not open or create file " + generalDataFile.getAbsolutePath());
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	protected void writeGroupDataForTestDriver(GroupGenerator groupGenerator){
		// Write number of groups
		File groupDataFile = new File(outputDir, groupDataFilename);
		ObjectOutputStream groupDataOutput;
		try {
			groupDataFile.createNewFile();
			groupDataOutput = new ObjectOutputStream(new FileOutputStream(groupDataFile, false));
			groupDataOutput.writeInt(groupGenerator.groupId);
			groupDataOutput.close();
		} catch(IOException e) {
			System.err.println("Could not open or create file " + groupDataFile.getAbsolutePath());
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}
	
	protected void writeLocationDataForTestDriver(LocationDictionary locationDic){
		// Write location information
		File locationDataFile = new File(outputDir, locationDataFilename);
		ObjectOutputStream locationDataOutput;
		try {
			locationDataFile.createNewFile();
			locationDataOutput = new ObjectOutputStream(new FileOutputStream(locationDataFile, false));
			/*
			for (int i = 0; i < locationDic.getVecLocations().size(); i++){
				locationDataOutput.writeObject(locationDic.getVecLocations().get(i).getName());
			}
			*/
			locationDataOutput.writeObject(locationDic.getVecLocations());
			
			locationDataOutput.close();
		} catch(IOException e) {
			System.err.println("Could not open or create file " + locationDataFile.getAbsolutePath());
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}
	
	protected void writeNamesDataForTestDriver(NamesDictionary namesDictionary){
		// Write user names
		File namesDataFile = new File(outputDir, userNameDataFilename);
		ObjectOutputStream namesDataOutput;
		try {
			namesDataFile.createNewFile();
			namesDataOutput = new ObjectOutputStream(new FileOutputStream(namesDataFile, false));
			/*
			for (int i = 0; i < locationDic.getVecLocations().size(); i++){
				locationDataOutput.writeObject(locationDic.getVecLocations().get(i).getName());
			}
			*/
			//namesDataOutput.writeObject(namesDictionary.getCountBySurNames());
			//namesDataOutput.writeObject(namesDictionary.getCountByGivenNames());
			//namesDataOutput.writeObject(namesDictionary.getGivenNamesByLocations());
			namesDataOutput.writeObject(namesDictionary.getSurNamesByLocations());
			
			namesDataOutput.close();
		} catch(IOException e) {
			System.err.println("Could not open or create file " + namesDataFile.getAbsolutePath());
			System.err.println(e.getMessage());
			System.exit(-1);
		}

	}
	
	// For experiments
	protected void writeSocialDegree(int[] socialDegrees, int numOfuser){
		// Write number of groups
		try {
			Writer socialDegreeOutput = null;
			File file = new File(experimentOutputDir, socialDegreeFileName + numOfuser + ".dat");
			socialDegreeOutput = new BufferedWriter(new FileWriter(file));
			  
			for (int i = 0; i < socialDegrees.length; i++){
				socialDegreeOutput.write(i + "	" + socialDegrees[i]);
				socialDegreeOutput.write("\n");
			}
			socialDegreeOutput.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	// For experiments
	protected void writeClusteringCoefficient(double[] coefficient, int[] socialdegree, int numOfuser){
		// Write number of groups
		try {
			Writer clusteringCoefOutput = null;
			File file = new File(experimentOutputDir, clustCoefficientFileName + numOfuser + ".dat");
			clusteringCoefOutput = new BufferedWriter(new FileWriter(file));
			  
			for (int i = 1; i < coefficient.length; i++){
				if (socialdegree[i] == 0){
					//clusteringCoefOutput.write(i + "	0");
					//clusteringCoefOutput.write("\n");
					continue;
				}
				clusteringCoefOutput.write(i + "	" + (double)(coefficient[i]/(double)socialdegree[i]));
				clusteringCoefOutput.write("\n");
			}
			clusteringCoefOutput.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
