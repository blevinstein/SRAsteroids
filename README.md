Special Relativity Asteroids
============================

Goal: demonstrate time dilation/length contractions, other SR oddities

Install dependencies. File listing in lib/contents.txt generated by
    cd lib && echo *.jar > contents.txt
Some of the files listed may not be necessary. YMMV

Libs:
- [JOGL](http://jogamp.org/)
- [junit](https://github.com/junit-team/junit/wiki/Download-and-Install)

Put libs in the lib/ directory.

To build:
> make

To run:
> make run

Controls:
- Arrow keys to move (turn left/right, accelerate forwards/backwards)
- E/Q to zoom in/out

TODO
----
- Experiment: add ability to switch between projections (seenBy, concurrentWith)
  - NOTE: Can add light pulses in concurrentWith projection, doesn't make any sense in seenBy
  projection

- Experiment: ability to change speed of light? e.g. "turn on/turn off" relativity?

- Implement basic collision, splitting, varied sizes of asteriods
  - For now, implement ellipse collision

- Use apache commons math lib (Matrix implementation)?

- implement TransformedTimeline

- implement EllipticalTimeline, add orbits around central point

- add light pulses

- remove objects when timeline is in past; requires adding object abstraction

- implement a lazy physics engine, RK4?
- implement WorldView : the position of all objects in a simulation, as seen by a particular
  observer moving at a particular speed. To simulate, an object should request a WorldView from
  the World, giving its own position and relative velocity, and use that to simulate a timestep.
  Problem: use seenBy or concurrentWith?
- implement SimulatedTimeline

    ctor: SimulatedTimeline(Event initialEvent)
    state: ArbitraryTimeline path
    update(Event observer, Velocity velocity, float tMin):
      simulate the path of this timeline until this.end() > tMin as observer by the observer
      moving at velocity. Problem: use seenBy or concurrentWith? seenBy exhibits weird behavior,
      not sure if buggy or just not intuitive.

- write Force abstraction, then in mainLoop

    for (Entity e : entities) {
      for (Force f : e.getForces()) {
        e.accelerate(f, dt)
      }
    }
- add SpringForce
- research GR: what would it take to add gravity?
- research EM: what would it take to add electromagnetic forces? I know electrical forces and magnetic forces can be transformed into each other through lorentz transformations, if implement naive "k q1 q2 / d12^2" without magnetism it will be inconsistent when transformed.

