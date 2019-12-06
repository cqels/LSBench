package sib.testdriver;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import sib.bibm.FormalParameter;
import sib.bibm.Query;
import sib.bibm.Exceptions.BadSetupException;
import sib.generator.DateGenerator;


public class SQLParameterPool extends SIBParameterPool {
	
	public SQLParameterPool(File resourceDirectory, Long seed) {
		parameterChar='@';
		init(resourceDirectory, seed);
	}

	/*
	 * (non-Javadoc)
	 * @see benchmark.testdriver.AbstractParameterPool#getParametersForQuery(benchmark.testdriver.Query)
	 */
	@Override
    public Object[] getParametersForQuery(Query query, int level) {
		FormalParameter[] fps=query.getFormalParameters();
		int paramCount=fps.length;
		Object[] parameters = new Object[paramCount];
		ArrayList<Integer> productFeatureIndices = new ArrayList<Integer>();
		
		for(int i=0;i<paramCount;i++) {
			FormalParameter fp=fps[i];
			byte parameterType = fp.parameterType;
			if(parameterType==PERSON_URI) {
			}
			else if(parameterType==LOCATION_URI)
				productFeatureIndices.add(i);
			else if(parameterType==CURRENT_DATE)
				parameters[i] = currentDateString;
				
			else
				parameters[i] = null;
		}
		
		return parameters;
	}
	
	@Override
	protected String formatDateString(GregorianCalendar date) {
		return DateGenerator.formatDate(currentDate);
	}

}
