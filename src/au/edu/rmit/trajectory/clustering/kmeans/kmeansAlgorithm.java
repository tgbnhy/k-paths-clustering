package au.edu.rmit.trajectory.clustering.kmeans;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import au.edu.rmit.trajectory.clustering.TrajectoryMtree;
import au.edu.rmit.trajectory.clustering.kpaths.KPathsOptimization;
import au.edu.rmit.trajectory.clustering.kpaths.Util;
import edu.wlu.cs.levy.cg.KeyDuplicateException;
import edu.wlu.cs.levy.cg.KeySizeException;

/*
 * ball-tree, M-tree, and Hierarchical k-means tree can used be extended to answer k-means, 
 * as index has grouped similar points into a node.
 */
@SuppressWarnings("restriction")
public class kmeansAlgorithm<T> extends KPathsOptimization<T>{
	protected ArrayList<cluster> CENTERSEuc; // it stores the k clusters

	double [][]dataMatrix;//we use matric to store instead of datamapEuc
	double [][]allBounds;//instead of using hashmap
	protected double[] distanceToFather; // the Euclidean space dataset
	int dimension = 0;//the dimension of the Euclidean dataset
	int dimension_start = 0;// the dimension start in a file
	int dimension_end = 0;// the dimension end in file
	indexNode root;// the root node
	int iteration = 0;
	private Scanner in;
	
	int prunenode=0;
	long numComputeEuc = 0;
	long bounCompute = 0;
	long dataReach = 0;
	long nodeReach = 0;
	
	boolean indexTKDE02=false;
	boolean usingIndex = false;
	String split = null;
	double [][]centroidsData;
	boolean nonkmeansTree = true;
	
	double time[]= new double[5];
	double assigntime[] = new double[5];
	double refinetime[] = new double[5];
	long computations[] = new long[5];
	long boundaccess[] = new long[5];//used for bound accessing.
	long dataAccess[] = new long[5];
	long nodeAccess[] = new long[5];
	int counter = 0;
	protected int [][]allCentroids = null;
	String datafilename;
	int capacity=30;
	
	boolean Yinyang = false;// ICML15: Yinyang K-means: A drop-in replacement of the classic K-means with consistent speedup
	boolean Hamerly = true;//Making k-means even faster, using the drift as bound

	public kmeansAlgorithm(String []datapath) {
		super(datapath);
		datafilename = datapath[4];
		dimension_start = Integer.valueOf(datapath[5]);// the dimensions
		dimension_end = Integer.valueOf(datapath[6]);
		dimension = dimension_end - dimension_start+1;
		if(datapath.length>7)
			split = datapath[7];
	}
	
	public void setScale(int scale) {
		trajectoryNumber = scale;
	}
	
	public void setCapacity(int scale) {
		capacity = scale;
	}
	
	public void setDimension(int dim) {
		dimension = dim;
		dimension_end =dimension_start+dimension-1;
	}
	
	public void loadDataEuc(String path, int number) {
		dataMatrix = new double[trajectoryNumber][];//store the data in this matrix.
		int pointid = 1;
		try {
			in = new Scanner(new BufferedReader(new FileReader(path)));			
			while (in.hasNextLine()) {// load the trajectory dataset, and we can efficiently find the trajectory by their id.
				String str = in.nextLine();
				String strr = str.trim();
			//	System.out.println(strr);
				String[] abc = null;				
				if(split!=null) {
					if (!split.equals("a")) {
						abc = strr.split(split);
					} else if (split.equals("b")) {
						abc = strr.split(" ");
					} else {
						if(strr.contains(","))
							abc = strr.split(",");// when both connected 
						else if(strr.contains(";"))
							abc = strr.split(";");
						else if(strr.contains(" "))
							abc = strr.split(" ");
					}
				}else{
					if(strr.contains(","))
						abc = strr.split(",");// when both connected 
					else if(strr.contains(";"))
						abc = strr.split(";");
					else if(strr.contains(" "))
						abc = strr.split(" ");
				}
				if(abc.length-1 < dimension_end)// the dimension is not right
					continue;
				dataMatrix[pointid-1] = new double[dimension];
				double []point = new double[dimension];
				boolean nan=false;
				for(int i=dimension_start; i<=dimension_end; i++) {					
					if(!abc[i].equals("?")) {
						dataMatrix[pointid-1][i-dimension_start] = Double.valueOf(abc[i]);
						point[i-dimension_start] = Double.valueOf(abc[i]);
					}
					else {
						dataMatrix[pointid-1][i-dimension_start] = 0;
						point[i-dimension_start] = 0;
					}
				}				
				if(nan==true)
					continue;				
				pointid++;
				if(pointid>number)
					break;
			}
			in.close();
		}		
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	//	trajectoryNumber = dataMatrix.length;
	//	trajectoryNumber = pointid;
	}
	
	/*
	 * access the data using point id
	 */
	double []accessPointById(int id){
		dataReach++;
		return dataMatrix[id-1];
	}
	/*
	 * compute the distance between any two centers for bound computation
	 */
	public void computeInterCentoridEuc(int k, ArrayList<cluster> Center, double [][]clustData) {
		for(int i=0; i<k; i++) {
			innerCentoridDis[i] = new double[k];
			double []a = clustData[i];		
			double min = Double.MAX_VALUE;
			for(int j=0; j<k; j++) {				
				if(i!=j) {
					double []b = clustData[j];
					double distance = Util.EuclideanDis(a, b, a.length);
					innerCentoridDis[i][j] = distance;
					if(distance<min) {
						min = distance;
					}					
				}
			}
			interMinimumCentoridDis[i] = min;
		}
		for (int i = 0; i < k; i++) {//the distance in each group
			innerCentoridDisGroup[i] = new double[group.size()];
			for (int groupid : group.keySet()) {
				ArrayList<Integer> arrayList = group.get(groupid);
				double min = Double.MAX_VALUE;
				for (int centerid : arrayList) {
					if (innerCentoridDis[i][centerid] < min) {
						min = innerCentoridDis[i][centerid];
					}
				}
				innerCentoridDisGroup[i][groupid] = min;
			}
		}
	}
	
