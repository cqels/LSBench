export CLASSPATH=./bin/:./lib/ssj.jar:./lib/jdom.jar:./lib/log4j-1.2.12.jar:./lib/hadoop-core-0.20.205.0.jar:./lib/hadoop-tools-0.20.205.0.jar

#javac -sourcepath src src/sib/testdriver/*.java src/sib/generator/*.java -d bin/
find ./src -name *.java > sources_list.txt

javac  -sourcepath src @sources_list.txt -d bin/

#echo Successfully
