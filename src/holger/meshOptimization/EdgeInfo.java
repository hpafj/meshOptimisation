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
public class EdgeInfo {

    int type = 0; //0 = just neighbours, 1 = border, 2 = internal border
    Vertex theThirdVrtx; //only interesting if edge is a border (type==1)

    void registerEdge(Vertex theThirdVrtx) {
	type++;
	this.theThirdVrtx = theThirdVrtx;
    }

    boolean isBorderEdge() {
	return type == 1;
    }
}
