package com.redwood;

public class KxRegion {
	public double top;
	public double left;
	public double right;
	public double bottom;
	
	public KxRegion()
	{
		this.top = this.left = 99999;
		this.bottom = this.right = 0;
	}
	
	public void inflate(double lr, double tb)
	{
		this.left = Math.min(this.left, lr);
		this.right = Math.max(this.right, lr);
		this.top = Math.min(this.top, tb);
		this.bottom = Math.max(this.bottom, tb);
	}
	
	public void reset()
	{
		this.top = this.left = 99999;
		this.bottom = this.right = 0;
	}
}
