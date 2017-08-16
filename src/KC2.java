import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.StringTokenizer;

/** Class: KC2.java
 *
 *  Similar to KC1, but the graph is much larger this time. So big, in fact, that the distances
 *  (i.e., edge costs) are only defined implicitly using bits, rather than being provided
 *  as an explicit list.
 *
 *  The format of the data files will be as follows:
 *  [# of nodes] [# of bits for each node's label]
 *  [first bit of node 1] ... [last bit of node 1]
 *  [first bit of node 2] ... [last bit of node 2]
 *  ...
 *  For example, if the 3rd line of the file states "0 1 1 0 0 1 1 0 0 1 0 1 1 1 1 1 1 0 1 0 1 1 0 1",
 *  it denotes the 24 bits associated with node #2.
 *
 *  The distance between two nodes u and v in this problem is defined as the Hamming distance--- the number of differing bits ---
 *  between the two nodes' labels. For example, the Hamming distance between the 24-bit label of node #2 above and
 *  the label "0 1 0 0 0 1 0 0 0 1 0 1 1 1 1 1 1 0 1 0 0 1 0 1" is 3 (since they differ in the 3rd, 7th, and 21st bits).
 *
 *  This class solves the following question:
 *  what is the largest value of k such that there is a k-clustering with spacing at least 3?
 *  That is, how many clusters are needed to ensure that no pair of nodes with all but 2 bits in common get
 *  split into different clusters?
 */
public class KC2 {
    private HashMap<BitSet, Integer> dataSet;	//Hashmap that keeps track of nodes (represented as BitSet) and their label no.)
    											//Note: BitSet is a built-in java class.

    private WeightedQuickUnionUF uf;  			//This is a custom class. See WeightedQuickUnionUF.java.
    private int numberOfBits;  					//Number of bits per node.
    private int numberOfNodes;  				//Total number of vertices.
    private String filename;					//Name of text file for reading in data
    private static boolean debugOn;				//If set to true, prints out extra (verbose) info to the console.

    /**
     * 2-arg constructor.
     * @param fileName Name of txt file
     * @param debugMode if set to true, prints out extra info to the console.
     */
    public KC2(String fileName, boolean debugMode) {
    	this.filename = fileName;
    	this.dataSet = new HashMap<BitSet, Integer>();
    	debugOn = debugMode;
    }

    /**
     * Method: run
     * @param distance the distance parameter. For this problem (read instructions above), this parameter is assumed to equal 3.
     * @return the maximum number of clusters such that no pair of nodes with all but 2 bits in common
     *         get split into different clusters (assuming distance = 3)
     */
    private int run(int distance) {
    	if (distance < 1) throw new RuntimeException("Distance parameter must be a positive integer.");
    	
        build();			//Read in from txt file and union any nodes with a Hamming distance of 0 (AKA duplicate nodes)
        
        //calDis(i): Efficient way to union all pairs of nodes that have exactly i hamming distances between them 
        for (int i = 1; i < distance; i++) calDis(i);

        return uf.count();	//Return the number of clusters after the above operations are complete. This is the answer.
    }

