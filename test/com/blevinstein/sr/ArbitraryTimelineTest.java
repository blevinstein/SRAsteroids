package com.blevinstein.sr;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ArbitraryTimelineTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void undefined() {
    ArbitraryTimeline timeline = new ArbitraryTimeline();
    assertEquals(null, timeline.start());
    assertEquals(null, timeline.end());
    
    thrown.expect(IllegalArgumentException.class);
    timeline.at(5);
  }

  @Test
  public void singleEvent() {
    ArbitraryTimeline timeline = new ArbitraryTimeline();
    timeline.add(new Event(1, 2, 3));
    assertEquals(new Event(1, 2, 3), timeline.start());
    assertEquals(new Event(1, 2, 3), timeline.end());
    assertEquals(new Event(1, 2, 3), timeline.at(3));
    
    thrown.expect(IllegalArgumentException.class);
    timeline.at(5);
  }

  @Test
  public void twoEvents() {
    ArbitraryTimeline timeline = new ArbitraryTimeline();
    timeline.add(new Event(10, 20, 30));
    timeline.add(new Event(20, 10, 40));

    assertEquals(new Event(10, 20, 30), timeline.start());
    assertEquals(new Event(20, 10, 40), timeline.end());
    assertEquals(new Event(11, 19, 31), timeline.at(31));
    assertEquals(new Event(19, 11, 39), timeline.at(39));

    assertEquals(10 / new Velocity(1, -1).gamma(), timeline.timeElapsed(30, 40), 0.001);
    assertEquals(5 / new Velocity(1, -1).gamma(), timeline.timeElapsed(31, 36), 0.001);

    thrown.expect(IllegalArgumentException.class);
    timeline.at(41);
  }

  @Test
  public void twoEvents_backwardsInTime() {
    ArbitraryTimeline timeline = new ArbitraryTimeline();
    timeline.add(new Event(30, 10, 30));
    timeline.add(new Event(20, 30, 40));

    assertEquals(-10 / new Velocity(-1, 2).gamma(), timeline.timeElapsed(40, 30), 0.001);
    assertEquals(-5 / new Velocity(-1, 2).gamma(), timeline.timeElapsed(37, 32), 0.001);

    thrown.expect(IllegalArgumentException.class);
    timeline.add(new Event(30, 20, 20));
  }

  @Test
  public void threeEvents() {
    ArbitraryTimeline timeline = new ArbitraryTimeline();
    timeline.add(new Event(0, 0, 0));
    timeline.add(new Event(10, -20, 10));
    timeline.add(new Event(0, -15, 15));

    assertEquals(new Event(0, 0, 0), timeline.start());
    assertEquals(new Event(0, -15, 15), timeline.end());

    assertEquals(5 / new Velocity(1, -2).gamma() + 2 / new Velocity(-2, 1).gamma(),
        timeline.timeElapsed(5, 12), 0.01);
    assertEquals(5 / new Velocity(1, -2).gamma() + new Velocity(-2, 1).gamma() * 5,
        timeline.timeElapsed(5, 15), 0.01);
    assertEquals(10 / new Velocity(1, -2).gamma() + new Velocity(-2, 1).gamma() * 5,
        timeline.timeElapsed(0, 15), 0.01);
  }
}

