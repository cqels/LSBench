package sib.graphalgo;
import java.util.HashMap;
import java.util.Random;

public class GeneratorSocialGraph {
		
	
	public static void main(String[] args) {
		int V;			// Number of vertices
		double p; 		// Probability that an edge is added
		int E;
		
		if (args.length < 2){
			System.out.println("Atleast two parameters need to be specified ");
			return;
			
		}
		
		V = Integer.parseInt(args[0]);
		E = 0; 
		p = Double.parseDouble(args[1]);		
		Random rand = new Random(); 
		
		Double pickedNumber; 
		for (int i = 0; i < V; i ++){
			for (int j = i; j  < V; j ++){
				pickedNumber = rand.nextDouble();
				if (pickedNumber.doubleValue() < p){
					//System.out.println("Add edge [" + i+" , "+j  +"]");
					E++;
				}
				//System.out.println("The next random number is: " + pickedNumber);
			
			}
		} 
		
		System.out.println("Number of edges: " + E );	
	}

}
