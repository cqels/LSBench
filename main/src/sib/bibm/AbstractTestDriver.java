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

import static sib.util.FileUtil.strings2file;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;

import sib.bibm.statistics.AbstractQueryMixStatistics;
import sib.util.DoubleLogger;
import sib.util.Exec;
import sib.util.Options;


public abstract class AbstractTestDriver extends Options {
    public static final String version="SIB Test Driver 0.9";

    public long startTime=System.currentTimeMillis();
    public Date date=new Date();

    BooleanOption versionOpt=new BooleanOption("version", 
            "prints the version of the Test Driver"); 
    BooleanOption helpOpt=new BooleanOption("help"
            ,"prints this help message"); 
    
    FileOption errFileName=new FileOption("err-log", null, 
            "log file name to write error messages", 
            "default: print errors only to stderr");
    
   FileOption xmlResultFile=new FileOption("o <benchmark results output file>", "benchmark_result.xml"
           ,"default: %%");
   public IntegerOption nrThreads=new IntegerOption("mt <Number of clients>", 1
           ,"Run multiple clients concurrently."
           ,"default: 1");
   
   public LongOption seed=new LongOption("seed <Long Integer>", 808080L
           ,"Init the Test Driver with another seed than the default."
           ,"default: %%");
   
   public IntegerOption timeout=new IntegerOption("t <timeout in ms>", 0
           ,"Timeouts will be logged for the result report."
           ,"default: "+0);
   
   StringOption sutCmd=new StringOption("sut <sutcommand>", null
           ,"Measures the server's CPU time using external program."
           ,"<sutcommand> - the command to run external program, arguments delimited with comma");
   
   private static final String def_qualificationFile = "run.qual";
   protected BooleanOption qualification=new BooleanOption("q"
           ,"generate output qualification file with the default name ("+def_qualificationFile+")");
   public FileOption qualificationFile=new FileOption("qf <output qualification file name>", def_qualificationFile
           ,"generate output qualification file with this name");
   protected FileOption qualificationCompareFile=new FileOption("qcf <input qualification file name>", null
           ,"To turn on comparison of resultst."
           ,"default: none.");

   protected FileOption queryRootDir=new FileOption("qrd <query root directory>", "."
           ,"Where to look for the directoried listed in the use case file."
           ,"default: current working directory");
   
    public BooleanOption useDefaultParams=new BooleanOption("defaultparams"
            ,"use default query parameters ");

    protected BooleanOption printResults=new BooleanOption("printres"
            ,"include results into the log");
    
    //For Rampup
    public BooleanOption rampup=new BooleanOption("rampup","Run rampup procedure.","default: not set");
    public IntegerOption nrOfPeriods=new IntegerOption("numPeriods < The last nrOfPeriods periods are compared>", 5
            ,"default: 5");
    public DoubleOption percentDifference =new DoubleOption("percentDifference < The different threshold between two periods in rampup>", 0.1
            ,"default: 0.1");
    public IntegerOption nrRampups=new IntegerOption("numRampup < The max number of ranpup runs>", 8000
            ,"default: 8000");

    
    //SQL
    public BooleanOption doSQL=new BooleanOption("sql"
            ,"use JDBC connection to a RDBMS. Instead of a SPARQL-Endpoint, a JDBC URL has to be supplied."
            ,"default: not set");
    
    public StringOption driverClassName=new StringOption("dbdriver <DB-Driver Class Name>", "com.mysql.jdbc.Driver"
            ,"default: %%");

    // SPARQL
    StringOption baseEndpoint=new StringOption("url <common prefix for all endpoints>", ""
            ,"url <common prefix for all endpoints>"
            ,"default: empty string");
    MultiStringOption updateEndpoints=new MultiStringOption("u <Sparql Update Service Endpoint URL>"
            ,"Use this if you have SPARQL Update queries in your query mix.");
    
    public StringOption sparqlUpdateQueryParameter=new StringOption("uqp <update query parameter>",  "update"
            ,"The forms parameter name for the query string."
            ,"default: %%");

    public StringOption defaultGraph=new StringOption("dg <default graph>", null
            ,"add &default-graph-uri=<default graph> to the http request");
    
    public StringOption retryErrorMessage=new StringOption("retry-msg <message from server indicating deadlock>", null // for Virtuoso, "40001";
            ,"default: <null, that does not match any string>");
    /** number of attempts to replay query if recoverable http code 500 received */
     public IntegerOption numRetries=new IntegerOption("retry-max <number of attemts to replay query if deadlock error message received>", 3
             ,"default: %%");
     IntegerOption retryInterval_lowOpt=new IntegerOption("retry-int <time interval between attempts to replay query (milliseconds)>", 200
             ,"Increases by 1.5 times for each subsequent attempt."
             ,"default: %% ms");
     IntegerOption retryInterval_highOpt=new IntegerOption("retry-intmax <upper bound of time interval between attempts to replay query (milliseconds)>", 0
             ,"If set, actual retry-int is picked up randomly between set retry-int and retry-intmax"
             ,"default: equals to retry-int");

    private String[] sparqlEndpoints = null;
    private String[] sparqlUpdateEndpoints = null;
    private int updateEndpointIndex=0;
    private int endpointIndex=0;
    protected double scaleFactor;
    public AbstractParameterPool parameterPool;
    protected File qualOutFile;
    int retryInterval_low;
    int retryInterval_high;
    Random retryGen; // = new Random();

