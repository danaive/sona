# Word2Vec

>  The Word2Vec algorithm is one of the well-known algorithms in the NLP field. It can learn the vector representation of words from text data and serve as input to other NLP algorithms.

##  Algorithm Introduction

The original intention of developing the Word2Vec module is to implement another commonly used Network Embedding algorithm, the Node2Vec algorithm. The Node2Vec algorithm is divided into two phases:

1. Random walk around the network
2. Use the Word2Vec algorithm

We only provide the implementation of the second phase  here. 

## Distributed Implemention

The  Word2Vec algorithm used for Network Embedding needs to handle network with billion nodes.  We implement  the SkipGram model with negative sampling optimization according to  Yahoo's paper[[1]](https://arxiv.org/abs/1606.08495)
![line_structure](../../img/line_structure.png)

## Running
### Algorithm IO parameters
  - input: hdfs path, random walks out of the sentences, word need to be consecutively numbered from 0, separated by white space or comma, such as:
          0 1 3 5 9
          2 1 5 1 7
          3 1 4 2 8
          3 2 5 1 3
          4 1 2 9 4
  - modelPath: hdfs path, the final model save path is hdfs:///.../epoch_checkpoint_x, where x represents the xth round epoch
  - modelCPInterval: save the model every few rounds of epoch
### Algorithm parameters
  - vectorDim: The embedded vector space dimension, which is the vector dimension of the embedding vector and the context 
  - netSample: the number of negative samples
  - learningRate: The learning rate of the batch gradient decent
  - BatchSize: the size of each mini batch
  - maxEpoch: the number of rounds used by the sample(the samples will be shuffled after each round)
  - window: the size of the trained window
  
### Submitting scripts

Several steps must be done before editing the submitting script and running.

1. confirm Hadoop and Spark have ready in your environment
2. unzip sona-<version>-bin.zip to local directory (SONA_HOME)
3. upload sona-<version>-bin directory to HDFS (SONA_HDFS_HOME)
4. Edit $SONA_HOME/bin/spark-on-angel-env.sh, set SPARK_HOME, SONA_HOME, SONA_HDFS_HOME and ANGEL_VERSION
 
Here's an example of submitting scripts, remember to adjust the parameters and fill in the paths according to your own task.
 
 ```
HADOOP_HOME=my-hadoop-home
input=hdfs://my-hdfs/data
output=hdfs:hdfs://my-hdfs/model
queue=my-queue
 
export HADOOP_HOME=$HADOOP_HOME
source ./bin/spark-on-angel-env.sh
$SPARK_HOME/bin/spark-submit \
  --master yarn-cluster\
  --conf spark.yarn.allocation.am.maxMemory=55g \
  --conf spark.yarn.allocation.executor.maxMemory=55g \
  --conf spark.driver.maxResultSize=20g \
  --conf spark.kryoserializer.buffer.max=2000m\
  --conf spark.ps.instances=2 \
  --conf spark.ps.cores=2 \
  --conf spark.ps.jars=$SONA_ANGEL_JARS \
  --conf spark.ps.memory=15g \
  --conf spark.ps.log.level=INFO \
  --conf spark.offline.evaluate=200 \
  --conf spark.hadoop.angel.model.partitioner.max.partition.number=1000\
  --conf spark.hadoop.angel.ps.backup.interval.ms=2000000000 \
  --conf spark.hadoop.angel.matrixtransfer.request.timeout.ms=60000\
  --conf spark.hadoop.angel.ps.jvm.direct.factor.use.direct.buff=0.20\
  --queue $queue \
  --name "word2vec sona" \
  --jars $SONA_SPARK_JARS  \
  --driver-memory 5g \
  --num-executors 2 \
  --executor-cores 2 \
  --executor-memory 10g \
  --class org.apache.spark.angel.examples.graph.Word2vecExample \
  ./lib/angelml-$SONA_VERSION.jar \
  input:$input output:$output embedding:100 negative:5 window:5 epoch:5 stepSize:0.1 numParts:20 batchSize:2560 subSample:false modelType:cbow interval:10000
  ```