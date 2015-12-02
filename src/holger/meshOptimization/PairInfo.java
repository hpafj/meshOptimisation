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
public class PairInfo {

    Vertex v1, v2;
    double err;
    int heapIndx;
    Point bestPos;

    PairInfo(Vertex v1, Vertex v2) {
	this.v1 = v1;
	this.v2 = v2;
	//heapIndx is set when this record enters the heap
    }
}
