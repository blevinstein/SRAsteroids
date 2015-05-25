import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class Driver {
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setSize(640, 480 + 25);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    SRAsteroids main = new SRAsteroids();
    frame.add(main);

    frame.setVisible(true);
    frame.addMouseMotionListener(main);

    main.run();
  }
}
