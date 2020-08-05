# Graph Visualizer

Sometimes it becomes very hard to debug a graph related problem in Competitive programming. And beginners find it difficult to understand the common graph related algorithms. This tool is an attempt to solve (maybe?) them both.

Just use graph visualizer's functions with your c++ code and run.    

## C++ Functions 
##### You can just use these functions in the debugger with your cpp code  
#### Building the graph
```c++
void add_edge(int u,int v);
```
Adds an undirected edge between u and v.
```c++
void build_graph();
```
Builds the graph with previously provided edges. Edges that are provided after this 
function call will not be added to the graph. Total node number is determined by the highest value used in the ``add_edge(int u,int v)`` function. Unused nodes will be shown at the bottom. 
#### Example
```c++
#include <bits/stdc++.h>
using namespace std;

int main(){

   add_edge(1,2);
   add_edge(2,4);
   add_edge(2,3);
   add_edge(1,5);
   build_graph();
   add_edge(2,5); //this edge won't be added


   return 0;
}

``` 
![Result Image](https://github.com/faridul-reja/GraphVisualizer/blob/master/download/example1.PNG)

