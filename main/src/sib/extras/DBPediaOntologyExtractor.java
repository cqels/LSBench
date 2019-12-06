package sib.extras;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class is used for extracting the information such as institutions and city names from 
 * a dbpedia dataset in form of N-triples.
 * @author duc
 *
 */
public class DBPediaOntologyExtractor {
	public static void main (String args[]){
		DBPediaOntologyExtractor extractor = new DBPediaOntologyExtractor(); 
		extractor.extract();
	}
	public void extract(){
		String strLine;
		String subject, predicate, object;
		String institutionName; 
		String triples[];
		String objecttype = ""; 
		try {
			//RandomAccessFile randFile = new RandomAccessFile("/export/scratch1/duc/rdfdatasets/dbpedia/instance_types_en_top2000.txt", "r");
			RandomAccessFile randFile = new RandomAccessFile("/export/scratch1/duc/rdfdatasets/dbpedia/instance_types_en.nt", "r");
			//FileWriter writerInstitutions = new FileWriter("/export/scratch1/duc/rdfdatasets/dbpedia/extractedInstitutions.txt");
			FileWriter writerInstitutions = new FileWriter("/export/scratch1/duc/rdfdatasets/dbpedia/extractedCities.txt");
			int i = 0;
			while ((strLine=randFile.readLine()) != null){
					i++;
					triples = strLine.split(">");
					subject = triples[0];
					institutionName = subject.substring(subject.lastIndexOf("/") + 1);
					predicate = triples[1];
					object = triples[2].trim();
					objecttype = object.substring(object.lastIndexOf("/") + 1);

					
					if (objecttype.compareTo("City") == 0){
						writerInstitutions.write(institutionName);
						writerInstitutions.write("\n");
					}
			}
			
			randFile.close();
			writerInstitutions.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
