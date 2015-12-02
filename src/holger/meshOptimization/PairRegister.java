package holger.meshOptimization;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author holger
 */
public class PairRegister extends HashMap<Pair, EdgeInfo> {

    int hit = 0;

    private EdgeInfo getOrCreateAccu(Pair key) {
	EdgeInfo infoAccu = get(key);
	if (infoAccu == null) {
	    infoAccu = new EdgeInfo();
	    put(key, infoAccu);
	    if (key.hashCode() == 16371) {
	    }
	} else {
	    hit++;
	}
	return infoAccu;
    }

    void registerEdge(Pair p, Vertex theThirdVrtx) {
	getOrCreateAccu(p).registerEdge(theThirdVrtx);
    }

    void registerNeighbourPair(Pair p) {
	getOrCreateAccu(p);
    }

    void listKeys() {
	System.out.printf("Keys in register:\n");
	Set<Pair> keys = keySet();
	for (Pair p : keys) {
	    System.out.printf("\t(%d, %d) with hash code %d\n", p.v1, p.v2, p.hashCode());
	}
    }

}
