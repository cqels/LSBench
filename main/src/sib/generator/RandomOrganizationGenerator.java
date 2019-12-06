package sib.generator;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.Vector;

public class RandomOrganizationGenerator {
	String dicFileName; 
	Vector<String> vecOrganizations; 		// vector for storing all the organizations URI
	RandomAccessFile dictionary; 
	Random rand; 
	private int organizationIdx; 
	
	public int getOrganizationIdx() {
		return organizationIdx;
	}

	public void setOrganizationIdx(int organizationIdx) {
		this.organizationIdx = organizationIdx;
	}

	public RandomOrganizationGenerator(String fileName){
		rand = new Random(); 
		dicFileName = fileName; 
		init();
	}
	
	public RandomOrganizationGenerator(String fileName, long seed){
		dicFileName = fileName;
		rand = new Random(seed);
		init();
	}
	
	public void init(){
		try {
			dictionary = new RandomAccessFile(dicFileName, "r");
			vecOrganizations = new Vector<String>();
			
			System.out.println("Extracting organizations into a dictionary ");
			extractOrganizations();
			
			dictionary.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void extractOrganizations(){
		String organization; 
		try {
			while ((organization = dictionary.readLine()) != null){
				vecOrganizations.add(organization);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(vecOrganizations.size() + " organizations were extracted");
		
	} 
	
	public String getRandomOrganization(){
		organizationIdx = rand.nextInt(vecOrganizations.size()-1);
		
		return vecOrganizations.elementAt(organizationIdx) ;
	}
	
	public String getCorrelatedOrganization(double probability, int correlatedIdx){

		double randProb = rand.nextDouble();
		if (randProb < probability){
			organizationIdx = correlatedIdx;
			return vecOrganizations.elementAt(organizationIdx) ;
		}
		
		organizationIdx = rand.nextInt(vecOrganizations.size()-1);
		return vecOrganizations.elementAt(organizationIdx) ;
	}	

}
