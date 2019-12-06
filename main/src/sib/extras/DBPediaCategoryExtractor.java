package sib.extras;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class is used for extracting the category labels from 
 * a dbpedia dataset in form of N-triples.
 * @author duc
 *
 */
public class DBPediaCategoryExtractor {
	public static void main (String args[]){
		DBPediaCategoryExtractor extractor = new DBPediaCategoryExtractor(); 
		extractor.extract();
	}
	public void extract(){
		String strLine;
		String subject, predicate, object;
		String categoryLabel= "";
		String lastCategoryLabel = "";
		String triples[];
		String objecttype = ""; 
		try {
			RandomAccessFile randFile = new RandomAccessFile("/export/scratch1/duc/work/SNBenchMark/dbpedia34/skos_categories_en.nt", "r");
			FileWriter writerCategoryTerms = new FileWriter("/export/scratch1/duc/work/SNBenchMark/dbpedia34/categoriesTermFull.txt");
			FileWriter writerCategories = new FileWriter("/export/scratch1/duc/work/SNBenchMark/dbpedia34/categoryFull.txt");
			int i = 0;
			while ((strLine=randFile.readLine()) != null){
					i++;
					triples = strLine.split(">");
					subject = triples[0];
					categoryLabel = subject.substring(subject.lastIndexOf("/Category:") + 10);
					predicate = triples[1];
					object = triples[2].trim();
					
					if (object.lastIndexOf("/Category:") != -1 ){
						objecttype = object.substring(object.lastIndexOf("/Category:") + 10);
						
						writerCategoryTerms.write(objecttype);
						writerCategoryTerms.write("\n");
						if (categoryLabel.compareTo(lastCategoryLabel) != 0){
							lastCategoryLabel = categoryLabel;
							writerCategories.write(categoryLabel);
							writerCategories.write("\n");
						}
					}

			}
			
			randFile.close();
			writerCategoryTerms.close();
			writerCategories.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			System.out.println("Last Objecttype: " + objecttype);
			System.out.println("Last Category: " + lastCategoryLabel);
			System.out.println("Current Category: " + categoryLabel);
		}
	}
}
