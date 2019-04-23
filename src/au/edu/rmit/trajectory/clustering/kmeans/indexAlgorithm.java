package au.edu.rmit.trajectory.clustering.kmeans;

import java.util.Map;
import java.util.Set;
import edu.wlu.cs.levy.cg.KDTree;
import edu.wlu.cs.levy.cg.KeyDuplicateException;
import edu.wlu.cs.levy.cg.KeySizeException;
import es.saulvargas.balltrees.BallTreeMatrix;
import es.saulvargas.balltrees.BallTreeMatrix.Ball;
import skyline0623.balltree.BallTree;
import skyline0623.balltree.Hypersphere;
import skyline0623.balltree.Point;
import skyline0623.balltree.Process;
public class indexAlgorithm<E> {

	public indexAlgorithm() {
		// TODO Auto-generated constructor stub
	}
	
	public void buildKDtree(int dims, Map<Integer, double[]> datamapEuc) throws KeySizeException, KeyDuplicateException {
		KDTree<Object> kt = new KDTree<Object>(dims);	
		for(int idx: datamapEuc.keySet()) {
			double[] point = datamapEuc.get(idx);
			kt.insert(point, idx);//fast construction: point, value
		//	System.out.println("inset kd"+idx);
		}		
		indexNode rootKmeans = new indexNode(dims);
	//	kt.traverseConvert(rootKmeans, kt.getroot(), dims);	// traversing the index is hard for kd-tree
	}
	
	/*
	 * get the count of the tree.
	 */
	public double[] updateSum(indexNode root, int dimension) {
		if(root.isLeaf()) {	
			return root.getSum();
		}
		else {
			Set<indexNode> listnode = root.getNodelist();
		//	System.out.println(listnode.size());
			double []sum = new double[dimension];
			for(indexNode aIndexNode: listnode) {
				double []sumd = updateSum(aIndexNode, dimension);
				for(int i=0; i<dimension; i++)
					sum[i] += sumd[i];
			}
			root.setSum(sum);
			return sum;
		}
	}
	
	public indexNode buildMtree(Map<Integer, double[]> datamapEuc, int dimension, int capacity) {// too slow
	//	System.out.println("Building M-tree...");
		long startTime1 = System.nanoTime();
		PointMtree mindex = new PointMtree(capacity);		//capacity	
		for(int idx: datamapEuc.keySet()) {
			double[] point = datamapEuc.get(idx);
			mindex.buildMtree(point, idx);//create the M-tree
		//	System.out.println("inset M-tree"+idx);
		}
		indexNode rootKmeans = new indexNode(dimension);
		mindex.traverseConvert(rootKmeans, mindex.getRoot(), dimension);		// traversing the index
		updateSum(rootKmeans, dimension);
		long endtime = System.nanoTime();
		System.out.println((endtime-startTime1)/1000000000.0);
	//	System.out.println("the count of M-tree is " + rootKmeans.getTotalCoveredPoints());
		return rootKmeans;
	}
	
	public indexNode buildBalltree(Map<Integer, double[]> datamapEuc, int dimension, int capacity) {// too slow	
	//	System.out.println("Building Ball-tree...");
		long startTime1 = System.nanoTime();	
		for(int idx: datamapEuc.keySet()) {
			double[] point = datamapEuc.get(idx);
			Process.DIMENSION = dimension;
			Process.INSTANCES.add(new Point(point));
			Process.MAX_INSTANCE_NUM_NOT_SPLIT = capacity;
		//	System.out.println("inset Ball-tree"+idx);
		}
		Hypersphere BALL_TREE = BallTree.buildAnInstance(null);		
		indexNode rootKmeans = new indexNode(dimension);
		BALL_TREE.traverseConvert(rootKmeans, dimension);
	//	computeFartherToChild(rootKmeans);
		long endtime = System.nanoTime();
		System.out.print((endtime-startTime1)/1000000000.0+",");
		System.out.println("the count of Ball-tree is " + 
				rootKmeans.getTotalCoveredPoints()+", the radius is "+rootKmeans.getRadius());
	//	System.out.println("the count of Ball-tree is " + rootKmeans.getTotalCoveredPoints());
		return rootKmeans;
	}
	
	public indexNode buildBalltree2(double[][] itemMatrix, int dimension, int capacity) {// too slow	
		//	System.out.println("Building Ball-tree using Matrix...");
		long startTime1 = System.nanoTime();
		int deepth = (int) (Math.log(itemMatrix.length)/Math.log(2));//the deepth is computed based on binary tree
	//	deepth = 100;
		indexNode rootKmeans = BallTreeMatrix.create(itemMatrix, capacity, deepth);//we cannot set the deepth too deep
		updateSum(rootKmeans, dimension);
		long endtime = System.nanoTime();
		System.out.println("index time cost: "+(endtime-startTime1)/1000000000.0);
		System.out.println("the count of Ball-tree using Matrix is " + 
			rootKmeans.getTotalCoveredPoints()+", the radius is "+rootKmeans.getRadius());
		return rootKmeans;
	}
}
