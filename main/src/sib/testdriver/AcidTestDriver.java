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
package sib.testdriver;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import sib.bibm.AbstractParameterPool;
import sib.bibm.QueryMix;
import sib.bibm.Exceptions.BadSetupException;
import sib.bibm.Exceptions.ExceptionException;
import sib.bibm.Exceptions.RequestFailedException;
import sib.util.DoubleLogger;
import sib.util.Options;


public class AcidTestDriver extends Options {
    public static final String version="Openlink BIBM ACID Test Driver 0.1";

    public long startTime=System.currentTimeMillis();
    public Date date=new Date();

    BooleanOption versionOpt=new BooleanOption("version", 
            "prints the version of the Test Driver"); 
    BooleanOption helpOpt=new BooleanOption("help"
            ,"prints this help message"); 
    
    StringOption scaleFactorOpt=new StringOption("scale <scale factor>", null
            ,"<scale factor> is the scale factor of the database being tested.");

    FileOption errFileName=new FileOption("err-log", null, 
            "log file name to write error messages", 
            "default: print errors only to stderr");
    
    public IntegerOption nrRuns=new IntegerOption("runs <number of query mix runs>", -1
            , "default: the number of clients");
    
    StringOption querymixDirNames=new StringOption("uc <use case query mix directory>"
            ,"Specifies the query mix directory.");
/*    
   FileOption xmlResultFile=new FileOption("o <benchmark results output file>", "benchmark_result.xml"
           ,"default: %%");
   public StringOption defaultGraph=new StringOption("dg <default graph>", null
           ,"add &default-graph-uri=<default graph> to the http request");
*/   

    public IntegerOption nrThreads=new IntegerOption("mt <Number of clients>", 1
           ,"Run multiple clients concurrently."
           ,"default: 1");
   
   public LongOption seedOpt=new LongOption("seed <Long Integer>", 808080L
           ,"Init the Test Driver with another seed than the default."
           ,"default: %%");
   
   public IntegerOption timeout=new IntegerOption("t <timeout in ms>", 0
           ,"Timeouts will be logged for the result report."
           ,"default: "+0);
   
   public StringOption driverClassName=new StringOption("dbdriver <DB-Driver Class Name>", "com.mysql.jdbc.Driver"
           ,"default: %%");
   FileOption queryRootDir=new FileOption("qrd <query root directory>", "."
           ,"Where to look for the directoried listed in the use case file."
           ,"default: current working directory");
   
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
    
    /*
    public StringOption sparqlUpdateQueryParameter=new StringOption("uqp <update query parameter>",  "update"
            ,"The forms parameter name for the query string."
            ,"default: %%");
    StringOption baseEndpoint=new StringOption("url <common prefix for all endpoints>", ""
            ,"url <common prefix for all endpoints>"
            ,"default: empty string");
  */  
    BooleanOption printResults=new BooleanOption("printres"
            ,"include results into the log");
    
    // BSBM
    FileOption resourceDir=new FileOption("idir <data input directory>", "td_data"
                ,"The input directory for the Test Driver data"
                , "default: %%");
    FileOption updateFile=new FileOption("udataset <update dataset file name>", null
            ,"Specified an update file generated by the BSBM dataset generator.");
    
    // BDSM
    public StringOption refreshProg1=new StringOption("rf1 <refresh command 1>", null
            ,"<refresh command> - the command to run the refresh function 1 (insert, see TPC-H spec)");
    public StringOption refreshProg2=new StringOption("rf2 <refresh command>", null
            ,"<refresh command 2> - the command to run the refresh function2  (delete, see TPC-H spec)");


    int retryInterval_low;
    int retryInterval_high;

    Random retryGen; // = new Random();
    public synchronized int getRetryInt() {
        int diap=retryInterval_high-retryInterval_low;
        if (diap>0) {
            return retryInterval_low+retryGen.nextInt(diap+1);          
        } else {
            return retryInterval_low;
        }
    }
    
    public File querymixDir;
    private String endPoint = null;

    private double scaleFactor;
    public AbstractParameterPool parameterPool;

    private String[] queryNames;

    protected Random rand;

    public String getEndpoint() {
        return endPoint;
    }

    /*
     * Process the program parameters typed on the command line.
     */
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
        
        String tpchscale = scaleFactorOpt.getValue();
        scaleFactor=Double.parseDouble(tpchscale);
        if (nrRuns.getValue()<0) {
            nrRuns.setValue(nrThreads.getValue());
        }
        
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

        List<String> endpointsLoc = super.args;

        if (endpointsLoc.size() != 1) {
            System.err.println("Exactly one endpoint must be provided:\n");
            printUsageInfos();
            System.exit(-1);
        }

        this.endPoint = endpointsLoc.get(0);

