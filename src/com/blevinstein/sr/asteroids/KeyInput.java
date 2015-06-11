package com.blevinstein.sr.asteroids;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

/**
 * More convenient abstraction for keyboard input.
 *
 * Example:
 * KeyInput ki = new KeyInput();
 * something.addKeyListener(ki);
 * if (ki.getKeyCode(KeyEvent.VK_UP)) { moveForwards(); }
 */
public class KeyInput implements KeyListener {
  private Map<Integer, Boolean> keys = new HashMap<>();
  public synchronized void keyPressed(KeyEvent e) {
    keys.put(e.getKeyCode(), true);
  }
  public synchronized void keyReleased(KeyEvent e) {
    keys.put(e.getKeyCode(), false);
  }
  public void keyTyped(KeyEvent e) {}
  public synchronized boolean getKeyDown(int keyCode) {
    return keys.containsKey(keyCode) && keys.get(keyCode);
  }
}
