package com.redwood;

import java.util.ArrayList;

public class KxUtils {
	public static int getMouseCloseNodeIndex(ArrayList<KxNode> arr, KxVector2d pos)
	{
		double minDistance = 9999;
		double curDistance = 9999;
		int minIndex = -1;
		for(int i = 0; i < arr.size(); i++)
		{
			curDistance = distanceBetween(arr.get(i).position, pos);
			if(curDistance < minDistance)
			{
				minDistance = curDistance;
				minIndex = i;
			}
		}
		
		return minIndex;
	}
	
	public static int getMouseCloseIndex(ArrayList<KxBlob> arr, KxVector2d pos)
	{
		double minDistance = 9999;
		double curDistance = 9999;
		int minIndex = -1;
		for(int i = 0; i < arr.size(); i++)
		{
			curDistance = distanceBetween(arr.get(i).position, pos);
			if(curDistance < minDistance)
			{
				minDistance = curDistance;
				minIndex = i;
			}
		}
		
		return minIndex;
	}
	
	public static double distanceBetween(KxVector2d posA, KxVector2d posB)
	{
		double dx = posA.x - posB.x;
		double dy = posA.y - posB.y;
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	public static KxNode getArrayElementByOffset(ArrayList<KxNode> arr, int curIndex, int offIndex)
	{
		return arr.get(getArrayIndexByOffset(arr, curIndex, offIndex));
	}
	
	public static int getArrayIndexByOffset(ArrayList<KxNode> arr, int curIndex, int offIndex)
	{
		if (curIndex + offIndex >= 0 && curIndex + offIndex < arr.size())
		{
			return curIndex+offIndex;
		}
		if (curIndex + offIndex > arr.size() - 1)
		{
			return curIndex - arr.size() + offIndex;
		}
		if (curIndex + offIndex < 0)
		{
			return arr.size() + (curIndex + offIndex);
		}
		
		return -1;
	}
}