	/*
	 * update the lower bound of trajectory toward group i
	 */
	protected void updateSingleLowerBound(Map<Integer, double[]> trajectoryBounds, int traid, 
			int group_i, double newbound, int groupNumber) {
		if(allBounds[traid-1] == null) {
			allBounds[traid-1] = new double[groupNumber+2];
			for(int i=0; i<groupNumber+2; i++) {
				allBounds[traid-1][i] = 0;//initialize the bound as zero
			}
			allBounds[traid-1][group_i+2] = newbound;
		}else {
			if(allBounds[traid-1][group_i+2] > newbound)
				allBounds[traid-1][group_i+2] = newbound;
		}
	}
	
	/*
	 * initialize the centroid
	 */
	public void initializeClustersRandom(int k){
		CENTERSEuc = new ArrayList<>();
		int unit= trajectoryNumber/k;
		System.out.println(unit+ " "+ k+" "+trajectoryNumber);
		if(centroids==null) {//if there is no given centroids
			Random rand = new Random();
			for(int t=0; t<k; t++) {
				cluster cl = null;
				int  n = rand.nextInt(trajectoryNumber)+1;
				n = t+1;// using same centroids every time.
				double[] cluster = accessPointById(n);
				if(usingIndex) {// if there is an index
					if(t==k-1)
						cl = new cluster(cluster, root, dimension);//assign the root node to the last cluster
					else {
						cl = new cluster(cluster, dimension);
					}
				}else {					
					int end = (t+1)*unit;
					if(t == k-1)
						end = trajectoryNumber;
					cl = new cluster(cluster, n, t*unit, end, dimension, dataMatrix);//no index
				}
				CENTERSEuc.add(cl);
			}
		}else {
			for(int t=0; t<k; t++) {
				int  n = centroids[t];
				double[] cluster = accessPointById(n);
				cluster cl = null;
				if(usingIndex) {// if there is an index
					if(t==0)
						cl = new cluster(cluster, root, dimension);//assign the root node to the first cluster
					else {
						cl = new cluster(cluster, dimension);
					}
				}else {					
					int end =(t+1)*unit;
					if(t == k-1)
						end = trajectoryNumber;
					cl = new cluster(cluster, n, t*unit, end, dimension, dataMatrix);//no index
				}
				CENTERSEuc.add(cl);
			}
		}
		System.out.println("Centroid is initialized");
	}
	
	/*
	 * initialize the k clusters by randomly choosing from existing trajectories
	 * this is for the indexing
	 */
	public void initializeClustersRandomForIndex(int k, Set<Integer> data) {
		CENTERSEuc = new ArrayList<>();
		Random rand = new Random();
		ArrayList<Integer> arrayList = new ArrayList<>(data);
		int unit = data.size()/k;
		ArrayList<Integer> tempor = new ArrayList<>();
		int num=0;
		System.out.println("data is");
		for(int t=0; t<data.size(); t++) {
			int idx = arrayList.get(t);
			tempor.add(idx);
			if((t+1)%unit == 0 || t==data.size()-1) {
				if(CENTERSEuc.size() == k-1 && t!=data.size()-1) {
					continue;
				}
				int n = rand.nextInt(unit);
				if( t == data.size()-1)
					n = 0;//the last group, we assign the value directly
				idx = tempor.get(n);
				num += tempor.size();
				double[] clusterdata = accessPointById(idx);// we may loose some data here.
				System.out.println("data is"+idx);
				cluster cl = new cluster(clusterdata, tempor, dimension, dataMatrix);//we need to assign the data gradually
				CENTERSEuc.add(cl);
				tempor = new ArrayList<>();
			}
		}
	//	System.out.println(CENTERSEuc.size());
		System.out.println("total initialized points: "+num);
	}
	
