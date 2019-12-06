package sib.graphalgo;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class GeneratorSmallWorld {
		
	
	public static void main(String[] args) {
		//long V;			// Number of vertices
		int V;				// Number of vertices
		double p; 			// Probability that an edge is added
		int d; 				// Degree parameter
		
		if (args.length < 3){
			System.out.println("Atleast three parameters need to be specified ");
			return;
			
		}
		
		// Init
		V = Integer.parseInt(args[0]);
		p = Double.parseDouble(args[1]);	
		d = Integer.parseInt(args[2]);

		Random rand = new Random(); 
		Writer writer = new Writer("outEdges.txt", V);
		
		Double pickedNumber; 
		//long maxE = (V * (V-1)) / 2;
		int maxE = (V * (V-1)) / 2;

		double lnP = Math.log(1 - p);
		double lnDelta; 
		
		HashMap<String, Integer> arrReplace = new HashMap<String, Integer>();		
		int m = 0;
		int j; 
		
		long k; 			// Number of skipped edges
		Set<String> E = new HashSet<String>();
	
		pickedNumber = rand.nextDouble();
		lnDelta = Math.log(1-pickedNumber);
		k = Math.round(Math.floor(lnDelta/lnP)); 

		for (int v = 1; v <= V; v++){
			for (int i = 1; i <= d; i ++){
				if (k > 0){
					j = v*(v-1)/2 + (v + i % V);
					if (E.contains("e"+j)) 	System.out.println("Already contained at v = "+ v +" i = "+ i +" and j = "+ j );
					E.add("e"+j);
					writer.writeEdge(j);
					k = k - 1; 
					m = m + 1; 
					if (!E.contains("e"+m)){
						arrReplace.put("e"+j, new Integer(m)); 
					}
					else
						arrReplace.put("e"+j, arrReplace.get("e"+m));
				}
				else{
					pickedNumber = rand.nextDouble();
					lnDelta = Math.log(1-pickedNumber);
					k = Math.round(Math.floor(lnDelta/lnP)); 
				}
					
			}
		}
		
		int pos;
		Random randInt = new Random();
		int edgeIdx;
		System.out.println("Number of fixed edges: " + m);
		for (int i = (m+1); i <= (V*d); i++){
			pos = randInt.nextInt(maxE-i) + i;
			if (!E.contains("e"+pos)){
				E.add("e"+pos);
				writer.writeEdge(pos);
			}
			else{
				edgeIdx = ((Integer) arrReplace.get("e"+pos)).intValue(); 
				E.add("e"+edgeIdx);
				writer.writeEdge(edgeIdx);
			}
			
			if (!E.contains("e"+i))	
				arrReplace.put("e"+pos, new Integer(i)); 
			else
				arrReplace.put("e"+pos, arrReplace.get("e"+i));
		}		
		
		writer.close();
		System.out.println("Number of edges: " + E.size());
		
		Iterator itr = E.iterator();

		while(itr.hasNext()) {
			System.err.println(itr.next() + "");
		}
 
	}

}

class Writer{
	private OutputStreamWriter out;
	private int V; 
	public Writer(String filename, int V){
		try{
			out = new OutputStreamWriter(new FileOutputStream(filename));
			this.V = V; 
		}
		catch (IOException e){
			
		}
	}
	public void writeEdge(int idx){
		long u = Math.round(Math.sqrt(2*idx));
		long v = idx - (u-1)*u/2; 
		try{
			out.write("[" + u +","+ v + "]");
			//out.write(""+idx);
			out.write("\n");
		}
		catch(IOException e){
			
		}
		
	}
	public void close(){
		try{
			out.close();
		}
		catch(IOException e){
			
		}
	}
}

/*
class Edge{
	private int idx; 
	
	public Edge(int _idx){
		this.idx = _idx; 
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}
}
*/

/*
 public class GeneratorSmallWorld {
		
	
	public static void main(String[] args) {
		//long V;			// Number of vertices
		int V;			// Number of vertices
		double p; 		// Probability that an edge is added

		int d; 			// Degree parameter
		
		if (args.length < 3){
			System.out.println("Atleast three parameters need to be specified ");
			return;
			
		}
		
		
		// Init
		V = Integer.parseInt(args[0]);
		p = Double.parseDouble(args[1]);	
		d = Integer.parseInt(args[2]);

		Random rand = new Random(); 
		Writer writer = new Writer("outEdges.txt", V);
		
		Double pickedNumber; 
		//long maxE = (V * (V-1)) / 2;
		int maxE = (V * (V-1)) / 2;

		double lnP = Math.log(1 - p);
		double lnDelta; 
		
		HashMap<Edge, Integer> arrReplace = new HashMap<Edge, Integer>();		
		int m = 0;
		int j; 
		
		long k; 			// Number of skipped edges
		Set<Edge> E = new HashSet<Edge>();
	
		pickedNumber = rand.nextDouble();
		lnDelta = Math.log(1-pickedNumber);
		k = Math.max (0, Math.round(Math.floor(lnDelta/lnP))); 

		for (int v = 0; v < V; v++){
			for (int i = 1; i <= d; i ++){
				if (k > 0){
					j = v*(v-1)/2 + (v + i) % V;
					E.add(new Edge(j));
					writer.writeEdge(j);
					k = k - 1; 
					m = m + 1; 
					if (!E.contains(new Edge(m))){
						arrReplace.put(new Edge(j), new Integer(m)); 
					}
					else
						arrReplace.put(new Edge(j), arrReplace.get(new Edge(m)));
				}
				else{
					pickedNumber = rand.nextDouble();
					lnDelta = Math.log(1-pickedNumber);
					k = Math.max (0, Math.round(Math.floor(lnDelta/lnP))); 
				}
					
			}
		}
		
		int pos;
		Random randInt = new Random();
		int edgeIdx;
		for (int i = (m+1); i <= (V*d); i++){
			pos = randInt.nextInt(maxE);
			if (!E.contains(new Edge(pos))){
				E.add(new Edge(pos));
				writer.writeEdge(pos);
			}
			else{
				edgeIdx = ((Integer) arrReplace.get(new Edge(pos))).intValue(); 
				E.add(new Edge(edgeIdx));
				writer.writeEdge(edgeIdx);
			}
			
			if (!E.contains(i))	
				arrReplace.put(new Edge(pos), new Integer(i)); 
			else
				arrReplace.put(new Edge(pos), arrReplace.get(new Edge(i)));
		}		
		
		writer.close();
		System.out.println("Number of edges: " + E.size());
	}

}
*/


