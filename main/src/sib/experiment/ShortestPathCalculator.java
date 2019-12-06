package sib.experiment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class ShortestPathCalculator {
	byte pathLength[][];
	ArrayList<Integer>[] arrEdges;
	public void getAvg(int numV, ArrayList<Integer>[] _arrEdges){
		pathLength = new byte[numV][numV];
		this.arrEdges = _arrEdges;
		for (int i = 0; i < numV; i ++){
			for (int j = 0; j < numV; j ++){
				pathLength[i][j] = -1;
			}
		}
		
		byte depth;
	
		byte min_of_max = 100;
		for (int i = 0; i < numV; i ++){
			//dfs((byte)0,i,i);
			
			byte max = bfs(i);
			if (min_of_max > max) min_of_max = max; 
		}
		
		System.out.println("Min of max = " + min_of_max);
		
		
		// Find the diameter and radius
		long sum = 0;
		byte max = 0; 
		int start = -1;
		int end = -1;
		
		byte radius = 100; 
		int start_r = -1; 
		int end_r = -1; 
		
		for (int i = 0; i < numV; i ++){
			byte max_i = 0;
			int end_i = -1; 
			for (int j = 0; j < numV; j ++){
				if (j == i) continue;
				if (max < pathLength[i][j]){ 
					max = pathLength[i][j];
					start = i; 
					end = j; 
				}
				if (pathLength[i][j] == -1){
					//System.out.println("ERROR HERE ( "+i+" , "+j+" )");
					pathLength[i][j] = 0;
					pathLength[j][i] = 0;
					//System.exit(-1);
				}
				if (max_i < pathLength[i][j]){
					max_i = pathLength[i][j];
					end_i = j; 
				}
				sum = (long)(sum + (long)pathLength[i][j]);
			}
			
			if (radius > max_i && max_i != 0){
				radius = max_i;
				start_r = i; 
				end_r = end_i; 
			}
			
		}		

		System.out.println("Diameter of the social graph = " + max + "  from ( " + start + " , " + end + " )" );
		System.out.println("Sum all path length = " + sum  );
		System.out.println("Average path length = " + (double)sum/(numV*(numV-1))  );
		
		for (int i = 0; i < numV; i ++){
			byte max_i = 0;
			int end_i = -1; 
			for (int j = 0; j < numV; j ++){
				if (max_i < pathLength[i][j]){
					max_i = pathLength[i][j];
					end_i = j; 
				}
			}
			
			if (radius > max_i && max_i != 0){
				radius = max_i;
				start_r = i; 
				end_r = end_i; 
			}
			
		}
		
		System.out.println("Radius of the social graph = " + radius + "  from ( " + start_r + " , " + end_r + " )" );
		System.out.println("Degree of the starting node is " + arrEdges[start_r].size());

	}
	
	public void dfs(byte depth, int root,int current){
		if (pathLength[root][current] != -1 && pathLength[root][current] <= depth) return;
		pathLength[root][current] = depth;
		pathLength[current][root] = depth;
		depth++;
		for (int j = 0; j < arrEdges[current].size(); j++){
			dfs(depth,root, arrEdges[current].get(j));
		}		
	}
	
	public byte bfs(int root){
		byte max = 0; 
		
		Queue<Integer> queuVertice = new LinkedList<Integer>();
		
		queuVertice.add(root);
		pathLength[root][root] = 0;
		while (!queuVertice.isEmpty()){
			int current = queuVertice.poll();
			for (int j = 0; j < arrEdges[current].size(); j++){
				int checkV = arrEdges[current].get(j);
				if (pathLength[root][checkV] == -1){
					pathLength[root][checkV] = (byte)(pathLength[root][current] + 1);
					
					if (pathLength[root][checkV] > max) max = pathLength[root][checkV];
					
					queuVertice.add(checkV);
				}
			}
		}
		
		return max;
		
	}
   
}
