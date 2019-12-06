/*
 * Copyright (C) 2011 Minh-Duc Pham, CWI (Centrum Wiskunde & Informatica)
 *	 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package sib.generator;

 import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

import org.apache.hadoop.mapred.lib.TotalOrderPartitioner;

import sib.dictionary.BrowserDictionary;
import sib.dictionary.CompanyDictionary;
import sib.dictionary.EmailDictionary;
import sib.dictionary.EventsDictionary;
import sib.dictionary.IPAddressDictionary;
import sib.dictionary.InterestDictionary;
import sib.dictionary.LocationDictionary;
import sib.dictionary.NamesDictionary;
import sib.dictionary.OrganizationsDictionary;
import sib.dictionary.PopularPlacesDictionary;
import sib.dictionary.UserAgentDictionary;
import sib.experiment.ShortestPathCalculator;
import sib.objects.Comment;
import sib.objects.Friend;
import sib.objects.GPS;
import sib.objects.Group;
import sib.objects.GroupMemberShip;
import sib.objects.Photo;
import sib.objects.PhotoAlbum;
import sib.objects.PhotoStream;
import sib.objects.Post;
import sib.objects.PostStream;
import sib.objects.ReducedUserProfile;
import sib.objects.RelationshipStatus;
import sib.objects.UserExtraInfo;
import sib.objects.UserProfile;
import sib.serializer.NTriples;
import sib.serializer.RDFFacts;
import sib.serializer.RDFTriples;
import sib.serializer.Serializer;
import sib.serializer.TriG;
import sib.serializer.Turtle;
import sib.storage.MFStoreManager;
import sib.storage.StorageManager;
import sib.storage.StreamStoreManager;
import sib.util.ExternalSort;
import sib.util.GPSExternalSort;
import sib.util.PhotoExternalSort;
import sib.util.PostExternalSort;

public class ScalableGenerator implements Runnable{
//public class ScalableGenerator{
	
	// For sliding window
	int 					cellSize = 200; // Number of user in one cell
	int 					numberOfCellPerWindow = 4;
	int	 					numtotalUser = 10000;
	int 					windowSize;
	int 					lastCellPos;
	int 					lastCell;
	
	//UserProfile 			userProfiles[];			// Store window of user profiles 
	//UserProfile 			userProfilesCell[]; 	// Store new cell of user profiles
	//UserProfile 			removedUserProfiles[]; 	// This is for group generation
											// to keep the information of user's friends
	
	ReducedUserProfile  	reducedUserProfiles[];
	ReducedUserProfile 		reducedUserProfilesCell[]; 	// Store new cell of user profiles
	ReducedUserProfile 		removedUserProfiles[];
	
	// For (de-)serialization
	FileOutputStream 		ofUserProfiles;
	ObjectOutputStream 		oosUserProfile;

	FileInputStream 		ifUserProfile;
	ObjectInputStream 		oisUserProfile;

	// For multiple files
	boolean 				isMultipleFile = true;
	int 					numFiles = 10;
	int 					numCellPerfile;
	int 					numCellInLastFile;
	int 					numCellRead = 0;
	Random 					randomFileSelect;
	Random 					randomIdxInWindow;
	HashSet<Integer> 		selectedFileIdx;

	//FileOutputStream 		multiFilesFOUserProfiles[]; // Set of file output streams
	//ObjectOutputStream 		multiFilesOOUserProfiles[];

	//FileInputStream 		multiFilesFIUserProfiles[]; // Set of file input streams
	//ObjectInputStream 		multiFilesOIUserProfiles[];
	
	// For friendship generation
	int 				friendshipNo = 0;
	int 				minNoFriends = 5;
	int 				maxNoFriends = 50;
	double 				friendRejectRatio = 0.02;
	double 				friendReApproveRatio = 0.5;
	
	int					numCorrDimensions = 3;			// Run 3 passes for friendship generation
	StorageManager	 	storeManager[];
	StorageManager		groupStoreManager; 		
	
	double 				friendsRatioPerPass[] = { 0.3, 0.3, 0.4 }; // Indicate how many
											// friends will be created for each pass
											// e.g., 30% in the first pass with
											// location information
	Random 				randFriendReject;
	Random 				randFriendReapprov;
	Random 				randInitiator;
	double 				alpha = 2;							// Alpha value for power-law distribution
	double	 			baseProbLocationCorrelated = 0.8; 	// Higher probability, faster
														// sliding along this pass
	double 				baseProbInterestCorrelated = 1.0; 	
	double 				baseProbNotCorrelation = 0.2;

	double 				baseProbCorrelated = 0.8;  	  // Probability that two user having the same 
													// atrributes value become friends

	double 				baseExponentialRate = 0.8; 
	// For each user
	int 				maxNoInterestsPerUser = 10;
	int 				maxNumLocationPostPerWeek = 10;
	int 				maxNumInterestPostPerWeek = 15;
	int 				maxNumComments = 20;
	

	// Random values generators
	PowerDistGenerator 	randPowerlaw;
	Random 				seedRandom = new Random(53223436L);
	Long[] 				seeds;
	Random 				randUniform;
	Random 				randNumInterest;
	Random 				randomFriendIdx; // For third pass
	Random 				randNumberPost;
	Random 				randNumberComments; // Generate number of comments per post
	Random 				randNumberPhotoAlbum;
	Random 				randNumberPhotos;
	Random 				randNumberGroup;
	Random 				randNumberUserPerGroup;
	
	Random				randUserRandomIdx;	// For generating the 
											// random dimension
	int					maxUserRandomIdx = 1000;  //SHOULE BE IMPORTANT PARAM
	
	DateGenerator 		dateTimeGenerator;
	int					startYear; 
	int					startMonth; 
	int					startDate;
	int					endYear; 
	int					endMonth;
	int					endDate;

	// Dictionaries
	
	LocationDictionary 		locationDic;
	String 					locationDicFile;
	
	InterestDictionary 		interestDic;
	String 					interestDicFile;
	String 					interestNamesFile;

	NamesDictionary 		namesDictionary;
	String 					namesDicFile;

	OrganizationsDictionary	organizationsDictionary;
	String 					organizationsDicFile;
	String 					topInstitutesFileName;
	double 					probUnCorrelatedOrganization = 0.05;
	double 					probTopUniv = 0.7; // Probability that a user having many friend
											   // studies at a top university

	CompanyDictionary 		companiesDictionary;
	String 					companiesDicFile;
	double 					probUnCorrelatedCompany = 0.05;
	
	UserAgentDictionary 	userAgentDic;
	String 					agentFile;
	
	EmailDictionary 		emailDic;
	String 					emailDicFile;
	
	BrowserDictionary 		browserDic;
	String 					browserDicFile;
	
	PopularPlacesDictionary	popularDictionary; 
	String 					popularPlacesDicFile;
	int  					maxNumPopularPlaces;
	Random 					randNumPopularPlaces; 
	double 					probPopularPlaces;		//probability of taking a photo at popular place 
	
	IPAddressDictionary 	ipAddDictionary;

	int locationIdx = 0; 	//Which is current index of the location in dictionary

	// For generating texts of posts and comments
	RandomTextGenerator 	textGenerator;
	int 					maxNumLikes = 10;
	String 					regionalArticleFile;
	String 					interestArticleFile;
	String 					stopWordFileName;

	GroupPostGenerator 		groupPostGenerator;
	String 					groupArticleFile;

	int 					numArticles = 3606; // May be set -1 if do not know
	int 					minTextSize = 20;
	int 					maxTextSize = 200;
	int 					minCommentSize = 20;
	int 					maxCommentSize = 60;
	double 					ratioReduceText = 0.8; // 80% text has size less than 1/2 max size

	// For photo generator
	PhotoGenerator 			photoGenerator;
	int 					maxNumUserTags = 10;
	int 					maxNumPhotoAlbums = 3;	// This is number of photo album per week
	int 					maxNumPhotoPerAlbums = 40;

	// For generating groups
	GroupGenerator 			groupGenerator;
	int 					maxNumGroupCreatedPerUser = 4;
	int 					maxNumMemberGroup = 100;
	double 					groupModeratorProb = 0.05;
	double 					levelProbs[] = { 0.5, 0.8, 1.0 }; 	// Cumulative 'join' probability
																// for friends of level 1, 2 and 3
																// of a user
	double 					joinProbs[] = { 0.7, 0.4, 0.1 };
	
	Random 					randFriendLevelSelect; 	// For select an level of moderator's
													// friendship
	Random 					randMembership; // For deciding whether or not a user is joined

	Random 					randMemberIdxSelector;
	Random 					randGroupMemStep;

	Random 					randGroupModerator; // Decide whether a user can be moderator
												// of groups or not

	// For group posts
	int 					maxNumGroupPostPerWeek = 5;
	Random 					randNumberGroupPost;


	// For serialize to RDF format
	Serializer 		serializer;
	String 			serializerType = "ttl";
	String 			rdfOutputFileName = "sibdataset";
	String 			outUserProfileName = "userProf.ser";
	String 			outUserProfile;
	int 			numRdfOutputFile = 1;
	boolean 		forwardChaining = false;
	int				mapreduceFileIdx; 
	String 			sibOutputDir;
	String 			sibHomeDir;

	// For user's extra info
	String 					gender[] = { "male", "female" };
	Random					randomExtraInfo;
	Random					randomExactLongLat;
	double 					missingRatio = 0.2;
	Random 					randomHaveStatus;
	Random 					randomStatusSingle;
	Random 					randomStatus;
	double 					missingStatusRatio = 0.5;
	double 					probSingleStatus = 0.8; // Status "Single" has more probability than
													// others'
	
	
	double 		probAnotherBrowser;

	String 		countryAbbrMappingFile;
	String 		ipZoneDir;

	double 		probHavingSmartPhone = 0.5;
	Random 		randUserAgent;
	double 		probSentFromAgent = 0.2;
	Random 		randIsFrequent;
	double 		probUnFrequent = 0.01; // Whether or not a user frequently changes

	// The probability that normal user posts from different location
	double 		probDiffIPinTravelSeason = 0.1; // in travel season
	double 		probDiffIPnotTravelSeason = 0.02; // not in travel season

	// The probability that travellers post from different location
	double 		probDiffIPforTraveller = 0.3;

	// For loading parameters;
	String 			paramFileName = "params.ini";
	static String	shellArgs[]; 
	// //////////////////////////

	// For debugging
	int 		shouldbeAdd = 0;
	int 		friendLocationNumInit = 0;
	int 		deserializedfriendLocationNum = 0;
	int 		numberSerializedObject = 0;
	int 		numberDESerializedObject = 0;
	int 		numGenerateTimesRun = 0;
	int 		numDerializedFromFile = 0;
	int 		numSerializedToFile = 0;

	// For MapReduce 
	public static int			numMaps = -1;
	
	MFStoreManager 		mfStore;
	
	// Writing data for test driver
	OutputDataWriter 	outputDataWriter;
	int 				thresholdPopularUser = 40;
	int 				numPopularUser = 0;

	// For doing experiment
	boolean 			isExperimenting = false;
	boolean 			isCalcShortPath = false;
	ArrayList<Integer> 	arrayUserFriends[];
	
	
	// Create Post Stream
	boolean				isPostStream = false;
	StreamStoreManager	postStreamStoreMng; 
	String				postStreamFileName = "postStream";
	Serializer 			postSerializer;
	String 				streamSerializerType = "nt";
	String 				postStreamOutputFileName = "rdfPostStream";
	int					numPostLikeDuplication; 
	
	// Create Photo Stream 
	boolean				isPhotoStream = false;
	StreamStoreManager	photoStreamStoreMng; 
	String				photoStreamFileName = "photoStream";
	Serializer 			photoSerializer; 
	String 				photoStreamOutputFileName = "rdfPhotoStream";
	int					numPhotoLikeDuplication;
	
	// Create GPS Stream
	boolean 			isGPSStream = false; 
	EventsDictionary 	eventsDic; 
	int					numEvents = 50; 
	StreamStoreManager 	gpsStreamStoreMng; 
	Serializer			gpsSerializer; 
	String				gpsStreamOutputFileName = "gpsStream";
	GPSGenerator 		gpsGenerator;
	double				probProvideGPS = 0.1; 
	int 				maxNumSharingGPSperUser = 100;
	int					maxNumGPSperSharingTime = 10;

	// Create Post/Photo Like Stream 
	Serializer 			postLikeSerializer;
	String 				postLikeStreamOutputFileName = "rdfPostLikeStream";
	Serializer 			photoLikeSerializer;
	String 				photoLikeStreamOutputFileName = "rdfPhotoLikeStream";
	
	public static void main(String args[]) {
		
		if (args.length < 3){
			System.out.println("At least 3 parameters need to be provided");
			System.exit(-1);
		}
		
		numMaps = Integer.parseInt(args[0]);
		//String sibOutputDir = "/export/scratch2/duc/work/SIB/workspace/SocialGraph/outputDir/";
		//String sibHomeDir = "/export/scratch2/duc/work/SIB/workspace/SocialGraph/";
		String sibOutputDir = args[1].toString();
		String sibHomeDir = args[2].toString();
		shellArgs = args; 
		for (int i = 0; i < numMaps; i++){
			(new Thread(new ScalableGenerator(i, sibOutputDir, sibHomeDir))).start();
		}
	}
	
    public void run() {
    	
    	System.out.println("Heap size at tihe beginning ");
    	//printHeapSize();
    	
    	long startRunTime = System.currentTimeMillis();
    	
    	//String[] args = new String[0];
		String[] mrfilenames = prepareMRInput(shellArgs, numMaps, "mr"+mapreduceFileIdx+"_"+"mrInputFile.txt");
		int numofcell = 0; 
		if (mapreduceFileIdx == (numMaps -1) )
			numofcell = numCellInLastFile;
		else
			numofcell = numCellPerfile;
		
		mapreduceTask(mrfilenames[mapreduceFileIdx], numofcell);
		
		long endRunTime = System.currentTimeMillis();
		System.out.println("Total time for a run is " + getDuration(startRunTime, endRunTime));
    }

	public ScalableGenerator(int _mapreduceFileIdx, String _sibOutputDir, String _sibHomeDir){
		mapreduceFileIdx = _mapreduceFileIdx;
		System.out.println("Map Reduce File Idx is: " + mapreduceFileIdx);
		if (mapreduceFileIdx != -1){
			
			outUserProfile = "mr" + mapreduceFileIdx + "_" + outUserProfileName;
		}
		
		sibOutputDir = _sibOutputDir;
		sibHomeDir = _sibHomeDir; 
		System.out.println("Current directory in ScaleGenerator is " + _sibHomeDir);
	}
	public String[] prepareMRInput(String args[], int _numMaps, String _mrInputFile){
		
		long startTime = System.currentTimeMillis();
		loadParamsFromFile();
		loadParamsFromShell(args);

		numFiles = _numMaps;
		
		rdfOutputFileName = "mr" + mapreduceFileIdx + "_" + rdfOutputFileName;
		rdfOutputFileName = rdfOutputFileName + numtotalUser;
		
		init();
		
		System.out.println("Number of files " + numFiles);
		System.out.println("Number of cells per file " + numCellPerfile);
		System.out.println("Number of cells in last file " + numCellInLastFile);

		long endInitTime = System.currentTimeMillis();

		System.out.println("Dictionary building  takes " + getDuration(startTime, endInitTime));
				
		System.out.println("");

		initStorageManager();

		for (int i = 0; i < numCorrDimensions; i++){
			if (i>0){
				long startSortingTime = System.currentTimeMillis();
				System.out.println("Heap size before sorting " + i);
				//printHeapSize();
				sortByDimensions(i-1, i);
				System.out.println("Heap size after sorting " + i);
				//printHeapSize();
				
				Runtime.getRuntime().gc();
				System.out.println("Heap size after gabage collection at sorting " + i);
				//printHeapSize();
				
				long endSortingTime = System.currentTimeMillis();
				System.out.println("Sort in pass "+i+" takes " + getDuration(startSortingTime, endSortingTime));
			}
			
			long startGenerateFriendship = System.currentTimeMillis();

			System.out.println("Heap size before generating friendships at pass " + i);
			//printHeapSize();
			generateFriendShip(i);
			System.out.println("Heap size after generating friendships at pass " + i);
			//printHeapSize();

			long endGenerateFriendship = System.currentTimeMillis();
			System.out.println("Friendship generation in pass "+i+" takes " + getDuration(startGenerateFriendship, endGenerateFriendship));
		}
		
		
		//System.exit(-1);
		
		//printHeapSize();
		
		return generateInputForMapReduce(numMaps, _mrInputFile);
		
		//String[] a = new String[0];
		//return a; 
		
	
	}
	
	public void mapreduceTask(String inputFile, int numberCell){
		startWritingUserData(); // for writing data needed by
		// testdriver

		long startPostGeneration = System.currentTimeMillis();
		
		if (isPostStream == true){
			postStreamStoreMng.initSerialization();
		}
		if (isPhotoStream == true){
			photoStreamStoreMng.initSerialization();
		}
		if (isGPSStream == true){
			gpsStreamStoreMng.initSerialization();
		}		
		

		if (isExperimenting) {
			printExperimentResults();
		} 
		else {
			generatePostandPhoto(inputFile, numberCell);
		
		
			long endPostGeneration = System.currentTimeMillis();
			System.out.println("Post generation takes " + getDuration(startPostGeneration, endPostGeneration));
	
			finishWritingUserData();

			long startGroupGeneration = System.currentTimeMillis();
			generateGroupAll(inputFile, numberCell);
			long endGroupGeneration = System.currentTimeMillis();
			System.out.println("Group generation takes " + getDuration(startGroupGeneration, endGroupGeneration));
		}
		
		if (mapreduceFileIdx != -1)
			serializer.serialize();

		
		if (isPostStream == true){
			// Sort the posting file
			postStreamStoreMng.endSerialization();
			
			PostExternalSort postSort = new PostExternalSort();
			String inputs[] = {sibOutputDir + postStreamStoreMng.getOutFileName(),
								sibOutputDir + postStreamStoreMng.getSortedFileName()};
			
			try {
				postSort.SortPosts(inputs);
				serializeRDFPostStream(sibOutputDir + postStreamStoreMng.getSortedFileName(),
						postStreamStoreMng.getNumberSerializedObject());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Get the sorted File name
		}
		
		if (isPhotoStream == true){
			// Sort the photoing file
			photoStreamStoreMng.endSerialization();
			
			PhotoExternalSort photoSort = new PhotoExternalSort();
			String inputs[] = {sibOutputDir + photoStreamStoreMng.getOutFileName(),
								sibOutputDir + photoStreamStoreMng.getSortedFileName()};
			
			try {
				photoSort.SortPhotos(inputs);
				serializeRDFPhotoStream(sibOutputDir + photoStreamStoreMng.getSortedFileName(),
						photoStreamStoreMng.getNumberSerializedObject());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Get the sorted File name
		}
		
		
		if (isGPSStream == true){
			
			// Generate the GPS for events
			gpsGenerator.generateAllGPSForAllEvents(gpsStreamStoreMng);

			// Sort the gps file
			gpsStreamStoreMng.endSerialization();
			
			GPSExternalSort gpsSort = new GPSExternalSort();
			String inputs[] = {sibOutputDir + gpsStreamStoreMng.getOutFileName(),
								sibOutputDir + gpsStreamStoreMng.getSortedFileName()};
			
			try {
				gpsSort.SortGPSs(inputs);
				serializeRDFGPSStream(sibOutputDir + gpsStreamStoreMng.getSortedFileName(),
						gpsStreamStoreMng.getNumberSerializedObject());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Get the sorted File name
		}

		// Write data for testdrivers
		System.out.println("Writing the data for test driver ");
		System.out.println("Number of popular users "
		+ numPopularUser);
		writeDataForTestDriver();
	}
	
	public void runFullGenerator(String args[]){
		long startTime = System.currentTimeMillis();

		
		loadParamsFromFile();
		loadParamsFromShell(args);

		rdfOutputFileName = rdfOutputFileName + numtotalUser;
		
		init();

		long endInitTime = System.currentTimeMillis();

		System.out.println("( Dictionary building  taes "
				+ (endInitTime - startTime) / 60000 + " minutes and "
				+ ((endInitTime - startTime) % 60000) / 1000 + " seconds )");
		System.out.println("");

		initStorageManager();
		
		for (int i = 0; i < numCorrDimensions; i++){
			if (i>0)
				sortByDimensions(i-1, i);
			
			generateFriendShip(i);
		}
		
		//System.exit(-1);
		//generateInputForMapReduce(5, "mrInputFile.txt");
		
		
		// Fourth pass: Generate posts & comments
		
		startWritingUserData(); // for writing data needed by
												// testdriver
		
		generatePostandPhoto(storeManager[numCorrDimensions-1].getPassOutUserProf(), lastCell + 1);
		finishWritingUserData();


		if (isExperimenting) {
			printExperimentResults();
		} else {
			generateGroupAll(storeManager[numCorrDimensions-1].getPassOutUserProf(), lastCell + 1);			
		}

		serializer.serialize();
		
		//System.exit(-1);

		long endTime = System.currentTimeMillis();

		System.out.println("Number of triples generated: "
				+ serializer.triplesGenerated());
		System.out.println("Generation done in " + (endTime - startTime)
				/ 60000 + " minutes and " + ((endTime - startTime) % 60000)
				/ 1000 + " seconds");

		// Write data for testdrivers
		System.out.println("Writing the data for test driver ");
		System.out.println("Number of popular users "
				+ numPopularUser);
		writeDataForTestDriver();
		long endTimeTestDriver = System.currentTimeMillis();
		System.out.println("Done in " + (endTimeTestDriver - endTime)
				+ " miliseconds");
	}
	
	public void loadParamsFromFile() {
		try {
			RandomAccessFile paramFile;
			paramFile = new RandomAccessFile(sibHomeDir + paramFileName, "r");
			String line;
			while ((line = paramFile.readLine()) != null) {
				String infos[] = line.split(": ");
				if (infos[0].startsWith("numtotalUser")) {
					numtotalUser = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("startYear")) {
					startYear = Integer.parseInt(infos[1].trim());
					continue;	
				} else if (infos[0].startsWith("startMonth")) {
					startMonth = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("startDate")) {
					startDate = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("endYear")) {
					endYear = Integer.parseInt(infos[1].trim());
					continue;	
				} else if (infos[0].startsWith("endMonth")) {
					endMonth = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("endDate")) {
					endDate = Integer.parseInt(infos[1].trim());
					continue;					
				} else if (infos[0].startsWith("cellSize")) {
					cellSize = Short.parseShort(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("numberOfCellPerWindow")) {
					numberOfCellPerWindow = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("minNoFriends")) {
					minNoFriends = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("maxNoFriends")) {
					maxNoFriends = Integer.parseInt(infos[1].trim());
					thresholdPopularUser = (int) (maxNoFriends * 0.9);
					continue;
				} else if (infos[0].startsWith("friendRejectRatio")) {
					friendRejectRatio = Double.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("friendReApproveRatio")) {
					friendReApproveRatio = Double.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("maxNoInterestsPerUser")) {
					maxNoInterestsPerUser = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("numPostLikeDuplication")) {
					numPostLikeDuplication = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("numPhotoLikeDuplication")) {
					numPhotoLikeDuplication = Integer.parseInt(infos[1].trim());
					continue;					
				} else if (infos[0].startsWith("maxNumLocationPostPerWeek")) {
					maxNumLocationPostPerWeek = Integer.parseInt(infos[1]
							.trim());
					continue;
				} else if (infos[0].startsWith("maxNumInterestPostPerWeek")) {
					maxNumInterestPostPerWeek = Integer.parseInt(infos[1]
							.trim());
					continue;
				} else if (infos[0].startsWith("maxNumComments")) {
					maxNumComments = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("baseProbInterestCorrelated")) {
					baseProbInterestCorrelated = Double.parseDouble(infos[1]
							.trim());
					continue;
				} else if (infos[0].startsWith("baseProbNotCorrelation")) {
					baseProbNotCorrelation = Double
							.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("locationDicFile")) {
					locationDicFile = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("interestDicFile")) {
					interestDicFile = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("interestNamesFile")) {
					interestNamesFile = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("namesDicFile")) {
					namesDicFile = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("organizationsDicFile")) {
					organizationsDicFile = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("topInstitutesFileName")) {
					topInstitutesFileName = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("companiesDicFile")) {
					companiesDicFile = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("popularPlacesDicFile")) {
					popularPlacesDicFile = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("regionalArticleFile")) {
					regionalArticleFile = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("interestArticleFile")) {
					interestArticleFile = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("stopWordFileName")) {
					stopWordFileName = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("groupArticleFile")) {
					groupArticleFile = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("agentFile")) {
					agentFile = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("emailDicFile")) {
					emailDicFile = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("browserDicFile")) {
					browserDicFile = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("probAnotherBrowser")) {
					probAnotherBrowser = Double.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("countryAbbrMappingFile")) {
					countryAbbrMappingFile = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("ipZoneDir")) {
					ipZoneDir = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("minTextSize")) {
					minTextSize = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("maxTextSize")) {
					maxTextSize = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("minCommentSize")) {
					minCommentSize = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("maxCommentSize")) {
					maxCommentSize = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("ratioReduceText")) {
					ratioReduceText = Double.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("maxNumUserTags")) {
					maxNumUserTags = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("maxNumPhotoAlbums")) {
					maxNumPhotoAlbums = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("maxNumPhotoPerAlbums")) {
					maxNumPhotoPerAlbums = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("maxNumGroupCreatedPerUser")) {
					maxNumGroupCreatedPerUser = Integer.parseInt(infos[1]
							.trim());
					continue;
				} else if (infos[0].startsWith("maxNumMemberGroup")) {
					maxNumMemberGroup = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("groupModeratorProb")) {
					groupModeratorProb = Double.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("maxNumGroupPostPerWeek")) {
					maxNumGroupPostPerWeek = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("numFiles")) {
					numFiles = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("serializerType")) {
					serializerType = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("streamSerializerType")) {
					streamSerializerType = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("rdfOutputFileName")) {
					rdfOutputFileName = infos[1].trim();
					continue;
				} else if (infos[0].startsWith("numRdfOutputFile")) {
					numRdfOutputFile = Integer.parseInt(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("missingRatio")) {
					missingRatio = Double.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("missingStatusRatio")) {
					missingStatusRatio = Double.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("probSingleStatus")) {
					probSingleStatus = Double.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("probHavingSmartPhone")) {
					probHavingSmartPhone = Double.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("probSentFromAgent")) {
					probSentFromAgent = Double.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("probUnFrequent")) {
					probUnFrequent = Double.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("probDiffIPinTravelSeason")) {
					probDiffIPinTravelSeason = Double.parseDouble(infos[1]
							.trim());
					continue;
				} else if (infos[0].startsWith("probDiffIPnotTravelSeason")) {
					probDiffIPnotTravelSeason = Double.parseDouble(infos[1]
							.trim());
					continue;
				} else if (infos[0].startsWith("probDiffIPforTraveller")) {
					probDiffIPforTraveller = Double.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("probUnCorrelatedCompany")) {
					probUnCorrelatedCompany = Double.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("probUnCorrelatedOrganization")) {
					probUnCorrelatedOrganization = Double.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("probTopUniv")) {
					probTopUniv = Double.parseDouble(infos[1].trim());
					continue;
				} else if (infos[0].startsWith("maxNumPopularPlaces")) {
					maxNumPopularPlaces = Integer.parseInt(infos[1].trim());
					continue;					
				} else if (infos[0].startsWith("probPopularPlaces")) {
					probPopularPlaces = Double.parseDouble(infos[1].trim());
					continue;	
				} else {
					System.out.println("This param " + line
							+ " does not match any option ");
					//System.exit(-1);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void loadParamsFromShell(String[] args){
		int i = 0;
		while (i < args.length) {
			try {
				if (args[i].equals("-size")) {
					numtotalUser = Integer.parseInt(args[i++ + 1]);
					//Set the number of files according to the number of users
					if (numtotalUser % 1000 !=0 ){	
						System.out.println("The number of total users shoulde be a factor of 1000");
						System.exit(-1);
					}
					else{
						numFiles = numtotalUser/1000; 
					}
				} else if (args[i].equals("-isExp")) {
					isExperimenting = true;
				} else if (args[i].equals("-o")) {
					rdfOutputFileName = args[i++ + 1];
				} else if (args[i].equals("-nof")) {
					numFiles = Integer.parseInt(args[i++ + 1]);
				} else if (args[i].equals("-maxFr")) {
					maxNoFriends = Integer.parseInt(args[i++ + 1]);
				} else if (args[i].equals("-alpha")) {
					alpha = Double.parseDouble(args[i++ + 1]);
				}
				else if (args[i].equals("-calSP")) {
					isCalcShortPath = true;
				}    
				else if (args[i].equals("-stream")) {
					isPostStream = true;
					isPhotoStream = true;
					isGPSStream = true; 
					System.out.println("Generate Post and Photo Stream ");
				}    
				i++;

			} catch (Exception e) {
				System.err.println("Invalid arguments:\n");
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
	// Init the data for the first window of cells
	public void init() {
		seedGenerate();

		windowSize = (int) cellSize * numberOfCellPerWindow;
		randPowerlaw = new PowerDistGenerator(minNoFriends, maxNoFriends,
				alpha, seeds[2]);
		randUniform = new Random(seeds[3]);
		randNumInterest = new Random(seeds[4]);
		randomFriendIdx = new Random(seeds[6]);
		randomFileSelect = new Random(seeds[7]);
		randomIdxInWindow = new Random(seeds[8]);
		randNumberPost = new Random(seeds[9]);
		randNumberComments = new Random(seeds[10]);
		randNumberPhotoAlbum = new Random(seeds[11]);
		randNumberPhotos = new Random(seeds[12]);
		randNumberGroup = new Random(seeds[13]);
		randNumberUserPerGroup = new Random(seeds[14]);
		randMemberIdxSelector = new Random(seeds[18]);
		randGroupMemStep = new Random(seeds[19]);
		randFriendLevelSelect = new Random(seeds[20]);
		randMembership = new Random(seeds[21]);
		randGroupModerator = new Random(seeds[22]);
		randomExtraInfo = new Random(seeds[27]);
		randomExactLongLat = new Random(seeds[27]);
		randUserAgent = new Random(seeds[29]);
		randIsFrequent = new Random(seeds[34]);
		randNumberGroupPost = new Random(seeds[36]);
		randFriendReject = new Random(seeds[37]);
		randFriendReapprov = new Random(seeds[38]);
		randInitiator = new Random(seeds[39]);
		randomHaveStatus = new Random(seeds[41]);
		randomStatusSingle = new Random(seeds[42]);
		randomStatus = new Random(seeds[43]);
		randNumPopularPlaces = new Random(seeds[47]);
		randUserRandomIdx = new Random(seeds[48]);

		//userProfiles = new UserProfile[windowSize]; // Collect of user
		reducedUserProfiles = new ReducedUserProfile[windowSize];	//// Collect of reduced user profile

		// Number of users should be a multiple of the cellsize
		if (numtotalUser % cellSize != 0) {
			System.out
					.println("Number of users should be a multiple of the cellsize ");
			System.exit(-1);
		}
		
		/*
		for (int i = 0; i < windowSize; i++) {
			userProfiles[i] = new UserProfile();
		}
		*/

		System.out.println("Building locations dictionary ");

		locationDic = new LocationDictionary(numtotalUser, sibHomeDir + locationDicFile);
		locationDic.init();
		
		//System.exit(-1);

		ipAddDictionary = new IPAddressDictionary(sibHomeDir + countryAbbrMappingFile,
				sibHomeDir + ipZoneDir, locationDic.getVecLocations(), seeds[33],
				probDiffIPinTravelSeason, probDiffIPnotTravelSeason,
				probDiffIPforTraveller);
		ipAddDictionary.init();

		dateTimeGenerator = new DateGenerator(new GregorianCalendar(startYear, startMonth,
				startDate), new GregorianCalendar(endYear, endMonth, endDate), seeds[0], seeds[1],
				alpha);

		lastCellPos = (int) (numtotalUser - windowSize) / cellSize;
		lastCell = (int) numtotalUser / cellSize - 1; // The last cell of the
														// sliding process

		// For multiple output files
		numCellPerfile = (lastCell + 1) / numFiles;
		numCellInLastFile = (lastCell + 1) - numCellPerfile * (numFiles - 1);

		if (numCellPerfile < numberOfCellPerWindow) {
			System.out
					.println("The number of Cell per file should be greater than that of a window ");
			System.exit(-1);
		}

		/*
		 * System.out.println("numCellPerfile =" + numCellPerfile );
		 * System.out.println("numCellInLastFile =" + numCellInLastFile );
		 * System.exit(-1);
		 */

		System.out
				.println("Building interests dictionary & locations/interests distribution ");

		interestDic = new InterestDictionary(sibHomeDir + interestDicFile,
				sibHomeDir + interestNamesFile, seeds[5]);
		interestDic.init();

		System.out.println("Building dictionary of articles ");
		textGenerator = new RandomTextGenerator(sibHomeDir + regionalArticleFile,
				sibHomeDir + interestArticleFile, sibHomeDir + stopWordFileName, 
				seeds[15], seeds[16],
				numArticles, locationDic.getLocationNameMapping(),
				interestDic.getInterestNames(), dateTimeGenerator, minTextSize,
				maxTextSize, minCommentSize, maxCommentSize, ratioReduceText,
				seeds[31]);

		groupGenerator = new GroupGenerator(dateTimeGenerator, locationDic,
				interestDic, numtotalUser, seeds[35]);

		namesDictionary = new NamesDictionary(sibHomeDir + namesDicFile,
				locationDic.getLocationNameMapping(), seeds[23]);
		namesDictionary.init();

		emailDic = new EmailDictionary(sibHomeDir + emailDicFile, seeds[32]);
		emailDic.init();

		browserDic = new BrowserDictionary(sibHomeDir + browserDicFile, seeds[44],
				probAnotherBrowser);
		browserDic.init();

		organizationsDictionary = new OrganizationsDictionary(
				sibHomeDir + organizationsDicFile, locationDic.getLocationNameMapping(),
				seeds[24], probUnCorrelatedOrganization, sibHomeDir + topInstitutesFileName,
				seeds[45], probTopUniv);
		organizationsDictionary.init();
		// organizationsDictionary.checkCompleteness();

		companiesDictionary = new CompanyDictionary(sibHomeDir + companiesDicFile,
				locationDic.getLocationNameMapping(), seeds[40],
				probUnCorrelatedCompany);
		companiesDictionary.init();

		// companiesDictionary.checkCompleteness();

		popularDictionary = new PopularPlacesDictionary(sibHomeDir + popularPlacesDicFile, 
				locationDic.getLocationNameMapping(), seeds[46]);
		popularDictionary.init();
		
		eventsDic = new EventsDictionary(numEvents, popularDictionary, 
				dateTimeGenerator, seeds[46]);
		eventsDic.initEventSet();
		
		gpsGenerator = new GPSGenerator(eventsDic.getEventSet(), 
				 seeds[46], numtotalUser, probProvideGPS, 
				 maxNumSharingGPSperUser, maxNumGPSperSharingTime);
		
		photoGenerator = new PhotoGenerator(dateTimeGenerator,
				locationDic.getVecLocations(), seeds[17], maxNumUserTags, 
				popularDictionary, probPopularPlaces);
		/*
		 * System.out.println("Building dictionary for group's posts");
		 * groupPostGenerator = new GroupPostGenerator(groupArticleFile,
		 * seeds[25], seeds[26], userCreatedDateGen);
		 * groupPostGenerator.groupArticlesInit();
		 */
		System.out.println("Building user agents dictionary");
		userAgentDic = new UserAgentDictionary(sibHomeDir + agentFile, seeds[28], seeds[30],
				probSentFromAgent);
		userAgentDic.init();

		textGenerator.setSupportDictionaries(userAgentDic, ipAddDictionary,
				browserDic);

		outputDataWriter = new OutputDataWriter();

		if (isPostStream == true){
			serializerType = streamSerializerType; 
		}
		serializer = getSerializer(serializerType, rdfOutputFileName);

		
		// For EXPERIMENT
		if (isExperimenting) {
			arrayUserFriends = new ArrayList[numtotalUser];
		}

	}	

	public void initStorageManager(){
		// init store manger for friendship generation
		storeManager = new StorageManager[numCorrDimensions];
		for (int i = 0; i < numCorrDimensions; i++){
			storeManager[i] = new StorageManager(cellSize, windowSize, i, outUserProfile, sibOutputDir);
			System.out.println("New storage manager: " + storeManager[i].getPassOutUserProf());
		}
		
		groupStoreManager = new StorageManager(cellSize, windowSize, outUserProfile, sibOutputDir);
		
		// For map-reduce
		mfStore = new MFStoreManager(cellSize, windowSize, numCorrDimensions-1, 
				lastCell, numMaps, outUserProfile, sibOutputDir);
		
		// For data stream
		if (isPostStream == true){
			postStreamStoreMng = new StreamStoreManager(cellSize, windowSize, postStreamFileName, sibOutputDir, mapreduceFileIdx);
			photoStreamStoreMng = new StreamStoreManager(cellSize, windowSize, photoStreamFileName, sibOutputDir, mapreduceFileIdx);
			gpsStreamStoreMng =  new StreamStoreManager(cellSize, windowSize, gpsStreamOutputFileName, sibOutputDir, mapreduceFileIdx);
		}
	}
	
	
	public String[] generateInputForMapReduce(int numberOfMapReduceInputFiles, String mrInputfile){
		//printHeapSize();
		
		System.out.println("Number of serialized objects: " + mfStore.getNumberSerializedObject());
		
		writeToOutputFile(mfStore.getMulpassOutUserProf(),mrInputfile);

		return mfStore.getMulpassOutUserProf();
	}
	
	public void generateGroupAll(String inputFile, int numberOfCell){
		// Fifth pass: Group & group posts generator
		//System.out.println("Generate group data from " + storeManager[numCorrDimensions-1].getPassOutUserProf());
		groupStoreManager.initDeserialization(inputFile);
		generateGroups();
		
		int curCellPost = 0;
		while (curCellPost < (numberOfCell - numberOfCellPerWindow)){
			curCellPost++;
			generateGroups(4, curCellPost, numberOfCell);
		}

		System.out.println("Done generating user groups and groups' posts");

		
		groupStoreManager.endDeserialization();
		System.out.println("Number of deserialized objects for group is " + groupStoreManager.getNumberDeSerializedObject());
		
		//scaleGenerator.endDeserialization(3);
	}
	public void sortByDimensions(int original, int dimensionId){
		// Sort
		ExternalSort externalSort = new ExternalSort();
		
		String filenames[]= { sibOutputDir + storeManager[original].getPassOutUserProf(),
				sibOutputDir + storeManager[dimensionId].getPassOutUserProfSorted()};
		
		try {
			externalSort.SortByDimensions(filenames, dimensionId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void generateFriendShip(int pass){
		storeManager[pass].initSerialization(pass);
		if (pass > 0){
			storeManager[pass].initDeserialization(pass);
		}
		initFriendShipWindow(pass);
		
		int curCellPos = 0;
		
		while (curCellPos < lastCellPos){
			curCellPos++;
			slideFriendShipWindow(pass,curCellPos);
		}
		
		int numLeftCell = numberOfCellPerWindow - 1;
		while (numLeftCell > 0){
			curCellPos++;
			slideLastCellsFriendShip(pass, curCellPos,	numLeftCell);
			numLeftCell--;
		}
		
		System.out.println("Pass "+ pass +" Total " + friendshipNo + " friendships generated");
		
		storeManager[pass].endSerialization();
		
		if (pass > 0){
			storeManager[pass].endDeserialization();
		}
		if (pass == (numCorrDimensions - 1)){
			mfStore.endSerialization();
		}
		
		
	}
	
	public void initFriendShipWindow(int pass) {		//Generate the friendship in the first window
		// Create the friend based on the location info
		//Runtime.getRuntime().gc();
		if (pass == 0){
			for (int i = 0; i < windowSize; i++) {
				//userProfiles[i] = generateGeneralInformation(i);
				UserProfile user = generateGeneralInformation(i); 
				reducedUserProfiles[i] = new ReducedUserProfile(user, pass, numCorrDimensions);
				user = null; 
			}
		}
		else{	//Deserialize from the appropriate file
			storeManager[pass].deserializeWindowlUserProfile(reducedUserProfiles);
		}
		
		if (pass == (numCorrDimensions - 1)){
			mfStore.initSerialization(numCorrDimensions-1);
		}

		double randProb;

		for (int i = 0; i < cellSize; i++) {
			// From this user, check all the user in the window to create friendship
			for (int j = i + 1; j < windowSize - 1; j++) {
				if (reducedUserProfiles[i].getNumFriendsAdded() 
						== reducedUserProfiles[i].getNumFriends(pass))
					break;
				if (reducedUserProfiles[j].getNumFriendsAdded() 
						== reducedUserProfiles[j].getNumFriends(pass))
					continue;

                if (reducedUserProfiles[i].isExistFriend(
                		reducedUserProfiles[j].getAccountId()))
                    continue;

				// Generate a random value
				randProb = randUniform.nextDouble();
				
				/*
				if (reducedUserProfiles[i].getDicElementId(pass) 
						== reducedUserProfiles[j].getDicElementId(pass)) {
					
					if ((randProb < baseProbCorrelated)) {
						// add a friendship
						createFriendShip(reducedUserProfiles[i], reducedUserProfiles[j],
								(byte) pass);
					}
				}
				
				// In case that it is not
				else {
					if ((randProb < baseProbNotCorrelation)) {
						// add a friendship
						createFriendShip(reducedUserProfiles[i], reducedUserProfiles[j],
								(byte) (numCorrDimensions-1));
					}
				}
				*/
				double prob = baseProbCorrelated * Math.pow(baseExponentialRate, 
				Math.abs(reducedUserProfiles[i].getDicElementId(pass)- reducedUserProfiles[j].getDicElementId(pass)));
				
				if ((randProb < prob)) {
					// add a friendship
					createFriendShip(reducedUserProfiles[i], reducedUserProfiles[j],
							(byte) pass);
				}

			}
		}

		updateLastPassFriendAdded(0, cellSize, pass);
		if (pass == 0){
			storeManager[pass].serializeReducedUserProfiles(0, cellSize, pass, reducedUserProfiles);
		}
		else{
			storeManager[pass].serializeReducedUserProfiles(0, cellSize, pass, reducedUserProfiles);
		}
		
		if (pass == (numCorrDimensions - 1)){
			mfStore.serialize(0, cellSize, pass, reducedUserProfiles);
		}
	}

	public void slideFriendShipWindow(int pass, int cellPos) {

		//Runtime.getRuntime().gc();
		// 1. Clean the information in the last generated cell --> cellPos mod
		// numberOfCellPerWindow

		// Generate the information for the new cell of the window

		// In window, position of new cell = the position of last removed cell =
		// cellPos - 1
		int newCellPosInWindow = (cellPos - 1) % numberOfCellPerWindow;

		int newStartIndex = newCellPosInWindow * cellSize;
		
		// Real userIndex in the social graph
		int newUserIndex = (cellPos + numberOfCellPerWindow - 1) * cellSize;

		int curIdxInWindow;

		// Init the number of friends for each user in the new cell
		if (pass == 0){
			for (int i = 0; i < cellSize; i++) {
					curIdxInWindow = newStartIndex + i;
					UserProfile user = generateGeneralInformation(newUserIndex + i);
					reducedUserProfiles[curIdxInWindow] = new ReducedUserProfile(user, pass, numCorrDimensions);
					user = null; 
			}
		}
		else { //Desrialize a cell of user profile
			storeManager[pass].deserializeOneCellUserProfile(newStartIndex, cellSize, reducedUserProfiles);
		}

		// Create the friendships
		// Start from each user in the first cell of the window --> at the
		// cellPos, not from the new cell
		newStartIndex = (cellPos % numberOfCellPerWindow) * cellSize;
		for (int i = 0; i < cellSize; i++) {
			curIdxInWindow = newStartIndex + i;
			// Generate set of friends list

			// Here assume that all the users in the window including the new
			// cell have the number of friends
			// and also the number of friends to add

			double randProb;

			if (reducedUserProfiles[curIdxInWindow].getNumFriendsAdded() 
					== reducedUserProfiles[curIdxInWindow].getNumFriends(pass))
				continue;

			// From this user, check all the user in the window to create
			// friendship
			for (int j = i + 1; (j < windowSize - 1)
					&& reducedUserProfiles[curIdxInWindow].getNumFriendsAdded() 
					< reducedUserProfiles[curIdxInWindow].getNumFriends(pass); j++) {

				int checkFriendIdx = (curIdxInWindow + j) % windowSize;

				if (reducedUserProfiles[checkFriendIdx].getNumFriendsAdded() 
						== reducedUserProfiles[checkFriendIdx].getNumFriends(pass))
					continue;

                if (reducedUserProfiles[curIdxInWindow].isExistFriend(
                		reducedUserProfiles[checkFriendIdx].getAccountId()))
                    continue;
                
                
                // Generate a random value
				randProb = randUniform.nextDouble();
				
				/*
				// Check the location ID of the current user's index
				// If same, then added friend with high probability
				if (reducedUserProfiles[curIdxInWindow].getDicElementId(pass) 
						== reducedUserProfiles[checkFriendIdx].getDicElementId(pass)) {
					if (randProb < baseProbCorrelated) {

						// add a friendship
						createFriendShip(reducedUserProfiles[curIdxInWindow],
								reducedUserProfiles[checkFriendIdx], (byte) pass);
					}
				} m 
				// In case that it is not
				else {
					if ((randProb < baseProbNotCorrelation)) {
						// add a friendship
						createFriendShip(reducedUserProfiles[curIdxInWindow],
								reducedUserProfiles[checkFriendIdx], (byte) (numCorrDimensions-1));
					}
				}
				*/
				double prob = baseProbCorrelated * Math.pow(baseExponentialRate, 
						Math.abs(reducedUserProfiles[curIdxInWindow].getDicElementId(pass)
								- reducedUserProfiles[checkFriendIdx].getDicElementId(pass)));
						
				if ((randProb < prob)) {
					// add a friendship
					createFriendShip(reducedUserProfiles[curIdxInWindow], reducedUserProfiles[checkFriendIdx],
							(byte) pass);
				}
			}

		}

		updateLastPassFriendAdded(newStartIndex, newStartIndex + cellSize, pass);
		storeManager[pass].serializeReducedUserProfiles(newStartIndex, newStartIndex + cellSize, pass, reducedUserProfiles);
		if (pass == (numCorrDimensions - 1)){
			mfStore.serialize(newStartIndex, newStartIndex + cellSize, pass, reducedUserProfiles);
		}


	}

	public void slideLastCellsFriendShip(int pass, int cellPos,	int numleftCell) {

		//Runtime.getRuntime().gc();
		
		int newStartIndex;

		int curIdxInWindow;

		newStartIndex = (cellPos % numberOfCellPerWindow) * cellSize;
		
		for (int i = 0; i < cellSize; i++) {
			curIdxInWindow = newStartIndex + i;
			// Generate set of friends list

			// Here assume that all the users in the window including the new
			// cell have the number of friends
			// and also the number of friends to add

			double randProb;

			if (reducedUserProfiles[curIdxInWindow].getNumFriendsAdded() 
					== reducedUserProfiles[curIdxInWindow].getNumFriends(pass))
				continue;

			// From this user, check all the user in the window to create
			// friendship
			for (int j = i + 1; (j < numleftCell * cellSize - 1)
					&& reducedUserProfiles[curIdxInWindow].getNumFriendsAdded() 
					   < reducedUserProfiles[curIdxInWindow].getNumFriends(pass); j++) {

				int checkFriendIdx = (curIdxInWindow + j) % windowSize;

				if (reducedUserProfiles[checkFriendIdx].getNumFriendsAdded() 
						== reducedUserProfiles[checkFriendIdx].getNumFriends(pass))
					continue;

                if (reducedUserProfiles[curIdxInWindow].isExistFriend(
                		reducedUserProfiles[checkFriendIdx].getAccountId()))
                    continue;
                
                
				// Generate a random value
				randProb = randUniform.nextDouble();
				/*
				// Check the location ID of the current user's index
				// If same, then added friend with high probability
				if (reducedUserProfiles[curIdxInWindow].getDicElementId(pass) 
						== reducedUserProfiles[checkFriendIdx].getDicElementId(pass)) {
					if (randProb < baseProbCorrelated) {
						// add a friendship
						createFriendShip(reducedUserProfiles[curIdxInWindow],
								reducedUserProfiles[checkFriendIdx], (byte) pass);
					}
				} else {
					if (randProb < baseProbNotCorrelation) {
						// add a friendship
						createFriendShip(reducedUserProfiles[curIdxInWindow],
								reducedUserProfiles[checkFriendIdx], (byte) (numCorrDimensions-1));
					}
				}
				*/
				double prob = baseProbCorrelated * Math.pow(baseExponentialRate, 
						Math.abs(reducedUserProfiles[curIdxInWindow].getDicElementId(pass)
								- reducedUserProfiles[checkFriendIdx].getDicElementId(pass)));
						
				if ((randProb < prob)) {
					// add a friendship
					createFriendShip(reducedUserProfiles[curIdxInWindow], reducedUserProfiles[checkFriendIdx],
							(byte) pass);
				}				
			}

		}

		updateLastPassFriendAdded(newStartIndex, newStartIndex + cellSize, pass);
		storeManager[pass].serializeReducedUserProfiles(newStartIndex, newStartIndex + cellSize, pass, reducedUserProfiles);
		if (pass == (numCorrDimensions - 1)){
			mfStore.serialize(newStartIndex, newStartIndex + cellSize, pass, reducedUserProfiles);
		}

	}
	

	// inputFile for this step is the file that  
	
	public void generatePostandPhoto(String inputFile, int numOfCells) {
		
		// Init neccessary objects
		StorageManager storeManager = new StorageManager(cellSize, windowSize, outUserProfile, sibOutputDir);
		storeManager.initDeserialization(inputFile);
		reducedUserProfilesCell = new ReducedUserProfile[cellSize];
		
		System.out.println("Generating the posts & comments ");
		UserExtraInfo extraInfo = new UserExtraInfo();

		// Processing for each cell in the file
		for (int j = 0; j < numOfCells; j++) {
			storeManager.deserializeOneCellUserProfile(reducedUserProfilesCell);

			for (int k = 0; k < cellSize; k++) {
				// Generate extra info such as names, organization before
				// writing out
					
				setInfoFromUserProfile(reducedUserProfilesCell[k], extraInfo);

				// serializer.gatherData(userProfilesCell[k]);
				serializer.gatherData(reducedUserProfilesCell[k], extraInfo);

				if (isExperimenting) {
					int userId = reducedUserProfilesCell[k].getAccountId();
					int numFriends = reducedUserProfilesCell[k].getNumFriendsAdded();
					if (numFriends == 0) {
						System.out.println("Dangling user " + userId
								+ " | "
								+ reducedUserProfilesCell[k].getNumFriends(numCorrDimensions-1));
					}
					Friend[] lstFriend = reducedUserProfilesCell[k].getFriendList();
					arrayUserFriends[userId] = new ArrayList<Integer>(
							numFriends);
					for (int m = 0; m < numFriends; m++) {
						arrayUserFriends[userId].add(lstFriend[m].getFriendAcc());
					}
				}
				else{

					generateLocationPost(reducedUserProfilesCell[k]);
					
					generateInterestPost(reducedUserProfilesCell[k]);

					generatePhoto(reducedUserProfilesCell[k]);
					
					generateGPS(reducedUserProfilesCell[k], extraInfo);	
				}
			}
		}
		
		storeManager.endDeserialization();
		System.out.println("Done generating the posts and photos....");
		System.out.println("Number of deserialized objects is " + storeManager.getNumberDeSerializedObject());
	}
	
	public void generateLocationPost(ReducedUserProfile user){
		// Generate location-related posts
		int numRegionalPost = getNumOfRegionalPost(user);
		for (int m = 0; m < numRegionalPost; m++) {
			Post post = textGenerator.getRandomRegionalPost(
					user, maxNumLikes);
			// Set user agent
			userAgentDic.setPostUserAgent(user, post);
			ipAddDictionary.setPostIPAdress(user.isFrequentChange(),
											user.getIpAddress(), post);
			// set browser idx
			post.setBrowserIdx(browserDic.getPostBrowserId(user.getBrowserIdx()));

			
			if (isPostStream == true){
				PostStream postStream = new PostStream(post);
				postStreamStoreMng.serialize(postStream);
			}	
			else
				serializer.gatherData(post);
			
			
			// Generate comments
			int numComment = randNumberComments
					.nextInt(maxNumComments);
			long lastCommentCreateDate = post.getCreatedDate();
			long lastCommentId = -1;
			long startCommentId = RandomTextGenerator.commentId;
			for (int l = 0; l < numComment; l++) {
				Comment comment = textGenerator
						.getRandomRegionalComment(post,	user,
													lastCommentCreateDate,
													startCommentId, lastCommentId);
				if (comment.getAuthorId() != -1) { // In case the
													// comment is
													// not created
													// because of
													// the
													// friendship's
													// createddate
					if (isPostStream == true){
						PostStream postStream = new PostStream(comment);
						postStreamStoreMng.serialize(postStream);
					}	
					else
						serializer.gatherData(comment);
					
					
					lastCommentCreateDate = comment.getCreateDate();
					lastCommentId = comment.getCommentId();
				}
			}
		}
	}

	public void generateInterestPost(ReducedUserProfile user){
		// Generate interest-related posts
		int numInterstPost = getNumOfInterestPost(user);
		for (int m = 0; m < numInterstPost; m++) {
			Post post = textGenerator.getRandomInterestPost(
					user, maxNumLikes);
			userAgentDic.setPostUserAgent(user, post);
			ipAddDictionary.setPostIPAdress(user.isFrequentChange(),
											user.getIpAddress(), post);

			// set browser idx
			post.setBrowserIdx(browserDic.getPostBrowserId(user.getBrowserIdx()));

			if (isPostStream == true){
				PostStream postStream = new PostStream(post);
				postStreamStoreMng.serialize(postStream);
			}	
			else
				serializer.gatherData(post);
			
			// Generate comment for interest-related post
			int numComment = randNumberComments
					.nextInt(maxNumComments);
			long lastCommentCreateDate = post.getCreatedDate();
			long lastCommentId = -1;
			long startCommentId = RandomTextGenerator.commentId;
			for (int l = 0; l < numComment; l++) {
				Comment comment = textGenerator.getRandomInterestComment(post,
										user,lastCommentCreateDate,
										startCommentId, lastCommentId);
				if (comment.getAuthorId() != -1) { 	// In case the
													// comment is
													// not created
													// because of
													// the
													// friendship's
													// createddate
					if (isPostStream == true){
						PostStream postStream = new PostStream(comment);
						postStreamStoreMng.serialize(postStream);
					}	
					else
						serializer.gatherData(comment);
					
					lastCommentCreateDate = comment.getCreateDate();
					lastCommentId = comment.getCommentId();
				}
			}
		}
	}
	
	public void generatePhoto(ReducedUserProfile user){
		// Generate photo Album and photos
		int numOfweeks = (int) dateTimeGenerator.numberOfWeeks(user);
		int numPhotoAlbums;
		
		if (numOfweeks == 0){
			numPhotoAlbums = randNumberPhotoAlbum
				.nextInt(maxNumPhotoAlbums);
		}
		else
			numPhotoAlbums = numOfweeks * randNumberPhotoAlbum
			.nextInt(maxNumPhotoAlbums);
		
		for (int m = 0; m < numPhotoAlbums; m++) {
			PhotoAlbum album = photoGenerator.generateAlbum(user);
			
			if (isPhotoStream == true){
				PhotoStream photoStream = new PhotoStream(album);
				photoStreamStoreMng.serialize(photoStream);
			}
			else
				serializer.gatherData(album);
			// Generate photos for this album
			int numPhotos = randNumberPhotos.nextInt(maxNumPhotoPerAlbums);
			for (int l = 0; l < numPhotos; l++) {
				Photo photo = photoGenerator.generatePhoto(user, album, l, maxNumLikes);

				// Set user agent
				userAgentDic.setPhotoUserAgent(user, photo);

				// set browser idx
				photo.setBrowserIdx(browserDic.getPostBrowserId(user.getBrowserIdx()));

				ipAddDictionary.setPhotoIPAdress(user.isFrequentChange(),
												user.getIpAddress(), photo);
				
				if (isPhotoStream == true){
					PhotoStream photoStream = new PhotoStream(photo);
					photoStreamStoreMng.serialize(photoStream);
				}
				else
					serializer.gatherData(photo);
			}
		}
	}

	public void generateGPS(ReducedUserProfile user, UserExtraInfo extraInfo){	
		if (isGPSStream){
			gpsGenerator.generateGPSperUser(user, extraInfo, gpsStreamStoreMng);
		}

	}
	

	// The group only created from users and their friends in the current
	// sliding window.
	// We do that, because we need the user's created date information for
	// generating the
	// group's joining datetime and group's post created date.
	// For one user, a number of groups are created. For each group, the number
	// of members is first
	// generated.
	// To select a member for joining the group
	// First, select which level of friends that we are considering
	// Second, randomSelect one user in that level
	// Decide whether that user can be a member of the group by their joinProb

	public void generateGroups() {
		System.out.println("Generating user groups ");

		//deserializeWindowlUserProfile(3);
		groupStoreManager.deserializeWindowlUserProfile(reducedUserProfiles);

		// Init a window of removed users, now it is empty
		
		removedUserProfiles = new ReducedUserProfile[windowSize];

		double moderatorProb;

		for (int i = 0; i < cellSize; i++) {
			moderatorProb = randGroupModerator.nextDouble();
			if (moderatorProb > groupModeratorProb)
				continue;

			Friend firstLevelFriends[];
			Vector<Friend> secondLevelFriends = new Vector<Friend>();

			// Get the set of first and second level friends
			firstLevelFriends = reducedUserProfiles[i].getFriendList();
			for (int j = 0; j < reducedUserProfiles[i].getNumFriendsAdded(); j++) {
				int friendId = firstLevelFriends[j].getFriendAcc();

				int friendIdxInWindow = getIdxInWindow(0, 0, friendId);

				if (friendIdxInWindow != -1) {
					Friend friendOfFriends[] = reducedUserProfiles[friendIdxInWindow]
							.getFriendList();

					for (int k = 0; k < friendOfFriends.length; k++) {
						if (friendOfFriends[k] != null)
							secondLevelFriends.add(friendOfFriends[k]);
						else
							break;
					}
				}
			}

			// Create a group whose the moderator is the current user
			int numGroup = randNumberGroup.nextInt(maxNumGroupCreatedPerUser);
			for (int j = 0; j < numGroup; j++) {
				createGroupForUser(reducedUserProfiles[i], firstLevelFriends,
						secondLevelFriends);
			}

		}
	}

	public void generateGroups(int pass, int cellPos, int numberCellInFile) {

		int newCellPosInWindow = (cellPos - 1) % numberOfCellPerWindow;

		int newIdxInWindow = newCellPosInWindow * cellSize;
		;

		// Store the to-be-removed cell to the window
		storeCellToRemovedWindow(newIdxInWindow, cellSize, pass);

		// Deserialize the cell from file
		
		//deserializeOneCellUserProfile(newIdxInWindow, cellSize, pass);
		groupStoreManager.deserializeOneCellUserProfile(newIdxInWindow, cellSize, reducedUserProfiles);
		

		int newStartIndex = (cellPos % numberOfCellPerWindow) * cellSize;
		int curIdxInWindow;
		double moderatorProb;
		for (int i = 0; i < cellSize; i++) {
			moderatorProb = randGroupModerator.nextDouble();
			if (moderatorProb > groupModeratorProb)
				continue;

			curIdxInWindow = newStartIndex + i;

			Friend firstLevelFriends[];
			Vector<Friend> secondLevelFriends = new Vector<Friend>();

			// Get the set of first and second level friends
			firstLevelFriends = reducedUserProfiles[curIdxInWindow].getFriendList();

			/*
			 * Do not find the second level of friendship. 
			 * It is because from the current userId, we cannot know the position
			 * of a friend in the window. The reason is that
			 * the users are sorted so that they have random
			 * userId order. Thus it does not mean that 
			 * two users with Ids 11, 12 are in the same window.  
			 */
			
			/*
			for (int j = 0; j < reducedUserProfiles[curIdxInWindow]
					.getNumFriendsAdded(); j++) {
				
				// Only consider friend of friends for those added in
				// the last friendship pass
				int friendId = firstLevelFriends[j].getFriendAcc();

				int friendIdxInWindow = getIdxInWindow(newStartIndex,
						reducedUserProfiles[newStartIndex].getAccountId(), friendId);

				if (friendIdxInWindow != -1) {
					Friend friendOfFriends[] = reducedUserProfiles[friendIdxInWindow]
							.getFriendList();

					for (int k = 0; k < friendOfFriends.length; k++) {
						if (friendOfFriends[k] != null)
							secondLevelFriends.add(friendOfFriends[k]);
						else
							break;
					}
				}

				else { // look at the removed window
			
					friendIdxInWindow = getIdxInRemovedWindow(newStartIndex,
							reducedUserProfiles[newStartIndex].getAccountId(),
							friendId);
					if (friendIdxInWindow != -1) {
						Friend friendOfFriends[];
						friendOfFriends = removedUserProfiles[friendIdxInWindow]
								.getFriendList();

						for (int k = 0; k < friendOfFriends.length; k++) {
							if (friendOfFriends[k] != null) {
								secondLevelFriends.add(friendOfFriends[k]);
							} else
								break;
						}
					}
				}
			}

			*/
			
			// Create a group whose the moderator is the current user
			int numGroup = randNumberGroup.nextInt(maxNumGroupCreatedPerUser);
			for (int j = 0; j < numGroup; j++) {
				createGroupForUser(reducedUserProfiles[curIdxInWindow],
						firstLevelFriends, secondLevelFriends);
			}

		}
	}

	public int getIdxInWindow(int startIndex, int startUserId, int userAccId) {
		// (cellPos % numberOfCellPerWindow) * cellSize;
		if (((startUserId + windowSize) <= userAccId)
				|| (startUserId > userAccId)) {
			return -1;
		} else
			return (startIndex + (userAccId - startUserId)) % windowSize;
	}

	public int getIdxInRemovedWindow(int startIndex, int startUserId,
			int userAccId) {

		if (userAccId >= startUserId
				|| ((userAccId + windowSize) < startUserId)) {
			return -1;
		} else
			return (startIndex + (userAccId + windowSize - startUserId))
					% windowSize;
	}

	public void createGroupForUser(ReducedUserProfile user,
			Friend firstLevelFriends[], Vector<Friend> secondLevelFriends) {
		double randLevelProb;
		double randMemberProb;

		Group group = groupGenerator.createGroup(user);

		HashSet<Integer> memberIds = new HashSet<Integer>();

		int numGroupMember = randNumberUserPerGroup.nextInt(maxNumMemberGroup);
		group.initAllMemberships(numGroupMember);

		while (group.getNumMemberAdded() < numGroupMember) {

			randLevelProb = randFriendLevelSelect.nextDouble();

			// Select the appropriate friend level
			if (randLevelProb < levelProbs[0]) { // ==> level 1
				// Find a friendIdx
				int friendIdx = randMemberIdxSelector.nextInt(user
						.getNumFriendsAdded());
				// Note: Use user.getNumFriendsAdded(), do not use
				// firstLevelFriends.length
				// because we allocate a array for friendLists, but do not
				// guarantee that
				// all the element in this array contain values

				int potentialMemberAcc = firstLevelFriends[friendIdx]
						.getFriendAcc();

				randMemberProb = randMembership.nextDouble();
				if (randMemberProb < joinProbs[0]) {
					// Check whether this user has been added and then add to
					// the group
					if (!memberIds.contains(potentialMemberAcc)) {
						memberIds.add(potentialMemberAcc);
						// Assume the earliest membership date is the friendship
						// created date
						GroupMemberShip memberShip = groupGenerator
								.createGroupMember(potentialMemberAcc, group
										.getCreatedDate(),
										firstLevelFriends[friendIdx]
												.getCreatedTime());
						group.addMember(memberShip);
					}
				}
			}

			else if (randLevelProb < levelProbs[1]) { // ==> level 2
				//
				if (secondLevelFriends.size() == 0)
					continue;

				int friendIdx = randMemberIdxSelector
						.nextInt(secondLevelFriends.size());
				int potentialMemberAcc = secondLevelFriends.get(friendIdx)
						.getFriendAcc();
				randMemberProb = randMembership.nextDouble();
				if (randMemberProb < joinProbs[1]) {
					// Check whether this user has been added and then add to
					// the group
					if (!memberIds.contains(potentialMemberAcc)) {
						memberIds.add(potentialMemberAcc);
						// Assume the earliest membership date is the friendship
						// created date
						GroupMemberShip memberShip = groupGenerator
								.createGroupMember(potentialMemberAcc, group
										.getCreatedDate(), secondLevelFriends
										.get(friendIdx).getCreatedTime());
						group.addMember(memberShip);
					}
				}
			}

			else { // ==> random users
				// Select a user from window
				int friendIdx = randMemberIdxSelector.nextInt(windowSize);
				int potentialMemberAcc = reducedUserProfiles[friendIdx].getAccountId();
				randMemberProb = randMembership.nextDouble();
				if (randMemberProb < joinProbs[2]) {
					// Check whether this user has been added and then add to
					// the group
					if (!memberIds.contains(potentialMemberAcc)) {
						memberIds.add(potentialMemberAcc);
						GroupMemberShip memberShip = groupGenerator
								.createGroupMember(potentialMemberAcc, group
										.getCreatedDate(),
										reducedUserProfiles[friendIdx]
												.getCreatedDate());
						group.addMember(memberShip);
					}
				}
			}
		}
		/*
		 * if (group.getNumMemberAdded() != group.getMemberShips().length){
		 * System.out.println("[DEBUG] GROUP MEMBERS DIFFER HERE"); }
		 */

		serializer.gatherData(group);
		// Generate posts and comments for this groups
		generatePostForGroup(group);
	}

	public void generatePostForGroup(Group group) {
		int numberGroupPost = getNumOfGroupPost(group);
		// System.out.println("Group post number for group " +
		// group.getGroupId() + " : " + numberGroupPost);
		for (int i = 0; i < numberGroupPost; i++) {
			Post groupPost = textGenerator.getRandomGroupPost(group,
					maxNumLikes);
			groupPost.setUserAgent("");
			groupPost.setBrowserIdx((byte) -1);
			// groupPost.setIpAddress(new
			// IP((short)-1,(short)-1,(short)-1,(short)-1));

			if (isPostStream == true){
				PostStream postStream = new PostStream(groupPost);
				postStreamStoreMng.serialize(postStream);
			}	
			else
				serializer.gatherData(groupPost);


			int numComment = randNumberComments.nextInt(maxNumComments);
			long lastCommentCreateDate = groupPost.getCreatedDate();
			long lastCommentId = -1;
			long startCommentId = RandomTextGenerator.commentId;

			for (int j = 0; j < numComment; j++) {
				Comment comment = textGenerator.getRandomGroupComment(
						groupPost, group, lastCommentCreateDate,
						startCommentId, lastCommentId);
				if (comment.getAuthorId() != -1) { // In case the comment is not
													// created because of
					// the friendship's createddate
					comment.setUserAgent("");
					comment.setBrowserIdx((byte) -1);
					if (isPostStream == true){
						PostStream postStream = new PostStream(comment);
						postStreamStoreMng.serialize(postStream);
					}	
					else
						serializer.gatherData(comment);

					
					lastCommentCreateDate = comment.getCreateDate();
					lastCommentId = comment.getCommentId();
				}
			}
		}

	}

	// User has more friends will have more posts
	// Thus, the number of post is calculated according
	// to the number of friends and createdDate of a user
	public int getNumOfRegionalPost(ReducedUserProfile user) {
		int numOfmonths = (int) dateTimeGenerator.numberOfWeeks(user);
		int numberPost;
		if (numOfmonths == 0) {
			numberPost = randNumberPost.nextInt(maxNumLocationPostPerWeek);
		} else
			numberPost = randNumberPost.nextInt(maxNumLocationPostPerWeek
					* numOfmonths);

		numberPost = (numberPost * user.getNumFriendsAdded()) / maxNoFriends;

		return numberPost;
	}

	public int getNumOfInterestPost(ReducedUserProfile user) {
		int numOfweeks = (int) dateTimeGenerator.numberOfWeeks(user);
		int numberPost;
		if (numOfweeks == 0) {
			numberPost = randNumberPost.nextInt(maxNumInterestPostPerWeek);
		} else
			numberPost = randNumberPost.nextInt(maxNumInterestPostPerWeek
					* numOfweeks);

		numberPost = (numberPost * user.getNumFriendsAdded()) / maxNoFriends;

		return numberPost;
	}

	public int getNumOfGroupPost(Group group) {
		int numOfweeks = (int) dateTimeGenerator.numberOfWeeks(group
				.getCreatedDate());
		// System.out.println("Number of month " + numOfmonths);
		int numberPost;
		if (numOfweeks == 0)
			numberPost = randNumberGroupPost.nextInt(maxNumGroupPostPerWeek);
		else
			numberPost = randNumberGroupPost.nextInt(maxNumGroupPostPerWeek
					* numOfweeks);

		// System.out.println("Number of post before divided " + numberPost);

		numberPost = (numberPost * group.getNumMemberAdded())
				/ maxNumMemberGroup;

		return numberPost;
	}

	public void seedGenerate() {
		seeds = new Long[50];
		for (int i = 0; i < 50; i++) {
			seeds[i] = seedRandom.nextLong();
		}
	}

	public UserProfile generateGeneralInformation(int accountId) {
		UserProfile userProf = new UserProfile();
		userProf.resetUser();
		userProf.setAccountId(accountId);
		// Create date
		userProf.setCreatedDate(dateTimeGenerator.randomDateInMillis());
		
		userProf.setNumFriends((short) randPowerlaw.getValue());
		userProf.allocateFriendListMemory(numCorrDimensions);
		
		short totalFriendSet = 0; 
		for (int i = 0; i < numCorrDimensions-1; i++){
			short numPassFriend = (short) Math.floor(friendsRatioPerPass[0] * userProf.getNumFriends());
			totalFriendSet = (short) (totalFriendSet + numPassFriend);
			//userProf.setNumPassFriends(numPassFriend,i);
			userProf.setNumPassFriends(totalFriendSet,i);
			
		}

		// Prevent the case that the number of friends added exceeds the total number of friends
		
		//userProf.setNumPassFriends((short) (userProf.getNumFriends() - totalFriendSet), numCorrDimensions-1);
		userProf.setNumPassFriends(userProf.getNumFriends(),numCorrDimensions-1);


		userProf.setNumFriendsAdded((short) 0);
		userProf.setLocationIdx(locationDic.getLocation(accountId));
		userProf.setLocationZId(locationDic.getZorderID(userProf.getLocationIdx()));
		
		userProf.setForumWallId(accountId * 2); // Each user has an wall
		userProf.setForumStatusId(accountId * 2 + 1);

		userProf.setNumInterests((short) (randNumInterest
				.nextInt(maxNoInterestsPerUser) + 1));
		// +1 in order to remove the case that the user does not have any
		// interest

		// User's Agent
		if (randUserAgent.nextDouble() > probHavingSmartPhone) {
			userProf.setHaveSmartPhone(true);
			userProf.setAgentIdx(userAgentDic.getRandomUserAgentIdx());
		} else {
			userProf.setHaveSmartPhone(false);
		}

		if (randIsFrequent.nextDouble() > probUnFrequent) {
			userProf.setFrequentChange(false);
		} else
			userProf.setFrequentChange(true);

		// User's browser
		userProf.setBrowserIdx(browserDic.getRandomBrowserId());

		// source IP
		userProf.setIpAddress(ipAddDictionary
				.getRandomIPAddressFromLocation(userProf.getLocationIdx()));

		// Popular places 
		byte numPopularPlaces = (byte) randNumPopularPlaces.nextInt(maxNumPopularPlaces);
		userProf.setNumPopularPlace(numPopularPlaces);
		short popularPlaces[] = new short[numPopularPlaces];
		for (int i = 0; i < numPopularPlaces; i++){
			popularPlaces[i] = popularDictionary.getPopularPlace(userProf.getLocationIdx());
			if (popularPlaces[i] == -1){ 	// no popular place here
				//System.out.println("[DEBUG] There is a location without any popular place");
				userProf.setNumPopularPlace((byte)0);
				break;
			}
		}
		userProf.setPopularPlaceIds(popularPlaces);
		
		// Get set of interests
		userProf.setSetOfInterests(interestDic.getInterests(userProf.getLocationIdx(),
															userProf.getNumInterests()));
		
		// Get random Idx
		userProf.setRandomIdx(randUserRandomIdx.nextInt(maxUserRandomIdx));
		
		numGenerateTimesRun++;

		return userProf;
	}
	

	public void setInfoFromUserProfile(ReducedUserProfile user,
			UserExtraInfo userExtraInfo) {

		// Set basic info
		userExtraInfo.setFirstName(namesDictionary.getRandomGivenName(user
				.getLocationIdx()));
		userExtraInfo.setLastName(namesDictionary.getRandomSurName(user
				.getLocationIdx()));

		double prob = randomExtraInfo.nextDouble();
		if (prob < missingRatio) {
			userExtraInfo.setOrganization("");
		} else {
			if (user.getNumFriendsAdded() > thresholdPopularUser) {
				userExtraInfo.setOrganization(organizationsDictionary
						.getRandomOrganization(user.getLocationIdx(), true));
			} else {
				userExtraInfo.setOrganization(organizationsDictionary
						.getRandomOrganization(user.getLocationIdx()));
			}
		}

		prob = randomExtraInfo.nextDouble();
		if (prob < missingRatio) {
			userExtraInfo.setCompany("");
		} else {
			userExtraInfo.setCompany(companiesDictionary.getRandomCompany(user
					.getLocationIdx()));
		}

		
		userExtraInfo.setLocation(locationDic.getLocatioName(user
				.getLocationIdx()));
		
		// We consider that the distance from 
		// where user is living and 
		double distance = randomExactLongLat.nextDouble() * 2;  
		userExtraInfo.setLatt(locationDic.getLatt(user
				.getLocationIdx()) + distance);
		userExtraInfo.setLongt(locationDic.getLongt(user
				.getLocationIdx()) + distance);
		
		// Relationship status
		if (randomHaveStatus.nextDouble() > missingStatusRatio) {

			if (randomStatusSingle.nextDouble() < probSingleStatus) {
				userExtraInfo.setStatus(RelationshipStatus.SINGLE);
				userExtraInfo.setSpecialFriendIdx(-1);
			} else {

				// The two first status, "NO_STATUS" and "SINGLE", are not
				// included
				int statusIdx = randomStatus.nextInt(RelationshipStatus
						.values().length - 2) + 2;
				userExtraInfo.setStatus(RelationshipStatus.values()[statusIdx]);

				// Select a special friend
				Friend friends[] = user.getFriendList();

				if (user.getNumFriendsAdded() > 0) {
					int specialFriendId = 0;
					int numFriendCheck = 0;

					do {
						specialFriendId = randomHaveStatus.nextInt(user
								.getNumFriendsAdded());
						numFriendCheck++;
					} while (friends[specialFriendId].getCreatedTime() == -1
							&& numFriendCheck < friends.length);

					if (friends[specialFriendId].getCreatedTime() == -1) // In
																			// case
																			// do
																			// not
																			// find
																			// any
																			// friendId
						userExtraInfo.setSpecialFriendIdx(-1);
					else
						userExtraInfo
								.setSpecialFriendIdx(friends[specialFriendId]
										.getFriendAcc());
				} else
					userExtraInfo.setSpecialFriendIdx(-1);
			}
		} else
			userExtraInfo.setStatus(RelationshipStatus.NOSTATUS);

		// Random create gender
		prob = randomExtraInfo.nextDouble();
		if (prob < missingRatio) {
			userExtraInfo.setGender(""); // Not specific
		} else if (prob > (double) (1 + missingRatio) / (double) 2) {
			userExtraInfo.setGender(gender[0]); // male
		} else {
			userExtraInfo.setGender(gender[1]); // female
		}

		// email is created by using the user's first name + userId
		prob = randomExtraInfo.nextDouble();
		if (prob < missingRatio) {
			userExtraInfo.setEmail(""); // Not specific
		} else {
			String email = userExtraInfo.getFirstName().replace(" ", "");
			email = email + "" + user.getAccountId() + "@"
					+ emailDic.getRandomEmail();
			userExtraInfo.setEmail(email);
		}

		// date of birth
		prob = randomExtraInfo.nextDouble();
		if (prob < missingRatio) {
			userExtraInfo.setDateOfBirth(-1); // Do not provide the birthday
		} else {
			userExtraInfo.setDateOfBirth(dateTimeGenerator.getBirthDay(user
					.getCreatedDate()));
		}

		// Set class year
		prob = randomExtraInfo.nextDouble();
		if ((prob < missingRatio) || userExtraInfo.getOrganization().equals("")) {
			userExtraInfo.setClassYear(-1);
		} else {
			userExtraInfo.setClassYear(dateTimeGenerator.getClassYear(
					user.getCreatedDate(), userExtraInfo.getDateOfBirth()));
		}

		// Set workFrom
		if (!userExtraInfo.getCompany().equals("")) {
			if (userExtraInfo.getClassYear() != -1) {
				userExtraInfo.setWorkFrom(dateTimeGenerator.getWorkFromYear(
						user.getCreatedDate(), userExtraInfo.getDateOfBirth()));
			} else
				userExtraInfo.setWorkFrom(dateTimeGenerator
						.getWorkFromYear(userExtraInfo.getClassYear()));
		}

		// write user data for test driver
		if (user.getNumFriendsAdded() > thresholdPopularUser) {
			outputDataWriter.writeUserData(user.getAccountId(),
					user.getNumFriendsAdded());
			numPopularUser++;
		}
	}

	public void storeCellToRemovedWindow(int startIdex, int cellSize, int pass) {
		for (int i = 0; i < cellSize; i++)
			removedUserProfiles[startIdex + i] = reducedUserProfiles[startIdex + i];
	}

	public void createFriendShip(ReducedUserProfile user1, ReducedUserProfile user2, byte pass) {
		long requestedTime = dateTimeGenerator.randomFriendRequestedDate(user1,
				user2);
		byte initiator = (byte) randInitiator.nextInt(2);
		long createdTime = -1;
		long declinedTime = -1;
		if (randFriendReject.nextDouble() > friendRejectRatio) {
			createdTime = dateTimeGenerator
					.randomFriendApprovedDate(requestedTime);
		} else {
			declinedTime = dateTimeGenerator
					.randomFriendDeclinedDate(requestedTime);
			if (randFriendReapprov.nextDouble() < friendReApproveRatio) {
				createdTime = dateTimeGenerator
						.randomFriendReapprovedDate(declinedTime);
			}
		}

		// user2.addNewFriend(new Friend(user1.getAccountId(),requestedTime,
		// declinedTime,createdTime,pass,initiator) );
		// user1.addNewFriend(new Friend(user2.getAccountId(),requestedTime,
		// declinedTime,createdTime,pass,initiator) );

		user2.addNewFriend(new Friend(user1, requestedTime, declinedTime,
				createdTime, pass, initiator));
		user1.addNewFriend(new Friend(user2, requestedTime, declinedTime,
				createdTime, pass, initiator));

		friendshipNo++;
	}

	
	public void updateLastPassFriendAdded(int from, int to, int pass) {
		if (to > windowSize) {
			for (int i = from; i < windowSize; i++) {
				reducedUserProfiles[i].setPassFriendsAdded(pass, reducedUserProfiles[i].getNumFriendsAdded());
			}
			for (int i = 0; i < to - windowSize; i++) {
				reducedUserProfiles[i].setPassFriendsAdded(pass, reducedUserProfiles[i].getNumFriendsAdded());
			}
		} else {
			for (int i = from; i < to; i++) {
				reducedUserProfiles[i].setPassFriendsAdded(pass, reducedUserProfiles[i].getNumFriendsAdded());
			}
		}
	}

	// private static Serializer getSerializer(String type) {
	private Serializer getSerializer(String type, String outputFileName) {
		String t = type.toLowerCase();
		if (t.equals("nt"))
			// return new NTriples(outputFileName, forwardChaining,
			// nrOfOutputFiles);
			return new NTriples(sibOutputDir + outputFileName, forwardChaining,
					numRdfOutputFile, interestDic.getInterestdsNamesMapping(),
					browserDic.getvBrowser());
		else if (t.equals("rdffact"))
			// return new NTriples(outputFileName, forwardChaining,
			// nrOfOutputFiles);
			return new RDFFacts(sibOutputDir + outputFileName, forwardChaining,
					numRdfOutputFile, interestDic.getInterestdsNamesMapping(),
					browserDic.getvBrowser());
		else if (t.equals("rdftriple"))
			// return new NTriples(outputFileName, forwardChaining,
			// nrOfOutputFiles);
			return new RDFTriples(sibOutputDir + outputFileName, forwardChaining,
					numRdfOutputFile, interestDic.getInterestdsNamesMapping(),
					browserDic.getvBrowser());		
		else if (t.equals("trig"))
			return new TriG();
		// return new TriG(outputFileName + ".trig", forwardChaining);
		else if (t.equals("ttl"))
			return new Turtle(sibOutputDir + outputFileName, forwardChaining,
					numRdfOutputFile, interestDic.getInterestdsNamesMapping(),
					browserDic.getvBrowser());
		else
			return null;
	}
	
	private void startWritingUserData() {
		outputDataWriter.initWritingUserData();
	}

	private void finishWritingUserData() {
		outputDataWriter.finishWritingUserData();
	}

	private void writeDataForTestDriver() {
		outputDataWriter.writeGeneralDataForTestDriver(numtotalUser,
				dateTimeGenerator);
		outputDataWriter.writeGroupDataForTestDriver(groupGenerator);
		outputDataWriter.writeLocationDataForTestDriver(locationDic);
		outputDataWriter.writeNamesDataForTestDriver(namesDictionary);
	}

	private void printExperimentResults() {
		int[] socialDegrees = new int[maxNoFriends + 1];
		double[] coefficient = new double[maxNoFriends + 1];
		double avgCoefficent = 0.0;
		double totalCoefficent = 0.0;
		for (int i = 0; i < numtotalUser; i++) {
			socialDegrees[arrayUserFriends[i].size()]++;
		}

		/*
		 * for (int i = 0; i <= maxNoFriends; i++){ System.out.println("Degree "
		 * + i + " :  " + socialDegrees[i]); }
		 */
		outputDataWriter.writeSocialDegree(socialDegrees, numtotalUser);

		for (int i = 0; i < numtotalUser; i++) {
			coefficient[arrayUserFriends[i].size()] += getClusteringCoefficient(i);
			totalCoefficent += getClusteringCoefficient(i);
		}

		outputDataWriter.writeClusteringCoefficient(coefficient, socialDegrees, numtotalUser);
		/*
		 * for (int i = 0; i <= maxNoFriends; i++){
		 * System.out.println("Avg coefficient for degree " + i + " :  " +
		 * coefficient[i]/socialDegrees[i]); }
		 */

		System.out.println("Average coefficient of the social graph: "
				+ totalCoefficent / numtotalUser);

		if (isCalcShortPath == true){
			showAvgShortestDistance();
		}
	}

	private void showAvgShortestDistance() {
		/*
		 * SimpleGraph simGraph = new SimpleGraph(numtotalUser,
		 * arrayUserFriends); SimpleDijkstra simDijkstra = new SimpleDijkstra();
		 * 
		 * double sum = 0.0;
		 * 
		 * for (int i = 0; i < 5000; i ++)
		 * //System.out.println("Avg shortest path for " + i + " : " +
		 * simDijkstra.getAvgShortestPath(simGraph,i)); sum = sum +
		 * simDijkstra.getAvgShortestPath(simGraph,i);
		 * 
		 * System.out.println("Avg shortest path for 100 users : " +
		 * (double)sum/100);
		 */
		ShortestPathCalculator calc = new ShortestPathCalculator();
		calc.getAvg(numtotalUser, arrayUserFriends);
	}

	private double getClusteringCoefficient(int uid) {
		ArrayList<Integer> friends = arrayUserFriends[uid];
		friends.add(uid); // Include the current user
		// get the total number of connectivities
		int numConnectivity = 0;
		for (int i = 0; i < friends.size(); i++) {
			for (int j = i + 1; j < friends.size(); j++) {
				if (isFriend(friends.get(i), friends.get(j))) {
					numConnectivity++;
				}
			}
		}

		return (double) 2 * numConnectivity
				/ (friends.size() * (friends.size() - 1));

	}

	private boolean isFriend(int uid1, int uid2) {
		ArrayList<Integer> friends = arrayUserFriends[uid1];
		for (int i = 0; i < friends.size(); i++) {
			if (friends.get(i) == uid2) {
				return true;
			}
		}
		return false;
	}
	public void writeToOutputFile(String filenames[], String outputfile){
		 	Writer output = null;
		 	File file = new File(outputfile);
		 	try {
				output = new BufferedWriter(new FileWriter(file));
				for (int i = 0; i < (filenames.length - 1); i++)
					output.write(filenames[i] + " " + numCellPerfile + "\n");
				
				output.write(filenames[filenames.length - 1] + " " + numCellInLastFile + "\n");
				
				output.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	public String getDuration(long startTime, long endTime){
		String duration = (endTime - startTime)
		/ 60000 + " minutes and " + ((endTime - startTime) % 60000)
		/ 1000 + " seconds";
		
		return duration; 
	}
	public void printHeapSize(){
		long heapSize = Runtime.getRuntime().totalMemory();
		
		long heapMaxSize = Runtime.getRuntime().maxMemory(); 
		
		long heapFreeSize = Runtime.getRuntime().freeMemory(); 
		
		System.out.println(" ---------------------- ");
		System.out.println(" Current Heap Size: " + heapSize/(1024*1024));
		System.out.println(" Max Heap Size: " + heapMaxSize/(1024*1024));
		System.out.println(" Free Heap Size: " + heapFreeSize/(1024*1024));
		System.out.println(" ---------------------- ");
	}
	public void serializeRDFPostStream(String sortedPostFile, int numSerializedObjects){
		postSerializer = getSerializer(streamSerializerType, postStreamOutputFileName + numtotalUser);
		postLikeSerializer  = getSerializer(streamSerializerType, postLikeStreamOutputFileName + numtotalUser);
		postLikeSerializer.setNumLikeDuplication(numPostLikeDuplication, numPhotoLikeDuplication);
		
		try {
			ObjectInputStream oos = new ObjectInputStream(new FileInputStream(sortedPostFile));
			PostStream postStream; 
			for (int i = 0; i < numSerializedObjects; i ++){
				postStream = (PostStream)oos.readObject();
				if (postStream.isIsaPost() == true){
					Post post = postStream.getPost(); 
					postSerializer.gatherData(post, false);
					postLikeSerializer.gatherData(post, true);
				}
				else{
					Comment comment = postStream.getComment();
					postSerializer.gatherData(comment);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		postSerializer.serialize();
		postLikeSerializer.serialize();
	}
	public void serializeRDFPhotoStream(String sortedPhotoFile, int numSerializedObjects){
		photoSerializer = getSerializer(streamSerializerType, photoStreamOutputFileName + numtotalUser);
		photoLikeSerializer = getSerializer(streamSerializerType, photoLikeStreamOutputFileName + numtotalUser);
		photoLikeSerializer.setNumLikeDuplication(numPostLikeDuplication, numPhotoLikeDuplication);
		
		try {
			ObjectInputStream oos = new ObjectInputStream(new FileInputStream(sortedPhotoFile));
			PhotoStream photoStream; 
			for (int i = 0; i < numSerializedObjects; i ++){
				photoStream = (PhotoStream)oos.readObject();
				if (photoStream.isPhoto() == true){
					Photo photo = photoStream.getPhoto(); 
					photoSerializer.gatherData(photo, false);
					photoLikeSerializer.gatherData(photo, true);
				}
				else{
					PhotoAlbum album = photoStream.getPhotoAlbum();
					photoSerializer.gatherData(album);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		photoSerializer.serialize();
		photoLikeSerializer.serialize();
	}
	
	public void serializeRDFGPSStream(String sortedGPSFile, int numSerializedObjects){
		gpsSerializer = getSerializer(streamSerializerType, gpsStreamOutputFileName + numtotalUser);
		
		try {
			ObjectInputStream oos = new ObjectInputStream(new FileInputStream(sortedGPSFile));
			GPS gpsStream; 
			for (int i = 0; i < numSerializedObjects; i ++){
				gpsStream = (GPS)oos.readObject();
				gpsSerializer.gatherData(gpsStream);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		gpsSerializer.serialize();
	}
}
 