package com.blevinstein.sr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ArbitraryTimeline extends Timeline {
  List<Event> events = new CopyOnWriteArrayList<>();

  public void add(Event e) {
    if (!events.isEmpty() && e.t() <= end().t()) {
      throw new IllegalArgumentException("Must move forwards in time.");
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
    if (events.size() == 1) {
      if (events.get(0).t() == t) {
        return events.get(0);
      } else {
        throw new IllegalArgumentException("Out of bounds.");
      }
    }
    int i = findSegment(t);
    return interpolate(events.get(i), events.get(i+1), t);
  }

  public Velocity velocityAt(double t) {
    int i = findSegment(t);
    return events.get(i+1).minus(events.get(i)).toVelocity();
  }

  /**
   * @return x such that t is between events[x] and events[x+1]
   */
  private int findSegment(double t) {
    if (events.isEmpty()) {
      throw new IllegalArgumentException("Empty.");
    }
    int iLow = 0, iHigh = events.size() - 1;
    if (events.get(iLow).t() > t || events.get(iHigh).t() < t) {
      throw new IllegalArgumentException("Out of bounds.");
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

  public double timeElapsed(double tStart, double tEnd) {
    // Handle inverted intervals
    if (tEnd < tStart) {
      return -timeElapsed(tEnd, tStart);
    }
    double totalTimeElapsed = 0;
    int iStart = findSegment(tStart); // index of last event before tStart
    int iEnd = findSegment(tEnd); // index of last event before tEnd

    if (iStart == iEnd) {
      // Only one relevant segment
      return this.at(tEnd).minus(this.at(tStart)).timeElapsed();
    }

    // segment from tStart to events[iStart+1]
    totalTimeElapsed += events.get(iStart + 1).minus(this.at(tStart)).timeElapsed();

    // segments from events[iStart+1] to events[iEnd]
    for (int i = iStart + 1; i < iEnd; i++) {
      totalTimeElapsed += events.get(i + 1).minus(events.get(i)).timeElapsed();
    }

    // segment from events[iEnd] to tEnd
    totalTimeElapsed += this.at(tEnd).minus(events.get(iEnd)).timeElapsed();

    return totalTimeElapsed;
  }
}
