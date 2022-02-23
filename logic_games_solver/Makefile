all: clean package run

package:
	mvn -q clean package

run: package
	mvn -q -e exec:java -D exec.mainClass=org.mvavrill.logicSolvers.TestMain

clean:
	mvn -q clean
