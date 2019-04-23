# k-paths
## Introduction
This repo holds the source code and experiments of k-paths clustering.

## Usage

1. If you run in Eclipse, just go to run configuration, creat a new java application, and fill the following parameters:

E:\dataset\new_porto\reassign\porto_mm_edge.dat 10 1000000 E:\dataset\new_porto\reassign\new_edge_street.txt E:\dataset\new_porto\reassign\new_graph.txt

Then, all the result will be recorded into the log file under the "logs" folder.

arg[0] is the data file
arg[1] is the number of clusters
arg[2] is the number of trajectories in the datafile which will be clustered
arg[3] is the edge info file which contains the street name
arg[4] is the graph file
arg[5] is the city name. 

2. If you want to run from commands (recommended):
A. mvn clean package
A .jar file will be generated under folder "target".
 java -Xmx16192M -cp ./torch-clus-0.0.1-SNAPSHOT.jar au.edu.rmit.trajectory.expriments.EBD ./data_porto/porto_mm_edge.dat 10 100000 ./data_porto/new_edge_street.txt ./data_porto/new_graph.txt porto

 #run the tdrive clustering
 java -Xmx16192M -cp ./torch-clus-0.0.1-SNAPSHOT.jar au.edu.rmit.trajectory.expriments.kpathEfficiency ./data_tdrive/beijing_mm_edge.txt.reassign 10 250997 ./data_tdrive/new_id_edge_raw_beijing.txt ./data_tdrive/beijing_graph_new.txt tdrive
 #run the porto clustering
 java -Xmx16192M -cp ./torch-clus-0.0.1-SNAPSHOT.jar au.edu.rmit.trajectory.expriments.kpathEfficiency ./data_porto/porto_mm_edge.dat 10 1565595 ./data_porto/new_edge_street.txt ./data_porto/new_graph.txt porto
 
 #run the test or build index
 #java -Xmx16192M -cp ./torch-clus-0.0.1-SNAPSHOT.jar au.edu.rmit.trajectory.clustering.Running ./data_porto/porto_mm_edge.dat 10 100000 ./data_porto/new_edge_street.txt ./data_porto/new_graph.txt porto
 #run the tdrive clustering
 #java -Xmx16192M -cp ./torch-clus-0.0.1-SNAPSHOT.jar au.edu.rmit.trajectory.clustering.Running ./data_tdrive/beijing_mm_edge.txt.reassign 10 10000 ./data_tdrive/new_id_edge_raw_beijing.txt ./data_tdrive/beijing_graph_new.txt tdrive
 
 #compare with other distance measure
 java -Xmx16192M -cp ./torch-clus-0.0.1-SNAPSHOT.jar au.edu.rmit.trajectory.expriments.EBD ./data_tdrive/beijing_mm_edge.txt.reassign 10 1000 ./data_tdrive/new_id_edge_raw_beijing.txt ./data_tdrive/beijing_graph_new.txt tdrive
 #run the porto clustering


### Datasets
We use the map-matched dataset, it covers the road network, and trajectory data composed of integer ids.
https://sites.google.com/site/shengwangcs/torch

## Paper


## Visualization
We use mapv to visulized the cluster result using different color.


## Citation

