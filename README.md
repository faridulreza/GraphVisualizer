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
#### Result
![Result Image](https://github.com/faridul-reja/GraphVisualizer/blob/master/download/example1.PNG)

## Command functions
##### These functions will be executed after the graph is built. You can use these functions anywhere in your code . The line from where these functions are called will be highlighted when you start the visualizer.
```c++
 void show_edge(int from,int to);
 void show_edge(int from,int to,string color);
```
Highlights the edge from-to with arrow. Green is used as the default arrow color. color parameter can be `"red", "yellow", "purple", "orange", "blue"`. 
```c++
  void update_node(int CurentNode,int PreviousNode,string TAG, string / int /long /double msg);
  void update_node(int CurentNode,string TAG, string / int /long /double msg);
  
```
Updates the value of the `CurrentNode` with `tag` : `msg`. if `PreviousNode` is specified the edge `PrevoiusNode - CurrentNode` is highlighted with red arrow. `TAG` can be anything except `"multiple"` . 

if `TAG` == `"multiple"` this function expects multiple tag and msg value separated by `,` in the `msg` parameter . Thus ``update_node(0,"multiple", "visited, yes,value , 10");`` shows at node 0 

```
visited: yes 
value: 10
```


```c++
  void alert(string/ int/ long/ double  msg);
```
Pops up an alert window with specified `msg` and waits for confimation.
```c++
  void logger(string/ int/ long/ double msg);
```
Writes the `msg` in a small window . The written value will persists throughout the execution.
## Download (windows only)
#### Download and extract the zip file . start Graph Visualizer. 

#### Java and MinGW must be installed to run the application
[download](https://github.com/faridul-reja/GraphVisualizer/raw/master/download/GraphVisualizer.zip)