    /**
     * Method: build
     * 1. read in the text file
     * 2. create a union find structure
     * 3. build the hash map and union the nodes that have 0 distances
     */
    private void build(){
        try {
            BufferedReader rd = new BufferedReader(new FileReader(new File(filename)));

            //Read the first line, which contains info re: total no. of vertices and no. of bits per vertex
            String line = rd.readLine();
            StringTokenizer tokenizer = new StringTokenizer(line);
            numberOfNodes = Integer.parseInt(tokenizer.nextToken());
            uf = new WeightedQuickUnionUF(numberOfNodes); //initialize the union-find data structure with the given no. of vertices.
            numberOfBits = Integer.parseInt(tokenizer.nextToken());
            int index = 0;		//Initialize the label no. of the first vertex (soon to be read from the txt file).

			while ((line = rd.readLine()) != null) {
                tokenizer = new StringTokenizer(line);
                BitSet bitSet = new BitSet();

                //Creates a bit set representing the current node AKA vertex
                for (int i = 0; i < numberOfBits; i++) {
                	//If a bit = 1, then set that bit's index number to true via the bitSet.set(i) method
                    if (tokenizer.nextToken().equals("1")) {
                        bitSet.set(i);
                    }
                }

                if (debugOn) System.out.printf("Created new bitset (label no. %s): \t%s\n", index, getBits(bitSet));

                //put it in the hash map if no identical nodes are already in there
                if (!dataSet.containsKey(bitSet)) {
                    dataSet.put(bitSet, index);
                }
                //else, if if the current node has a duplicate in the hash map (meaning their distance is zero), union the two nodes.
                else {
                	int duplicateNodeIndex = dataSet.get(bitSet);
                	if (debugOn) System.out.printf("\tThis is a duplicate of node %s (AKA distance zero)! "
                			+ "Performing union operation...\n", duplicateNodeIndex);
                    uf.union(index, duplicateNodeIndex);
                }
                index++;	//increment the label no. of the next vertex to be read
            }
            //end while
            rd.close();	//close the reader when done.
            if (debugOn) System.out.printf("Number of clusters so far: %s\n", uf.count());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //end private void build()

    /**
     * Method: getBits
     * @param bitSet a BitSet object. Remember BitSet is a built-in class.
     * @return the binary representation of the given BitSet object.
     */
    private String getBits(BitSet bitSet) {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < this.numberOfBits; i++) {
    		//If the bit at this given index i is true, then append "1". Otherwise, append "0".
			sb.append(bitSet.get(i) == true ? 1 : 0);
		}
    	return sb.toString();
    }

    /**
     * Method: calDis
     *         Finds all pairs of nodes that have the specified distance between them, and performs union operation on them.
     * @param distance the distance parameter.
     */
    private void calDis(int distance) {
    	if (debugOn) System.out.printf("\nStarting calDis() method with distance parameter %s...\n", distance);

    	/* If two nodes are to have a Hamming distance of n, then the two nodes must differ by exactly n bits.
    	 * So go thru all BitSet objects, and for each object, find and union with all other BitSets that
    	 * differ exactly by n bits. */
    	for (BitSet bitSet : dataSet.keySet()) {
    		calDis(distance, 0, bitSet, (BitSet)bitSet.clone(), 0);	//Invoke recursive method!
    	}
    	if (debugOn) System.out.printf("\ncalDis() method finished. Current number of clusters: %s\n", uf.count());
    }

    /**
     * Method: calDis
     *         Overloaded method. Uses recursion.
     *         Given a BitSet object, finds and unions with every other BitSet that differs exactly by the distance parameter.
     * @param distance the distance. Assumed to be positive.
     * @param level the current level of this recursive method. Starting level is assumed to be 0.
     * @param bs a BitSet object.
     * @param temp a temp BitSet object with one or more of the bits flipped. NOTE: when this method is first invoked, temp will be
     *        a clone of the bs object, with none of the bits flipped.
     * @param index the index from which to begin the loop.
     */
    private void calDis(int distance, int level, BitSet bs, BitSet temp, int index) {
    	/* BASE CASE. All necessary bits for the temp BitSet have been flipped. Now all we have to do is to see
    	 * if the HashSet (dataSet) contains the temp BitSet and if so, union them.
    	 * See comments on the now defunct calDis1() and calDis2() methods for more details. */
    	if (distance == level) {
    		if (dataSet.containsKey(temp)) {
            	if (debugOn) System.out.printf("Found a BitSet (%s) which is %s Hamming distance away from BitSet %s.\n",
            			this.getBits(temp), distance, this.getBits(bs));
            	uf.union(dataSet.get(bs), dataSet.get(temp));
            }
    		return;	//Base case is over. Exit method.
    	}

    	/* If the base case above is not triggered, then we must go thru every bit in the given BitSet object (bs),
    	 * and flip the number of bits as needed. */
    	for (int i = index; i < numberOfBits; i++) {
            //Flip exactly one bit at this index. So if this index used to contain (true) 1, it is now 0 (false); and vice versa.
            temp.flip(i);
            calDis(distance, level+1, bs, temp, i+1);	//Recursive call!
            //Now restore the flipped bit.
            temp.flip(i);
    	}
    }

    /**
     * Method: printTestResults
     * @param distance the distance parameter.
     * @param expectedResult expected result
     */
	public void printTestResults(int distance, long expectedResult) {
		System.out.printf("\n=====================================================\nTest results for file %s:\n"
				        + "The max. number of clusters such that no pair of nodes with all but %s bits in common\n"
				        + "get split into different clusters (expected answer %s): %s\n",
				        this.filename, distance-1, expectedResult, this.run(distance));
	}

    /**
     * Method: main
     * @param args
     */
    public static void main(String[] args) {
    	KC2 kcm =
    			new KC2("kc2_small01.txt", true);
    	kcm.printTestResults(2, 1);


    	/* Large test data file */
    	long startTime = System.currentTimeMillis();
    	kcm = new KC2("kc2_big.txt", false);
        kcm.printTestResults(3, 6118);
        System.out.printf("Elapsed time (in millisecs): %s", System.currentTimeMillis() - startTime);
    }
}