/*
 *  Big Database Semantic Metric Tools
 *
 * Copyright (C) 2011 OpenLink Software <bdsmt@openlinksw.com>
 * All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation;  only Version 2 of the License dated
 * June 1991.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package sib.bibm;

import sib.bibm.qualification.QueryResult;
import sib.bibm.qualification.QueryResultAssembler;
import sib.bibm.qualification.ResultDescription;
import sib.util.json.StringPrinter;


public abstract class AbstractQueryResult  {
	protected boolean timeout;
	protected double timeInSeconds;
	private CompiledQuery query;
	private ResultDescription[] rds;
	protected QueryResult qr;
    
	public AbstractQueryResult(CompiledQuery query) {
		this.query=query;
        this.rds = query.getQuery().getResultDescriptions();
        qr= new QueryResult(this);
    }

	protected QueryResultAssembler getQueryResultAssembler() {
        QueryResultAssembler as = qr.getQueryResultAssembler(getqName(), getQueryMixRun()+1);
        return as;
    }

	public void reportTimeOut() {
		timeout=true;
	}
	
	/*
	 * Set the time (seconds) of the current Query
	 */
	public void setCurrent(double timeInSeconds) {
		this.timeInSeconds=timeInSeconds;
	}

    public String logResultInfo() {
        StringBuilder sb = new StringBuilder(1000);
		sb.append("\n\n\tQuery " + query.getName() + " of run " + query.getRun() + " has been executed ");
		sb.append("in " + String.format("%.6f",timeInSeconds) + " seconds.\n" );
		sb.append("\n\tQuery string:\n\n");
		sb.append(query.getProcessedQueryString());
		sb.append("\n\n");
	
		//Log results
		byte queryType = query.getQueryType();
		if(queryType==Query.DESCRIBE_TYPE)
			sb.append("\tQuery(Describe) result (" + getResultCount() + " Bytes): \n\n");
		else if(queryType==Query.CONSTRUCT_TYPE)
			sb.append("\tQuery(Construct) result (" + getResultCount() + " Bytes): \n\n");
		else {
			sb.append("\tQuery results (" + getResultCount() + " results): \n");
	        StringPrinter printer = new StringPrinter(sb);
//          prepareObject(printer);
	        printer.walk(qr);
		}
		sb.append("\n__________________________________________________________________________________\n");
		return sb.toString();
	}

    public int getResultCount() {
        return qr.getResultCount();
    }
    
	public String getqName() {
		return query.getName();
	}

	public int getqNr() {
		return query.getNr();
	}

	public boolean isTimeout() {
		return timeout;
	}

	public int getQueryMixRun() {
		return query.getRun();
	}

	public void setTimeInSeconds(double timeInSeconds) {
		this.timeInSeconds = timeInSeconds;
	}

	public double getTimeInSeconds() {
		return timeInSeconds;
	}

    public ResultDescription[] getRds() {
        return rds;
    }

    public CompiledQuery getQuery() {
        return query;
    }

    public QueryResult getQueryResult() {
        return qr;
    }

}