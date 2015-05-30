empty :=
space := $(empty) $(empty)

CLASSPATH = src:test:$(subst $(space),:,$(wildcard lib/*.jar))

# TODO: move **/*.class to build/

SRCS = \
		$(wildcard src/*/*/*/*.java) \
		$(wildcard src/*/*/*/*/*.java) \
		$(wildcard test/*/*/*/*.java) \
		$(wildcard test/*/*/*/*/*.java)

CLASSES = \
		$(wildcard src/*/*/*/*.class) \
		$(wildcard src/*/*/*/*/*.class) \
		$(wildcard test/*/*/*/*.class) \
		$(wildcard test/*/*/*/*/*.class)

TESTS = com.blevinstein.sr.ArbitraryTimelineTest

default: build

build: $(subst .java,.class,${SRCS})

%.class: %.java
	javac -cp ${CLASSPATH} $<

run: build
	java -cp ${CLASSPATH} com.blevinstein.sr.asteroids.Driver

tests: build
	java -cp ${CLASSPATH} org.junit.runner.JUnitCore ${TESTS}

clean:
	rm -rf ${CLASSES}
