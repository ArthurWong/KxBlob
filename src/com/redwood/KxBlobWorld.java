package com.redwood;

import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;

public class KxBlobWorld extends JFrame{
	public ArrayList<KxBlob> blobs;
	public KxVector2d mousePos;
	public KxBlob mouseBlob;
	public int[] mergeBlobs;
	public boolean isDown;
	public KxVector2d gravity;
	public Thread painter;
	
	public static final long serialVersionUID = 90;
	
	public static void main(String[] args)
	{
		KxBlobWorld world = new KxBlobWorld();
		world.init();
	}
	
	public KxBlobWorld()
	{
		super("BlobWorldDemo");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(800, 600);
		this.setVisible(true);
		
		isDown = false;
		gravity = new KxVector2d(0, 0.802);
		blobs = new ArrayList<KxBlob>();
		mousePos = new KxVector2d();
		mergeBlobs = new int[2];
		mergeBlobs[0] = mergeBlobs[1] = -1;
		
		addMouseListener(new MouseMonitor());
	}
	
	public void init()
	{
		createBlob(new KxVector2d(100, 100), new KxVector2d(0.5, 0));
		createBlob(new KxVector2d(700, 100), new KxVector2d(0.5, 0));
		
		//创建刷新线程
		painter = new Painter("paint Blob");
		painter.start();
	}
	
