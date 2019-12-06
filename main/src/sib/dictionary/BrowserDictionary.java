package sib.dictionary;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.Vector;

public class BrowserDictionary {
	
	Vector<Double>  	vBrowserCummulative; 
	Vector<String> 		vBrowser;

	Random 				randBrowsers; 
	RandomAccessFile 	browserDictionary; 
	String 				browserDicFileName;
	
	int 				totalNumBrowsers;
	
	double 				probAnotherBrowser; 	// Probability that a user uses another browser
	Random				randDifBrowser;			// whether user change to another browser or not
	
	public BrowserDictionary(String _browserDicFileName, long seedBrowser,double _probAnotherBrowser){
		randBrowsers = new Random(seedBrowser);
		randDifBrowser = new Random(seedBrowser);
		browserDicFileName = _browserDicFileName;
		probAnotherBrowser = _probAnotherBrowser;
	}
	
	public void init(){
		try {
			browserDictionary = new RandomAccessFile(browserDicFileName, "r");
			vBrowser = new Vector<String>();
			vBrowserCummulative = new Vector<Double>();
			
			browsersExtract();
			
			browserDictionary.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	public void browsersExtract(){
		String browser; 
		double cumdistribution = 0.0;	//cummulative distribution value
		String line; 
		int i = 0; 
		totalNumBrowsers = 0;

		try {
			while ((line = browserDictionary.readLine()) != null){
				String infos[] = line.split("  ");
				browser = infos[0];
				cumdistribution = cumdistribution + Double.parseDouble(infos[1]);
				vBrowser.add(browser);
				//System.out.println(cumdistribution);
				vBrowserCummulative.add(cumdistribution);
				i++;
				
				totalNumBrowsers++;
			}
			
			System.out.println("Done ... " + vBrowser.size() + " browsers were extracted");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getRandomBrowser(){
		double prob = randBrowsers.nextDouble();

		int minIdx = 0;
		int maxIdx = totalNumBrowsers - 1;

		if (prob < vBrowserCummulative.get(minIdx)){
			return vBrowser.get(minIdx);
		}
		
		while ((maxIdx - minIdx) > 1){
			
			if (prob > vBrowserCummulative.get(minIdx + (maxIdx - minIdx)/2)){
				minIdx =  minIdx + (maxIdx - minIdx)/2;
			}
			else{
				maxIdx =  minIdx + (maxIdx - minIdx)/2;
			}
		}
		
		return vBrowser.get(maxIdx);
	}
	
	public String getBrowserName(byte browserId){
		return vBrowser.get(browserId);
	}
	public byte getRandomBrowserId(){
		double prob = randBrowsers.nextDouble();
		int minIdx = 0;
		int maxIdx = totalNumBrowsers - 1;

		if (prob < vBrowserCummulative.get(minIdx)){
			return (byte)minIdx;
		}
		
		while ((maxIdx - minIdx) > 1){
			
			if (prob > vBrowserCummulative.get(minIdx + (maxIdx - minIdx)/2)){
				minIdx =  minIdx + (maxIdx - minIdx)/2;
			}
			else{
				maxIdx =  minIdx + (maxIdx - minIdx)/2;
			}
		}
		
		return (byte)maxIdx;
	}

	public byte getPostBrowserId(byte userBrowserId){
		double prob = randDifBrowser.nextDouble();
		if (prob < probAnotherBrowser){
			return getRandomBrowserId();
		}
		else{
			return userBrowserId;
		}
	}
	public byte getCommentBrowserId(byte userBrowserId){
		double prob = randDifBrowser.nextDouble();
		if (prob < probAnotherBrowser){
			return getRandomBrowserId();
		}
		else{
			return userBrowserId;
		}
	}	
	
	public String getBrowserForAUser(String originalBrowser){
		double prob = randDifBrowser.nextDouble();
		if (prob < probAnotherBrowser){
			return getRandomBrowser();
		}
		else{
			return originalBrowser;
		}
	}
	
	public Vector<String> getvBrowser() {
		return vBrowser;
	}

	public void setvBrowser(Vector<String> vBrowser) {
		this.vBrowser = vBrowser;
	}

}
