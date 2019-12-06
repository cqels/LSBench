package sib.util;
/**
 * 
 * @author Minh-Duc Pham
 *
 */

public class ZOrder {

	/**
	 * @param args
	 */
	public int MAX_BIT_NO = 8;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ZOrder zorder = new ZOrder(8); 
		zorder.getZValue(2, 0);
	}
			
	public ZOrder(int maxNumBit){
		this.MAX_BIT_NO = maxNumBit;
	} 
	
	public int getZValue(int x, int y){
		String sX = Integer.toBinaryString(x);
		
		
		int numberToAddX = MAX_BIT_NO - sX.length();
		for (int i = 0; i < numberToAddX; i++){
			sX = "0" + sX;
		}
		
		String sY = Integer.toBinaryString(y);
		
		int numberToAddY = MAX_BIT_NO - sY.length();
		for (int i = 0; i < numberToAddY; i++){
			sY = "0" + sY;
		}		
		
		
		//System.out.println(sX);
		//System.out.println(sY); 
		
		String sZ = ""; 
		for (int i = 0; i < sX.length(); i++){
			sZ = sZ + sX.substring(i, i+1) + "" + sY.substring(i, i+1);
		}
		
		//System.out.println(sZ);
		//System.out.println("The z-value is: " + Integer.parseInt(sZ, 2));
		
		return Integer.parseInt(sZ, 2);
		
	}
}
