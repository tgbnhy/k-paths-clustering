# Fast large-scale trajectory clustering
## Technical Report

https://github.com/tgbnhy/k-paths-clustering/blob/master/k-paths-vldb.pdf

## Introduction
This repo holds the source code and scripts for reproduce the key experiments of k-paths trajectory clustering.

## Usage


1. If you run in Eclipse, just go to "au.edu.rmit.trajectory.expriments.kpathEfficiency", and click the "run configuration", creat a new java application, and fill the following parameters:

```
.\data_porto\reassign\porto_mm_edge.dat 10 1000000 .\data_porto\reassign\new_edge_street.txt .\data_porto\reassign\new_graph.txt Porto
```
There are six parameters:
```
arg[0] is the trajectory data file
arg[1] is the number of clusters (k)
arg[2] is the number of trajectories in the datafile which will be clustered (|D|)
arg[3] is the edge info file which contains the street name
arg[4] is the road network graph file
arg[5] is the city name.
```
Then, all the result will be recorded into the log file under the "logs" folder.

2. If you want to run from commands (recommended):

```
mvn clean package
```
A file "torch-clus-0.0.1-SNAPSHOT.jar" will be generated under folder "target".

 #run the tdrive clustering for efficiency comparision.
```
 java -Xmx16192M -cp ./torch-clus-0.0.1-SNAPSHOT.jar au.edu.rmit.trajectory.expriments.kpathEfficiency ./data_tdrive/beijing_mm_edge.txt.reassign 10 250997 ./data_tdrive/new_id_edge_raw_beijing.txt ./data_tdrive/beijing_graph_new.txt tdrive
```
 #run the porto clustering for efficiency comparision.
```
 java -Xmx16192M -cp ./torch-clus-0.0.1-SNAPSHOT.jar au.edu.rmit.trajectory.expriments.kpathEfficiency ./data_porto/porto_mm_edge.dat 10 1565595 ./data_porto/new_edge_street.txt ./data_porto/new_graph.txt porto
```
 
 #run the porto clustering, and produce clustering results for visualization.
```
 #java -Xmx16192M -cp ./torch-clus-0.0.1-SNAPSHOT.jar au.edu.rmit.trajectory.clustering.Running ./data_porto/porto_mm_edge.dat 10 100000 ./data_porto/new_edge_street.txt ./data_porto/new_graph.txt porto
```
 #run the tdrive clustering, and produce clustering results for visualization.
```
 #java -Xmx16192M -cp ./torch-clus-0.0.1-SNAPSHOT.jar au.edu.rmit.trajectory.clustering.Running ./data_tdrive/beijing_mm_edge.txt.reassign 10 10000 ./data_tdrive/new_id_edge_raw_beijing.txt ./data_tdrive/beijing_graph_new.txt tdrive
```
 
 #compare with other distance measure in Tdrive dataset
```
 java -Xmx16192M -cp ./torch-clus-0.0.1-SNAPSHOT.jar au.edu.rmit.trajectory.expriments.EBD ./data_tdrive/beijing_mm_edge.txt.reassign 10 1000 ./data_tdrive/new_id_edge_raw_beijing.txt ./data_tdrive/beijing_graph_new.txt tdrive
```
 #compare with other distance measure on Porto dataset
```
 java -Xmx16192M -cp ./torch-clus-0.0.1-SNAPSHOT.jar au.edu.rmit.trajectory.expriments.EBD ./data_porto/porto_mm_edge.dat 10 100000 ./data_porto/new_edge_street.txt ./data_porto/new_graph.txt porto
```

## Datasets
We use the map-matched dataset, and trajectory data composed of integer ids. Since they have a size above the standard of Github, we store it in Google Drive, and you can find the dataset from:
https://sites.google.com/site/shengwangcs/torch

Download the trajectory dataset from the above link, and put the dataset into "data_porto" or "data_tdrive". (The road network graph datasets are already there.)

## Visualization
We use MapV (https://github.com/huiyan-fe/mapv) to visulized the cluster result using different color.

If you are familar with javascript, you can use WebStorm (https://www.jetbrains.com/webstorm/) to open the webpage and see how the data is demonstrated.

An online visualization using dynamic flow can also be found in http://203.101.224.103:8080/TTorchServer/.
![alt text](visualization/direction1.gif)

## Citation
If you use our code for research work, please cite our paper as below:
```
@inproceedings{wang2019kpaths,
  author          = {{Wang}, Sheng and {Bao}, Zhifeng and {Culpepper}, J. Shane and {Sellis}, Timos and {Qin}, Xiaolin},
  title           = "{Fast Large-Scale Trajectory Clustering}",
  year            = 2019,
}
```
If you use our mapped trajectory dataset for research work, please cite our paper as below:
```
@inproceedings{wang2018torch,
  author          = {{Wang}, Sheng and {Bao}, Zhifeng and {Culpepper}, J. Shane and {Xie}, Zizhe and {Liu}, Qizhi and {Qin}, Xiaolin},
  title           = "{Torch: {A} Search Engine for Trajectory Data}",
  booktitle       = {Proceedings of the 41th International ACM SIGIR Conference on Research & Development in Information Retrieval},
  organization    = {ACM},
  pages     = {535--544},
  year            = 2018,
}
```
