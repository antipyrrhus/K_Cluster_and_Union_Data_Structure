/****************************************************************************
 *  Compilation:  javac WeightedQuickUnionUF.java
 *  Execution:  java WeightedQuickUnionUF < input.txt
 *  Dependencies: StdIn.java StdOut.java 
 * (Note: These dependencies are optional and are found in the stdlib.jar file in the root directory C:\Users\PP\algs4.
 *  If you want to use them, be sure to import the .jar files stdlib.jar and algs4.jar into Eclipse by right clicking on
 *  this project, going to Build Path -> Configure Build Path...and then choose the Libraries tab and select Add JARs... )
 *
 *  Weighted quick-union (with path compression).
 *
 ****************************************************************************/
/**
 *  The <tt>WeightedQuickUnionUF</tt> class represents a union-find data structure.
 *  It supports the <em>union</em> and <em>find</em> operations, along with
 *  methods for determining whether two objects are in the same component
 *  and the total number of components.
 *  <p>
 *  This implementation uses weighted quick union by size (without path compression).
 *  Initializing a data structure with <em>N</em> objects takes linear time.
 *  Afterwards, <em>union</em>, <em>find</em>, and <em>connected</em> take
 *  logarithmic time (in the worst case) and <em>count</em> takes constant
 *  time.
 *  <p>
 *  For additional documentation, see <a href="http://algs4.cs.princeton.edu/15uf">Section 1.5</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *     
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 *  
 *  (with comments by Yury Park)
 */
public class WeightedQuickUnionUF {
    private int[] parent;   // parent[i] = parent of i
    private int[] size;     // size[i] = number of objects in subtree rooted at i
    private int count;      // number of groups of nodes. (nodes that are grouped together)

    /**
     * Initializes an empty union-find data structure with N isolated components 0 through N-1.
     * @throws java.lang.IllegalArgumentException if N < 0
     * @param N the number of objects
     */
    public WeightedQuickUnionUF(int N) {
        count = N;			//initially, all the nodes are unconnected. So the total number of groups is equal to the total number of nodes.
        
        /* Initialize a parent array that keeps track of the parent node for each node.
         * For example, if Node 4 is the parent of Node 1, then parent[1] = 4. */
        parent = new int[N];
        
        /* Initialize a size array to keep track of the total size of a group of nodes. 
         * For ex., size[3] = size of a group of nodes where Node 3 is the root.
         * Note that this array does not bother to keep track of the size of a group of nodes for every node that's in that group. 
         * It ONLY tracks it for a group with a given node as its root. 
         * 
         * So for example, if we have a group of nodes grouped together as follows:
         * 
         *  2  
         * 1 3
         * 
         * Then, size[2] = 3, since 2 is the root, and there are a total of 3 nodes grouped together.
         * But, calling size[1] or size[3] will NOT necessarily return 3, because the size array is 
         * only updated for a given root of a Node group.
         * */
        size = new int[N];		
        
        /* Initially, each node is its own parent, and the size of each node is 1, because every node is disconnected from each other. */
        for (int i = 0; i < N; i++) {
            parent[i] = i;	//each node is its own parent
            size[i] = 1;	//size of each node is 1.
        }
    }
    //end public WeightedQuickUnionUF

    /**
     * Returns the number of groups of Nodes (components).
     * @return the number of components (between 1 and N)
     */
    public int count() {
        return count;
    }

    /**
     * Returns the component identifier (AKA, the root) for the component containing <tt>p</tt>.
     * @param p the integer representing the Node (AKA site)
     * @return the component identifier (the "root") for the component containing site <tt>p</tt>
     * @throws java.lang.IndexOutOfBoundsException unless 0 <= p < N
     */
    public int find(int p) {
        validate(p);	//custom method to validate that 0 <= p < N, where N is the total number of nodes.
        
        while (p != parent[p])	//do this while loop as long as p is not its own parent. This effectively moves "up the tree" of nodes until we find the root.
        {
        	/* Imagine a group of nodes connected together in the following manner. Let's say that the given parameter p = 0, and
        	 * we want to find and return the root (which is 3 in this case.) See a representation of the tree below:
        	 * 
        	 *        3
        	 *      /   \
        	 *     1     2
        	 *          / \
        	 *         9   6
        	 *            /
        	 *           4
        	 *            \
        	 *             0
        	 * 
        	 * Wouldn't it be nice and efficient if, as we're moving "up the tree" all the way from node 0, we could dynamically
        	 * re-structure the tree so that it looks more like the following after we're done with this method:
        	 * 
        	 *          3
        	 *        / | \
        	 *       1  2  6
        	 *         /  / \
        	 *        9  4   0
        	 *
        	 * This is a much more efficient tree now.
        	 * And any additional find() operations performed on this tree will now run faster than before.
        	 * 
        	 * This is precisely what the single line of code below is designed to do.
        	 * As we make our way "up" the tree, the parent of p will be dynamically updated to the parent of the parent of p.
        	 * 
        	 * So, to continue with our example where p = 0...
        	 * 0's parent is initially 4 (see first graph above). Due to the code below, 0's parent is updated to the 4's parent instead. 
        	 * So now, 0's parent is 6.
        	 * Then, p itself is updated to 6 (see the 2nd line of code below).
        	 * 
        	 * Then, during the next while loop iteration, where p = 6...
        	 * 6's is initially 2. Due to the code below, 6's parent is updated to 2's parent instead. So now, 6's parent is 3.
        	 * Then, p itself is updated to 3 (see the 2nd line of code below).
        	 * Then this while loop ends because p = parent[p] (p = 3, and parent[p] = 3 also), meaning we found our root. 
        	 * 
        	 **/                
        	parent[p] = parent[parent[p]];
        	p = parent[p];
        }
        //end while
        return p;	//return the root
    }

    // validate that p is a valid index
    private void validate(int p) {
        int N = this.parent.length;	//parent.length is the same as the total number of Nodes.
        if (p < 0 || p >= N) {
            throw new IndexOutOfBoundsException("index " + p + " is not between 0 and " + N);
        }
    }

    /**
     * Are the two sites <tt>p</tt> and <tt>q</tt> in the same component (AKA in the same group)?
     * @param p the integer representing one site
     * @param q the integer representing the other site
     * @return <tt>true</tt> if the two sites <tt>p</tt> and <tt>q</tt>
     *         are in the same component, and <tt>false</tt> otherwise
     * @throws java.lang.IndexOutOfBoundsException unless both 0 <= p < N and 0 <= q < N
     */
    public boolean connected(int p, int q) {
        return find(p) == find(q);	//return whether the root of p equals the root of q.
    }

  
    /**
     * Merges the component containing site<tt>p</tt> with the component
     * containing site <tt>q</tt>.
     * @param p the integer representing one site
     * @param q the integer representing the other site
     * @throws java.lang.IndexOutOfBoundsException unless both 0 <= p < N and 0 <= q < N
     */
    public void union(int p, int q) {
        int rootP = find(p);
        int rootQ = find(q);
        if (rootP == rootQ) return;	//if their roots are the same, it means they are already in the same group. So do nothing

        // make smaller-sized group join the larger one. This allows for much better efficiency when doing find() operations.
        if (size[rootP] < size[rootQ]) {	//if rootP's group has fewer nodes than rootQ's group..
            parent[rootP] = rootQ;			//set rootP's parent to rootQ
            size[rootQ] += size[rootP];		//and update the size of rootQ
        }
        else {								//Else, vice versa....
            parent[rootQ] = rootP;
            size[rootP] += size[rootQ];
        }
        count--;			//Decrement total number of components (groups) because one group just joined the other one.
    }
}