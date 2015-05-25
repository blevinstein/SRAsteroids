default: build

build: *.java
	javac *.java

run:
	java Driver

clean:
	rm -rf *.class
