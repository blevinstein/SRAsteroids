package com.blevinstein.sr;

import java.util.ArrayList;
import java.util.List;

public class ArbitraryTimeline extends Timeline {
  List<Event> events = new ArrayList<>();

  public void add(Event e) {
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

  public Event at(float t) {
    if (events.isEmpty()) {
      throw new IllegalArgumentException();
    }
    int iLow = 0, iHigh = events.size() - 1;
    while (iHigh - iLow > 1) {
      int iMid = (iLow + iHigh) / 2;
      float tMid = events.get(iMid).t();
      if (tMid < t) {
        iLow = iMid;
      } else if(tMid > t) {
        iHigh = iMid;
      } else {
        // Found exact solution
        return events.get(iMid);
      }
    }
    return interpolate(events.get(iLow), events.get(iHigh), t);
  }

  /**
   * @return Event c on the interval between a and b, such that c.t() == t
   * Requires that a.t() < t < b.t()
   * Package private for testing
   */
  Event interpolate(Event a, Event b, float t) {
    if (a.t() > t || b.t() < t) {
      throw new IllegalArgumentException();
    }
    if (a.t() == t) { return a; }
    if (b.t() == t) { return b; }
    // Find x such that:
    // a * (1 - x) + b * x = c
    // a  + (b - a) * x = c
    float x = (t - a.t()) / (b.t() - a.t());
    return a.plus(b.minus(a).times(x));
  }
}
