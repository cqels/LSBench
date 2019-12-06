package sib.testdriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import sib.bibm.AbstractParameterPool;
import sib.bibm.FormalParameter;
import sib.bibm.Exceptions.BadSetupException;
import sib.bibm.Exceptions.ExceptionException;
import sib.generator.DateGenerator;
import sib.objects.Location;


public abstract class SIBParameterPool extends AbstractParameterPool {
    static final int baseScaleFactor = 10000; // 10000 is the number of users
    

    // Parameter constants
    protected static final byte PERSON_URI = 1;		//User account
    protected static final byte USER_URI = 2;
    protected static final byte LOCATION_URI = 3;
    protected static final byte RANDOM_NAME = 4;
    protected static final byte CURRENT_DATE = 5;
    protected static final byte BEFORE_30DAYS = 6;
    protected static final byte BEFORE_60DAYS = 7;
    protected static final byte RECENT_DATE = 8;
    protected static final byte GROUP_URI = 9;
    protected static final byte RECENT_10DAYS = 10;
    protected static final byte LOCATION_TAG = 11;
    protected static final byte AGO_20YEARS = 12;
    protected static final byte AGO_30YEARS = 13;
    
    // Initialize Parameter mappings
    private static Map<String, Byte> parameterMapping;
    static {
        parameterMapping = new HashMap<String, Byte>();
        parameterMapping.put("PersonURI", PERSON_URI);
        parameterMapping.put("UserURI", USER_URI);        
        parameterMapping.put("LocationURI", LOCATION_URI);
        parameterMapping.put("RandomName", RANDOM_NAME);
        parameterMapping.put("CurrentDate", CURRENT_DATE);
        parameterMapping.put("Before30Days", BEFORE_30DAYS);
        parameterMapping.put("Before60Days", BEFORE_60DAYS);
        parameterMapping.put("10RecentDays", RECENT_10DAYS);
        parameterMapping.put("RecentDate", RECENT_DATE);
        parameterMapping.put("GroupURI", GROUP_URI);
        parameterMapping.put("LocationTag", LOCATION_TAG);
        parameterMapping.put("20YearsAgo", AGO_20YEARS);
        parameterMapping.put("30YearsAgo", AGO_30YEARS);
    }

    protected ValueGenerator valueGen;
    protected ValueGenerator valueGen2;
    protected GregorianCalendar currentDate;
    protected String currentDateString;
    final StringBuilder sb = new StringBuilder();
    protected Random seedGen;
    
    protected int numGroup; 				// Number of groups
    protected int numUsers; 				// Total number of users
    protected long currentDateInMillis; 

    //protected String[] arrLocationNames; 	// Array of location names
    protected Vector<Location> locations; 
    protected Vector<Vector<String>> vSurNamesByLocations;
    protected Vector<Integer> vPopularUsers; 		// Users who have many friends
    

    protected double scalefactor;

    protected void init(File resourceDir, long seed) {
        seedGen = new Random(seed);
        valueGen = new ValueGenerator(seedGen.nextLong());

        valueGen2 = new ValueGenerator(seedGen.nextLong());

        // Read general information such as total number of users, current datetime
        readGeneralInfo(resourceDir);
        
        // Read the number of groups
        readGroupNumber(resourceDir);
        
        // Read the array of locations
        readLocationNames(resourceDir);
        
        // Get all the surnames (categorizing by locations)
        readNamesByLocations(resourceDir);
        
        // Get list of popular users
        readPopularUsers(resourceDir);
    }
   
    public double getScalefactor() {
        return scalefactor / baseScaleFactor;
    }
    
    public int getRandomUserId(){
    	return valueGen.randomInt(0, numUsers - 1);
    }
    public int getPopularUserId(){
    	int idx = valueGen.randomInt(0, vPopularUsers.size()-1);
    	return vPopularUsers.get(idx);
    }
    
    public String getRandomLocationName(){
    	int locationId = valueGen.randomInt(0, locations.size()-1);
    	return locations.get(locationId).getName();
    }
    
    public String getRandomSurnames(){
    	int randomLocatioinId = valueGen.randomInt(0, vSurNamesByLocations.size() - 1);
    	int surnamesIdinLocation = valueGen.randomInt(0, vSurNamesByLocations.get(randomLocatioinId).size()-1);
    	
    	return vSurNamesByLocations.get(randomLocatioinId).get(surnamesIdinLocation);
    }
    
    public int getRandomGroupId(){
    	return valueGen.randomInt(1, numGroup);
    }
    
