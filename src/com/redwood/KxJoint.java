package com.redwood;

public class KxJoint {
	public KxNode node;
	public float strength;
	public KxVector2d strain;
	
	public KxJoint(KxNode b, float e)
	{
		this.node = b;
		this.strength = e;
		this.strain = new KxVector2d();
	}
}
