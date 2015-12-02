/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holger.meshOptimization;

import java.util.Arrays;

/**
 *
 * @author holger
 */
public class QMatrix {

    double[] elements;

    QMatrix() {
	elements = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    }

    QMatrix(QMatrix m) {
	elements = Arrays.copyOf(m.elements, 16);
    }

    /**
     *
     * QMatrix from plane equation.
     */
    QMatrix(double a, double b, double c, double d) {
	elements = new double[]{
	    a * a, a * b, a * c, a * d,
	    a * b, b * b, b * c, b * d,
	    a * c, b * c, c * c, c * d,
	    a * d, b * d, c * d, d * d
	};
    }

    void add(QMatrix a) {
	for (int i = 0; i < 16; i++) {
	    elements[i] += a.elements[i];
	}
    }

    void scale(double s) {
	for (int i = 0; i < 16; i++) {
	    elements[i] *= s;
	}
    }

    double det() {
	// http://www.cg.info.hiroshima-cu.ac.jp/~miyazaki/knowledge/teche23.html

	final double a11 = elements[0], a12 = elements[1], a13 = elements[2], a14 = elements[3],
		a21 = elements[4], a22 = elements[5], a23 = elements[6], a24 = elements[7],
		a31 = elements[8], a32 = elements[9], a33 = elements[10], a34 = elements[11],
		a41 = elements[12], a42 = elements[13], a43 = elements[14], a44 = elements[15];

	final double det = a11 * a22 * a33 * a44 + a11 * a23 * a34 * a42 + a11 * a24 * a32 * a43
		+ a12 * a21 * a34 * a43 + a12 * a23 * a31 * a44 + a12 * a24 * a33 * a41
		+ a13 * a21 * a32 * a44 + a13 * a22 * a34 * a41 + a13 * a24 * a31 * a42
		+ a14 * a21 * a33 * a42 + a14 * a22 * a31 * a43 + a14 * a23 * a32 * a41
		- a11 * a22 * a34 * a43 - a11 * a23 * a32 * a44 - a11 * a24 * a33 * a42
		- a12 * a21 * a33 * a44 - a12 * a23 * a34 * a41 - a12 * a24 * a31 * a43
		- a13 * a21 * a34 * a42 - a13 * a22 * a31 * a44 - a13 * a24 * a32 * a41
		- a14 * a21 * a32 * a43 - a14 * a22 * a33 * a41 - a14 * a23 * a31 * a42;
	return det;
    }

    String toStr(String indentStr) {
	return String.format(
		"%s%10.2f%10.2f%10.2f%10.2f\n%s%10.2f%10.2f%10.2f%10.2f\n%s%10.2f%10.2f%10.2f%10.2f\n%s%10.2f%10.2f%10.2f%10.2f",
		indentStr, elements[0], elements[1], elements[2], elements[3],
		indentStr, elements[4], elements[5], elements[6], elements[7],
		indentStr, elements[8], elements[9], elements[10], elements[11],
		indentStr, elements[12], elements[13], elements[14], elements[15]
	);
    }

    double squaredDistSum(Point p) {
	double[] v = {p.x, p.y, p.z, 1};
	double sum1, sum2 = 0;
	for (int col = 0; col < 4; col++) {
	    sum1 = 0;
	    for (int row = 0; row < 4; row++) {
		sum1 += v[row] * elements[row * 4 + col];
	    }
	    sum2 += sum1 * v[col];
	}
	return sum2;
    }
}
