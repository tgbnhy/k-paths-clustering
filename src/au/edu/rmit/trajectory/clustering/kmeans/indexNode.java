package au.edu.rmit.trajectory.clustering.kmeans;

import java.util.HashSet;
import java.util.Set;

// this class will build the index based on the 
public class indexNode {	
	protected Set<Integer> pointIdList; // the leaf node, the index node is a leaf node when this is not empty.	
	protected Set<indexNode> nodeList; // the internal node	
	protected double pivot[];// the mean value
	protected double radius;// the radius from the pivot to the furthest point
	protected double distanceToFarther;//this is the distance to father for bound estimation
	protected double []sum;// the sum of all the points inside this node.
	double[] bounds;//the lower bound distance to the non nearest neighbor;
	private int totalCoveredPoints;
	
	public indexNode(int dimension) {
		pointIdList = new HashSet<>();
		nodeList = new HashSet<>();
		pivot = new double[dimension];
		radius = Double.MAX_VALUE; // this is for the root node
		sum = new double[dimension];
	}
	
	public void addNodes(indexNode newNode) {
		nodeList.add(newNode);
	}
	
	public void addPoint(Set<Integer> newPoint) {
		pointIdList.addAll(newPoint);
	}
	
	public void addSinglePoint(int newPoint) {
		pointIdList.add(newPoint);
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public void setdistanceToFarther(double distanceToFarther) {
		this.distanceToFarther = distanceToFarther;
	}
	
	public void setSum(double []sum) {
		for(int i=0; i<sum.length; i++)
			this.sum[i] = sum[i];
	}
	
	public void addSum(double []sum) {
		for(int i=0; i<sum.length; i++)
			this.sum[i] += sum[i];
	}
	
	public void setPivot(double pivot[]) {
		for(int i=0; i<pivot.length; i++)
			this.pivot[i] = pivot[i];
	}
	
	public void setTotalCoveredPoints(int totalCoveredPoints) {
		this.totalCoveredPoints = totalCoveredPoints;
	}
	
	public void setBounds(double bounds[], int groupNumber) {		
		this.bounds = new double[groupNumber+2];
		for(int i=0; i<groupNumber; i++)
			this.bounds[i+2] = bounds[i+2]-distanceToFarther;//update the bound with the distance from the farther to child
	}
	
	public void setBoundsEmpty() {		
		this.bounds = null;
	}
	
	protected void updateSingleLowerBound(int traid, int group_i, double newbound, int groupNumber) {
		if(bounds==null) {
			bounds = new double[groupNumber+2];
			for(int i=0; i<groupNumber+2; i++) {
				bounds[i] = 0;//initialize as the maximum value
			}
			bounds[group_i+2] = newbound;
		}else {
			if(group_i+2>=bounds.length) {//update the size from last round
				bounds = new double[groupNumber+2];
				for(int i=0; i<groupNumber+2; i++) {
					bounds[i] = 0;//initialize as the maximum value
				}
				bounds[group_i+2] = newbound;
			}else {
				if(bounds[group_i+2] > newbound)
					bounds[group_i+2] = newbound;
			}
		}		
	}
	
	public boolean isLeaf() {
		if(pointIdList.isEmpty())
			return false;
		else
			return true;
	}
	
	public Set<indexNode> getNodelist() {
		return nodeList;
	}
	
	public Set<Integer> getpointIdList() {
		return pointIdList;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public double[] getSum() {
		return sum;
	}
	
	public double[] getPivot() {
		return pivot;
	}
	
	public double[] getBounds() {
		return bounds;
	}
	
	public int getTotalCoveredPoints() {
		return totalCoveredPoints;
	}
}