    public String getDateTimesnDaysBefore(int nDays){
    	long dateTimendaysBefore = currentDateInMillis - ((long)nDays * 24*60*60*1000);
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.setTimeInMillis(dateTimendaysBefore);
    	return formatDateString(calendar);
    }
    
    public String getCurrentDateTime(){
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.setTimeInMillis(currentDateInMillis);
    	return formatDateString(calendar);
    }
    
    public String getDateTimesnYearsBefore(int nYears){
    	long dateTimenYearsBefore = currentDateInMillis - ((long)nYears * 24*60*60*1000*365);
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.setTimeInMillis(dateTimenYearsBefore);
    	return formatDateString(calendar);
    }
    
    private void readGroupNumber(File resourceDir){
        File groupDataFile = new File(resourceDir, "gr.dat");
        ObjectInputStream groupDataInput;
        try {
        	groupDataInput = new ObjectInputStream(new FileInputStream(groupDataFile));
        	numGroup = groupDataInput.readInt();
        	System.out.println("Number of group is " + numGroup);
        	groupDataInput.close();
        } catch (IOException e) {
            throw new ExceptionException("Could not open or process file " + groupDataFile.getAbsolutePath(), e);
        }
    }
    
    private void readGeneralInfo(File resourceDir){
        File generalDataFile = new File(resourceDir, "general.dat");
        ObjectInputStream generalDataInput;
        try {
        	generalDataInput = new ObjectInputStream(new FileInputStream(generalDataFile));
        	numUsers = generalDataInput.readInt();
        	currentDateInMillis = generalDataInput.readLong();
        	generalDataInput.close();
        	System.out.println("Number of users is " + numUsers);
        	GregorianCalendar calendar = new GregorianCalendar();
        	calendar.setTimeInMillis(currentDateInMillis);
        	System.out.println("Current date time is  " + DateGenerator.formatDate(calendar));
        	generalDataInput.close();
        } catch (IOException e) {
            throw new ExceptionException("Could not open or process file " + generalDataFile.getAbsolutePath(), e);
        }
    }

    private void readLocationNames(File resourceDir){
        File locationDataFile = new File(resourceDir, "loc.dat");
        ObjectInputStream locationDataInput;
        try {
        	locationDataInput = new ObjectInputStream(new FileInputStream(locationDataFile));
        	locations = (Vector<Location>) locationDataInput.readObject();
        	System.out.println("Number of locations: " + locations.size());
        	locationDataInput.close();
        } catch (IOException e) {
            throw new ExceptionException("Could not open or process file " + locationDataFile.getAbsolutePath(), e);
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
    } 
    
    private void readNamesByLocations(File resourceDir){
        File namesDataFile = new File(resourceDir, "names.dat");
        ObjectInputStream nameDataInput;
        try {
        	nameDataInput = new ObjectInputStream(new FileInputStream(namesDataFile));
        	vSurNamesByLocations = (Vector<Vector<String>>) nameDataInput.readObject();
        	System.out.println("Number of surnames by locations: " + vSurNamesByLocations.size());
        	nameDataInput.close();
        } catch (IOException e) {
            throw new ExceptionException("Could not open or process file " + namesDataFile.getAbsolutePath(), e);
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
    } 
    
    private void readPopularUsers(File resourceDir){
        File usersDataFile = new File(resourceDir, "users.dat");
        ObjectInputStream usersDataInput;
        int userId; 
        int numofFriends;
        vPopularUsers = new Vector<Integer>();
        try {
        	usersDataInput = new ObjectInputStream(new FileInputStream(usersDataFile));
        	while ((userId = usersDataInput.readInt()) != -1){
        		numofFriends = usersDataInput.readInt();
        		vPopularUsers.add(userId);
        	}
        	
        	usersDataInput.close();
        } catch (IOException e) {
            throw new ExceptionException("Could not open or process file " + usersDataFile.getAbsolutePath(), e);
        } 
    } 
    
    public FormalParameter createFormalParameter(String paramClass, String[] addPI, String defaultValue) {
    	//System.out.println("Creating parameter for " + paramClass);
        Byte byteType = parameterMapping.get(paramClass);
        if (byteType == null) {
            throw new BadSetupException("Unknown parameter class: " + paramClass);
        } 
        // PMDUC: Can handle some more types here
        else {
            return new FormalParameter(byteType);
        }
    }

    abstract protected String formatDateString(GregorianCalendar date);


}
