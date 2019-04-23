package au.edu.rmit.trajectory.clustering.kpaths;

import java.util.ArrayList;
import java.util.Map;

import au.edu.rmit.trajectory.clustering.visualization.mapv;

public class SIGMOD07 {

	public SIGMOD07() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * this will produce the edges that have a higher frequency than the threshold, SIGMOD07 Trajectory clustering: a partition-and-group framework
	 * into the file-output,
	 */
	public static void SIGMOD07FrequentEdgesMapv(Map<Integer, Integer> edgeHistogram, Map<Integer, String> edgeInfo,
			int frequencyThreshold, String output) {
		ArrayList<Integer> highFreEdge = new ArrayList<Integer>();
		for (int edgeid : edgeHistogram.keySet()) {
			if (edgeHistogram.get(edgeid) > frequencyThreshold) {
				highFreEdge.add(edgeid);
			}
		}
		mapv.generateHighEdges(edgeInfo, highFreEdge, output);
	}
}
