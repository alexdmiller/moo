package spacefiller.sensor;

import processing.serial.Serial;

import java.util.ArrayList;
import java.util.List;

public class SerialConnection implements Runnable {
  private Serial port;
  private List<SerialPressureSensor> sensors;

  public SerialConnection(Serial port, int numSensors) {
    this.port = port;
    this.sensors = new ArrayList<>(numSensors);

    for (int i = 0; i < numSensors; i++) {
      this.sensors.add(new SerialPressureSensor());
    }

    (new Thread(this)).start();
  }

  public SerialPressureSensor getSensor(int index) {
    return sensors.get(index);
  }

  int index = -1;

  @Override
  public void run() {
    while (true) {
      while (port.available() > 0) {
        if (index == -1) {
          if (port.read() == 99) {
            index = 0;
          }
        } else {
          int value = port.read();
//          System.out.println(index + ": " + value);
          sensors.get(index).setSensorValue(value);
          index++;
        }

        if (index >= sensors.size()) {
          index = -1;
        }
      }
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