    public abstract  Collection<Query>getQueries();
    
    public String[] getSparqlEndpoints() {
        return sparqlEndpoints;
    }

    public String[] getSparqlUpdateEndpoints() {
        return sparqlUpdateEndpoints;
    }

    public synchronized String getNextEndPoint(byte queryType) {
        String serviceURL;
        if (queryType==Query.UPDATE_TYPE) {
            serviceURL=sparqlUpdateEndpoints[(updateEndpointIndex++)%sparqlUpdateEndpoints.length];
        } else {
            serviceURL=sparqlEndpoints[(endpointIndex++)%sparqlEndpoints.length];
        }
        return serviceURL;
    }
    
    public synchronized int getRetryInt() {
        int diap=retryInterval_high-retryInterval_low;
        if (diap>0) {
            return retryInterval_low+retryGen.nextInt(diap+1);          
        } else {
            return retryInterval_low;
        }
    }
    
    public AbstractTestDriver(String... usageHeader) throws IOException {
        super(usageHeader);
   }

    @Override
    protected void processProgramParameters(String[] args) {
        super.processProgramParameters(args);
        if (versionOpt.getValue()) {
            System.out.println(version);
            System.exit(0);
        }
        if (helpOpt.getValue()) {
            printUsageInfos();
            System.exit(0);
        }
        
        List<String> endpointsLoc = super.args;
        List<String> updateEndpointsLoc = updateEndpoints.getValue();

        String baseEndpoint = this.baseEndpoint.getValue();
        if (baseEndpoint != null) {
            for (int k = 0; k < endpointsLoc.size(); k++) {
                endpointsLoc.set(k, baseEndpoint +endpointsLoc.get(k));
            }
        }
        
        if (updateEndpointsLoc.size() == 0) {
            updateEndpointsLoc=endpointsLoc;
        } else if (baseEndpoint != null) {
            for (int k = 0; k < updateEndpointsLoc.size(); k++) {
                updateEndpointsLoc.set(k, baseEndpoint +updateEndpointsLoc.get(k));
            }
        }

        if (endpointsLoc.size() == 0 && updateEndpointsLoc.size() == 0) {
            System.err.println("No endpoints provided:\n");
            printUsageInfos();
            System.exit(-1);
        }

        this.sparqlEndpoints = endpointsLoc.toArray(new String[endpointsLoc.size()]);
        this.sparqlUpdateEndpoints = updateEndpointsLoc.toArray(new String[updateEndpointsLoc.size()]);
        
        System.out.println("Reading Test Driver data...");
        System.out.flush();

        if  (qualification.getValue() || qualificationFile.getSetValue()!=null) {
            qualOutFile=qualificationFile.getSetValue();
            if (qualOutFile==null) { // only construct new file name for default
                qualOutFile=qualificationFile.newNumberedFile();
            }
            System.out.println("Result data will be written in "+qualOutFile.getAbsolutePath());
       }

        long seed = this.seed.getValue();
        retryGen = new Random(seed);
        retryInterval_low=this.retryInterval_lowOpt.getValue();
        retryInterval_high=this.retryInterval_highOpt.getValue();
        if (retryInterval_low>retryInterval_high) {
            if (retryInterval_high==0) { // just was not set
                retryInterval_high=retryInterval_low*2;
            } else  {
                System.err.println("invalid parameters: -retry-maxint < -retry-max\n");
                printUsageInfos();
                System.exit(-1);
            } 
        }
    }

    //***** SUT time *****//
   
    private Double sutStartTime;
    private Double sutEndTime;

    public void sutStart() {
        String sutCmd = this.sutCmd.getValue();
        if (sutCmd==null) return;
        sutStartTime=sutCurrent(sutCmd);      
    }

    public void sutEnd() {
        String sutCmd = this.sutCmd.getValue();
        if (sutCmd==null) return;
        sutEndTime=sutCurrent(sutCmd);        
    }
    
    public Double getSUT() {
        if (sutStartTime==null || sutEndTime==null) {
            return null;
        }
        return sutEndTime-sutStartTime;
    }

    private Double sutCurrent(String sutCmd) {
        String[] lines=null;
        try {
            lines=Exec.execProcess(sutCmd.split(","));
        } catch (Exception e) {
            e.printStackTrace();
        }
        double res=0;
        if (lines==null) {
            DoubleLogger.getErr().println("sutCurrent: lines=null");
            return null;
        }
        for (String line: lines) {
            String[] tokens=line.split(":"); // hh:mm:ss
            if (tokens.length!=3) {
                DoubleLogger.getErr().println("sutCurrent: wrong line from sut command: "+line);
                return null;
            }
            double time=Double.parseDouble(tokens[0])*3600+Double.parseDouble(tokens[1])*60+Double.parseDouble(tokens[2]);
            res+=time;
        }
        return res;
    }
    
   /**
     * if xmlResultFile exists, print to xmlResultFileName.(N+1).xml
     * @param queryMixStat 
     */
    protected void printXML(AbstractQueryMixStatistics queryMixStat) {
        File resFile=xmlResultFile.newNumberedFile();
        String xml=queryMixStat.toXML();
        strings2file(resFile, xml);
    }

}
