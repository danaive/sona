# MLR

## 1. Algorithm Introduction
### Model
MLR is a sub-regional linear model that is widely used in advertising ctr estimates. MLR adopts the divide and conquer strategy: firstly divide the feature space into multiple local intervals, then fit a linear model in each local interval, and the output is the weighted sum of multiple linear models. These two steps are to minimize the loss function. For the goal, learn at the same time. For details, see the Large Scale Piece-wise Linear Model (LS-PLM).
The MLR algorithm has three distinct advantages:
1. Nonlinear：Choosing enough partitions, the MLR algorithm can fit arbitrarily complex nonlinear functions.
2. Scalability：Similar to the LR algorithm, the MLR algorithm has a good scalability for massive samples and ultra-high dimensional models.
3. Sparsity：MLR algorithm with![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20L_1),![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20L_{2,1})regular terms can get good sparsity.

### Formula

![model](http://latex.codecogs.com/png.latex?\dpi{150}P(y|x)=g(\sum_{j=1}^m\sigma(u_j^Tx)\eta(yw_j^Tx)))

Note：![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20\sigma(\cdot)) is the partition function,
![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20w_1,w_2,...,w_m)is the parameter of fitting function
![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20\eta(\cdot)). For a given sample x, our prediction function model
![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20p(y|x))has two parts. The first part
![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20\sigma(u_{j^T}x))divides the feature space into m regions, and the second part
![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20\eta(yw_j^Tx))gives the predicted value for each region. The function
![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20g(\cdot))ensures that the model satisfies the definition of the probability function.

MLR algorithm model uses softmax as the partition function
![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20\sigma(x))，Sigmoid function as a fitting function
![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20\eta(x))，and：
![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20g(x)=x)，gets the model of MLR as follows：

![model](http://latex.codecogs.com/png.latex?\dpi{150}%20P(y=1|x)=\sum_{i=1}^m%20\frac{\exp(u_i^T%20x)}{\sum_{j=1}^m%20\exp(u_j^Tx)}\cdot\frac{1}{1+\exp(-w_i^Tx)})

The schematic diagram of the MLR model is as follows,

![](../imgs/mlr.png)

This model can be understood from two perspectives:
- The MLR can be regarded as a three-layer neural network with threshold. There are k sigmoid neurons in the hidden layer. The output of each neuron has a gate. The output value of softmax is the switch of the gate.
- The MLR can be regarded as an ensemble model, which is composed of k simple sigmoid models. The output value of softmax is the combination coefficient.

In many cases, a sub-model needs to be built on a part of the data, and then predicted by multiple models. MLR uses softmax to divide the data (soft division) and predict it with a unified model. Another advantage of MLR is that it can be characterized. Combination, some features are active for sigmoid, and other features are active for softmax, multiplying them is equivalent to making feature combinations at lower levels.

Note: Since the output value of sigmoid model is between 0 and 1, and the output value of softmax is between 0 and 1 and normalized, the combined value is also between 0 and 1 (when all sigmoid values are 1, the maximum value can be obtained, of course, in other cases, the combined sum is 1), which can be regarded as a probability.

## 2. Distributed Implementation on Angel
### Gradient descent method
For![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20y\in%20\\{-1,1\\})，the model can be unified:

![model](http://latex.codecogs.com/png.latex?\dpi{150}\begin{array}{ll}P(y|x)&=\sum^m_{i=1}\frac{exp(u_i^Tx)}{\sum_{j=1}^m%20exp(u_j^Tx)}\cdot\frac{1}{1+exp(-yw_i^Tx)}\\\\\\\\%20&=\sum^m_{i=1}\frac{exp(u_i^Tx)}{\sum_{j=1}^m%20exp(u_j^Tx)}\cdot\sigma(yw_i^Tx)\\\\\\\\%20&=\sum^m_{i=1}P_{softmax}^i(x)P_{sigmoid}^i(y|x)\end{array})

For the sample (x, y), the cross entropy loss function is:

![model](http://latex.codecogs.com/png.latex?\dpi{150}l(x,y)=-\ln{P(y|x)})

Note: Under normal circumstances, cross entropy manifests itself as![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20loss(x,y)=y\log{P(y|x)}+(1-y)\log{(1-P(y|x))}), The meaning of ![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20P(y|x))is given, and the probability at y = 1, if![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20P(y|x))represents the probability of Y at given x (i.e., y is not only the probability of y = 1), the expression of cross entropy is as follows:![model](http://latex.codecogs.com/png.latex?\dpi{100}l\inline%20(x,y)=-\ln{P(y|x)})

In this way, the derivative for a single sample is,

![model](http://latex.codecogs.com/png.latex?\dpi{150}\begin{array}{rl}l(x,y)&=-\ln{P(y|x)}=-\ln\frac{1}{\sum_{j=1}^m%20e^{u_j^Tx}}\sum_{i=1}^m{e^{u_i^Tx}}\sum(yw_i^Tx)\\\\\\\\%20&=\ln\sum_{j=1}^m%20e^{u_j^Tx}-\ln(\Sigma_{i=1}^m%20e^{u_i^Tx}\sigma(yw_i^Tx))\end{array})

Gradient：

![model](http://latex.codecogs.com/png.latex?\dpi{150}\begin{array}{rl}\triangledown_{u_k}l&=\frac{e^{u_k^Tx}x}{\Sigma_{j=1}^m%20e^{u_j^Tx}}-\frac{e^{u_k^Tx}\sigma(yw_k^Tx)x}{\Sigma_{i=1}^m%20e^{u_i^Tx}\sigma(yw_i^Tx)}=(P_{softmax}^k(x)-\frac{P_{softmax}^k(x)P_{sigmoid}^k(y|x)}{P(y|x)})x\\\\\\\\%20\triangledown_{w_k}l&=\frac{ye^{u_k^Tx}\sigma(yw_k^Tx)(\sigma(yw_k^Tx)-1)x}{\Sigma_{i=1}^m%20e^{u_i^Tx}\sigma(yw_i^Tx)}=\frac{P_{softmax}^k(x)P_{sigmoid}^k(y|x)}{P(y|x)}(P_{sigmoid}^k(y|x)-1)yx\end{array})

### Implementation based on Angel
* Model Storage：
    * The model parameters of MLR algorithm are: soft Max function parameters：![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20u_1,u_2,...,u_m)，Sigmoid function parameters：![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20w_1,w_2,...,w_m). Where ![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20u_i)、![](http://latex.codecogs.com/png.latex?\dpi{100}\inline%20w_i)is an N-dimensional vector，N is the dimension of the data, that is, the number of features. A matrix of two m*N dimensions is used to represent a softmax matrix and a sigmodi matrix, respectively.
    * The truncated values of softmax function and sigmoid function are represented by two m*1 dimension matrices.
    
* Model Calculation：    
    * MLR model is trained by gradient descent method, and the algorithm is carried out iteratively. At the beginning of each iteration, worker pulls up the latest model parameters from PS, calculates the gradient with its own training data, and pushes the gradient to PS.
    * PS receives all the gradient values pushed by the worker, takes the average, and updates the PSModel.

## 3. Operation
### Input format

The format of data is set by "ml. data. type" parameter, and the number of data features, that is, the dimension of feature vectors, is set by "ml. feature. num" parameter.

MLR on Angel supports "libsvm" and "dummy" data formats as follows:

* **dummy format**

Each line of text represents a sample in the format of "y index 1 Index 2 index 3...". Among them: the ID of index feature; y of training data is the category of samples, which can take 1 and -1 values; y of prediction data is the ID value of samples. For example, the text of a positive sample [2.0, 3.1, 0.0, 0.0, -1, 2.2] is expressed as "10145", where "1" is the category and "0145" means that the values of dimension 0, 1, 4 and 5 of the eigenvector are not zero. Similarly, samples belonging to negative classes [2.0, 0.0, 0.1, 0.0, 0.0, 0.0] are represented as "-102".

 * **libsvm format**

Each line of text represents a sample in the form of "y index 1: value 1 index 2: value 1 index 3: value 3...". Among them: index is the characteristic ID, value is the corresponding eigenvalue; y of training data is the category of samples, and can take 1 and - 1 values; y of prediction data is the ID value of samples. For example, the text of a positive sample [2.0, 3.1, 0.0, 0.0, -1, 2.2] is expressed as "10:2.01:3.14:-15:2.2", where "1" is the category and "0:2.0" means the value of the zero feature is 2.0. Similarly, samples belonging to negative classes [2.0, 0.0, 0.1, 0.0, 0.0, 0.0] are represented as "-10:2.02:0.1".


### Submitting script

Several steps must be done before editing the submitting script and running.

1. confirm Hadoop and Spark have ready in your environment
2. unzip sona-<version>-bin.zip to local directory (SONA_HOME)
3. upload sona-<version>-bin directory to HDFS (SONA_HDFS_HOME)
4. Edit $SONA_HOME/bin/spark-on-angel-env.sh, set SPARK_HOME, SONA_HOME, SONA_HDFS_HOME and ANGEL_VERSION

Here's an example of submitting scripts, remember to adjust the parameters and fill in the paths according to your own task.

```
#test description
actionType=train or predict
jsonFile=path-to-jsons/mixedlr.json
modelPath=path-to-save-model
predictPath=path-to-save-predict-results
input=path-to-data
queue=your-queue

HADOOP_HOME=my-hadoop-home
source ./bin/spark-on-angel-env.sh
export HADOOP_HOME=$HADOOP_HOME

$SPARK_HOME/bin/spark-submit \
  --master yarn-cluster \
  --conf spark.ps.jars=$SONA_ANGEL_JARS \
  --conf spark.ps.instances=10 \
  --conf spark.ps.cores=2 \
  --conf spark.ps.memory=10g \
  --jars $SONA_SPARK_JARS \
  --files $jsonFile \
  --driver-memory 20g \
  --num-executors 20 \
  --executor-cores 5 \
  --executor-memory 30g \
  --queue $queue \
  --class org.apache.spark.angel.examples.JsonRunnerExamples \
  ./lib/angelml-$SONA_VERSION.jar \
  jsonFile:./mixedlr.json \
  dataFormat:libsvm \
  data:$input \
  modelPath:$modelPath \
  predictPath:$predictPath \
  actionType:$actionType \
  numBatch:500 \
  maxIter:2 \
  lr:4.0 \
  numField:39
```