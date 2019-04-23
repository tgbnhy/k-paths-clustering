package au.edu.rmit.trajectory.clustering;

import java.io.IOException;
import java.sql.SQLException;

import au.edu.rmit.mtree.MTree;
import au.edu.rmit.mtree.MTree.KMeansHMTree;
import au.edu.rmit.trajectory.clustering.kpaths.KPathsOptimization;
import au.edu.rmit.trajectory.clustering.kpaths.KPaths;

public class Running {
	public static void main(String[] args) throws IOException, SQLException, InterruptedException {
		boolean indexPivot = true;
		boolean indexInverte = true;
	//	Process aProcess = new Process(args);
	//	aProcess.staticKpath(args, false);
	//1565595
		//250997
		for(int i=0; i<100; i++)
		for(int scale = 250997 ; scale<=250997; scale*=10) {//create the index for different using different radius.
			args[2] = Integer.toString(scale);
			KPathsOptimization run2 = new KPathsOptimization(args);
			run2.staticKpath(false, i);
			run2.staticKpath(true, i);
		//	run2.runIndexbuildQueue(10, 20); // the first parameter is radius, the second is the capacity, 
		}
	}
}