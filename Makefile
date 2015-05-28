CLASSPATH = src

CLASSES = $(subst .java,.class, \
		$(wildcard src/*/*/*/*.java) \
		$(wildcard src/*/*/*/*/*.java))

default: build

build: ${CLASSES}

%.class: %.java
	javac -cp ${CLASSPATH} $<

run: build
	java -cp ${CLASSPATH} com.blevinstein.sr.asteroids.Driver