	/*
	 * use the k-means to build the index, when a group has a radius less than the 
	 * threshold use the capacity as k to run kmeans
	 */
	public indexNode runIndexbuildQueuePoint(double radius, int capacity, int fanout) throws IOException {
		root = new indexNode(dimension);// this will store the 
		k = fanout;
		GroupedTrajectory = new HashSet<>();
		System.out.println("Building Hiarachical k-means tree...");
		String LOG_DIR = "./index/index.log";
		PrintStream fileOut = new PrintStream(LOG_DIR);
		System.setOut(fileOut);	
		String groupFilename = datafile+"_"+Integer.toString(trajectoryNumber)+"_"+Double.toString(radius)+"_"+Integer.toString(capacity)+"_index";
		String pivotname = groupFilename+".all";
		pivotGroup = new HashMap<>();
		Queue<Pair<Set<Integer>, indexNode>> queue = new LinkedList<>();//queue with
		Set<Integer> KeySet = new HashSet<>();
		for(int i=1; i<=trajectoryNumber; i++)
			KeySet.add(i);
		Pair<Set<Integer>, indexNode> aPair = new ImmutablePair<>(KeySet, root);// build the index using a queue
		queue.add(aPair);
		usingIndex = false;
		assBoundSign = true;// we use the bound here to accelerate the indexing building.
		indexTKDE02 = false;
		int firstIteration = 0;
		long startTime1 = System.nanoTime();
		while(!queue.isEmpty()) {
			aPair = queue.poll();
			Set<Integer> candidates = aPair.getKey();
			indexNode fatherNode = aPair.getValue();// the nodes 
			CENTERSEuc = new ArrayList<cluster>();	
			interMinimumCentoridDis = new double[k];
			innerCentoridDis = new double[k][];								
			System.out.println("#data points: "+candidates.size());// the size of the dataset
			initializeClustersRandomForIndex(k, candidates);// initialize the center	
			runkmeans(fanout, candidates);// run k-means to divide into k groups
			String content = "";
			for(int i = 0; i<fanout; i++) {
				cluster node = CENTERSEuc.get(i);
				int nodeCapacity = node.getcoveredPoints().size();
			//	content += Integer.toString(node.getTrajectoryID()) + ","; // we should write the name
			}
			int num = 0;
			for(int i = 0; i<fanout; i++) {
				indexNode childNode = new indexNode(dimension);
				cluster node = CENTERSEuc.get(i);
				candidates = node.getcoveredPoints();
				int nodeCapacity = candidates.size();
				num += nodeCapacity;
				if(nodeCapacity==0)// no trajectory
					continue;
			//	System.out.print(nodeCapacity+";");
				double[] nodeSum = new double[dimension];
				double nodeRadius = node.getRadius(candidates, dataMatrix, nodeSum);//compute the distance
			//	System.out.println(nodeSum[0]);
			//	double[] nodeSum = node.getSumTotal();
				childNode.setRadius(nodeRadius);
				childNode.setSum(nodeSum);
				double[] newpivot = new double[dimension];
				for(int j=0; j<dimension; j++)
					newpivot[j] = nodeSum[j]/nodeCapacity;
				childNode.setPivot(node.getcentroid());
				childNode.setTotalCoveredPoints(nodeCapacity);
				content = nodeRadius + ":";
				for (int idx : candidates) {
					content += Integer.toString(idx) + ",";
				}
				if(nodeCapacity <= capacity || nodeRadius <= radius) {// stop splitting and form the leaf node
				/*	if(firstIteration == 0 && !content.equals("0:0,"))
						Util.write(pivotname, content+"\n");//write all the contents into the pivot table
					if (nodeCapacity >= capacity/2 && nodeRadius <= radius) {
						Util.write(groupFilename, content + "\n");// write the group into file
					}else if(nodeCapacity>0) {					
						GroupedTrajectory.addAll(candidates);// add to another file
					}*/
					childNode.addPoint(candidates);	// this is the leaf node				
				}else {
					aPair = new ImmutablePair<Set<Integer>, indexNode>(candidates, childNode);
					queue.add(aPair);// conduct the iteration again
				}
				fatherNode.addNodes(childNode);//add the nodes.
			}
		//	System.out.println("after clustering: "+num);// the size of the dataset
		}
		long endtime = System.nanoTime();	
	//	System.out.println("the height is: "+getHeight(root)+", the #points is: "+getcount(root));
		System.out.println("Indexing time");
		System.out.print((endtime-startTime1)/1000000000.0+ ",");
		return root;//return the root node of the tree.
	}
	
	/*
	 * get the minimum bound
	 */
	public double getMinimumLowerbound(double [] bounds, int groupNumber, int grouplocate) {
		double lowerboud = Double.MAX_VALUE;		
		for(int group_j=0; group_j<groupNumber; group_j++) {//get the minimum lower bound of all group
			if(group_j == grouplocate)
				continue;
			double lowerboud_temp = bounds[group_j+2] - group_drift[group_j];
			if(lowerboud > lowerboud_temp)//choose the minimum one
				lowerboud = lowerboud_temp;
		}
		return lowerboud;
	}
	
