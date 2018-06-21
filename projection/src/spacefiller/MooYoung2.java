package spacefiller;

import geomerative.RShape;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import spacefiller.modes.Mode;
import spacefiller.modes.NoOpMode;
import spacefiller.modes.WarpMode;

import java.util.List;

public class MooYoung2 extends PApplet {
  private Mode currentMode;
  private Transformable currentSelection;

  private CornerPinSurface surface;
  private PGraphics canvas;
  private List<RShape> shapes;

  public void settings() {
    fullScreen(P3D, 1);
  }

  public void setup() {
    canvas = createGraphics(width, height, P3D);

    surface = new CornerPinSurface(width, height, 10);
    currentSelection = surface;

    currentMode = new WarpMode(currentSelection);

    registerMethod("mouseEvent", this);
    registerMethod("keyEvent", this);
  }

  public void draw() {
    background(0);

    if (currentMode != null) {
      currentMode.draw(this.getGraphics());
    }

    // TODO: draw the current selection
    surface.render(this.getGraphics(), canvas, true);
  }

  public void mouseEvent(MouseEvent mouseEvent) {
    currentMode.mouseEvent(mouseEvent);
  }

  public void keyEvent(KeyEvent keyEvent) {
    currentMode.keyEvent(keyEvent);
  }

  public void mousePressed() {

  }

  public void mouseReleased() {

  }

  public void keyPressed() {

  }

  public void keyReleased() {

  }


  private void setMode(Mode nextMode) {
    currentMode = nextMode;
  }



  public static void main(String[] args) {
    PApplet.main("spacefiller.MooYoung2");
  }
}
