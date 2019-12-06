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
package sib.util.csvLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

import sib.bibm.Exceptions.BadSetupException;
import sib.bibm.Exceptions.ExceptionException;
import sib.csvreader.CsvReader;
import sib.util.csv2ttl.CsvField;
import sib.util.csv2ttl.CsvTableSchema;
import sib.util.json.JsonList;


public class Worker implements Runnable {
	static final int MAX_FILE_BUFFER_SIZE = 64*1024;

	CsvLoader csvLoader;
	protected Connection conn;
	HashMap<String, Integer> paramCounts=new HashMap<String, Integer>(); 
	HashMap<String, PreparedStatement> statements=new HashMap<String, PreparedStatement>(); 
	
	public Worker(CsvLoader csvLoader) {
		this.csvLoader=csvLoader;
		try {
			conn = DriverManager.getConnection(csvLoader.endPoint);
		} catch (SQLException e0) {
			SQLException e=e0;
			while(e!=null) {
				e.printStackTrace();
				e=e.getNextException();
			}
			throw new ExceptionException("SQLConnection()", e0);
		}
	}

    private int getParamCount(String tableName) {
        CsvTableSchema tableSchema=csvLoader.dbSchema.getTables().get(tableName);
        JsonList<CsvField> fields = tableSchema.getFields();
        return fields.size();
    }

	private String createUpdateString(CsvTableSchema tableSchema) {
		StringBuilder sb=new StringBuilder();
		sb.append("insert into ").append(tableSchema.getName()).append("(");
		boolean first=true;
		JsonList<CsvField> fields = tableSchema.getFields();
        for (CsvField field: fields) {
            if (first) {
                first=false;
            } else {
                sb.append(", ");
            }
            sb.append("\n").append(field.getName());
        }
        sb.append(")\n").append("values (");
        first=true;
        for (int k=0; k<fields.size()-1; k++) {
            sb.append("?, ");
        }
		sb.append("?)\n");
	    return sb.toString();
	}

	/*
	private PreparedStatement getStatement(String tableName) throws SQLException {
		PreparedStatement res=statements.get(tableName);
		if (res!=null) {
			return res;
		}
	     res = createPreparedStatement(tableName);
//		res.setQueryTimeout(timeoutInSeconds);
		statements.put(tableName, res);
		return res;
	}
*/

	private PreparedStatement createPreparedStatement(CsvTableSchema tableSchema) throws SQLException {
         String updateString=createUpdateString(tableSchema);
         PreparedStatement res=null;
        try {
            res = conn.prepareStatement(updateString);
            return res;
        } catch (SQLException e) {
//            System.err.println(e.toString());
            System.err.println(updateString);
            throw e;
        }
    }

	void loadFile(File src) throws IOException, SQLException {
		System.out.println("start loading file "+src+"...");
        String fileName=src.getName();
		String[] parts=fileName.split("\\.");
		if (parts.length<2 || !parts[1].equals(csvLoader.ext)) {
			// not an error, probably we are passed something like"data/*" 
			// System.err.println("Source file name, skipped: "+destFileName);				
			System.out.println("  ... file "+src+" is not a ."+csvLoader.ext);
			return;
		}
		String tableName=parts[0];
        CsvTableSchema tableSchema=csvLoader.dbSchema.getTables().get(tableName);
        if (tableSchema==null) {
            System.out.println("no schema for :"+tableName);
            return;
        }
        String updateString=createUpdateString(tableSchema);
//        System.out.println(updateString);
		BufferedReader freader = new BufferedReader(new FileReader(src)	, MAX_FILE_BUFFER_SIZE);
		CsvReader reader=new CsvReader(freader, '|');
		PreparedStatement stmt=createPreparedStatement(tableSchema) ;
        int paramCount=getParamCount(tableName);
        int batchSize=2000;
        int recN=0;
        boolean eof=false;
        JsonList<CsvField> fields = tableSchema.getFields();
        for ( ; ; ) {
//          stmt=createPreparedStatement(tableName) ; // workaround: jdbc bug arrayindexoutofbounds
            int n;
            String[] values=null;
            for (n=0; n < batchSize; n++) {
                if (!reader.readRecord()) {
                    eof=true;
                    break;
                }
                recN++;
                values = reader.getValues();
                if (values.length < paramCount) {
                    throw new BadSetupException("in file:" + src + ", record:" + recN + " has " + values.length + " columns; " + paramCount + " requred");
                }
                for (int k = 0; k < paramCount; k++) {
                    String value = values[k];
                    CsvField field=fields.get(k);
                    switch (field.getType()) {
                    case INT:
                        stmt.setLong(k+1, Long.parseLong(value));
                        break;
                    case DECIMAL:
                    case REAL:
                      stmt.setFloat(k+1, Float.parseFloat(value));
                      break;
                    case DOUBLE:
                      stmt.setDouble(k+1, Double.parseDouble(value));
                      break;
                    case DATE: 
                        stmt.setDate(k+1, Date.valueOf(value));
                        break;
                    case STR:
                        stmt.setString(k+1, value);
                        break;
                    default:
                        throw new BadSetupException("unknown field type:"+field.getType());
                    }
                }
                stmt.addBatch();
            }
            if (n==0) {
                break;
            }
            try {
                stmt.executeBatch();
 //               System.out.println("  executeUpdate passed for "+tableName+" rec:"+recN);
            } catch (SQLException e) {
                System.err.println("  executeUpdate failed for: "+src.getAbsolutePath()+"; rec="+recN);
                System.err.print("values=[");
                for (int k = 0; k < paramCount; k++) {
                    System.err.print(values[k]);
                    System.err.print(" ,");
                }
                System.err.println("]");
                throw e;
            }
            if (eof) {
                break;
            }
		}
        stmt.close();
		System.out.println("  ... file "+src+": "+recN+" loaded");
	}

    @Override
	public void run() {
		for (int k=0; ; k++) {
			File f = csvLoader.getNextTask();
			if (f==null) {
			    System.out.println("Worker exiting after "+k+" loadings.");
			    return;
			}
			try {
				loadFile(f);
			} catch (Exception e) {
				if (csvLoader.reportFailure(f.getAbsolutePath(), e)) {
					return;
				}
			}
		}
	}

}