	// assignment, all the optimizations are in one functions, Elkan, Harmly, Yinyang, Newlying's bound will be used here
	// if a node cannot be safely assigned, we need to remove it from current cluster and add the children into the queue
	public void assignmentBounds(int k, int groupNumber) {
		Map<Integer, ArrayList<Integer>> idxNeedsIn = new HashMap<>();//it stores all the idxs of trajectories that move in
		Map<Integer, ArrayList<Integer>> idxNeedsOut = new HashMap<>();
		Map<Integer, ArrayList<indexNode>> nodeNeedsIn = new HashMap<>();
		for (int j = 0; j < k; j++) {// combine the inverted index for pruning
			centroidsData[j] = new double[dimension];
			double []clustra = CENTERSEuc.get(j).getcentroid();
			for(int d=0; d<dimension; d++) {
				centroidsData[j][d] = clustra[d];
			}
		}
		computeInterCentoridEuc(k, CENTERSEuc, centroidsData);//compute the inter centroid bound martix
		numeMovedTrajectories= 0;
		for (int group_i = 0; group_i < groupNumber; group_i++) {//check each group
			ArrayList<Integer> centers = group.get(group_i);//get the belonging 
			for (int centerID:centers){//check each center in the group
				Set<indexNode> nodeList = CENTERSEuc.get(centerID).getcoveredNodes();		
				Set<Integer> pointlist = CENTERSEuc.get(centerID).getcoveredPoints();	// the list of points
				if(pointlist.isEmpty() && nodeList.isEmpty())//there is no point or node in this cluster
					continue;		
				Queue<Object> queue = new LinkedList<>(pointlist);// create a queue to store all the candidates node or point			
				for(indexNode aIndexNode: nodeList)
					queue.add(aIndexNode);//add all the nodes
				while(!queue.isEmpty()) {
					int idx=0;
					Object aObject = queue.poll();
					double[] tra;
					double [] bounds= null;
					double radius=0;
					indexNode node = null;
					if(aObject instanceof Integer) {// this is a point
						idx = (int) aObject;
						tra = accessPointById(idx);
						if(allBounds[idx-1]!=null)
							bounds = allBounds[idx-1];
					}else {//this is a node
						nodeReach++;
						node = (indexNode)aObject;
						tra = node.getPivot();
						radius = node.getRadius();
						bounds = node.getBounds();// get the bounds for index node which has consider the radius	
					}
					boolean allPrune = false;//initialized as unpruned
					int newCenterId = centerID;//initialize as the original center		
					double min_dist = Util.EuclideanDis(tra, centroidsData[centerID], dimension);//compute the distance with new center	
					numComputeEuc++;					
					if(aObject instanceof Integer) 	//update the lower bound of point				
						updateSingleLowerBound(trajectoryBounds, idx, group_i, min_dist, groupNumber);
					else// node
						node.updateSingleLowerBound(idx, group_i, min_dist, groupNumber);
					double second_min_dist = 0;
					double radiusExp = 2*min_dist+ interMinimumCentoridDis[centerID];										
					if(assBoundSign) {//check the bound one by one							
						double newupperbound = min_dist + 2*radius;// tighten the upper bound
						double lowerbound=0;
						if(bounds != null && Hamerly)
							lowerbound = getMinimumLowerbound(bounds, groupNumber, group_i);	// bound from drift
						bounCompute++;
						lowerbound = Math.max(lowerbound, interMinimumCentoridDis[centerID]/2.0);//global bounds												
						if(lowerbound < newupperbound){	//cannot not pass the global filtering
							int[] candidate = ExponionBound(centerID, radiusExp);//the ICML'16 exp, used to filter the centroids other than 2 nearest.
							for(int group_j=0; group_j<groupNumber; group_j++) {
								if( group_j == group_i && Yinyang==false)	//skip current group
									continue;								
								double localbound = innerCentoridDisGroup[centerID][group_j]/2.0;
								localbound = 0;
								if(bounds!=null && Hamerly) {
									localbound = Math.max((bounds[group_j+2] - group_drift[group_j]), innerCentoridDisGroup[centerID][group_j]/2.0);
								}
								double newlowerbound = localbound;//use the last one
								bounCompute++;
								if( localbound < newupperbound) {//cannot pass the group filtering of bound 							
									ArrayList<Integer> centerCandidates = group.get(group_j);
									for(int center_j: centerCandidates) {// goto the local filtering on center in a group, by checking the candidate list and bounds												
										double localbound_center = innerCentoridDis[centerID][center_j]/2.0;
										bounCompute++;
										if(localbound_center < newupperbound) {//pass the inner centroid bound prunning
											if(candidate[center_j]==0)//pruned by Exp 
												continue;
											double dist = Util.EuclideanDis(tra, centroidsData[center_j], dimension);//if it is a point
											numComputeEuc++;
											if (min_dist > dist) {
												second_min_dist = min_dist;//pass it to the second one
												min_dist = dist; // maintain the one with min distance
												newCenterId = center_j;											
											}else if(dist<second_min_dist && dist != min_dist){
												second_min_dist = dist;
											}
											if(newlowerbound > dist) {// update the bound with minimum distance in the group
												newlowerbound = dist;
											}
										}else {
											numFillocal++;// local pruning											
										}
									}
								}else {
									numFilGroup += group.get(group_j).size();//pruned 
								}
								if(Hamerly) {
									if(node == null)	//update the lower bound of point
										updateSingleLowerBound(trajectoryBounds, idx, group_j, newlowerbound, groupNumber);
									else
										node.updateSingleLowerBound(idx, group_j, newlowerbound, groupNumber);
								}
							}
							if(node != null) {
								if(second_min_dist - min_dist >= 2*radius)//pruned a node using real distance
									allPrune = true;
							}
						}else {//global filtering: all prune
							allPrune = true;
							if(node!=null)
								numFilGlobal += k*node.getTotalCoveredPoints();//k centroids are all pruned, we also have a group of points
							else
								numFilGlobal += k;
						}
					}else {//brute force if do not use bounds						
						for (int j=0; j<k; j++) {
							if(j==centerID)
								continue;
							double dist = Util.EuclideanDis(tra, centroidsData[j], dimension);
							numComputeEuc++;							
							if (min_dist > dist) {
								second_min_dist = min_dist;//pass it to the second one
								min_dist = dist; // maintain the one with min distance, and second min distance
								newCenterId = j;
							}else if(dist<second_min_dist && dist != min_dist){
								second_min_dist = dist;
							}
						}
						if(second_min_dist - min_dist >= 2*radius)//how to 
							allPrune = true;
					}
					if(aObject instanceof Integer) {// the point moves to other center, this should be counted into the time of refinement.
						CENTERSEuc.get(centerID).addPointToCluster(idx, tra);//the first iteration
						if(newCenterId != centerID) {
							numeMovedTrajectories++;
							long startTime1 = System.nanoTime();							
							ArrayList<Integer> idxlist;
							if(idxNeedsIn.containsKey(newCenterId))
								idxlist = idxNeedsIn.get(newCenterId);
							else
								idxlist = new ArrayList<Integer>();
							idxlist.add(idx);
							idxNeedsIn.put(newCenterId, idxlist);// temporal store as we cannot add them the trajectory list which will be scanned later, batch remove later
							if(idxNeedsOut.containsKey(centerID))
								idxlist = idxNeedsOut.get(centerID);
							else
								idxlist = new ArrayList<Integer>();
							idxlist.add(idx);
							idxNeedsOut.put(centerID, idxlist);// temporal store, batch remove later							
							CENTERSEuc.get(newCenterId).addSum(tra);
							CENTERSEuc.get(centerID).minusSum(tra);							
							long endtime = System.nanoTime();
							assigntime[counter] -= (endtime-startTime1)/1000000000.0;
							refinetime[counter] += (endtime-startTime1)/1000000000.0;
						}
					}
					if(aObject instanceof indexNode) {// this is a node
						if (allPrune == false) {//this node cannot be pruned
							if(!node.getNodelist().isEmpty())
								for (indexNode childnode : node.getNodelist()) {
									if(Hamerly)
										childnode.setBounds(node.getBounds(), groupNumber);
									queue.add(childnode);// push all the child node or point with farther's bounds to the queue,
								}
							else
								for (int childpoint : node.getpointIdList()) {//update the bounds
									if(Hamerly) {
										allBounds[childpoint-1] = new double[groupNumber+2];
										for(int i=0; i<groupNumber; i++) {
											allBounds[childpoint-1][i+2] = node.getBounds()[i+2] - distanceToFather[childpoint];
										}																			
									}
									queue.add(childpoint);// push all the child node or point with father's bounds to the queue,
								}
							CENTERSEuc.get(centerID).removeNode(node);//this should be revised, store them temporally.
						}else {
							prunenode++;
							if(newCenterId != centerID) {//the nodes need to leave current node
								CENTERSEuc.get(centerID).removeNode(node);								
								numeMovedTrajectories += node.getTotalCoveredPoints();								
							}							
							ArrayList<indexNode> nodelist;
							if(nodeNeedsIn.containsKey(newCenterId))
								nodelist = nodeNeedsIn.get(newCenterId);
							else
								nodelist = new ArrayList<indexNode>();
							nodelist.add(node);
							nodeNeedsIn.put(newCenterId, nodelist);//temporal store
						}
					}
				}
			}
		}
		updatePointsToCluster(idxNeedsIn, idxNeedsOut);
		updateNodesToCluster(nodeNeedsIn);
		System.out.println("#moved point "+numeMovedTrajectories+", #computations: "+numComputeEuc);	
	}