        try {
            Class.forName(driverClassName.getValue());
        } catch(ClassNotFoundException e) {
            throw new ExceptionException("Driver class not found:", e);
        }
    }

    public AcidTestDriver(String args[]) throws IOException {
        super(version,
                "Usage: com.openlinksw.bdsm.TestDriver <options> endpoints...", // CHECK package
                 "endpoint: The URL of the HTTP SPARQL or SQL endpoint");
        processProgramParameters(args);

        // create ParameterPool
        boolean doSQL=true; //this.doSQL.getValue();
        long seed = this.seedOpt.getValue();
        char parameterChar=doSQL? '@' : '%';
        parameterPool = new sib.testdriver.ParameterPool(parameterChar, seed, scaleFactor);  

        rand = new Random(seed);
        retryGen = new Random(seed);

        // read query mix
        String uscaseDirname=this.querymixDirNames.getValue();
        this.querymixDir = new File(queryRootDir.getValue(), uscaseDirname);
        QueryMix queryMix= new QueryMix(this.querymixDir);
        this.queryNames=queryMix.getQueryNames();
        
        System.out.println("..done");
  }
    
   static abstract class AcidTest {
       AcidTestDriver driver;
       protected Statement statement;
       protected Connection conn;
        
        AcidTest(AcidTestDriver driver) {
            this.driver=driver;
            int timeoutInSeconds = driver.timeout.getValue()/1000;
            try {
                conn = DriverManager.getConnection(driver.endPoint);
                statement = conn.createStatement();
                statement.setQueryTimeout(timeoutInSeconds);
 //               statement.setFetchSize(fetchSize);
            } catch (SQLException e0) {
                SQLException e=e0;
                while(e!=null) {
                    e.printStackTrace();
                    e=e.getNextException();
                }
                throw new ExceptionException("SQLConnection()", e0);
            }
            
        }

         protected static BigDecimal getBigDecimal(ResultSet resultset, int col) throws SQLException {
            return resultset.getBigDecimal(col).setScale(2, BigDecimal.ROUND_HALF_EVEN);
        }

        public static BigDecimal toBigDecimal(double d, int precision) {
            for (int k=0; k<precision; k++) {
                d=d*10;
            }
            return BigDecimal.valueOf((long)d, precision);
        }

        protected static BigDecimal trunc(BigDecimal multiply, int precision) {
            return multiply.setScale(precision, BigDecimal.ROUND_DOWN);
        }

        /**
         *  O_KEY selected at random from the same distribution as that
         *   used to populate L_ORDERKEY in the qualification database (see Clause 4.2.3)
         *    4.2.3: O_ORDERKEY unique within [SF * 1,500,000 * 4].
         *   The ORDERS and LINEITEM tables are sparsely populated by generating a key value that
         *    causes the first 8 keys of each 32 to be populated, yielding a 25% use of the key range.
         *    
         * @return
         */
        protected long selectRandomOrderKey() {
            long sz = Math.round(driver.scaleFactor * 1500000 / 8);
            if (sz>Integer.MAX_VALUE) {
                throw new BadSetupException("scale factor too big:"+driver.scaleFactor); // FIXME
            }
            for (;;) {
                long hiBits = driver.rand.nextInt((int) sz);
                long lowBits =driver.rand.nextInt(8);
                long oKey= ((hiBits <<5) | lowBits);
                if (oKey!=0) { // experimental fact
                    return oKey;
                }
            }
        }
        
        public int getTestNum() {
           throw new UnsupportedOperationException();
        }

        /**
         * @param num test number
         * @return error string, or null if the test passed
         * @throws SQLException
         * @throws InterruptedException 
         */
        public String runTest(int num) throws SQLException, InterruptedException {
            throw new UnsupportedOperationException();
        }

        static final BigDecimal big0_2 = BigDecimal.valueOf(0, 2);
        static final BigDecimal big1_2 = BigDecimal.valueOf(100, 2);

        class TransactionParams {
            long o_key;
            int l_key;
            BigDecimal delta;
            
            TransactionParams(long o_key, int l_key) throws SQLException {
                this.o_key=o_key;
                this.l_key=l_key;
                Random rand = driver.rand;
                // System.out.println(" o_key="+ o_key+"; m="+m);
                // [delta] selected at random within [1 .. 100]:
                delta = new BigDecimal(rand.nextInt(100) + 1);
            }

            TransactionParams(long o_key) throws SQLException {
                this.o_key=o_key;
                // L_KEY selected at random from [1 .. M] where
                // M = SELECT MAX(L_LINENUMBER) from LINEITEM where L_ORDERKEY = O_KEY
                String queryString = "select MAX(L_LINENUMBER) from LINEITEM where L_ORDERKEY = " + o_key;
                ResultSet resultset = statement.executeQuery(queryString);
                resultset.next();
                int m = resultset.getInt(1);
                resultset.close();
                if (m<1) {
                    throw new BadSetupException("no LINEITEM found for L_ORDERKEY="+o_key);
                }
                Random rand = driver.rand;
                l_key = rand.nextInt(m) + 1;
                // System.out.println(" o_key="+ o_key+"; m="+m);
                // [delta] selected at random within [1 .. 100]:
                delta = new BigDecimal(rand.nextInt(100) + 1);
            }

            TransactionParams() throws SQLException {
                this(selectRandomOrderKey());
            }
        }
         
        class CheckFields {
            BigDecimal ototal;
            BigDecimal extprice;
            BigDecimal quantity;
            
            CheckFields(TransactionParams params) throws SQLException {
                String queryString="select O_TOTALPRICE from ORDERS where O_ORDERKEY = "+params.o_key;
                ResultSet resultset = statement.executeQuery(queryString);
                if (!resultset.next()) {
                    throw new BadSetupException("no order found with key="+params.o_key);
                }
                ototal=resultset.getBigDecimal(1);
                queryString="select L_EXTENDEDPRICE,L_QUANTITY from LINEITEM where  L_ORDERKEY = "+params.o_key+" and L_LINENUMBER = "+params.l_key;
                resultset = statement.executeQuery(queryString);
                if (!resultset.next()) {
                    throw new BadSetupException("no lineitem found with orderkey="+params.o_key+" linenumber="+params.l_key);
                }
                extprice=getBigDecimal(resultset, 1);
                quantity=getBigDecimal(resultset, 2);
                resultset.close();
            }

            @Override
            public boolean equals(Object obj) {
                if (obj==null) {
                    return false;
                }
                if (!(obj instanceof CheckFields)) {
                    return false;
                }
                CheckFields other=(CheckFields)obj;
                return ototal.equals(other.ototal)&&extprice.equals(other.extprice)&&quantity.equals(other.quantity);
            }
            
        }
   }
    
   static class AcidTransaction extends AcidTest {
       public int step=0;
       BigDecimal l_quantity;
       BigDecimal l_extendedPrice;
       
       AcidTransaction(AcidTestDriver driver) {
           super(driver);
       }

        void startAcidTransaction(TransactionParams params) throws SQLException {
            // BEGIN TRANSACTION
            conn.setAutoCommit(false);
            // Read O_TOTALPRICE from ORDERS into [ototal] where O_ORDERKEY = [o_key]
            String queryString = "select O_TOTALPRICE from ORDERS where O_ORDERKEY = " + params.o_key;
            ResultSet resultset = statement.executeQuery(queryString);
            if (!resultset.next()) {
                throw new BadSetupException("no order found with key="+params.o_key);
            }
            BigDecimal ototal = resultset.getBigDecimal(1);
            step=1;
            
            // Read L_QUANTITY, L_EXTENDEDPRICE, L_PARTKEY, L_SUPPKEY, L_TAX, L_DISCOUNT
            // into [quantity], [extprice], [pkey], [skey], [tax], [disc]
            // where L_ORDERKEY = [o_key] and L_LINENUMBER = [l_key]
            queryString = "select  L_QUANTITY, L_EXTENDEDPRICE, L_PARTKEY, L_SUPPKEY, L_TAX, L_DISCOUNT " + " from LINEITEM where L_ORDERKEY = " + params.o_key
                    + " and L_LINENUMBER = " + params.l_key;
            resultset = statement.executeQuery(queryString);
            if (!resultset.next()) {
                throw new BadSetupException("no LINEITEM found with o_key="+params.o_key+" linenumber="+params.l_key);
            }
            BigDecimal quantity = getBigDecimal(resultset, 1);
            BigDecimal extprice = getBigDecimal(resultset, 2);
            int pkey = resultset.getInt(3);
            int skey = resultset.getInt(4);
            BigDecimal tax = getBigDecimal(resultset, 5);
            BigDecimal disc = getBigDecimal(resultset, 6);
            resultset.close();
            step=2;
            
            // Set [ototal] = [ototal] - trunc( trunc([extprice] * (1 - [disc]), 2) * (1 + [tax]), 2)
            ototal = ototal.subtract(trunc(trunc(extprice.multiply(big1_2.subtract(disc)), 2).multiply(big1_2.add(tax)), 2));
            // Set [rprice] = trunc([extprice]/[quantity], 2)
            BigDecimal rprice = toBigDecimal(extprice.doubleValue() / quantity.doubleValue(), 2);
            // Set [cost] = trunc([rprice] * [delta], 2)
            BigDecimal cost = trunc(rprice.multiply(params.delta), 2);
            // Set [new_extprice] = [extprice] + [cost]
            BigDecimal new_extprice = extprice.add(cost);
            // Set [new_ototal] = trunc([new_extprice] * (1.0 - [disc]), 2)
            BigDecimal new_ototal = trunc(new_extprice.multiply(big1_2.subtract(disc)), 2);
            // Set [new_ototal] = trunc([new_ototal] * (1.0 + [tax]), 2)
            new_ototal = trunc(new_ototal.multiply(big1_2.add(tax)), 2);
            // Set [new_ototal] = [ototal] + [new_ototal]
            new_ototal = ototal.add(new_ototal);
            // Update LINEITEM where L_ORDERKEY = [o_key] and L_LINENUMBER = [l_key]
            // Set L_EXTENDEDPRICE = [new_extprice]
            // Set L_QUANTITY = [quantity] + [delta]
            // Write L_EXTENDEDPRICE, L_QUANTITY to LINEITEM
            l_quantity = quantity.add(params.delta);
            l_extendedPrice=new_extprice;
            queryString = "update  LINEITEM " + " set L_EXTENDEDPRICE = " + new_extprice + " , L_QUANTITY = " + l_quantity
                    + " where L_ORDERKEY = " + params.o_key + " and L_LINENUMBER = " + params.l_key;
            statement.executeUpdate(queryString);
            step=3;
            // Txn1.L_EXTENDEDPRICE+ (DELTA1 * (Txn1.L_EXTENDEDPRICE / Txn1.L_QUANTITY))
            
            // Update ORDERS where O_ORDERKEY = [o_key]
            // Set O_TOTALPRICE = [new_ototal]
            // Write O_TOTALPRICE to ORDERS
            queryString = "update  ORDERS " + " set O_TOTALPRICE = " + new_ototal + " where O_ORDERKEY = " + params.o_key;
            statement.executeUpdate(queryString);
            step=4;

            // Insert Into HISTORY Values ([pkey], [skey], [o_key], [l_key], [delta], [current_date_time])
        }

        void commitTransaction(boolean doCommit) throws SQLException {
            if (doCommit) {
                conn.commit();
            } else {
                conn.rollback();
            }
            step=5;
            conn.setAutoCommit(true);
        }

        void runAcidTransaction(TransactionParams params, boolean doCommit) throws SQLException {
            startAcidTransaction(params);
            // COMMIT TRANSACTION
            commitTransaction(doCommit);
            // Return [rprice], [quantity], [tax], [disc], [extprice], [ototal]
        }

   }

   static class AtomicityTest extends AcidTest {

       AtomicityTest(AcidTestDriver driver) {
           super(driver);
           // TODO Auto-generated constructor stub
       }

       @Override
       public int getTestNum() {
           return 2;
       }

       @Override
       public String runTest(int num) throws SQLException {
           switch (num) {
           case 1: {
               // Perform the ACID Transaction for a randomly selected set of input data
               AcidTransaction ts=new AcidTransaction(driver);
               TransactionParams params=new TransactionParams();
               CheckFields before=new CheckFields(params);
               ts.runAcidTransaction(params, true);
               //  verify that the appropriate rows have been changed in the ORDERS, LINEITEM, and HISTORY tables.
               CheckFields after=new CheckFields(params);
               if (after.equals(before)) {
                   return "database records not changed";
               } else {
                   return null;
               }
           }
           case 2: { 
               // Perform the ACID Transaction for a randomly selected set of input data
               AcidTransaction ts=new AcidTransaction(driver);
               TransactionParams params=new TransactionParams();
               CheckFields before=new CheckFields(params);
               // substituting a ROLLBACK of the transaction for the COMMIT of the transaction.
               ts.runAcidTransaction(params, false);
               //  verify that the appropriate rows have not been changed in the ORDERS, LINEITEM, and HISTORY tables.
               CheckFields after=new CheckFields(params);
               if (after.equals(before)) {
                   return null;
               } else {
                   return "database records changed";
               }
           }
           default:
               throw new BadSetupException("bad test number:"+num);
           }
       }
       
   }

   static class ConsistencyTest extends AcidTest {
       long[] oKeys;
       
       ConsistencyTest(AcidTestDriver driver) {
           super(driver);
           // TODO Auto-generated constructor stub
       }

       @Override
       public int getTestNum() {
           return 1;
       }

       /** checks that  O_TOTALPRICE = SUM(trunc(trunc(L_EXTENDEDPRICE *(1 - L_DISCOUNT),2) * (1+L_TAX),2))
        * @param oKey
        * @return
     * @throws SQLException 
        */
       boolean consistencyCheck(long oKey) throws SQLException {
           String queryString="select O_TOTALPRICE from ORDERS where O_ORDERKEY = "+oKey;
           ResultSet resultset = statement.executeQuery(queryString);
           if (!resultset.next()) {
               throw new BadSetupException("no order found with key="+oKey);
           }
           BigDecimal ototal = getBigDecimal(resultset, 1);
//           System.out.println(ototal.toPlainString());
           queryString="select L_EXTENDEDPRICE, L_DISCOUNT, L_TAX from LINEITEM where  L_ORDERKEY = "+oKey;
           resultset = statement.executeQuery(queryString);
           BigDecimal ltotal=big0_2;
           int itemCount=0;
           while (resultset.next()) {
               BigDecimal extprice = getBigDecimal(resultset, 1);
               BigDecimal disc = getBigDecimal(resultset, 2);
               BigDecimal tax = getBigDecimal(resultset, 3);
//               System.out.print(" extprice="+extprice.toPlainString());
               BigDecimal mult1 = big1_2.subtract(disc);
               BigDecimal mult2 = big1_2.add(tax);
               BigDecimal price1 = trunc(extprice.multiply(mult1), 2);
               BigDecimal price = trunc(price1.multiply(mult2), 2);
               ltotal=ltotal.add(price);
               itemCount++;
           }
           resultset.close();
           if (itemCount==0) {
               throw new BadSetupException("no lineitem found with order key="+oKey);
           }
//           System.out.println(ltotal.toPlainString());
           return ototal.equals(ltotal);
       }
       
       @Override
       public String runTest(int num) throws SQLException {
           if (num!=1) {
               throw new BadSetupException("bad test number:"+num);
           }               
           // Verify that the ORDERS, and LINEITEM tables are initially consistent as defined in Clause 3.3.2.1,
           // based on a random sample of at least 10 distinct values of O_ORDERKEY.
           int count=10;
           oKeys=new long[count];
           for (int i = 0; i < count; i++) {
               long o_key = selectRandomOrderKey();
               oKeys[i]=o_key;
               boolean ok = consistencyCheck(o_key);
               if (!ok) {
                   return "first consistency check failed for orderkey="+o_key;
               }
           }
           
           // Submit at least 100 ACID Transactions from each of at least the number of execution streams
           // ( # query streams + 1 refresh stream) used in the reported throughput test (see Clause 5.3.4).
           int count2=100;
           AcidTransaction ts=new AcidTransaction(driver);
           for (int i = 0; i < count2; i++) {
               long o_key;
               // Ensure that all the values of O_ORDERKEY chosen in Step 1 are used by some transaction in Step 2.
               if (i<oKeys.length) {
                   o_key=oKeys[i];
               } else {
                   o_key = selectRandomOrderKey();
               }
               // Each transaction must use values of (O_KEY, L_KEY, DELTA) randomly generated within the ranges defined in Clause 3.1.6.2.
               TransactionParams params=new TransactionParams(o_key);
               ts.runAcidTransaction(params, true);
           }

           // Re-verify the consistency of the ORDERS, and LINEITEM tables as defined in Clause 3.3.2.1 based on the same sample values
           // of O_ORDERKEY selected in Step 1.
           for (int i = 0; i < count; i++) {
               long o_key =  oKeys[i];
               boolean ok = consistencyCheck(o_key);
               if (!ok) {
                   return "second consistency check failed";
               }
           }
           return null;
       }
       
   }

   static class IsolationTest extends AcidTest implements Runnable {
       TransactionParams params1;
       AcidTransaction txn2;
       SQLException txn2Exception;
       
       IsolationTest(AcidTestDriver driver) {
           super(driver);
       }

       @Override
       public int getTestNum() {
           return 6;
       }

        @Override
        public void run() {
            txn2 = new AcidTransaction(driver);
            try {
                TransactionParams params2 = new TransactionParams(params1.o_key);
                txn2.startAcidTransaction(params2);
                txn2.commitTransaction(true);
            } catch (SQLException e) {
                txn2Exception=e;
            }
        }

        /**This test demonstrates isolation for the read-write conflict of a read-write transaction
         *  and a read-only transaction  when the read-write transaction is committed.
         * @return error string, or null if the test passed
         * @throws SQLException 
         * @throws InterruptedException 
         */
        String test1() throws SQLException, InterruptedException {
        //   Perform the following steps:
        //        1. Start an ACID Transaction Txn1 for a randomly selected O_KEY, L_KEY, and DELTA.
        //        2. Suspend Txn1 immediately prior to COMMIT.
            AcidTransaction txn1=new AcidTransaction(driver);
            params1 = new TransactionParams();
            txn1.startAcidTransaction(params1);
        //        3. Start an ACID Query Txn2 for the same O_KEY as in Step 1. (Txn2 attempts to read the data that has just been updated by Txn1.)
            Thread txn2Thr=new Thread(this);
            txn2Thr.start();
            try {
                 Thread.sleep(3000);
            } catch (InterruptedException e) {
                 txn1.commitTransaction(false);
                 txn2Thr.interrupt();
                 throw e;
            }
        //        4. Verify that Txn2 does not see Txn1's updates.
            int step=txn2.step;
            if (step!=0) {
                txn1.commitTransaction(false);
                txn2Thr.interrupt();
                txn2Thr.join(3000);
                return "Txn2 advanced to step "+step;
            }
        //        5. Allow Txn1 to complete.
            txn1.commitTransaction(true);
        //        6. Txn2 should now have completed.
            txn2Thr.join(3000);
            if (txn2Thr.isAlive()) {
                step=txn2.step;
                txn2Thr.interrupt();
                return "Txn2 hangs on step "+step;
            }
            if (txn2Exception!=null) {
                return "Txn2 threw "+txn2Exception;
            }
            
            return null;
        }

        /** This test demonstrates isolation for the read-write conflict of a read-write transaction
         *  and a read-only transaction when the read-write transaction is rolled back.
         * @return error string, or null if the test passed
         * @throws SQLException 
         * @throws InterruptedException 
         */
        String test2() throws SQLException, InterruptedException {
        //   Perform the following steps:
        //        1. Start an ACID Transaction Txn1 for a randomly selected O_KEY, L_KEY, and DELTA.
        //        2. Suspend Txn1 immediately prior to COMMIT.
            AcidTransaction txn1=new AcidTransaction(driver);
            params1 = new TransactionParams();
            txn1.startAcidTransaction(params1);
        //        3. Start an ACID Query Txn2 for the same O_KEY as in Step 1. (Txn2 attempts to read the data that has just been updated by Txn1.)
            Thread txn2Thr=new Thread(this);
            txn2Thr.start();
            try {
                 Thread.sleep(3000);
            } catch (InterruptedException e) {
                 txn1.commitTransaction(false);
                 txn2Thr.interrupt();
                 throw e;
            }
        //        4. Verify that Txn2 does not see Txn1's updates.
            int step=txn2.step;
            if (step!=0) {
                txn1.commitTransaction(false);
                txn2Thr.interrupt();
                txn2Thr.join(3000);
                return "Txn2 advanced to step "+step;
            }
        //        5. Force Txn1 to rollback.
            txn1.commitTransaction(false);
        //        6. Txn2 should now have completed.
            txn2Thr.join(3000);
            if (txn2Thr.isAlive()) {
                step=txn2.step;
                txn2Thr.interrupt();
                return "Txn2 hangs on step "+step;
            }
            if (txn2Exception!=null) {
                return "Txn2 threw "+txn2Exception;
            }
            
            return null;
        }

        /** This test demonstrates isolation for the write-write conflict of two update transactions when the first transaction is committed.
         * FIXME:  The test specification is wrong: AcidTransaction starts with reading, so read-write conflict occurs, not write-write one.
         * However, the test follows the spec.
         * @return error string, or null if the test passed
         * @throws SQLException 
         * @throws InterruptedException 
         */
        String test3() throws SQLException, InterruptedException {
        //   Perform the following steps:
        //        1. Start an ACID Transaction Txn1 for a randomly selected O_KEY, L_KEY, and DELTA1.
        //        2. Suspend Txn1 immediately prior to COMMIT.
            AcidTransaction txn1=new AcidTransaction(driver);
            params1 = new TransactionParams();
            txn1.startAcidTransaction(params1);
        //    3.  Start another ACID Transaction Txn2 for the same O_KEY, L_KEY and for a randomly selected DELTA2.
        //    (Txn2 attempts to read and update the data that has just been updated by Txn1.)
            Thread txn2Thr=new Thread(this);
            txn2Thr.start();
            try {
                 Thread.sleep(3000);
            } catch (InterruptedException e) {
                 txn1.commitTransaction(false);
                 txn2Thr.interrupt();
                 throw e;
            }
        //        4. Verify that Txn2 waits.
            int step=txn2.step;
            if (step!=0) {
                txn1.commitTransaction(true);
                txn2Thr.interrupt();
                txn2Thr.join(3000);
                return "Txn2 advanced to step "+step;
            }
        //        5. Allow Txn1 to complete.
            txn1.commitTransaction(true);
        //        Txn2 should now have completed.
            txn2Thr.join(3000);
            if (txn2Thr.isAlive()) {
                step=txn2.step;
                txn2Thr.interrupt();
                return "Txn2 hangs on step "+step;
            }
            if (txn2Exception!=null) {
                return "Txn2 threw "+txn2Exception;
            }
            // 6. Verify that      Txn2.L_EXTENDEDPRICE =
            // Txn1.L_EXTENDEDPRICE+ (DELTA1 * (Txn1.L_EXTENDEDPRICE / Txn1.L_QUANTITY))
            BigDecimal bigDec1 = txn1.l_extendedPrice.divide(txn1.l_quantity, 2, RoundingMode.HALF_EVEN); // scale and rounding mode must be provided, though the spec misses that
            BigDecimal bigDec2=txn1.l_extendedPrice.add(params1.delta.multiply(bigDec1));
            if (!txn2.l_extendedPrice.equals(bigDec2)) {
                return "comparison failed: "+ txn2.l_extendedPrice.toPlainString()+" != "+bigDec2.toPlainString();
            }
            
            return null;
        }

        /** This test demonstrates isolation for the write-write conflict of two update transactions when the first transaction is rolled back.
         * FIXME:  The test specification is wrong: AcidTransaction starts with reading, so read write conflict occurs, not write-write one.
         * However, the test follows the spec.
         * @return error string, or null if the test passed
         * @throws SQLException 
         * @throws InterruptedException 
         */
        String test4() throws SQLException, InterruptedException {
        //   Perform the following steps:
        //        1. Start an ACID Transaction Txn1 for a randomly selected O_KEY, L_KEY, and DELTA1.
        //        2. Suspend Txn1 immediately prior to COMMIT.
            AcidTransaction txn1=new AcidTransaction(driver);
            params1 = new TransactionParams();
            txn1.startAcidTransaction(params1);
        //    3.  Start another ACID Transaction Txn2 for the same O_KEY, L_KEY and for a randomly selected DELTA2.
        //    (Txn2 attempts to read and update the data that has just been updated by Txn1.)
            Thread txn2Thr=new Thread(this);
            txn2Thr.start();
            try {
                 Thread.sleep(3000);
            } catch (InterruptedException e) {
                 txn1.commitTransaction(false);
                 txn2Thr.interrupt();
                 throw e;
            }
        //        4. Verify that Txn2 waits.
            int step=txn2.step;
            if (step!=0) {
                txn1.commitTransaction(true);
                txn2Thr.interrupt();
                txn2Thr.join(3000);
                return "Txn2 advanced to step "+step;
            }
        //    5. Force Txn1 to rollback.
            txn1.commitTransaction(false);
        //        Txn2 should now have completed.
            txn2Thr.join(3000);
            if (txn2Thr.isAlive()) {
                step=txn2.step;
                txn2Thr.interrupt();
                return "Txn2 hangs on step "+step;
            }
            if (txn2Exception!=null) {
                return "Txn2 threw "+txn2Exception;
            }
            // 6. Verify that Txn2.L_EXTENDEDPRICE = Txn1.L_EXTENDEDPRICE
            if (!txn2.l_extendedPrice.equals(txn1.l_extendedPrice)) {
                return "comparison failed: "+ txn2.l_extendedPrice.toPlainString()+" != "+txn1.l_extendedPrice;
            }
            
            return null;
        }

        /** This test demonstrates the ability of read and write transactions affecting different database tables to make progress concurrently.
         * @return error string, or null if the test passed
         * @throws SQLException 
         * @throws InterruptedException 
         */
        String test5() throws SQLException, InterruptedException {
        //   Perform the following steps:
        //        1. Start an ACID Transaction Txn1 for a randomly selected O_KEY, L_KEY, and DELTA.
        //        2. Suspend Txn1 immediately prior to COMMIT.
            AcidTransaction txn1=new AcidTransaction(driver);
            params1 = new TransactionParams();
            txn1.startAcidTransaction(params1);
        //    3. Start a transaction Txn2 that does the following:
        //    4. Select random values of PS_PARTKEY and PS_SUPPKEY. 
        //        Return all columns of the PARTSUPP table for which PS_PARTKEY and PS_SUPPKEY are equal to the selected values.
            Thread txn2Thr=new Thread(this);
            txn2Thr.start();
            try {
                 Thread.sleep(3000);
            } catch (InterruptedException e) {
                 txn1.commitTransaction(false);
                 txn2Thr.interrupt();
                 throw e;
            }
        //     5. Verify that Txn2 completes.
            txn2Thr.join(3000);
            if (txn2Thr.isAlive()) {
                int step=txn2.step;
                txn2Thr.interrupt();
                return "Txn2 hangs on step "+step;
            }
            if (txn2Exception!=null) {
                return "Txn2 threw "+txn2Exception;
            }
        //    6. Allow Txn1 to complete. 
            txn1.commitTransaction(true);
        //        Verify that the appropriate rows in the ORDERS, LINEITEM and HISTORY tables have been changed.
            if (!txn2.l_extendedPrice.equals(txn1.l_extendedPrice)) {
                return "comparison failed: "+ txn2.l_extendedPrice.toPlainString()+" != "+txn1.l_extendedPrice;
            }
            
            return null;
        }

       @Override
       public String runTest(int num) throws SQLException, InterruptedException {
           switch (num) {
           case 1:
               return test1();
           case 2:
               return test2();
           case 3:
               return test3();
           case 4:
               return test4();
           case 5:
               return test5();
           case 6:
               return new IsolationTest6(driver).test6();
           default:
               throw new BadSetupException("bad test number:"+num);
           }
       }

   }

   static class IsolationTest6 extends AcidTest implements Runnable {
       TransactionParams params1;
       AcidTransaction txn2;
       SQLException txn2Exception;
       
       IsolationTest6(AcidTestDriver driver) {
           super(driver);
       }

       @Override
       public int getTestNum() {
           return 6;
       }

        @Override
        public void run() {
            txn2 = new AcidTransaction(driver);
            try {
                TransactionParams params2 = new TransactionParams(params1.o_key);
                txn2.startAcidTransaction(params2);
                txn2.commitTransaction(true);
            } catch (SQLException e) {
                txn2Exception=e;
            }
        }

        /** This test demonstrates that the continuous submission of arbitrary (read-only) queries against one or more tables of the database
         *  does not indefinitely delay update transactions affecting those tables from making progress.
         * @return error message string
         * @throws SQLException
         */
       String test6() throws SQLException {
           throw new UnsupportedOperationException();
//           1. Start a transaction Txn1. Txn1 executes Q1 (from Clause 2.4) against the qualification database
//           where the sub-stitution parameter [delta] is chosen from the interval [0 .. 2159] so that the query runs for a sufficient length of time.
//           Comment: Choosing [delta] = 0 will maximize the run time of Txn1.
           
//           2. Before Txn1 completes, submit an ACID Transaction Txn2 with randomly selected values of O_KEY, L_KEY and DELTA.
//           If Txn2 completes before Txn1 completes, verify that the appropriate rows in the ORDERS, LINEITEM and HIS-TORY tables have been changed.
//           In this case, the test is complete with only Steps 1 and 2. If Txn2 will not complete before Txn1 completes, perform Steps 3 and 4:
           
//           3. Ensure that Txn1 is still active. Submit a third transaction Txn3, which executes Q1 against the qualification database
//           with a test-sponsor selected value of the substitution parameter [delta] that is not equal to the one used in Step 1.
           
//           4. Verify that Txn2 completes before Txn3, and that the appropriate rows in the ORDERS, LINEITEM and HIS-TORY tables have been changed.
//           Comment: In some implementations Txn2 will not queue behind Txn1.
       }

   }
   
   
   //****** runtime support *******//
    protected static Logger logger = Logger.getLogger(AcidTestDriver.class);
    HashMap<String, AcidTest> drivers=new HashMap<String, AcidTest>();

    private void checkQueryNames() {
        ArrayList<String> wrongNames=new ArrayList<String>();
        for (String queryName: queryNames) {
            if (queryName.length()!=2) {
                wrongNames.add(queryName);
                continue;
            }

            String id=queryName.substring(0, 1);
            AcidTest driver=drivers.get(id);
            if (driver==null) {
                wrongNames.add(queryName);
                continue;
            }
            
            int num;
            try {
                num = Integer.parseInt(queryName.substring(1));
            } catch (Exception e) {
                wrongNames.add(queryName);
                continue;
            }
            if (num>driver.getTestNum()) {
                wrongNames.add(queryName);
                continue;
            }
            
        }
        
        if (wrongNames.size()>0) {
            StringBuilder sb=new StringBuilder();
            sb.append("Wrong query name");
            if (wrongNames.size()>1) {
                sb.append('s');
            }
            sb.append(": ");
            for (String wrongName: wrongNames) {
                sb.append(wrongName).append(' ');
            }
            sb.append("\n");
            System.err.println(sb.toString());
            System.exit(-1);
        }
    }

    public void run() throws Exception {
        drivers.put("a", new AtomicityTest(this));
        drivers.put("c", new ConsistencyTest(this));
        drivers.put("i", new IsolationTest(this));
        checkQueryNames();

        for (String queryName: queryNames) {
            String id=queryName.substring(0, 1);
            int num=Integer.parseInt(queryName.substring(1));
            AcidTest driver=drivers.get(id);
            String err=driver.runTest(num);
            if (err==null) {
                System.out.println("Test "+queryName+" passed");
            } else {
                System.out.println("Test "+queryName+" failed:"+err);                
            }
        }

    }

    public static void main(String argv[]) throws InterruptedException {
        DOMConfigurator.configureAndWatch("log4j.xml", 60 * 1000);
        AcidTestDriver testDriver = null;
        boolean printST=true; // TODO turn off
        try {
            testDriver = new AcidTestDriver(argv);
            System.out.println("\nStarting test...\n");
            testDriver.run();
        } catch (ExceptionException e) {
            DoubleLogger.getErr().println(e.getMessages());
            if (printST || e.isPrintStack()) {
                 e.getCause().printStackTrace();
            }
        } catch (BadSetupException e) {
            DoubleLogger.getErr().println(e.getMessage());
            if (printST || e.isPrintStack()) {
                 e.printStackTrace();
            }
        } catch (RequestFailedException e) {
            if (e.getMessage()!=null) {
                DoubleLogger.getErr().println("Request failed: ", e.getMessage());              
            }
            if (printST) {
                 e.printStackTrace();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
        }
    }
}
