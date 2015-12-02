/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holger.meshOptimization;

/**
 *
 * @author holger
 */
public class Pair {

    Vertex v1, v2;
    int hash;

    Pair(Vertex v1, Vertex v2) {
	int h1 = v1.hashCode(), h2 = v2.hashCode();
	if (h1 <= h2) {
	    this.v1 = v1;
	    this.v2 = v2;
	    hash = calcHash(h1, h2);
	} else {
	    this.v1 = v2;
	    this.v2 = v1;
	    hash = calcHash(h2, h1);
	}
    }

    private int calcHash(int h1, int h2) {
	int hash = 17;
	hash = hash * 31 + h1;
	hash = hash * 31 + h2;
	return hash;
    }
//    Pair(Vertex v1, Vertexv v2) {
//	if (v1 <= v2) {
//	    this.v1 = v1;
//	    this.v2 = v2;
//	} else {
//	    this.v1 = v2;
//	    this.v2 = v1;
//	}
//    }
//    @Override
//    public int compareTo(Pair p) {
//	int c = v1 - p.v1;
//	if (c == 0) {
//	    c = v2 - p.v2;
//	}
//	return c;
//    }

    @Override
    public int hashCode() {
	return hash;
    }

    @Override
    public boolean equals(Object o) {
	if (o instanceof Pair) {
	    Pair p = (Pair) o;
	    if (v1 == p.v1) {
		return v2 == p.v2;
	    } else {
		return v1 == p.v2 && v2 == p.v1;
	    }
	} else {
	    return false;
	}
    }
}
