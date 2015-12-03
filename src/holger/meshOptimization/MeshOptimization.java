/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holger.meshOptimization;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Holger Jespersen
 */
public class MeshOptimization {

    static Point[] vrtx; // vertex-to-coordinate map
    static Triangle[] triangle;
    static QMatrix[] qMatrix_vrtx;
    static int[] redirect_vrtx;
    static int[] v1_triangle, v2_triangle, v3_triangle; // triangle table, v1, v2 and v3 refers to vertices
    static int nVrtx, nTriangles; //Number of triangles loaded from triangle file
    static PairInfo[] heap_array;
    static int heap_size;

    public static void main(String[] args) {

	PairRegister pairReg = new PairRegister();
	BufferedReader br;
	Scanner scnr;
	//
	// Read command line arguments
	//
	int i_arg = 0;
	String inputVrtxFileName = args[i_arg++];
	String inputTriangleFileName = args[i_arg++];
	//
	// Read Nodes
	//
	try {
	    Map<Integer, Vertex> nodeNum2vrtx; // maps vertex numbers to vertex records
	    /*
	     *  Read Vertices
	     */
	    System.out.print("Reading node file ...");
	    br = new BufferedReader(new FileReader(inputVrtxFileName));
	    scnr = new Scanner(br);
	    nVrtx = scnr.nextInt();
	    nodeNum2vrtx = new HashMap<Integer, Vertex>(2 * nVrtx);
	    for (int i = 0; i < nVrtx; i++) {
		scnr.nextLine();
		int nodeNum = scnr.nextInt();
		double x = scnr.nextDouble();
		double y = scnr.nextDouble();
		double z = scnr.nextDouble();
		nodeNum2vrtx.put(nodeNum, new Vertex(new Point(x, y, z)));
	    }
	    br.close();
	    System.out.print(" done\n");
	    /*
	     *  Read Triangles
	     */
	    System.out.print("Reading triangle file ...");
	    br = new BufferedReader(new FileReader(inputTriangleFileName));
	    scnr = new Scanner(br);
	    nTriangles = scnr.nextInt();
	    triangle = new Triangle[nTriangles];
	    for (int i = 0; i < nTriangles; i++) {
		scnr.nextLine();
		scnr.nextInt(); //skip record number
		int node1 = scnr.nextInt();
		int node2 = scnr.nextInt();
		int node3 = scnr.nextInt();
		// Lookup the vertex numbers and get the coresponding vertex object
		Vertex v1 = nodeNum2vrtx.get(node1), v2 = nodeNum2vrtx.get(node2), v3 = nodeNum2vrtx.get(node3);
		triangle[i] = new Triangle(v1, v2, v3);
		QMatrix plane = qMatrixFromTrianglePlane(v1.pos, v2.pos, v3.pos);
		v1.planeSet.add(plane);
		v2.planeSet.add(plane);
		v3.planeSet.add(plane);
		pairReg.registerEdge(new Pair(v1, v2), v3);
		pairReg.registerEdge(new Pair(v2, v3), v1);
		pairReg.registerEdge(new Pair(v3, v1), v2);
	    }
	    br.close();
	    System.out.print(" done\n");
	    nodeNum2vrtx = null; //we can now throw this map away
	    /*
	     *  Register Neighbour Pairs
	     */

	    //
	    // For all neighbours register them
	    //
	    System.out.printf("we have %d triangles and %d pairs (%d hits)\n", nTriangles, pairReg.size(), pairReg.hit);
	    for (int i = 0; i < nTriangles; i++) {
//		Vertex v1 = triangle[i].v1, v2 = triangle[i].v2, v3 = triangle[i].v3;
//		System.out.printf("Triangle %d ---------------------------\n", i);
//		System.out.printf("\tQMatrix of vertex 1 with determinant of %.4f\n", v1.planeSet.det());
//		System.out.println(v1.planeSet.toStr("\t\t"));
//		System.out.printf("\tSquared distance is %f\n", v1.planeSet.squaredDistSum(new Point(0, 0, 3)));
	    }
	    Set<Map.Entry<Pair, EdgeInfo>> entries = pairReg.entrySet();
	    heap_initStruct(entries.size());
	    int ii = 0;
	    for (Entry<Pair, EdgeInfo> e : entries) {
		Pair k = e.getKey();
		EdgeInfo ei = e.getValue();
		//System.out.printf("(%6.2f, %6.2f, %6.2f)-(%6.2f, %6.2f, %6.2f) is of type %d\n",
		//	k.v1.pos.x, k.v1.pos.y, k.v1.pos.z, k.v2.pos.x, k.v2.pos.y, k.v2.pos.z, ei.type);
		PairInfo pi = new PairInfo(k.v1, k.v2);
		updateProposal(pi);
		heap_addToStruct(ii, pi);
		ii++;
	    }
	    //heap_dump();
	    heap_build();
	    heap_dump();

	    int[] stop = {75, 50, 25};
	    for (int s = 0; s < stop.length; s++) {
		int nRemovals = (int) Math.round(nVrtx * stop[s] / 100.0); //calculate from s how many vertices to remove
		removeNVrtcs(nRemovals);
		String reducedVrtxFileName = inputVrtxFileName + "-" + s;
		String reducedTriangleFileName = inputTriangleFileName + "-" + s;
		outputReducedMesh(reducedVrtxFileName, reducedTriangleFileName);
	    }

	    // for each triangle register the edges in a pair table: node x node -map-> (int x vertex). Keep a list of triangle
	    // objects. A triangle object references 3 edge or vertex objects
	    // Now, go through the edges:
	    // For each edge, find the plane equation and add it to the Q-matrix of both it's vertices. If the edge is a border
	    // edge, find the equation for the perpendicular plane that goes through the edge and add it with a given weight to the
	    // Q-matrices of the vertices.
	    // Now register all neighbour pairs (and create a new pair only if it's not already registered as an edge).
	    // Throw away the node table so all unused vertex objects will be garbage collected.
	    // Run through all pairs and calculate its initial cost, and then place it in the heap. And put a edge reference in the
	    // in both of the vertices' pair list.
	    // Throw away the pair table.
	    // Run the algorithm. When I contract two vertices I remove their pair an one of the vertices. Then I have to update all
	    // the pairs (and remove the doubles)
	    // Run through the list of triangles, and print out the ones that are not degenerated
	} catch (FileNotFoundException ex) {
	    Logger.getLogger(MeshOptimization.class
		    .getName()).log(Level.SEVERE, null, ex);
	} catch (IOException ex) {
	    Logger.getLogger(MeshOptimization.class
		    .getName()).log(Level.SEVERE, null, ex);
	}

    }

