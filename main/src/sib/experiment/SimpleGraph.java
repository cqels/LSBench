package sib.experiment;

import java.util.ArrayList;

public class SimpleGraph {
	
	   private boolean [][]  edges;  // adjacency matrix
	   private int numVertex; 
	   public int size() { return numVertex; }
	   public SimpleGraph(int numV){
		   this.numVertex = numV;
		   edges = new boolean[numV][numV];
	   }
	   
	   public SimpleGraph(int numV, ArrayList<Integer>[] arrEdges){
		   this.numVertex = numV;
		   edges = new boolean[numV][numV];
		   
		   for (int i = 0; i < numV; i ++){
			   for (int j = 0; j < arrEdges[i].size(); j++){
				   edges[i][arrEdges[i].get(j)] = true; 
			   }
		   }
	   }
	   
	   public int [] neighbors (int vertex) {
		   int count = 0;
		   for (int i=0; i<edges[vertex].length; i++) {
			   if (edges[vertex][i]) count++;
		   }
		   final int[]answer= new int[count];
		   count = 0;
		
		   for (int i=0; i<edges[vertex].length; i++) {
			   if (edges[vertex][i]) answer[count++]=i;
		   }
		   return answer;
	   }
}
