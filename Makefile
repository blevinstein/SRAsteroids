CLASSPATH = src

CLASSES = $(subst .java,.class, \
		$(wildcard src/*/*/*/*.java) \
		$(wildcard src/*/*/*/*/*.java))

default: build

build: ${CLASSES}

%.class: %.java
	javac -cp ${CLASSPATH} -d build $<

run:
	java -cp ${CLASSPATH}:build com.blevinstein.sr.asteroids.Driver

clean:
	rm -rf *.class
