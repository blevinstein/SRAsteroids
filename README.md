Special Relativity Asteroids
============================

This 'game' demonstrates time dilation/length contraction and redshift. Game is in quotes
because currently it's not fun.

How To Run
----------

1. Install dependencies. File listing in lib/contents.txt generated by
      lib/update.sh
  Get the right jars for your system.
  Some of the files listed in contents.platform.txt may not be necessary. YMMV

  Libs:
  - [JOGL](http://jogamp.org/)
  - [junit](https://github.com/junit-team/junit/wiki/Download-and-Install)
  - [Apache Commons Lang3](https://commons.apache.org/proper/commons-lang/download_lang.cgi)

2. Put libs in the lib/ directory.

3. To build:
    make

4. To run:
    make run

5. Controls:
  - Arrow keys to move (turn left/right, accelerate forwards/backwards)
  - E/Q to zoom in/out
  - Right click to autopilot to a destination, left click to cancel

Not much 'gameplay' yet, currently exploring visualizations and game mechanics.

Effects demonstrated
--------------------
- Length contraction
- Time dilation
- Redshift/blueshift (frequency of 'twinkling' changes, not actual color)

TODO
====

- EXPERIMENT with arcade features - lasers, missiles, etc.
  - may require better object system than Galaxy currently supports

- IMPORTANT fix AutoPilot, it flounders helplessly [git bisect? new implementation?]

- add HUD, allow lock on to Star, show relative velocity arrow

- implement HyperbolicTimeline
- implement AcceleratingTimeline to represent uniform acceleration

- implement EllipticalGalaxy, like CircleGalaxy

- dump autopilot tracking data to csv, analyze to find better planning algorithm

Basic Features
--------------

- create dynamic galaxies that are stable over time
  - fractal galaxy generation?

Procedural Generation
---------------------

- Create a Galaxy that has clusters of stars that are interesting to navigate.

- Use e.g. Perlin noise to create a Galaxy
  - smoothly varying star velocities for stability.
  - smoothly vary density, include large and small stars
  - add randomly chosen colors? smoothly vary hue/saturation of colors?

- OR use fractal generation
  - galaxy has clusters, clusters have systems, systems have stars?
  - more effective with circular/elliptical movement