    private static void removeNVrtcs(int n) {
	for (int i = n; i > 0; i--) {
	    rmVrtex(); // printcipal iteration of the basic algorithm
	}
    }

    static void rmVrtex() {
	PairInfo p = heap_pop();
	p.v1.pos = p.bestPos;
	p.v2.redirect = p.v1;
	p.v1.planeSet.add(p.v2.planeSet);
	p.v1.pairs.addAll(p.v2.pairs);
	p.v2.pairs = null;
	for (Iterator<PairInfo> i = p.v1.pairs.iterator(); i.hasNext();) {
	    PairInfo pInLst = i.next();
	    if (pInLst == p) {
		i.remove();
	    } else {
		updateProposal(pInLst);
	    }
	}
    }

    static void updateProposal(PairInfo p) {
	QMatrix combinedPlaneSet = new QMatrix(p.v1.planeSet);
	combinedPlaneSet.add(p.v2.planeSet);
	Point hw = Point.halfway(p.v1.pos, p.v2.pos);
	double err_v1 = combinedPlaneSet.squaredDistSum(p.v1.pos),
		err_v2 = combinedPlaneSet.squaredDistSum(p.v2.pos),
		err_hw = combinedPlaneSet.squaredDistSum(hw);
	double minErrOfV1V2 = Math.min(err_v1, err_v2);
	if (err_hw <= minErrOfV1V2) {
	    p.bestPos = hw;
	    p.err = err_hw;
	} else if (err_v1 < err_v2) {
	    p.bestPos = p.v1.pos;
	    p.err = err_v1;
	} else {
	    p.bestPos = p.v2.pos;
	    p.err = err_v2;
	}
    }

    static QMatrix qMatrixFromTrianglePlane(Point p1, Point p2, Point p3) {
	final double x1 = p1.x, y1 = p1.y, z1 = p1.z, x2 = p2.x, y2 = p2.y, z2 = p2.z, x3 = p3.x, y3 = p3.y, z3 = p3.z;
	final double x12 = x2 - x1;
	final double y12 = y2 - y1;
	final double z12 = z2 - z1;
	final double x13 = x3 - x1;
	final double y13 = y3 - y1;
	final double z13 = z3 - z1;
	double a = y12 * z13 - y13 * z12;
	double b = z12 * x13 - z13 * x12;
	double c = x12 * y13 - x13 * y12;
	// let's normalize the plane normal
	final double len = Math.sqrt(a * a + b * b + c * c);
	a /= len;
	b /= len;
	c /= len;
	final double d = -(a * x1 + b * y1 + c * z1);
	return new QMatrix(a, b, c, d);
    }

    /**
     * @param args the command line arguments
     */
    static void heap_initStruct(int size) {
	heap_array = new PairInfo[size];
	heap_size = size;
    }

    static final void heap_addToStruct(int i, PairInfo elem) {
	heap_array[i] = elem;
	elem.heapIndx = i;
    }

