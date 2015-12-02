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
public class Point {

    double x, y, z;

    Point(double x, double y, double z) {
	this.x = x;
	this.y = y;
	this.z = z;
    }

    Point(Point p1) {
	x = p1.x;
	y = p1.y;
	z = p1.z;
    }

    void scale(double s) {
	x *= s;
	y *= s;
	z *= s;
    }

    void subtract(Point p1) {
	x -= p1.x;
	y -= p1.y;
	z -= p1.z;
    }

    static double dotProd(Point p1, Point p2) {
	double sum = p1.x * p2.x;
	sum += p1.y * p2.y;
	sum += p1.z * p2.z;
	return sum;
    }

    static Point diff(Point p1, Point p2) {
	Point d = new Point(p1);
	d.subtract(p2);
	return d;
    }

    static Point halfway(Point p1, Point p2) {
	return new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2, (p1.z + p2.z) / 2);
    }
}
