package sib.bibm.connection;

import sib.bibm.AbstractQueryResult;
import sib.bibm.CompiledQuery;

public interface ServerConnection {
	/*
	 * Execute Query with Query Object
	 */
	
	public AbstractQueryResult executeQuery(CompiledQuery query) throws InterruptedException;

	
	public void close();
}