    static void heap_build() {
	heap_size = heap_array.length;
	for (int i = heap_size / 2 - 1; i >= 0; i--) {
	    heap_siftDown(i);
	}
    }

    static PairInfo heap_pop() {
	PairInfo rootElem = heap_array[0];
	heap_array[0] = heap_array[heap_size - 1];
	heap_size--;
	heap_siftDown(0);
	return rootElem;
    }

    static void heap_siftUp(int i) {
	if (i > 0) {
	    int p = heap_parent(i);
	    if (heap_array[i].err < heap_array[p].err) {
		// swap
		heap_swap(i, p);
		heap_siftUp(p);
	    }
	}
    }

    static void heap_siftDown(int i) {
	int l = heap_left(i), r = heap_right(i), smallest = i;
	if (l < heap_size && heap_array[l].err < heap_array[i].err) {
	    smallest = l;
	}
	if (r < heap_size && heap_array[r].err < heap_array[smallest].err) {
	    smallest = r;
	}
	if (smallest != i) {
	    //swap the parent element with the smallest error child element
	    heap_swap(i, smallest);
	    heap_siftDown(smallest);
	}
    }

    static final int heap_parent(int indx) {
	return (indx - 1) / 2;
    }

    static int heap_left(int indx) {
	return 2 * indx + 1;
    }

    static int heap_right(int indx) {
	return 2 * indx + 2;
    }

    static void heap_swap(int i1, int i2) {
	PairInfo temp = heap_array[i1];
	heap_array[i1] = heap_array[i2];
	heap_array[i2] = temp;
	heap_array[i1].heapIndx = i1;
	heap_array[i2].heapIndx = i2;
    }

    static void heap_dump() {
	System.err.println("Heap Dump");
	for (int i = 0; i < heap_size; i++) {
	    PairInfo p = heap_array[i];
	    System.err.printf("\t%d: (%5.2f,%5.2f,%5.2f)-(%5.2f,%5.2f,%5.2f)-> %5.2f,%5.2f,%5.2f) with error %f\n",
		    i,
		    p.v1.pos.x, p.v1.pos.y, p.v1.pos.z,
		    p.v2.pos.x, p.v2.pos.y, p.v2.pos.z,
		    p.bestPos.x, p.bestPos.y, p.bestPos.z,
		    p.err
	    );

	}
    }

    private static void outputReducedMesh(String reducedVrtxFileName, String reducedTriangleFileName) {
	// First Pass
	// Go through our original array of triangles and
	// 1. clean up redirects on vertices
	// 2. make sure that the triangles points to actual vertices (not redirects)
	// 3. if the triangle is degenerated, erase if from the array, or else save it
	//    in a list succsesive printing
	cleanUpTrianglesAndVrtcs();
	//
	List<Triangle> triangles = new ArrayList<Triangle>();
	Map<Vertex, Integer> vrtcs = new HashMap<Vertex, Integer>();
	for (int i = 0; i < triangle.length; i++) {
	    if (triangle[i] != null) {
		int numV1 = getVrtxNum(vrtcs, triangle[i].v1);
		int numV2 = getVrtxNum(vrtcs, triangle[i].v2);
		int numV3 = getVrtxNum(vrtcs, triangle[i].v3);
	    }
	}
    }

    private static int getVrtxNum(Map<Vertex, Integer> vrtcs, Vertex v) {
	Integer num = vrtcs.get(v);
	if (num == null) {
	    num = new Integer(vrtcs.size());
	    vrtcs.put(v, num);
	}
	return num;
    }

    private static void cleanUpTrianglesAndVrtcs() {
	for (int i = 0; i < triangle.length; i++) {
	    if (triangle[i] != null) { //haven't been cleaned up yet
		cleanUpRedirects(triangle[i]);
		if (triangle[i].v1 == triangle[i].v2 || triangle[i].v1 == triangle[i].v3 || triangle[i].v2 == triangle[i].v3) {
		    // This triangle is degenerated
		    triangle[i] = null;
		}
	    }
	}
    }

    private static void cleanUpRedirects(Triangle t) {
	if (t.v1.redirect != null) {
	    shortCutRedirects(t.v1);
	    t.v1 = t.v1.redirect;
	}
	if (t.v2.redirect != null) {
	    shortCutRedirects(t.v2);
	    t.v2 = t.v2.redirect;
	}
	if (t.v2.redirect != null) {
	    shortCutRedirects(t.v3);
	    t.v3 = t.v3.redirect;
	}
    }

    private static void shortCutRedirects(Vertex hd) {
	Vertex tail = hd.redirect;
	if (tail.redirect != null) {
	    shortCutRedirects(tail);
	}
	hd.redirect = tail.redirect;
    }

}
