package sib.testdriver;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractParameterPool {
	protected ValueGenerator valueGen;
	protected Integer numberOfUsers = 10000;		// Need to read from output file of data generator
	protected Integer scalefactor; 
	
	public abstract Object[] getParametersForQuery(Query query);
	
	public Integer getScalefactor() {
		return scalefactor;
	}
	
    protected void init(File resourceDir, long seed) {
    	valueGen = new ValueGenerator(seed);
	}
    
    protected Integer getRandomUserIdx(){
    	return valueGen.randomInt(1, numberOfUsers);
    }
    
}
