package sib.experiment;

public class AdjGraph {

	   private boolean [][] edges;  // adjacent matrix
	   private Object [] labels;

	   public AdjGraph (int n) {
	      edges = new boolean [n][n];
	      labels = new Object[n];
	   }


	   public int size() { return labels.length; }

	   public void   setLabel (int vertex, Object label) { labels[vertex]=label; }
	   public Object getLabel (int vertex)               { return labels[vertex]; }

	   public void    addEdge    (int source, int target)  { edges[source][target] = true; }
	   public boolean isEdge     (int source, int target)  { return edges[source][target]; }
	   public void    removeEdge (int source, int target)  { edges[source][target] = false; }

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

	   public void print () {
	      for (int j=0; j<edges.length; j++) {
		 System.out.print (labels[j]+": ");
		 for (int i=0; i<edges[j].length; i++) {
		    if (edges[j][i]) System.out.print (labels[i]+" ");
		 }
		 System.out.println ();
	      }
	   }

	   public void depthFirstPrintRecurse (int start) {
	      boolean [] marked = new boolean [size()]; // all false;
	      depthFirstRecurse (start, marked);
	   }

	   public void depthFirstRecurse (int v, boolean[] marked) {
	      marked[v]=true;
	      System.out.println (getLabel(v));

	      final int [] connections = neighbors(v);
	      for (int i=0; i<connections.length; i++) {
		 final int n = connections[i];
		 if (!marked[n]) depthFirstRecurse (n, marked);
	      }
	   }

	   public void breadthFirstPrint (final int start) {
	      final boolean [] marked = new boolean [size()]; // all false;
	      final IntQueue q = new IntQueue ();
	      
	      System.out.print ("BF traversal: ");
	      q.addRear (start);
	      while (!q.isEmpty()) {
	         final int v = q.removeFront ();
		 marked[v]=true;
		 System.out.print (getLabel(v)+" ");
		 
		 final int [] connections = neighbors(v);
		 for (int i=0; i<connections.length; i++) {
		    final int n = connections[i];
		    if (!marked[n]) q.addRear (n);
	         }
	      }
	      System.out.println ();
	   }

	   public void depthFirstPrint (final int start) {
	      final boolean [] marked = new boolean [size()]; // all false;
	      final IntStack st = new IntStack ();
	      
	      System.out.print ("DF traversal: ");
	      st.addFront (start);
	      while (!st.isEmpty()) {
	         final int v = st.removeFront ();
		 marked[v]=true;
		 System.out.print (getLabel(v)+" ");
		 
		 final int [] connections = neighbors(v);
		 for (int i=connections.length-1; i>=0; i--) {
		 final int n = connections[i];
		    if (!marked[n]) st.addFront (n);
	         }
	      }
	      System.out.println ();
	   }

	   public static void main (String args[]) {
	      final AdjGraph t = new AdjGraph (7);
	      t.setLabel (0, "v0");
	      t.setLabel (1, "v1");
	      t.setLabel (2, "v2");
	      t.setLabel (3, "v3");
	      t.setLabel (4, "v4");
	      t.setLabel (5, "v5");
	      t.setLabel (6, "v6");
	      t.addEdge (0,1);
	      t.addEdge (0,4);
	      t.addEdge (1,3);
	      t.addEdge (2,0);
	      t.addEdge (3,0);
	      t.addEdge (3,6);
	      t.addEdge (3,5);
	      t.addEdge (6,1);
	      t.print();

	      t.depthFirstPrint (0);
	      t.breadthFirstPrint (0);
	      t.depthFirstPrintRecurse (0);
	   }
	}
	   

