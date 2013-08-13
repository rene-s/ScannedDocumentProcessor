import javax.swing.*;
import java.awt.*;

/**
 * Custom progress monitor
 *
 * @author Rene Schmidt <rene@reneschmidt.de>
 */
public class ScadopProgressMonitor extends ProgressMonitor {
  /**
   * Progress
   */
  public int progress = -1;

  /**
   * Constructor
   *
   * @param parent Parent component
   */
  public ScadopProgressMonitor(Component parent, String title, int min, int max) {
    super(parent, null, title, min, max);
  }

  /**
   * Increment progress by 1
   */
  public void increment() {
    setScadopProgress(++progress);
  }

  /**
   * Set progress
   *
   * @param i Progress indicator
   */
  public void setScadopProgress(int i) {
    progress = i;
    setProgress(i);
  }

  /**
   * Set new maximum. You should use this method when progress is at 50 % of set maximum
   * and when you have found out how long the process is going to take. This method will reset the maximum and
   * will make sure the progress bar won't "jump".
   *
   * @param i new maximum
   */
  public void setNewMaximum(int i) {
    int newMaximum = i * 2 + progress * 2;
    int newProgress = newMaximum / 2;
    setMaximum(newMaximum);
    setScadopProgress(newProgress);
  }
}