	/*
	 * update the trajectories in each clusters
	 */
	public void updatePointsToCluster(Map<Integer, ArrayList<Integer>> idxNeedsIn, Map<Integer, ArrayList<Integer>> idxNeedsOut) {
		for(int idx: idxNeedsIn.keySet()) {
			ArrayList<Integer> idxs = idxNeedsIn.get(idx);
			cluster newCluster = CENTERSEuc.get(idx);
			newCluster.mergePointToCluster(idxs);
		}
		for(int idx: idxNeedsOut.keySet()) {
			ArrayList<Integer> idxs = idxNeedsOut.get(idx);
			cluster newCluster = CENTERSEuc.get(idx);
			newCluster.removePointToCluster(idxs);
		}
	}
	
	/*
	 * update the trajectories in each clusters
	 */
	public void updateNodesToCluster(Map<Integer, ArrayList<indexNode>> nodeNeedsIn) {
		for(int idx: nodeNeedsIn.keySet()) {
			ArrayList<indexNode> idxs = nodeNeedsIn.get(idx);
			cluster newCluster = CENTERSEuc.get(idx);
			newCluster.mergeNodesToCluster(idxs);
		}
	}
	
	/*
	 * get the possible two nearest neighbors, ICML16 Newling
	 */
	public int[] ExponionBound(int i,  double radius) {
		int centroidList[] = new int[k];
		for(int i1=0; i1<k; i1++) {
			if(i1==i)
				continue;
			if(innerCentoridDis[i][i1] <= radius)//the distance from the other centroids to current centroid
				centroidList[i1] = 1;
		}
		return centroidList;
	}
	
	/*
	 * rank all centroids based on the norm distance to the origin, return the possible centroids
	 */
	public ArrayList<Integer> AnnularBound(Map<Integer, double[]> clustData, 
			double []currentCentroid, double radius) {
		double annbound[] = new double[k];		
		double distance = 0;
		for(int i=0; i<dimension; i++) {
			distance += Math.pow(currentCentroid[i], 2);
		}
		double centroidNorm = Math.sqrt(distance);
		ArrayList<Integer> centroidList = new ArrayList<>();
		int count=0;
		for(double[] centroid: clustData.values()) {
			distance = 0;
			for(int i=0; i<dimension; i++) {
				distance += Math.pow(centroid[i], 2);
			}
			annbound[count] = Math.sqrt(distance);
			if(Math.abs(annbound[count++]-centroidNorm)<radius)
				centroidList.add(count);
			count++;
		}
		return centroidList;
	}
	
	public Map<Integer, ArrayList<Integer>> GroupsBuilt(){
		groupsBuilt = new HashMap<>();
		int count = 0;
		for(int i=0; i<k; i++) {			
			ArrayList<Integer> arrayList = new ArrayList<Integer>();
			for(int ids: CENTERSEuc.get(i).getcoveredPoints()) {
				arrayList.add(ids-1);
				count++;
			}			
			groupsBuilt.put(i, arrayList);
		}
		System.out.println(count);
		return groupsBuilt;
	}
	
