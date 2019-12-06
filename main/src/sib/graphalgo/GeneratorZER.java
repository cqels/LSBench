package sib.graphalgo;
import java.util.Random;

public class GeneratorZER {
		
	
	public static void main(String[] args) {
		long V;			// Number of vertices
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
		long maxE = (V * (V-1)) / 2;
		//System.out.println(" MAX_VALUE = " + Long.MAX_VALUE);
		//System.out.println("Max E = " + maxE );
		long i = -1;
		long k; 			// Number of skipped edges
		double lnP = Math.log(1 - p);
		double lnDelta; 
		
		while (i < maxE){
			pickedNumber = rand.nextDouble();
			
			lnDelta = Math.log(pickedNumber);
			k = Math.max (0, Math.round(Math.ceil(lnDelta/lnP) - 1)); 
				
			i = i + k + 1;
			E++;

		} 
		
		System.out.println("Number of edges: " + E );
	}

}
