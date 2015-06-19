package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;
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
    Velocity v = new Velocity(c / 2, 0);
    Event image = new Event(3, -2, 0); // NOTE: t = 0
    Event concurrent = observer.relativePlus(image, v);

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
          t.concurrentWith(observer, v).image(),
          0.1);
    }
  }

  @Test
  public void seenBy() {
    Event observer = new Event(1, 1, 1);
    Velocity v = new Velocity(c / 2, 0);
    Event image = new Event(3, -4, -5 / c); // NOTE: (c * t)^2 = x^2 + y^2
    Event seen = observer.relativePlus(image, v);

    List<Timeline> timelines = new ArrayList<>();
    timelines.add(new StaticTimeline(seen.x(), seen.y()));
    timelines.add(new ConstantTimeline(seen, v));
    timelines.add(new ConstantTimeline(seen, v.times(-1)));
    timelines.add(new ArbitraryTimeline()
        .add(new Event(-1, -2, -100))
        .add(seen)
        .add(new Event(1, 2, 100)));

    for (Timeline t : timelines) {
      assertEquals(t.toString(),
          image,
          t.seenBy(observer, v).image(),
          0.1);
    }
  }
}
