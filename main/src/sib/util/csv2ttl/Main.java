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
package sib.util.csv2ttl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;

import sib.csvreader.CsvReader;
import sib.util.Options;
import sib.util.json.JsonList;


public class Main  extends Options {
	private static final int MAX_FILE_BUFFER_SIZE = 64*1024;
	private static final String defaultExt= "csv";

	String ext;
	DBSchema dbSchema;
    ArrayDeque<File>sourceFiles=new ArrayDeque<File>();
	File destDir;
	
	public Main(String[] args) throws Exception {
	    super("Usage: com.openlink.util.csv2ttl.Csv2ttl [options]... [sourcefiles]...",
	            "  sourcefiles can be directories");
	    StringOption extOpt=new StringOption("ext <input file extention> (to search in souce directories)", defaultExt
	            ,"default: '"+defaultExt+"'"
	            );
	    StringOption schema=new StringOption("schema <conversion schema> (json file)", null);
	    StringOption destDirName=new StringOption("d <destination directory>", "."
	            , "default: current working directory");

        super.processProgramParameters(args);

        this.ext=extOpt.getValue();

        String schemaName = schema.getValue();
        if (schemaName==null) {
            System.err.println("No schema provided. Exiting. ");
            usage();            
        }
        try {
            dbSchema=new DBSchema(schemaName);
        } catch (Exception e) {
            System.err.println("Problems loading scema file "+schemaName);
            System.err.println(e.getMessage());
            throw e;
        }

        destDir=new File(destDirName.getValue());
        if (!destDir.exists()) {
            destDir.mkdirs();
            if (!destDir.exists()) {
                System.err.println("Cannot create destination directory: "+destDir.getAbsolutePath());
                usage();                
            }
        }

        ArrayList<File>sourceFiles=new ArrayList<File>();
		for (String arg: super.args) {
				File f=new File(arg);
				if (!f.exists()) {
					System.err.println("file not exists: "+f.getAbsolutePath());
					usage();					
				}
				if (f.isDirectory()) {
					String ext1="."+ext;
					String ext2=ext1+".";
					for (String fn: f.list()) {
						if (!(fn.endsWith(ext1)||fn.contains(ext2))) continue;
						File ff=new File(f, fn);
						if (ff.isDirectory()) continue;
						sourceFiles.add(ff);
					}
				} else {
					sourceFiles.add(f);
				}
		}
		if (sourceFiles.size()==0) {
			System.err.println("No source files. Exiting. ");
			usage();			
		}
		sortSources(sourceFiles.toArray(new File[sourceFiles.size()]));
	}

    private void usage() {
        printUsageInfos();
        System.exit(-1);
    }

	private void sortSources(File[] files) {
		Comparator<File> comparat=new Comparator<File>(){

			@Override
			public int compare(File o1, File o2) {
				long size1 = o1.length();
				long size2 = o2.length();
				return size1>size2?+1:(size1<size2?-1:0);
			}
			
		};
//		Arrays.sort(files, comparat);  - no improvement, to be investigated
		for (File file: files) {
			sourceFiles.add(file);
		}
	}

	synchronized File getNextTask() {
		File f = sourceFiles.poll();
		return f;
	}

	int maxFailureCount = 10;
	ArrayList<String> failures = new ArrayList<String>();

	synchronized boolean reportFailure(String msg) {
		failures.add(msg);
		return failures.size()>=maxFailureCount;
	}
	
	void checkFailures() {
		if (failures.size()==0) return;
		System.err.println("Some file convertions failed:");
		for (int k=0; k<failures.size(); k++) {
			System.err.println(failures.get(k));
		}
	}

