package com.redwood;

import java.util.ArrayList;
import java.util.Date;

public class KxBlob {
	public KxVector2d position;
	public KxVector2d velocity;
	public double radius;
	public ArrayList<KxNode> nodes;
	public KxVector2d rotation;
	public int dragNodeIndex;
	public int lastSplitTime;
	public int quality;
	public KxRegion dirtyRegion;
	
	public KxBlob()
	{
		init();
	}
	
	private void init()
	{
		this.position = new KxVector2d();
		this.velocity = new KxVector2d();
		this.radius = 60f;
		this.nodes = new ArrayList<KxNode>();
		this.rotation = new KxVector2d();
		this.dragNodeIndex = -1;
		this.lastSplitTime = 0;
		this.quality = 16;
		this.dirtyRegion = new KxRegion();
	}
	
	public void generateNodes()
	{
		if(this.nodes != null)		this.nodes.clear();
		this.nodes = null;
		this.nodes = new ArrayList<KxNode>();
		
		int i = 0;
		KxNode node;
		for(i = 0; i < this.quality; i++)
		{
			node = new KxNode(this.position.x, this.position.y, this.position.x, this.position.y);
			this.nodes.add(node);
		}
		updateJoints();
		updateNormals();
	}
	
	public void updateJoints()
	{
		int i = 0;
		for (i = 0; i < this.quality; i++)
		{
			KxNode node = this.nodes.get(i);
			if(node.joints != null)			node.joints.clear();
			node.joints = null;
			node.joints = new ArrayList<KxJoint>();
			node.joints.add(new KxJoint(KxUtils.getArrayElementByOffset(this.nodes, i, -1), 0.4f));
			node.joints.add(new KxJoint(KxUtils.getArrayElementByOffset(this.nodes, i, 1), 0.4f));
			
			if(this.quality > 4)
			{
				node.joints.add(new KxJoint(KxUtils.getArrayElementByOffset(this.nodes, i, -2), 0.4f));
				node.joints.add(new KxJoint(KxUtils.getArrayElementByOffset(this.nodes, i, 2), 0.4f));
			}
			
			if(this.quality > 8)
			{
				node.joints.add(new KxJoint(KxUtils.getArrayElementByOffset(this.nodes, i, -3), 0.4f));
				node.joints.add(new KxJoint(KxUtils.getArrayElementByOffset(this.nodes, i, 3), 0.4f));
			}
		}
	}
	
	public void updateNormals()
	{
		int i = 0;
		int index = 0;
		KxNode node;
	
		for(i = 0; i < this.quality; i++)
		{
			node = this.nodes.get(i);
			if(this.dragNodeIndex != -1)
			{
				index = i - Math.round(this.dragNodeIndex);
				index = index < 0 ? (this.quality + index) : index;
			}
			else 
			{
				index = i;
			}
			
			node.angle = ((double)index / this.quality) * Math.PI * 2 + this.rotation.y;
			node.normalTarget.x = Math.cos(node.angle) * this.radius;
			node.normalTarget.y = Math.sin(node.angle) * this.radius;
			if(node.normal.x == 0 && node.normal.y == 0)
			{
				node.normal.x = node.normalTarget.x;
				node.normal.y = node.normalTarget.y;
			}
			
			//System.out.println(i + "   " + index + "   " + ((double)index / this.quality) + "   " + node.angle + "   " + node.normalTarget.x + "   " + node.normalTarget.y + "   " + node.normal.x + "   " + node.normal.y);
		}
	}
	
	public KxBlob split()
	{
		double tmpVx = this.radius / 10;
		int newQuality = (int)(this.nodes.size() * 0.5);
		double newRadius = this.radius * .5;
		KxBlob newBlob = new KxBlob();
		int i = 0;
		
		newBlob.position.x = this.position.x;
		newBlob.position.y = this.position.y;
		for(i = 0; i < newQuality; i++)
		{
			newBlob.nodes.add(this.nodes.get(0));
			this.nodes.remove(0);
		}
		
		double sumAx = 0;
		double sumBx = 0;
		
		for(i = 0; i < this.nodes.size(); i++)
		{
			sumAx += this.nodes.get(i).position.x;
		}
		for(i = 0; i < newBlob.nodes.size(); i++)
		{
			sumBx += newBlob.nodes.get(i).position.x;
		}
		
		newBlob.velocity.x = sumBx > sumAx? tmpVx: -tmpVx;
		newBlob.velocity.y = this.velocity.y;
		newBlob.radius = newRadius;
		newBlob.quality = newBlob.nodes.size();
		this.velocity.x = sumAx > sumBx? tmpVx:-tmpVx;
		this.radius = newRadius;
		this.quality = this.nodes.size();
		this.dragNodeIndex = -1;
		this.updateJoints();
		this.updateNormals();
		newBlob.dragNodeIndex = -1;
		newBlob.updateJoints();
		newBlob.updateNormals();
		Date d = new Date();
		newBlob.lastSplitTime = (int)(d.getTime());
		this.lastSplitTime = (int)(d.getTime());
		d = null;
		
		return newBlob;
	}
	
	public void merge(KxBlob b)
	{
		this.velocity.x *= .5;
		this.velocity.y *= .5;
		this.velocity.x += b.velocity.x * .5;
		this.velocity.y += b.velocity.y * .5;
		while(b.nodes.size() > 0)
		{
			this.nodes.add(b.nodes.get(0));
			b.nodes.remove(0);
		}
		this.quality = this.nodes.size();
		this.radius += b.radius;
		this.dragNodeIndex = b.dragNodeIndex != -1 ? b.dragNodeIndex : this.dragNodeIndex;
		this.updateJoints();
		this.updateNormals();
	}
}
