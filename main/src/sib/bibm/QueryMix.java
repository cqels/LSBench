package sib.bibm;

import static sib.util.FileUtil.file2string;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import sib.bibm.Exceptions.BadSetupException;
import sib.bibm.Exceptions.ExceptionException;
import sib.util.Util;


public class QueryMix {
	protected HashMap<String, Query> queries;
	protected String[] queryNames;
    private String prefix =Query.QUERY_PREFIX;
    private String qSuffix=Query.QUERY_SUFFIX;
    private String qDescSuffix=Query.QUERY_DESC_SUFFIX;
	
    public QueryMix(File queryDir) throws IOException {
        if (!queryDir.exists()) {
            throw new BadSetupException("query directory "+queryDir.getPath()+" does not exists.");
        }
        
        queryNames = getQueryMixInfo(queryDir);		//PMDUC: queryNames will be "1", "2", "3", ...     
    }
    
	public QueryMix(AbstractParameterPool parameterPool, File queryDir) throws IOException {
	    this(queryDir);
        File descDir=new File(queryDir, "querydescriptions");
        lookForDescs: {
            if (descDir.exists()) {
                if (descDir.isDirectory()) {
                    break lookForDescs; // first alternative: directory inside the query dir
                }
                try {
                    String anotherName = file2string(descDir);
                    anotherName=anotherName.trim();
                    File descDir2=anotherName.startsWith(File.pathSeparator)?
                            new File(anotherName): new File(queryDir, anotherName);
                    if (descDir2.exists() && descDir2.isDirectory()) {
                        descDir=descDir2;
                        break lookForDescs; // second alternative: directory which name is stored in the file
                    } else {
                        System.err.println("Could not read dir: "+descDir2.getAbsolutePath());
                    }
                } catch (Exception e) {
                    System.err.println("Could not read: "+descDir.getAbsolutePath());
                }
            }
            File descDir3=new File(queryDir.getAbsoluteFile().getParent(), "querydescriptions");
            if (descDir3.exists() && descDir3.isDirectory()) {
                descDir=descDir3;
                break lookForDescs; // third alternative: directory outside the query dir
            }
            descDir=null; // no alternative worked
        }
        if (descDir!=null) {
            System.out.println("Using descriptions directory:"+descDir.getAbsolutePath());
        }
        
        queries = new HashMap<String, Query>();
        
        for (String qName: queryNames) {
            Query q=queries.get(qName);
            if (q==null) {
                q= new Query(qName, parameterPool, queryDir, descDir);
                q.setQueryMix(this);
                queries.put(qName, q);
            }
        }
    }
        
	private String[] getQueryMixInfo(File queryDir) {
        File ignoreFile = new File(queryDir, "ignoreQueries.txt");
        Set<String> ignoreQueries = getIgnoreQueryInfo(ignoreFile);
		File queryMixFile = new File(queryDir,  "querymix.txt");
		ArrayList<String> qm=new ArrayList<String>(); 

		if (queryMixFile.exists()) {
	        System.out.println("Reading query mix file: " + queryMixFile);
            StringTokenizer st = new StringTokenizer(file2string(queryMixFile));
            while (st.hasMoreTokens()) {
                String qName = st.nextToken();
                if (ignoreQueries.contains(qName)) continue;
                qm.add(qName);
            }
            return qm.toArray(new String[qm.size()]);
		} else {
		    // just find all query files
			String[] list = queryDir.list();
			for (String fn: list) {
				try {
                    if (fn.endsWith(qDescSuffix)) continue;
                    if (!fn.endsWith(qSuffix)) continue;
                    if (!fn.startsWith(prefix)) continue;
					String qName=fn.substring(prefix.length(), fn.lastIndexOf(qSuffix));
                    if (ignoreQueries.contains(qName)) continue;
					qm.add(qName);
				} catch (NumberFormatException e) {
				       //ok, just not a query file
				}
			}
	        String[] sortedNames = qm.toArray(new String[qm.size()]);
            Arrays.sort(sortedNames, new Util.NumLexComparator());
            return sortedNames;
		}
	}

	private Set<String> getIgnoreQueryInfo(File file) {
		Set<String> ignoreQueries=new TreeSet<String>(); 
		if (!file.exists()) {
			return ignoreQueries;
		}
		System.out.println("Reading query ignore file: " + file);
		try {
			addFromFile(file, ignoreQueries);
		} catch (IOException e) {
			throw new ExceptionException("Error processing query ignore file: " + file, e);
		}
		return ignoreQueries;
	}

	private void addFromFile(File file, Collection<String> coll) 	throws FileNotFoundException, IOException {
        StringTokenizer st = new StringTokenizer(file2string(file));
		while (st.hasMoreTokens()) {
			coll.add(st.nextToken());
		}
	}

	public void append(QueryMix another) {
		HashMap<String,Query> anotherQueries=another.queries;
		ArrayList<String> newQueryMix=new ArrayList<String>();
		for (String qName:queryNames) {
		    newQueryMix.add(qName);
		}

		for (Query query: anotherQueries.values()) {
		    String qName=query.getName();
		    if (queries.get(qName)==null) {
		        queries.put(qName, query);
	            newQueryMix.add(qName);
		    } else {
		        // create new unique name
		        for (int k=1; ; k++) {
	                String newName=qName+'_'+k;
	                if (queries.get(newName)==null) {
	                    query.setName(newName);
	                    queries.put(newName, query);
	                    newQueryMix.add(newName);
	                    break;
	                }
		        }
		    }
		}
		queryNames=newQueryMix.toArray(new String[newQueryMix.size()]);
	}

    public HashMap<String, Query> getQueries() {
        return queries;
    }
    
    public String[] getQueryNames() {
        return queryNames;
    }
    
}