	public void render(Graphics g)
	{
		g.setXORMode(getBackground());
		
		int i = 0;
		KxBlob b;
		int blobAIndex;
		int blobBIndex;
		int blobCIndex;
		
		for(i = 0; i < blobs.size(); i++)
		{
			b = blobs.get(i);
			b.dirtyRegion.reset();
		}
		
		if(this.mergeBlobs[0] != -1 && this.mergeBlobs[1] != -1)
		{
			blobAIndex = mergeBlobs[0];
			blobBIndex = mergeBlobs[1];
			int costTime = (int)(new Date().getTime());
			if(this.blobs.get(blobAIndex) != null && this.blobs.get(blobBIndex) != null)
			{
				if(costTime - blobs.get(blobAIndex).lastSplitTime > 500 && costTime - blobs.get(blobBIndex).lastSplitTime > 500)
				{
					blobs.get(blobAIndex).merge(blobs.get(blobBIndex));
					if(this.mouseBlob == blobs.get(blobAIndex) && isDown) this.mouseBlob = blobs.get(blobAIndex);
					blobs.remove(blobBIndex);
				}
				mergeBlobs[0] = -1;
				mergeBlobs[1] = -1;
			}
		}
		
		if(this.mouseBlob != null)
		{
			this.mouseBlob.velocity.x += (this.mousePos.x - this.mouseBlob.position.x) * .01;
			this.mouseBlob.velocity.y += (this.mousePos.y+ 100 - this.mouseBlob.position.y) * .01;
		}
		
		blobAIndex = 0;
		g.clearRect(0, 0, 800, 600);
		
		for(blobBIndex = blobs.size(); blobAIndex < blobBIndex; blobAIndex++)
		{
			b = blobs.get(blobAIndex);
			for(blobCIndex = 0; blobCIndex < blobBIndex; blobCIndex++)
			{
				KxBlob bb = blobs.get(blobCIndex);
				if(b != bb)
				{
					double dis = KxUtils.distanceBetween(b.position, bb.position);
					//System.out.println(dis + "   " + b.position.x + "   " + b.position.y + "   " + bb.position.x + "   " + bb.position.y);
					//trace(dis);
					if( dis - 120 < b.radius + bb.radius)
					{
						this.mergeBlobs[0] = b.position.x > bb.position.x ? blobAIndex : blobCIndex;
						this.mergeBlobs[1] = b.position.x > bb.position.x ? blobCIndex : blobAIndex;
					}
				}
			}
			
			//b.velocity.x += Math.random() * 0;
			//b.velocity.y += Math.random() * 20;
			
			KxVector2d friction = new KxVector2d(1.035, 1.035);
			
			if(b.position.x > 800 - 2)
			{
				b.velocity.x -= (b.position.x - 800 + 2) * 0.04;
				friction.y += 0.035;
			}
			else if(b.position.x < 2)
			{
				b.velocity.x += Math.abs(b.position.x) * 0.04;
				friction.y += 0.035;
			}
			
			if(b.position.y + b.radius * .25 > 600 -2)
			{
				b.velocity.y -= (b.position.y + b.radius * .25 - 600 + 2) * 0.04;
				friction.x += 0.015;
			}
			else if(b.position.y < 2)
			{
				b.velocity.y += Math.abs(b.position.y) * 0.04;
				friction.x += 0.015;
			}
			
			b.velocity.x += gravity.x;
			b.velocity.y += gravity.y;
			
			b.velocity.x /= friction.x;
			b.velocity.y /= friction.y;
			b.position.x += b.velocity.x;
			b.position.y += b.velocity.y;
			
			KxNode bn;
			KxJoint bnj;
			int d = 0;
			int r = 0;
			KxVector2d tPos;
			
			for(d = 0; d < b.nodes.size(); d++)
			{
				bn = b.nodes.get(d);
				bn.ghost.x = b.position.x;
				bn.ghost.y = b.position.y;
			}
			//if(b.nodes.get(b.dragNodeIndex) != null)
			if(b.dragNodeIndex >= 0 && b.dragNodeIndex < b.nodes.size())
			{
				b.rotation.y = Math.atan2((this.mousePos.y - b.position.y - b.radius * 4), this.mousePos.x - b.position.x);
				b.rotation.x += (b.rotation.y - b.rotation.x) * .2;
				b.updateNormals();
			}
			
			for(d = 0; d < b.nodes.size(); d++)
			{
				bn = b.nodes.get(d);
				bn.normal.x += (bn.normalTarget.x - bn.normal.x) * 0.05;
				bn.normal.y += (bn.normalTarget.y - bn.normal.y) * 0.05;
				tPos = new KxVector2d(b.position.x, b.position.y);
				for(r = 0; r < bn.joints.size(); r++)
				{
					bnj = bn.joints.get(r);
					bnj.strain.x += (bnj.node.ghost.x - bn.ghost.x - (bnj.node.normal.x - bn.normal.x) - bnj.strain.x) * 0.3;
					bnj.strain.y += (bnj.node.ghost.y - bn.ghost.y - (bnj.node.normal.y - bn.normal.y) - bnj.strain.y) * 0.3;
					tPos.x += bnj.strain.x * bnj.strength;
					tPos.y += bnj.strain.y * bnj.strength;
				}
				
				tPos.x += bn.normal.x;
				tPos.y += bn.normal.y;
				r = KxUtils.getArrayIndexByOffset(b.nodes, b.dragNodeIndex, -1);
				int tmpBn = KxUtils.getArrayIndexByOffset(b.nodes, b.dragNodeIndex, 1);
				double tmpNum = 0;
				if(b.dragNodeIndex != -1 && (d == b.dragNodeIndex || b.nodes.size() > 8 && (d == r || d == tmpBn)))
				{
					
					tmpNum = (d == b.dragNodeIndex ? 0.7 : 0.5);
					tPos.x += (this.mousePos.x - tPos.x) * tmpNum;
					tPos.y += (this.mousePos.y - tPos.y) * tmpNum;
				}
				//System.out.println(this.mousePos.x + "   " + this.mousePos.y + "   " + tPos.x + "   " + tPos.y + "   " + d);
				
				bn.position.x += (tPos.x - bn.position.x) * 0.1;
				bn.position.y += (tPos.y - bn.position.y) * 0.1;
				bn.position.x = Math.max(Math.min(bn.position.x, 800 - 2), 2);
				bn.position.y = Math.max(Math.min(bn.position.y, 600 - 2), 2);
				b.dirtyRegion.inflate(bn.position.x, bn.position.y);
				//System.out.println(bn.position.x + "   " + bn.position.y + "   " + d);
			}
			
			KxNode ce = KxUtils.getArrayElementByOffset(b.nodes, 0, -1);
			KxNode pe = KxUtils.getArrayElementByOffset(b.nodes, 0, 0);
			
			//g.setColor(new Color(0x555555));
			//this.graphics.lineStyle(2,0x555555);
			//this.graphics.moveTo(ce.position.x + (pe.position.x - ce.position.x)/2, ce.position.y + (pe.position.y - ce.position.y)/2);
			
			for(d = 0; d < b.nodes.size(); d++)
			{
//				ce = KxUtils.getArrayElementByOffset(b.nodes, d, 0);
//				pe = KxUtils.getArrayElementByOffset(b.nodes, d, 1);
//				
//				for(int dd = 0; dd < ce.joints.size(); dd++)
//				{
//					bnj = ce.joints.get(dd);
//					//this.graphics.moveTo(ce.position.x, ce.position.y);
//					//this.graphics.lineTo(bnj.node.position.x, bnj.node.position.y);
//					//System.out.println(dd + "   " + ce.position.x + "   " + ce.position.y + "   " + bnj.node.position.x + "   " + bnj.node.position.y);
//					g.drawLine((int)ce.position.x, (int)ce.position.y, (int)bnj.node.position.x, (int)bnj.node.position.y);
//				}
				
				//this.graphics.beginFill(0x222222)
				//this.graphics.drawCircle(b.nodes[d].position.x, b.nodes[d].position.y, 5);
				
				g.drawOval((int)b.nodes.get(d).position.x, (int)b.nodes.get(d).position.y, 5, 5);
			}
			//this.graphics.beginFill(0xff3234, 0.1);
			//this.graphics.moveTo(ce.position.x + (pe.position.x - ce.position.x)/2, ce.position.y + (pe.position.y - ce.position.y)/2);
			//this.graphics.lineStyle(2.5, 0xffffff);
			for(d = 0; d < b.nodes.size(); d++)
			{
				//System.out.println(d + "   " + KxUtils.getArrayIndexByOffset(b.nodes, d, 0) + "   " + KxUtils.getArrayIndexByOffset(b.nodes, d, 1));
				ce = KxUtils.getArrayElementByOffset(b.nodes, d, 0);
				pe = KxUtils.getArrayElementByOffset(b.nodes, d, 1);
				g.drawLine((int)ce.position.x, (int)ce.position.y, (int)pe.position.x, (int)pe.position.y);
				//this.graphics.curveTo(ce.position.x, ce.position.y, ce.position.x + (pe.position.x - ce.position.x)/2, ce.position.y + (pe.position.y - ce.position.y)/2);
			
			}
			
			//this.graphics.curveTo(ce.position.x, ce.position.y, ce.position.x + (pe.position.x - ce.position.x)/2, ce.position.y + (pe.position.y - ce.position.y)/2);
			
			//this.graphics.endFill();
			//repaint();
		}
	}
	
