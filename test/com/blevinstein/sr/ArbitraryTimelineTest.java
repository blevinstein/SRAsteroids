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
    timeline.at(5f);
  }

  @Test
  public void singleEvent() {
    ArbitraryTimeline timeline = new ArbitraryTimeline();
    timeline.add(new Event(1f, 2f, 3f));
    assertEquals(new Event(1f, 2f, 3f), timeline.start());
    assertEquals(new Event(1f, 2f, 3f), timeline.end());
    assertEquals(new Event(1f, 2f, 3f), timeline.at(3f));
    
    thrown.expect(IllegalArgumentException.class);
    timeline.at(5f);
  }

  @Test
  public void twoEvents() {
    ArbitraryTimeline timeline = new ArbitraryTimeline();
    timeline.add(new Event(10f, 20f, 30f));
    timeline.add(new Event(20f, 10f, 40f));

    assertEquals(new Event(10f, 20f, 30f), timeline.start());
    assertEquals(new Event(20f, 10f, 40f), timeline.end());
    assertEquals(new Event(11f, 19f, 31f), timeline.at(31f));
    assertEquals(new Event(19f, 11f, 39f), timeline.at(39f));

    thrown.expect(IllegalArgumentException.class);
    timeline.at(41f);
  }

  @Test
  public void twoEvents_backwardsInTime() {
    ArbitraryTimeline timeline = new ArbitraryTimeline();
    timeline.add(new Event(20f, 30f, 40f));
    thrown.expect(IllegalArgumentException.class);
    timeline.add(new Event(30f, 20f, 20f));
  }
}

