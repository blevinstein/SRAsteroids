package com.blevinstein.sr;

import static com.blevinstein.sr.SRTest.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TimelineTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void concurrentWith() {
    Event observer = new Event(1, 1, 1);
    Velocity v = new Velocity(5, 0);

    List<Timeline> timelines = new ArrayList<>();
    timelines.add(new StaticTimeline(11, 1));
    timelines.add(new ConstantTimeline(new Event(1, 2, 3), v));
    timelines.add(new ConstantTimeline(new Event(1, 2, 3), v.times(-1)));
    timelines.add(new ArbitraryTimeline().add(new Event(-1, -2, -4)).add(new Event(1, 2, 4)));

    for (Timeline t : timelines) {
      Assert.assertEquals(0,
          t.concurrentWith(observer, v).t(),
          0.001);

      if (t.start() != null) {
        assertEquals(Event.ORIGIN, t.concurrentWith(t.start(), v), 0.001);
      }

      if (t.end() != null) {
        assertEquals(Event.ORIGIN, t.concurrentWith(t.end(), v), 0.001);
      }
    }
  }
}