	/*
	 * divide k clusters into t groups when k not equals to t
	 */
	protected void groupInitialClusters(int t, int k) throws IOException {
		group = new HashMap<>();
		centerGroup = new HashMap<>();
		if(t==k) {// when k is small, we do not divide into too many groups
			for(int i = 0;i<k; i++) {
				ArrayList<Integer> a = new ArrayList<>();
				a.add(i);
				group.put(i, a);
				centerGroup.put(i, i);
			}
		}else {
			String LOG_DIR = "./seeds/Groupcentroid.txt";
			PrintStream fileOut = new PrintStream(LOG_DIR);
			System.setOut(fileOut);	
			for(int i = 0; i<k; i++) {
				double[] aa = CENTERSEuc.get(i).getcentroid();
				int counter1 = 0;
				for(double id: aa) {
					System.out.print(id);
					if(counter1++ < aa.length-1)
						System.out.print(",");
				}
				System.out.println();
			}
			String LOG_DIR1 = "./seeds/Groupcentroid.log";
			PrintStream fileOut1 = new PrintStream(LOG_DIR1);
			System.setOut(fileOut1);
			String args[] = new String[7];
			args[0] = LOG_DIR;
			args[1] = Integer.toString(t);// groups
			args[2] = Integer.toString(k);// number of data points
			args[5] = "0";
			args[6] = Integer.toString(dimension_end-dimension_start);
			kmeansAlgorithm<?> run2 = new kmeansAlgorithm(args);
			run2.staticKmeans(false, false, false);
			group = run2.GroupsBuilt();			
			for(int i = 0; i<t; i++) {
				ArrayList<Integer> centers = group.get(i);
				for(int center: centers) {
					centerGroup.put(center, i);
				}
			}
			System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		}
		System.out.println("The centroids are grouped");
	}
	
	/*
	 * compute the sum distance after refinement
	 */
	public double getSumDis() {
		double sum = 0;
		Set<Integer> allPoints = new HashSet<>();
		for(int i=0; i<k; i++) {
			sum += CENTERSEuc.get(i).computeSum(dataMatrix, allPoints);
		}
		System.out.println(allPoints.size());
		return sum;
	}
	
	public int getPunedNodes() {
		int sum = 0;
		for(int i=0; i<k; i++) {
			sum += CENTERSEuc.get(i).computeNumberofNode();
		}
		return sum;
	}
	
	//candidate set stores the keyset of the dataset
	public int runkmeans(int k, Set<Integer> candidateset) throws IOException {
		int groupNumber = k;
		if(k>10 && Yinyang)//used for the grouping
			groupNumber = k/10;
		numComputeEuc = 0;
		bounCompute = 0;
		nodeReach = 0;
		dataReach = 0;
		groupInitialClusters(groupNumber, k); // Step 1: divide k centroid into t groups
		interMinimumCentoridDis = new double[k];
		innerCentoridDis = new double[k][];
		innerCentoridDisGroup = new double[k][];
		centroidsData = new double[k][];
		centoridData = new ArrayList<>();
		trajectoryBounds = new HashMap<>();
		allBounds = new double[trajectoryNumber][];
		center_drift = new HashMap<Integer, Double>();
		group_drift = new double[groupNumber];
		for (int i = 0; i < groupNumber; i++) {
			group_drift[i] = Double.MAX_VALUE;// initialize as max in the begining
		}
		int t = 0;
		double finalsum =0 ;
		double []iterationtime = new double[TRY_TIMES];
		for(; t < TRY_TIMES; t++){
			long startTime1 = System.nanoTime();		
			assignmentBounds(k, groupNumber);
			long endtime = System.nanoTime();
			assigntime[counter] += (endtime-startTime1)/1000000000.0;
			long startTime = System.nanoTime();
			for(int i=0; i<groupNumber; i++) {
	        	group_drift[i] = 0;// initialize as min
	        }
			for(int i=0; i<k; i++) {
				double drfit = 0;
				if(assBoundSign)//we use the incremental for bounds only.
					drfit = CENTERSEuc.get(i).extractNewCentroidByMeansIncremental();
				else {
					dataReach += CENTERSEuc.get(i).getcoveredPoints().size();
					drfit = CENTERSEuc.get(i).extractNewCentroidByMeans(dataMatrix);//this moves
				}
				center_drift.put(i, drfit);				
				int groupid = centerGroup.get(i);
				if(group_drift[groupid] < drfit) //update the group drift as maximum
					group_drift[groupid] = drfit;
			}
			endtime = System.nanoTime();
			refinetime[counter] +=(endtime-startTime)/1000000000.0;		
			System.out.print("\niteration "+(t+1)+", time cost: ");
			System.out.printf("%.5f", (endtime-startTime1)/1000000000.0);
			iterationtime[t] = (endtime-startTime1)/1000000000.0;
			System.out.println("s");
			if(numeMovedTrajectories==0) {
				runrecord.setIterationtimes(t+1);
				break;//convergence
			}	
		//	System.out.println("the sum distance after refinement is: "+getSumDis());	
			if(indexTKDE02) {//we will empty every cluster, traverse the tree from root again
				boolean first = true;
				for(cluster clus:CENTERSEuc) {
					if(first) {
						clus.reset(root); // reset this as root
						first = false;
					}else {
						clus.reset(null);
					}
				}
			}
		}		
		System.out.println("\n#filtered points by bound: "+(numFilGroup+numFilGlobal));
		System.out.println("#moved trajectories: "+numeMovedTrajectories);
		System.out.println("#computation: "+numComputeEuc);
		System.out.println("Sum is: "+finalsum);
		System.out.println("#Pruned nodes: "+getPunedNodes());
		for(int i=0; i<TRY_TIMES; i++)
			System.out.println(iterationtime[i]);
		System.out.println();
		computations[counter] += numComputeEuc;
		boundaccess[counter] += bounCompute;
		dataAccess[counter] += dataReach;
		nodeAccess[counter] += nodeReach;
		return t;
	}
	
