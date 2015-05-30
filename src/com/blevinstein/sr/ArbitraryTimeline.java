package com.blevinstein.sr;

import java.util.ArrayList;
import java.util.List;

public class ArbitraryTimeline extends Timeline {
  List<Event> events = new ArrayList<>();

  public void add(Event e) {
    if (!events.isEmpty() && e.t() <= end().t()) {
      throw new IllegalArgumentException();
    }
    events.add(e);
  }

  public Event start() {
    if (events.isEmpty()) { return null; }
    return events.get(0);
  }

  public Event end() {
    if (events.isEmpty()) { return null; }
    return events.get(events.size() - 1);
  }

  public List<Event> history(int n) {
    return new ArrayList<>(events.subList(
        Math.max(0, events.size() - n),
        events.size()));
  }

  public Event at(double t) {
    int i = findSegment(t);
    return interpolate(events.get(i), events.get(i+1), t);
  }

  public Velocity velocityAt(double t) {
    int i = findSegment(t);
    return events.get(i+1).relativeTo(events.get(i)).toVelocity();
  }

  /**
   * @return x such that t is between events[x] and events[x+1]
   */
  private int findSegment(double t) {
    if (events.isEmpty()) {
      throw new IllegalArgumentException();
    }
    int iLow = 0, iHigh = events.size() - 1;
    if (events.get(iLow).t() > t || events.get(iHigh).t() < t) {
      throw new IllegalArgumentException();
    }
    while (iHigh - iLow > 1) {
      int iMid = (iLow + iHigh) / 2;
      double tMid = events.get(iMid).t();
      if (tMid <= t) {
        iLow = iMid;
      } else if(tMid > t) {
        iHigh = iMid;
      }
    }
    return iLow;
  }

  /**
   * @return Event c on the interval between a and b, such that c.t() == t
   * Requires that a.t() < t < b.t()
   */
  private Event interpolate(Event a, Event b, double t) {
    if (a.t() > t || b.t() < t) {
      throw new IllegalArgumentException();
    }
    if (a.t() == t) { return a; }
    if (b.t() == t) { return b; }
    // Find x such that:
    // a * (1 - x) + b * x = c
    // a  + (b - a) * x = c
    double x = (t - a.t()) / (b.t() - a.t());
    return a.plus(b.minus(a).times(x));
  }
}