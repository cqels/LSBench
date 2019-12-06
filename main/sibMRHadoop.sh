export HADOOP_HOME=/export/scratch2/duc/work/Hadoop/hadoop-0.20.205.0

$HADOOP_HOME/bin/hadoop dfs -mkdir input
$HADOOP_HOME/bin/hadoop dfs -mkdir input/sib

#$HADOOP_HOME/bin/hadoop dfs -copyFromLocal sib/input/file01 input/sib

$HADOOP_HOME/bin/hadoop dfs -rmr output

$HADOOP_HOME/bin/hadoop jar /export/scratch2/duc/work/SIB/workspace/SocialGraph/sibMRgenerator.jar input/sib output/sib 5 /export/scratch2/duc/work/SIB/workspace/SocialGraph/ /export/scratch2/duc/work/SIB/workspace/SocialGraph/outputDir/

