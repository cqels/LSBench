package sib.generator;

import java.util.Random;

import umontreal.iro.lecuyer.probdist.PowerDist;

import java.util.Arrays;

public class PowerDistGenerator {
	private PowerDist powerDist; 
	private Random rand; 
	double a; 
	double b; 
	public PowerDistGenerator(double a, double b, double alpha, long seed){
		this.a = a;
		this.b = b;
		//powerDist = new PowerDist(alpha);
		powerDist = new PowerDist(a, b, alpha);
		rand = new Random(seed);
	}
	public int getValue(){
		double randVal = powerDist.inverseF(rand.nextDouble());
		//return (int)(a + (b - a) * randVal);
		return (int)randVal;
	}
	
	public double getDouble(){
		return powerDist.inverseF(rand.nextDouble());
	}

	public static void main(String args[]){
		PowerDistGenerator pdg = new PowerDistGenerator(5.0, 50.0, 0.8
				, 80808080);
		int[] arr = new int[400];
		for (int i = 0; i < 400; i ++){
			//System.out.println(pdg.getValue() + " ");
			 arr[i] = pdg.getValue();
		}
		Arrays.sort(arr);
		System.out.println(Arrays.toString(arr));
		
		int j = 0;
		int lastvalue = -1;
		for (int i = 0; i < 400; i ++){
			if (lastvalue != arr[i]){
				System.out.println(lastvalue + " :   " + j);
				lastvalue = arr[i];
				j = 0; 
			}
			j ++;
			
		}
	}
}
