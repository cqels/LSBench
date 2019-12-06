package sib.testdriver;

import java.util.Locale;

import sib.bibm.AbstractQueryResult;
import sib.bibm.AbstractTestDriver;
import sib.bibm.CompiledQuery;
import sib.bibm.CompiledQueryMix;
import sib.bibm.Query;
import sib.bibm.Exceptions.BadSetupException;
import sib.bibm.Exceptions.ExceptionException;
import sib.bibm.connection.SPARQLConnection;
import sib.bibm.connection.SQLConnection;
import sib.bibm.connection.ServerConnection;
import sib.util.DoubleLogger;


public class ClientThread implements Runnable {
	private ServerConnection conn;
	protected QueryMixStatistics queryMixStat;
	private ClientManager manager;
	private int nr;
	
	ClientThread(ClientManager parent, int clientNr) {
		this.manager = parent;
		this.nr = clientNr;
		AbstractTestDriver driver=parent.driver;
		int timeoutInSeconds = driver.timeout.getValue()/1000;
		if(driver.doSQL.getValue()) {
	        String driverClassName = driver.driverClassName.getValue();
	        String endPoint = driver.getNextEndPoint(Query.SELECT_TYPE);
			conn = new SQLConnection(driverClassName, endPoint, timeoutInSeconds);
		} else {
			conn = new SPARQLConnection(driver);
	   }
        queryMixStat = new QueryMixStatistics();
	}
	
    private void _run() throws InterruptedException {
		for (;;) {
			if (Thread.interrupted()) throw new InterruptedException();
			CompiledQueryMix queryMix =  manager.getNextQueryMix();

			//Either the warmup querymixes or the run querymixes ended
			if (queryMix==null) break;
            int run = queryMix.getRun();
			long startTime = System.nanoTime();
			for (Object nextQ: queryMix.getQueryMix()) {
                CompiledQuery next = (CompiledQuery) nextQ;
                //System.out.println("---------------------");
                //System.out.println(next.getProcessedQueryString());
                //System.out.println("---------------------");
                AbstractQueryResult result = conn.executeQuery(next);
                String qName = next.getName();
                if (result.isTimeout()) {
                    queryMixStat.reportTimeOut(qName);
                } else  if ( result.getQueryMixRun() >= 0) {
                    queryMixStat.setCurrent(qName, result.getResultCount(), result.getTimeInSeconds());
                    manager.addResult(result);
                }
			}
			System.out.println( String.format(Locale.US, "Thread %d: query mix: %d  %.2f ms, total: %.2f ms", nr, run,
					queryMixStat.getQueryMixRuntime()*1000,	(System.nanoTime()-startTime)/(double)1000000) );
			queryMixStat.finishRun();
		} // for
	}

	@Override
    public void run() {
		DoubleLogger logger = DoubleLogger.getErr();
		try {
			_run();
		} catch (InterruptedException e) {
			logger.println("Client Thread ", nr, " interrupted. Quitting...");
		} catch (ExceptionException e) {
			Throwable cause=e.getCause();
			logger.println("Exception in Client Thread ", nr, ":");
			logger.println(e.getMessages());
//			if (e.isPrintStack()) {
			cause.printStackTrace();
//			}
		} catch (BadSetupException e) {
			logger.println(e.toString());
		} finally {
			manager.finishRun(queryMixStat);
		}
	
	}
	
	public void closeConn() {
		conn.close();
	}
}
