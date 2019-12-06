package sib.dictionary;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import sib.objects.Location;

// Dictionary of emails contains five top email domains which their popularities and list of 460 free emails

public class EmailDictionary {
	Vector<Double>  vTopEmailCummulative; 
	double 			randomFreeEmailCummulative;
	
	Vector<String> 	vEmail;
	Random 			randEmail; 
	Random 			randIdx; 
	int 			numTopEmail = 5; 
	
	int 			totalNumEmail = 0;
	RandomAccessFile emailDictionary; 
	String 				emailDicFileName; 
	public EmailDictionary(String _emailDicFileName, long seedEmail){
		randEmail = new Random(seedEmail);
		randIdx = new Random(seedEmail);
		emailDicFileName = _emailDicFileName;
	}
	
	public void init(){
		try {
			emailDictionary = new RandomAccessFile(emailDicFileName, "r");
			vTopEmailCummulative = new Vector<Double>();
			vEmail = new Vector<String>();
			
			emailExtract();
			
			emailDictionary.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	public void emailExtract(){
		String emailDomain; 
		double cumdistribution = 0.0;	//cummulative distribution value
		String line; 
		int i = 0; 
		

		try {
			while ((line = emailDictionary.readLine()) != null){
				if (i < numTopEmail){
					String infos[] = line.split(" ");
					emailDomain = infos[0];
					cumdistribution = cumdistribution + Double.parseDouble(infos[1]);
					vEmail.add(emailDomain);
					vTopEmailCummulative.add(cumdistribution);
					i++;
				}
				else 
					vEmail.add(line);
				
				totalNumEmail++;
			}
			
			System.out.println("Done ... " + vEmail.size() + " email domains were extracted");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getRandomEmail(){
		double prob = randEmail.nextDouble();
		
		int idx = 0;
		int minIdx = 0;
		int maxIdx = numTopEmail - 1;
		if (prob > vTopEmailCummulative.get(maxIdx)){
			//Randomly select one email from non-top email
			idx = randIdx.nextInt(totalNumEmail - numTopEmail) + numTopEmail;
			return vEmail.get(idx);
		}
		if (prob < vTopEmailCummulative.get(minIdx)){
			return vEmail.get(minIdx);
		}
		
		while ((maxIdx - minIdx) > 1){
			
			if (prob > vTopEmailCummulative.get(minIdx + (maxIdx - minIdx)/2)){
				minIdx =  minIdx + (maxIdx - minIdx)/2;
			}
			else{
				maxIdx =  minIdx + (maxIdx - minIdx)/2;
			}
		}
		
		return vEmail.get(maxIdx);
	}
	
	/*
	public static void main (String args[]){
		EmailDictionary emailDic = new EmailDictionary("/export/scratch1/duc/work/SIB/workspace/SocialGraph/email.txt", 808080);
		emailDic.init();
		
		for (int i = 0; i < 20; i ++){
			System.out.println(emailDic.getRandomEmail());
		}
	}
	*/
	
	
}
