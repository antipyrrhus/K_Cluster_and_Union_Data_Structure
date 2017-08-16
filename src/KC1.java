/** Class: KC1.java
 *  @author Yury Park
 *  
 *  This class contains a clustering algorithm for computing a max-spacing k-clustering. 
 *  Say you are given a text data file that describes a distance function (equivalently, 
 *  a complete graph with edge costs) with the following format:
 *  
 *  [Total number_of_nodes]
 *  [edge 1 node 1] [edge 1 node 2] [edge 1 cost]
 *  [edge 2 node 1] [edge 2 node 2] [edge 2 cost]
 *  ...
 *  
 *  Each line after the first one describes a single (i, j) connected pair of vertices along with the edge length.
 *  
 *  For example, if a line of the file is "1 3 5250", it indicates that the distance between 
 *  nodes 1 and 3 (equivalently, the cost of the edge (1,3)) is 5250. 
 *  For purposes of this experiment, we assume that distances are positive, 
 *  but we do NOT assume that the distances are distinct.
 *  
 *  There is one edge (i,j) for each choice of 1 ≤ i < j ≤ n, where n is the number of nodes. 
 *  
 *  This class runs the clustering algorithm, where the target number of clusters is set to k, a positive integer. 
 *  What is the maximum spacing of a k-clustering? That is, what is the maximum length edge connecting a pair of vertices
 *  after k-clustering is complete?
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

public class KC1 {

	private ArrayList<Edge> edgesAL;		//ArrayList of all Edges.
	private Map<Integer, Vertex> vertexMap;	//Maps an int value (label no.) to the associated Node AKA Vertex.
	private String fileName;				//name of txt file to read data from in order to construct a graph
	private HashSet<Vertex> vertices;		//A set of vertices.
	
	private static boolean debugOn;
	
	/**
	 * 2-arg constructor.
	 * @param fileName Name of txt file.
	 * @param debugMode If set to true, prints out extra info to the console.
	 */
	public KC1(String fileName, boolean debugMode) {
		this.fileName = fileName;
		this.vertices = new HashSet<>();
		this.edgesAL = new ArrayList<>();
		this.vertexMap = new HashMap<Integer, Vertex>();
		debugOn = debugMode;
		readFile();
	}
	
	/**
	 * Method: readFile
	 * Reads data re: graph from txt file.
	 */
	private void readFile() {
		
		try {
			InputReader r = new InputReader(new FileInputStream(fileName));	//InputReader is a custom class that's included in here.
			r.nextInt();	//1st entry (in 1st line of txt file) contains total number of vertices. Technically not needed for this algorithm.
			
			/* Now begin reading data from the 2nd line. 
			 * Once we reach end of file, will throw NullPointerException which will be caught below. */
			while (true) {
				//Every line contains 3 pieces of data: the label no. of vertex 1, label no. of vertex 2, and the distance between them.
				int vLabel1 = r.nextInt();
				int vLabel2 = r.nextInt();
				int distance = r.nextInt();
				
				/* If vertexMap (HashMap) doesn't yet contain a Vertex object with the corresponding label (vLabel1), create one. */
				Vertex v1 = vertexMap.get(vLabel1);
				if (v1 == null) {
					v1 = new Vertex(vLabel1);
					vertexMap.put(vLabel1, v1);
				}
				
				/* Ditto for vLabel2. */
				Vertex v2 = vertexMap.get(vLabel2);
				if (v2 == null) {
					v2 = new Vertex(vLabel2);
					vertexMap.put(vLabel2, v2);
				}
				
				/* Add both vertices to the HashSet. (automatically takes care of any duplicates) */
				this.vertices.add(v1);
				this.vertices.add(v2);
				
				/* Create new edge object, linking v1 and its neighbor. */
				Edge edge = new Edge(v1, v2, distance);
				edgesAL.add(edge);
		
				/* Add edge to v1's and vNeighbor's respective data field (ArrayList of edges). */
				v1.edges.add(edge);
				v2.edges.add(edge);
				
				/* Also, add the vertex as each other's neighbors, and add the corresponding distance
				 * to each vertex's data field. */
				v1.neighbors.add(v2);
				v1.distances.add(distance);
				v2.neighbors.add(v1);
				v2.distances.add(distance);
			}
			//end while(true)
		}
		catch (FileNotFoundException fnf) {
			System.out.println("File not found!");
			fnf.printStackTrace();
		}
		/* We'll get this exception once we're done reading from txt file.*/
		catch (NullPointerException npe) {
			
			//Sort the Edges ArrayList. Sorts in ascending order starting with the edge with smallest distance.
			Collections.sort(this.edgesAL, new Comparator<Edge>() {
				@Override
				public int compare(Edge e1, Edge e2) {
					return e1.distance - e2.distance;
				}
			});
			
			//testing
			if(debugOn) {
				System.out.println("\nEnd of file! Vertices:\n-----------------");
				for (Vertex v: this.vertices) {
					System.out.printf("Vertex: %s\nneighbors: %s\ndistances: %s\n\n",
							v, v.neighbors, v.distances);
				}
				
			}
			//end if(debugOn)
		}
		//end try/catch
	}
	//end private void readFile
	
	/**
	 * Method: maxDistanceAfterClustering
	 *         Uses union-find data structure to greedily cluster the set of vertices into separate groups
	 *         based on how close the vertices are to each other.
	 *         See WeightedQuickUnionUF.java for more details.
	 *         
	 * @param k the number of clusters to create.
	 * @return the min. distance between any two clusters after the clustering process is complete.
	 */
	public int maxDistanceAfterClustering(int k) {
		
		/* Initialize union-find data structure. (See WeightedQuickUnionUF.java)
		 * The + 1 in the parameter below is needed to ensure the union-find data structure has enough
		 * space to hold all the vertices, because in the given txt file, the vertex label begins at 1, not 0. */
		WeightedQuickUnionUF uf = new WeightedQuickUnionUF(this.vertices.size() + 1);
		
		//Verify that given value of k is valid
		if (k < 2 || k >= uf.count()) {
			throw new RuntimeException("VIOLATION: k must follow the constraint 2 <= k < N, where N = total no. of Vertices");
		}
		
		int index = 0;	//initialize the index from which to begin iterating thru the sorted Edges ArrayList. 
		
		/* Keep this up until the number of clusters (AKA, the groups of vertices clustered together by the union-find
		 * data structure) equals k. */
		while (uf.count() > k) {
			Edge e = this.edgesAL.get(index++);	//get the edge with the next smallest distance (remember this ArrayList is sorted)
			uf.union(e.v1.lbl, e.v2.lbl);		//join the two corresponding vertices into a cluster.
		}
		
		/* Once the while loop above is over, we have k clusters of vertices. 
		 * The distance of the last edge that we pulled in the while loop above is EQUAL to
		 * the max. edge belonging to a cluster. Return it. */
		return this.edgesAL.get(--index).distance;
	}
	
	/**
	 * Class: Vertex
	 *        Vertex object. Inner (nested) class.
	 */
	private class Vertex {
		int lbl;	//name (label no.) of vertex

		ArrayList<Edge> edges;			//list of edges connected to this vertex.
		ArrayList<Vertex> neighbors;	//List of neighbors
		ArrayList<Integer> distances;	//List of corresponding distances for each neighbor
		

		/**
		 * 1-arg constructor.
		 * @param lbl the name (label) to assign to this vertex
		 */
		Vertex(int lbl) {
			this.lbl = lbl;
			init();	//custom method
		}
		
		/**
		 * Method: init
		 * Initializes vars.
		 */
		void init() {
			this.edges = new ArrayList<Edge>();
			
			this.neighbors = new ArrayList<Vertex>();
			this.distances = new ArrayList<Integer>();
		}
		
		@Override
		/**
		 * Method: toString
		 * @return the label no. of this Vertex.
		 */
		public String toString() {
			return this.lbl + "";
		}
		
	}
	//end private class Vertex

	/**
	 * Class: Edge
	 * Edge connecting two vertices.
	 */
	private class Edge {
		Vertex v1, v2;	//the two vertices connected by this Edge.
		int distance;	//the distance (or cost) of this Edge.

		/**
		 * 2-arg constructor.
		 * @param v1 one end of the vertex connected to this Edge
		 * @param v2 the other end of the vertex connected to this Edge
		 * @param distance the distance cost of this edge.
		 */
		Edge(Vertex v1, Vertex v2, int distance) {
			this.v1 = v1;
			this.v2 = v2;
			this.distance = distance;
		}
		
		@Override
		/**
		 * Method: toString
		 * @return the vertices connected by this arrow along with the distance
		 */
		public String toString() {
			return this.v1.lbl + "-" + this.v2.lbl + "(" + this.distance + ")";
		}
	}
	//end private class Edge
	
	/**
	 * class: InputReader
	 * 
	 * purpose: efficiently reads in String either from System.in or from a text file.
	 *
	 */
	class InputReader {
		public BufferedReader reader;
		public StringTokenizer tokenizer;

		public InputReader(InputStream stream) {
			reader = new BufferedReader(new InputStreamReader(stream), 32768);
			tokenizer = null;
		}

		public String next() {
			while (tokenizer == null || !tokenizer.hasMoreTokens()) {
				try {
					tokenizer = new StringTokenizer(reader.readLine());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			return tokenizer.nextToken();
		}

		public String nextLine() {
			tokenizer = null;
			String ret = "";
			try {
				ret = reader.readLine();
			}
			catch(IOException e) {
				throw new RuntimeException(e);
			}
			return ret;
		}

		public int nextInt() {
			return Integer.parseInt(next());
		}

		public long nextLong() {
			return Long.parseLong(next());
		}
	}
	//end class InputReader
	
	/**
	 * Method: printTestResults
	 * @param k the number of clusters that we need to form
	 * @param expectedResult the expected result (for debugging).
	 */
	public void printTestResults(int k, int expectedResult) {
		System.out.printf("\nTest results for file %s:\n"
				        + "The max. edge belonging to a cluster after having formed %s clusters (expected %s): %s\n",
				        this.fileName, k, expectedResult, this.maxDistanceAfterClustering(k));
	}
	
	/**
	 * Method: main
	 * @param args
	 */
	public static void main(String[] args) {
		/* Series of small data files. Debug (verbose mode) on. */
		KC1 kcm = new KC1("kc1_small01.txt", true);
		kcm.printTestResults(2, 6);
		kcm.printTestResults(3, 5);
		kcm.printTestResults(4, 2);
		
		kcm = new KC1("kc1_small02.txt", true);
		kcm.printTestResults(2, 4472);
		kcm.printTestResults(3, 3606);
		kcm.printTestResults(4, 1414);
		
		kcm = new KC1("kc1_small03.txt", true);
		kcm.printTestResults(2, 498);
		kcm.printTestResults(3, 262);
		kcm.printTestResults(4, 236);
		kcm.printTestResults(5, 114);
		kcm.printTestResults(6, 103);
		kcm.printTestResults(7, 86);
		kcm.printTestResults(8, 82);
		kcm.printTestResults(9, 79);
		kcm.printTestResults(10, 54);
		
		/* Longer data file. Debug off. */
		long startTime = System.currentTimeMillis();
		kcm = new KC1("kc1_big.txt", false);
		kcm.printTestResults(4, 106);
		System.out.println("Total time elapsed (in millisecs): " + (System.currentTimeMillis() - startTime));
	}
}
