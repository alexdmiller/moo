package spacefiller.sensor;

import processing.core.PApplet;
import processing.serial.Serial;

import java.util.Arrays;

public class SerialPressureSensor implements Sensor, Runnable {
  private Serial port;
  private boolean thresholdPassed;

  public SerialPressureSensor(PApplet parent) {
    System.out.println(Arrays.toString(Serial.list()));
    port = new Serial(parent, "/dev/cu.usbmodem4517191", 9600);

    (new Thread(this)).start();
  }

  @Override
  public boolean isDepressed() {
    return thresholdPassed;
  }

  @Override
  public void run() {
    while (true) {
      while (port.available() > 0) {
        int value = port.read();
        thresholdPassed = value > 5;
      }
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
