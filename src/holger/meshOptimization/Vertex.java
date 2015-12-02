/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holger.meshOptimization;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author holger
 */
public class Vertex {

    Point pos;
    List<PairInfo> pairs = new LinkedList<PairInfo>();
    QMatrix planeSet = new QMatrix();
    Vertex redirect = null;

    Vertex(Point pos) {
	this.pos = pos;
    }
}
