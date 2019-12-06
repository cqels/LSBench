package sib.experiment;

import java.util.Collection;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class Graph {

   HashMap vertices = new HashMap ();
   //    TreeMap edges    = new TreeMap ();

   class Vertex {
      List in = new ArrayList (), out = new ArrayList ();
   }
   
   class Edge {
      Vertex source, destination;
      Object weight;
      Edge (Vertex s, Vertex d, Object w) {
	 source=s; destination=d; weight=w;
      }
   }

   public void add (Object vertex_label) {
      if (!vertices.containsKey (vertex_label)) {
	 vertices.put (vertex_label, new Vertex());
      }
   }

   private Vertex get (Object vertex_label) {
      Object value;
      if (vertices.containsKey (vertex_label)) {
	 value = vertices.get (vertex_label);
      } else {
	 value = vertices.put (vertex_label, new Vertex());
      }
      return (Vertex) value;
   }
	 
   private void addEdge (Vertex source, Vertex destination, Object weight) {
      Edge e = new Edge (source, destination, weight);
      //      edges.add (e);
      source.out.add (destination);
      destination.in.add (source);
   }

   public void addEdge (Object source, Object destination, Object weight) {
      addEdge (get (source), get (destination), weight);
   }

   public Iterator neighbors (Object vertex_label) {
      final List out = get (vertex_label) . out;
      final List list = new ArrayList ();
      for (Iterator i=out.iterator(); i.hasNext(); ) {
	 list.add (((Edge)i.next()).destination);
      }
      return list.iterator();
   }
      
}

