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

TEST_SRCS = $(wildcard test/*/*/*/*Test.java) \
						$(wildcard test/*/*/*/*/*Test.java)
TESTS = $(subst /,.,$(subst test/,,$(subst .java,,${TEST_SRCS})))

default: build

build: $(subst .java,.class,${SRCS})

%.class: %.java
	javac -cp ${CLASSPATH} $<

run: build
	java -cp ${CLASSPATH} com.blevinstein.sr.asteroids.JOGLDriver

tests: build
	java -cp ${CLASSPATH} org.junit.runner.JUnitCore ${TESTS}

clean:
	rm -rf ${CLASSES}
