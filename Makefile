empty :=
space := $(empty) $(empty)

JARS = $(subst $(space),:,$(wildcard lib/*.jar))
BUILDPATH = src:test:${JARS}
RUNPATH = build:${JARS}

BUILDFLAGS = -Xlint:all

# TODO: move **/*.class to build/

SRCS = \
		$(wildcard src/*/*/*/*.java) \
		$(wildcard src/*/*/*/*/*.java) \
		$(wildcard test/*/*/*/*.java) \
		$(wildcard test/*/*/*/*/*.java)

TEST_SRCS = $(wildcard test/*/*/*/*Test.java) \
						$(wildcard test/*/*/*/*/*Test.java)
TESTS = $(subst /,.,$(subst test/,,$(subst .java,,${TEST_SRCS})))

default: compile

compile: ${SRCS}
	javac -cp ${BUILDPATH} ${BUILDFLAGS} ${SRCS} -d build

run: compile
	java -cp ${RUNPATH} com.blevinstein.sr.asteroids.JOGLDriver

tests: compile
	java -cp ${RUNPATH} org.junit.runner.JUnitCore ${TESTS}

clean:
	rm -rf build/*