	public void createBlob(KxVector2d pos, KxVector2d vel)
	{
		KxBlob b = new KxBlob();
		b.position.x = pos.x;
		b.position.y = pos.y;
		b.velocity.x = vel.x;
		b.velocity.y = vel.y;
		b.generateNodes();
		blobs.add(b);
	}
	
//	@Override
//	public void paint(Graphics g) {
//		//System.out.println("in paint");
//		if(isDown)
//		{
//			getMousePos();
//		}
//		
//		super.paint(g);
//		render(g);
//	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		if(painter != null)
		{
			painter.interrupt();
			painter = null;
		}
	}

	public void getMousePos()
	{
		//获取鼠标的相对于对话框的坐标
		mousePos.x = MouseInfo.getPointerInfo().getLocation().x - this.getX();
		mousePos.y = MouseInfo.getPointerInfo().getLocation().y - this.getY();
	}

	class Painter extends Thread {
	    public Painter(String threadName) {
	        super(threadName);
	    }
	 
	    public void run() {
	        System.out.println(getName() + " 线程运行开始!");
	        while(true)
	        {
	        	if(isDown)
	    		{
	    			getMousePos();
	    		}
	        	
	        	render(getGraphics());
	            try {
	                sleep(20);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	                System.out.println(getName() + "线程运行结束!");
	            }
	        }
	    }
	}
	
	class MouseMonitor implements MouseListener
	{

		@Override
		public void mouseClicked(MouseEvent e) {
			//单击
			if(e.getClickCount() == 1)
			{
				
			}
			//双击
			else if(e.getClickCount() == 2)
			{
				getMousePos();
				KxBlob b = blobs.get(KxUtils.getMouseCloseIndex(blobs, mousePos));
				KxVector2d mPos = new KxVector2d(mousePos.x, mousePos.y);
				if(KxUtils.distanceBetween(b.position, mPos) < b.radius + 30 && b.quality > 8)
				{
					blobs.add(b.split());
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			//不处理
			return;
		}

		@Override
		public void mouseExited(MouseEvent e) {
			//不处理
			return;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			isDown = true;
			getMousePos();
			mouseBlob = blobs.get(KxUtils.getMouseCloseIndex(blobs, mousePos));
			mouseBlob.dragNodeIndex = KxUtils.getMouseCloseNodeIndex(mouseBlob.nodes, mousePos);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			isDown = false;
			if(mouseBlob != null)
			{
				mouseBlob.dragNodeIndex = -1;
				mouseBlob = null;
			}
		}
		
	}
}
