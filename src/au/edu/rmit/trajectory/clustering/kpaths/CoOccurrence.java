package au.edu.rmit.trajectory.clustering.kpaths;

import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.math.RoundingMode;
import java.text.DecimalFormat;
public class CoOccurrence {
	private static DecimalFormat df2 = new DecimalFormat("#.###");
	public CoOccurrence() {
		// TODO Auto-generated constructor stub
	}
	
	public void buildMatrix(Map<Integer, int[]> datamap, String output) {
		
		//get the count for each edge,
		Multiset<String> edges_frequency = HashMultiset.create();
		Multiset<Double> coOccur = HashMultiset.create();
		HashMap<String, Double> cooccur_probability = new HashMap<>();
		for(int traid: datamap.keySet()) {
			System.out.println(traid);
			int[] trajectory = datamap.get(traid);
			for(int i=0; i<trajectory.length; i++) {
				for(int j=i+1; j<trajectory.length; j++) {					
					String cooccur = Integer.toString(trajectory[i])+","+Integer.toString(trajectory[j]);
					edges_frequency.add(cooccur);
				}
			}	
		}
		for(String aString: edges_frequency.elementSet()) {
			int count = edges_frequency.count(aString);
			String []adStrings= aString.split(",");
			String reverseString = adStrings[1]+","+adStrings[0];
			int reverseCount = edges_frequency.count(reverseString);
			if(cooccur_probability.containsKey(aString) ||cooccur_probability.containsKey(reverseString))
				continue;
			double probablebility;
			String temp = aString;
			if(count>reverseCount) {
				probablebility = (double)count/(count+reverseCount);
			//	cooccur_probability.put(aString, probablebility);
			}else {
				probablebility = (double)reverseCount/(count+reverseCount);
			//	cooccur_probability.put(reverseString, probablebility);
				temp = reverseString;
			}
			int all=count+reverseCount;
		//	System.out.println(Math.round(probablebility * 100.0)/100.0);
		//	coOccur.add(Math.round(probablebility * 1000.0)/1000.0);
			coOccur.add(Math.round(probablebility * 1000.0)/1000.0, all);	
		//	Util.write(output, aString+"\t"+df2.format(probablebility)+"\t"+count+"\t"+reverseCount+"\n");//output the frequency
		//	Util.write(output, df2.format(probablebility)+"\t"+(count+reverseCount)+"\n");//output the frequency
		}
		for(double a: coOccur.elementSet()) {
			Util.write(output, a+"\t"+coOccur.count(a)+"\n");//output the frequency
		}
	}
}