	void runAll() throws IOException {
		int availableProcessors = Runtime.getRuntime().availableProcessors();
//		int availableProcessors =1;
		int nThreads=Math.min(availableProcessors, sourceFiles.size());
		Thread[] threads=new Thread[nThreads];
		for (int k=0; k<nThreads; k++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					for (;;) {
						File f = getNextTask();
						if (f==null) return;
						try {
							convertFile(f);
						} catch (IOException e) {
							if (reportFailure(e.getMessage())) {
								return;
							}
						}
					}
				}
			});
			threads[k]=thread;
			thread.start();
		}
		try {
			for (int k=0; k<nThreads; k++) {
				threads[k].join();
			}
		} catch (InterruptedException e) {
			for (int k=0; k<nThreads; k++) {
				threads[k].interrupt();
			}
		}
	}

	void convertFile(File src) throws IOException {
		// create dest file name: dir/table.ext.n => table.n.ttl
		String destFileName=src.getName();
		String[] parts=destFileName.split("\\.");
		if (parts.length<2 || !parts[1].equals(ext)) {
			// not an error, probably we are passed something like"data/*" 
			// System.err.println("Source file name, skipped: "+destFileName);				
			System.out.println("file "+src+" is not a ."+ext);
			return;
		}
		String tableName=parts[0];
		String fileNumber=(parts.length==2)? "": "."+parts[2];
        CsvTableSchema tableSchema=dbSchema.tables.get(tableName);
        if (tableSchema==null) {
            System.out.println("no schema for "+ext+" file:"+src);
            return;
        }

        System.out.println("start converting file "+src+"...");
        destFileName=tableName+fileNumber+".ttl";
		File dest=new File(destDir, destFileName);
		BufferedReader freader = new BufferedReader(new FileReader(src)	, MAX_FILE_BUFFER_SIZE);
		CsvReader reader=new CsvReader(freader, '|');
		JsonList<String> headers=dbSchema.header;
		String tag=dbSchema.default_tag;
		JsonList<CsvField> fields = tableSchema.getFields();
		BufferedWriter writer=new BufferedWriter(new FileWriter(dest), MAX_FILE_BUFFER_SIZE);
		for (String header: headers) {
		    if (header.startsWith("\"") || header.startsWith("\'")) {
		         writer.write(header, 1,  header.length()-2);
		    } else {
                writer.write(header);
		    }
		    writer.write("\n\n");
		}
		int recN=0;
		int errCount=0;
		while (reader.readRecord()) {
			recN++;
			String[] values = reader.getValues();
			if (values.length<fields.size()) {
				if (errCount++ <10) {
					System.err.println("bad record "+recN+", skipped");
				} else {
					System.err.println("bad record "+recN+", exiting.");
					break;
				}
			} // else it may contain excessive fields, so use fields.length
			
			// construct subject:
			StringBuilder sb=new StringBuilder();
			sb.append(tableSchema.getName());
            int[] keyIdndexes = tableSchema.getKeyIdndexes();
			for (int i=0; i<keyIdndexes.length; i++) {
                String key=values[keyIdndexes[i]];
				sb.append("_").append(key);
			}
			sb.append('\n');
			
			// construct predefined property "a"
			sb.append("    a ").append(tag).append(':').append(tableName).append(" ;\n");

			// construct property+subject
			for (int k=0; k<fields.size(); k++) {
				String lastChar=((k+1)<fields.size())?" ;\n":" .\n";
				CsvField field = fields.get(k);
				if (field==null) {
					throw new RuntimeException("table: '"+tableName+"' field "+k+" is null");
				}
				if (!field.prop) {
					continue; // not a property
				}
				String value=values[k];
				if (field.refto!=null) { // TODO: this works only for simple foreign keys
					value=field.refto+'_'+value;
                } else  if (field.type==DataType.STR) {
                    value='\"'+value+'\"';
                } else  if (field.type==DataType.REAL || field.type==DataType.DOUBLE) {
                    // make sure string representation contain exponent, to be distinguished from decimal
                    if (!value.contains("E") && !value.contains("e")) {
                        value=value+"e0";
                    }
				} else	if (field.type==DataType.DATE) {
					value='\"'+value+"\"^^xsd:dateTime";
				}
				sb.append("    ").append(tag).append(':').append(field.name)
				   .append(" ").append(value).append(lastChar);
			}
			
			writer.write(sb.toString());
			
		}
		writer.close();
		System.out.println("  ... file "+src+" converted");
	}
	
    public static void main(String[] args) throws Exception {
		Main csv2ttl;
        try {
            csv2ttl = new Main(args);
        } catch (Exception e) {
            return;
        }
		csv2ttl.runAll();
		csv2ttl.checkFailures();
	}
}
