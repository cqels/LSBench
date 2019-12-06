export CLASSPATH=./bin/:./lib/ssj.jar:./lib/jdom.jar:./lib/log4j-1.2.12.jar:./lib/hadoop-core-0.20.205.0.jar:./lib/hadoop-tools-0.20.205.0.jar

javac -sourcepath src src/sib/generator/ScalableGenerator.java -d bin/
javac -sourcepath src src/sib/testdriver/TestDriver.java -d bin/

#echo Successfully
