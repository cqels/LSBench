package sib.testdriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import sib.bibm.FormalParameter;
import sib.bibm.Query;
import sib.bibm.Exceptions.ExceptionException;
import sib.generator.DateGenerator;
import sib.vocabulary.DBP;
import sib.vocabulary.SIB;
import sib.vocabulary.XSD;


public class LocalSPARQLParameterPool extends SIBParameterPool {
	private File updateDatasetFile;
	private BufferedReader updateFileReader = null;
	
	public LocalSPARQLParameterPool(File resourceDirectory, Long seed) {
		parameterChar='%';
		init(resourceDirectory, seed);
	}
	
	public LocalSPARQLParameterPool(File resourceDirectory, long seed, File updateDatasetFile) {
		this(resourceDirectory, seed);
		this.updateDatasetFile=updateDatasetFile;
		try {
			updateFileReader = new BufferedReader(new FileReader(updateDatasetFile));
		} catch(IOException e) {
			throw new ExceptionException("Could not open update dataset file: " + updateDatasetFile.getAbsolutePath(), e);
		}
	}
	
	@Override
	public Object[] getParametersForQuery(Query query, int level) {
		FormalParameter[] fps = query.getFormalParameters();
		int paramCount=fps.length;
		Object[] parameters = new Object[paramCount];
		
		for (int i=0; i<paramCount; i++) {
			FormalParameter fp=fps[i];
			byte parameterType = fp.parameterType;
			switch (parameterType) {
			case PERSON_URI:
				parameters[i] = getRandomPersonURI();
				break;
			case USER_URI:
				parameters[i] = getRandomUserURI();
				break;
			case LOCATION_URI:
				parameters[i] = getLocationURI();
				break;
			case GROUP_URI:
				parameters[i] = getGroupURI();
				break;				
			case CURRENT_DATE:
				parameters[i] = getCurrentDate();
				break;
			case RANDOM_NAME:
				parameters[i] = getRandomName();
				break;
			case BEFORE_30DAYS:
				parameters[i] = getThirtydayDayBefore();
				break;
			case BEFORE_60DAYS:
				parameters[i] = getSixtydayDayBefore();
				break;
			case RECENT_DATE:
				parameters[i] = getRecentDate();
				break;
			case RECENT_10DAYS:
				parameters[i] = getRecent10day();
				break;
			case LOCATION_TAG:
				parameters[i] = getLocationTag();
				break;
			case AGO_20YEARS:
				parameters[i] = getDate20YearsAgo();
				break;
			case AGO_30YEARS:
				parameters[i] = getDate30YearsAgo();
				break;				
			default:
				parameters[i] = null;
			}
		}
		
		return parameters;
	}
	
	private String getRandomPersonURI(){
		//return SIB.getPersonURI(getRandomUserId());
		return SIB.getPersonURI(getPopularUserId());
	}
	private String getRandomUserURI(){
		//return SIB.getUserURI(getRandomUserId());
		return SIB.getUserURI(getPopularUserId());
	}
    private String getLocationURI(){
    	return DBP.prefixed(getRandomLocationName());
    }
    private String getGroupURI(){
    	return SIB.getGroupURI(getRandomGroupId());
    }
    private String getCurrentDate(){
    	return getCurrentDateTime();
    }
    private String getThirtydayDayBefore(){
    	return getDateTimesnDaysBefore(30);
    }    
    private String getSixtydayDayBefore(){
    	return getDateTimesnDaysBefore(60);
    } 
    private String getRecentDate(){
    	return getDateTimesnDaysBefore(10);
    }
    private String getRecent10day(){
    	return getDateTimesnDaysBefore(10);
    }
    private String getDate20YearsAgo(){
    	return getDateTimesnYearsBefore(20);
    }
    private String getDate30YearsAgo(){
    	return getDateTimesnYearsBefore(30);
    }
    private String getLocationTag(){
    	String locationTag = getRandomLocationName();
    	return "\"" + locationTag + "\"";
    }
    private String getRandomName(){
    	String randomSurname = getRandomSurnames();
	int maxIdx = randomSurname.length();
	if (maxIdx > 4){	
		maxIdx = 4;
	}
    	String reduceRandomSurname = randomSurname.substring(1,maxIdx);
    	//System.out.println("Random name is: " + randomSurname + " --> " + reduceRandomSurname);
    	return "\"" + reduceRandomSurname + "\"";
    }
    
  	@Override
	protected String formatDateString(GregorianCalendar date) {
		return "\"" + DateGenerator.formatDateTime(date) + "\"^^" + XSD.prefixed("dateTime");
	}
}
