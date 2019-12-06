package sib.graphalgo;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class GeneratorPreAtt {
		
	
	public static void main(String[] args) {
		//long V;			// Number of vertices
		int V;			// Number of vertices
		int d; 			// Minimum degree
		
		if (args.length < 2){
			System.out.println("Atleast three parameters need to be specified ");
			return;
			
		}
		
		// Init
		V = Integer.parseInt(args[0]);
		d = Integer.parseInt(args[1]);

		Random rand = new Random();
		int[] M = new int[2*V*d];
		
		int r;  
		for (int v = 0; v < V; v ++){
			for (int i = 0; i  < d; i ++){
				M[2*(v*d+i)] = v;
				if (v == 0 && i== 0) r = 0;
				else r = rand.nextInt(2*(v*d+i));
				M[2*(v*d+i) + 1] = M[r];
			}
		} 
		
		Set<String> edgeSet = new HashSet<String>();
		
		
		for (int i = 0; i < (V*d - 1);i++){
			//System.out.println("Number of edges: " + E );
			//System.out.println("["+ M[2*i] + "," + M[2*i+1] + "]");
			edgeSet.add("["+ M[2*i] + "," + M[2*i+1] + "]");
		}
		
		System.out.println("Number of distict edges " + edgeSet.size());
		
		writeToOutputFile(edgeSet);
	}
	
	public static void writeToOutputFile(Set edgeSet){
		try{
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream("outPreAtt.txt"));
			Iterator it = edgeSet.iterator();
			while (it.hasNext()){
				out.write(""+ it.next()+"\n");
			}
			out.close();
		}
		catch(IOException e){
			System.out.println("");
		}
		
		
	}

}
