package com.redwood;

import java.util.ArrayList;

public class KxNode {
	public KxVector2d normal;
	public KxVector2d normalTarget;
	public KxVector2d position;
	public KxVector2d ghost;
	double angle = 0;
	public ArrayList<KxJoint> joints;
	
	public KxNode(double px, double py, double gx, double gy)
	{
		normal = new KxVector2d();
		normalTarget = new KxVector2d();
		position = new KxVector2d(px, py);
		ghost = new KxVector2d(gx, gy);
		angle = 0;
		joints = new ArrayList<KxJoint>();
	}
}