	/*
	 * get the highest weight of the tree
	 */
	public int getHeight(indexNode root) {
		if(root.isLeaf())
			return 0;
		else {
			Set<indexNode> listnode = root.getNodelist();
			int max = 0;
			for(indexNode aIndexNode: listnode) {
				if(getHeight(aIndexNode)>max) {
					max = getHeight(aIndexNode);
				}
			}
			return max+1;
		}
	}
	
	/*
	 * get the count of the tree.
	 */
	public int getcount(indexNode root) {
		if(root == null)
			return 0;
		if(root.isLeaf()) {	
			return root.getpointIdList().size();
		}
		else {
			Set<indexNode> listnode = root.getNodelist();
			int max = 0;
			for(indexNode aIndexNode: listnode) {
				max += getcount(aIndexNode);
			}
			return max;
		}
	}
	
	/*
	 * get the count of the tree.
	 */
	public int getNodesCount(indexNode root) {
		if(root == null)
			return 0;
		if(root.isLeaf()) {	
			return 1;
		}else {
			Set<indexNode> listnode = root.getNodelist();
			int max = listnode.size();
			for(indexNode aIndexNode: listnode) {
				max += getNodesCount(aIndexNode);
			}
			return max;
		}
	}
	
	/*
	 * get the count of the tree.
	 */
	public void setBoundsEmpty(indexNode root) {
		root.setBoundsEmpty();
		Set<indexNode> listnode = root.getNodelist();
		for (indexNode aIndexNode : listnode) {
			aIndexNode.setBoundsEmpty();
		}
	}
	
	/*
	 * compute the distance from child (node or point) to father node
	 */
	public void computeFartherToChild(indexNode root) {
		double[] pivot = root.getPivot();
		if(root.isLeaf()) {
			Set<Integer> listpoints = root.getpointIdList();
			for(int pointid: listpoints) {
				double[] childPivot = accessPointById(pointid);
				double distance = Util.EuclideanDis(pivot, childPivot, dimension);
				distanceToFather[pointid-1] = distance; //create a map to store this value
			}
		}else {
			Set<indexNode> listnode = root.getNodelist();
			for(indexNode aIndexNode: listnode) {
				double[] childPivot = aIndexNode.getPivot();
				double distance = Util.EuclideanDis(pivot, childPivot, dimension);
				aIndexNode.setdistanceToFarther(distance);
				computeFartherToChild(aIndexNode);
			}
		}
	}
	
	public void staticKmeans(boolean index, boolean bound, boolean tkde02) throws IOException {
		usingIndex = index;
		if(dataMatrix==null)// avoid importing the data every time
			loadDataEuc(datafile, trajectoryNumber);
		if(usingIndex && root == null) {
			root = runIndexbuildQueuePoint(0.01, 20, 10);//radius, capacity, and fanout, we 
		}
		initializeClustersRandom(k); 	 //randomly choose k		
		assBoundSign = bound;
		indexTKDE02 = tkde02;
		Set<Integer> KeySet = new HashSet<>();
		for(int i=1; i<=trajectoryNumber; i++)
			KeySet.add(i);
		runkmeans(k, KeySet);
		time[counter] = assigntime[counter] + refinetime[counter];
		System.out.println("the overall time is "+time[counter]+"s: "+ assigntime[counter]+" "+refinetime[counter]+"\n");
		counter++;
		if(root!=null)
			setBoundsEmpty(root); //reset the bound in the node for next test
		runrecord.clear();
		clearStatistics();
	}
	
	void InitializeAllUnifiedCentroid(int maximumk, int group) {
		allCentroids = new int[group][];
		Random rand = new Random();
		for(int i=0; i<group; i++) {
			allCentroids[i] = new int[maximumk]; //it is initialized to use a same centroid set.	
			for(int j=0; j<maximumk; j++) {
				allCentroids[i][j] = rand.nextInt(trajectoryNumber)+1;
			}
		}
	}
	
	void InitializeUnifiedCentroid() {
		centroids = new int[k]; //it is initialized to use a same centroid set.
		Random rand = new Random();
		for(int i=0; i<k; i++) {
			centroids[i] = rand.nextInt(trajectoryNumber)+1;
		}
	}
	
