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
    Event image = new Event(3, -2, 0); // NOTE: t = 0
    Event concurrent = observer.plusRelative(image, v);

    List<Timeline> timelines = new ArrayList<>();
    timelines.add(new StaticTimeline(concurrent.x(), concurrent.y()));
    timelines.add(new ConstantTimeline(concurrent, v));
    timelines.add(new ConstantTimeline(concurrent, v.times(-1)));
    timelines.add(new ArbitraryTimeline()
        .add(new Event(-1, -2, -100))
        .add(concurrent)
        .add(new Event(1, 2, 100)));

    for (Timeline t : timelines) {
      assertEquals(t.toString(),
          image,
          t.concurrentWith(observer, v),
          0.01);

      if (t.start() != null) {
        assertEquals(Event.ORIGIN, t.concurrentWith(t.start(), v), 0.001);
      }

      if (t.end() != null) {
        assertEquals(Event.ORIGIN, t.concurrentWith(t.end(), v), 0.001);
      }
    }
  }
}