	void writelogs(int testTime, String indexname) throws FileNotFoundException {
		String LOG_DIR = "./logs/icml_logs2/"+datafilename+"_"+trajectoryNumber+"_"+dimension+"_"+k+"_"+indexname+"_"+capacity+".log";
		PrintStream fileOut = new PrintStream(LOG_DIR);
		System.setOut(fileOut);			
		for(int i=0; i<5; i++) {			// show the time speedup over Lloyd algorithm
			System.out.printf("%.2f",time[i]/testTime);
			System.out.print(" & ");
		}
		System.out.println();
		for(int i=0; i<5; i++) {			// show the time speedup over Lloyd algorithm
			System.out.printf("%.2f",1/(time[i]/time[0]));
			System.out.print(" & ");	
		}
		System.out.println();
		System.out.println();
		
		for(int i=0; i<5; i++) {			// show the time speedup over Lloyd algorithm
			System.out.printf("%.2f",assigntime[i]/testTime);
			System.out.print(" & ");
		}
		System.out.println();
		for(int i=0; i<5; i++) {			// show the time speedup over Lloyd algorithm
			System.out.printf("%.2f",1/(assigntime[i]/assigntime[0]));
			System.out.print(" & ");	
		}
		System.out.println();
		System.out.println();
		
		for(int i=0; i<5; i++) {			// show the time speedup over Lloyd algorithm
			System.out.printf("%.2f",refinetime[i]/testTime);
			System.out.print(" & ");
		}
		System.out.println();
		for(int i=0; i<5; i++) {			// show the time speedup over Lloyd algorithm
			System.out.printf("%.2f",1/(refinetime[i]/refinetime[0]));
			System.out.print(" & ");	
		}
		System.out.println();
		System.out.println();
		
		for(int i=0; i<5; i++){		// show the #computation speedup over Lloyd algorithm				
			System.out.print(computations[i]/testTime);
			System.out.print(" & ");	
		}
		System.out.println();
		for(int i=0; i<5; i++) {			// show the #computation speedup over Lloyd algorithm
			System.out.printf("%.2f",1-computations[i]/(double)computations[0]);
			System.out.print(" & ");
		}
		
		System.out.println();
		System.out.println();
		for(int i=0; i<5; i++){		// show the #computation speedup over Lloyd algorithm				
			System.out.print(boundaccess[i]/testTime);
			System.out.print(" & ");	
		}
		
		System.out.println();
		System.out.println();
		for(int i=0; i<5; i++){		// show the #computation speedup over Lloyd algorithm				
			System.out.print(dataAccess[i]/testTime);
			System.out.print(" & ");	
		}
		
		System.out.println();
		System.out.println();
		for(int i=0; i<5; i++){		// show the #computation speedup over Lloyd algorithm				
			System.out.print(nodeAccess[i]/testTime);
			System.out.print(" & ");
		}
		for(int i=2; i<5; i++) {//only maintain the Lloyd's and Sequential as they will not be affected by the type of index
			computations[i] = 0;
			time[i] = 0;
			assigntime[i] = 0;
			refinetime[i] = 0;
			dataAccess[i] = 0;
			boundaccess[i] = 0;
			nodeAccess[i] = 0;
		}
	}
	
	/*
	 * we test different baselines, parameters
	 */
	public void experiments(int []setK, int testTime) throws IOException, KeySizeException, KeyDuplicateException {	
	//	Hamerly = false;
		Yinyang = true;
		if(Hamerly=false)
			Yinyang = false;		
		loadDataEuc(datafile, trajectoryNumber);	// load the data and create index
		indexAlgorithm<Object> indexkd = new indexAlgorithm<>();
		indexNode rootHKT=null, rootMtree=null, rootBall=null;
	//	rootHKT = runIndexbuildQueuePoint(0, capacity, 10);//load the dataset and build one index for all testing methods
		String LOG_DIR = "./logs/icml_logs2/"+datafilename+"_"+trajectoryNumber+"_"+dimension+"_"+capacity+"_index.log";
		PrintStream fileOut = new PrintStream(LOG_DIR);
		System.setOut(fileOut);	
		rootBall = indexkd.buildBalltree2(dataMatrix, dimension, capacity);
	//	rootMtree = indexkd.buildMtree(datamapEuc, dimension, capacity);
		System.out.println(getNodesCount(rootHKT)+" "+ getNodesCount(rootBall) + " "+getNodesCount(rootMtree));
		for(int i=0; i<5; i++) {//only maintain the Lloyd's and Sequential as they will not be affected by the type of index
			computations[i] = 0;
			time[i] = 0;
			assigntime[i] = 0;
			refinetime[i] = 0;
			dataAccess[i] = 0;
			boundaccess[i] = 0;
			nodeAccess[i] = 0;
		}
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		for(int kvalue: setK) {//test various k
			k = kvalue;
			ArrayList<Pair<indexNode, String>> roots =  new ArrayList<>();
			roots.add(new ImmutablePair<>(rootHKT, "HKT"));
			roots.add(new ImmutablePair<>(rootBall, "BallMetrix"));
			roots.add(new ImmutablePair<>(rootMtree, "Mtree"));			
			InitializeAllUnifiedCentroid(1000, testTime);//maximum k, time time
			boolean LloydandSeq = false;
			for(Pair<indexNode, String> newroot: roots) { 
				root = newroot.getLeft();
				if(root==null)
					continue;
				distanceToFather = new double[trajectoryNumber];//store the point distance
				computeFartherToChild(root);
				String indexname = newroot.getRight();
				if(!indexname.equals("HKT"))
					nonkmeansTree = false;			
				System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
				for (int testtime = 0; testtime < testTime; testtime++) {				
					counter = 0;
					centroids = new int[k];
					for(int i=0; i<k; i++)
						centroids[i] = allCentroids[testtime][i];					
					if(!LloydandSeq) {
						System.out.println("Lloyd");
						staticKmeans(false, false, false);// the lloyd's algorithm, the standard method
						System.out.println("Sequential");
						staticKmeans(false, true, false);// the sequential method, ICML16 & ICML15
					}
					counter = 2;//jump the first two					
					System.out.println("Index-TKDE");
					staticKmeans(true, false, true);// the index method that traverse the index every time, but without any bound computation.				
					System.out.println("Index-single");
					staticKmeans(true, true, false);// the unified framework we propose, traversing the tree in single time
					
					System.out.println("Index-multiple");//if the pruning in the first iteration is good, we can set indexTKDE02 as true, traverse the tree from start.
					staticKmeans(true, true, true);// the unified framework we propose, traversing the tree in every iteration
			
					System.out.println(k+" "+testtime+"============================= ends here!");//write into logs for comparison						
				}
				LloydandSeq = true;
				writelogs(testTime, indexname);
			}
		}
		counter = 0;
	}
}
