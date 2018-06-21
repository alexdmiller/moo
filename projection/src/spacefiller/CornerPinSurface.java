package spacefiller; /**
 * Copyright (C) 2009-15 David Bouchard
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.Point;
import java.awt.geom.Point2D;

import javax.media.jai.PerspectiveTransform;
import javax.media.jai.WarpPerspective;

import processing.core.*;
import processing.data.XML;

/**
 * A simple Corner Pin "keystoned" surface. The surface is a quad mesh that can
 * be skewed to an arbitrary shape by moving its four corners.
 *
 * September-2011 Added JAI library for keystone calculus (@edumo)
 *
 * March-2013 Added methods to programmatically move the corner points
 */
public class CornerPinSurface implements Draggable, Transformable {
  private MeshPoint[] mesh;

  protected float x;
  protected float y;
  private float clickX;
  private float clickY;

  private int width;
  private int height;
  private int res;

  // Daniel Wiedeman: made them public static
  public int TL; // top left
  public int TR; // top right
  public int BL; // bottom left
  public int BR; // bottom right

  int gridColor;
  int controlPointColor;

  boolean visible = false;

  // Jai class for keystone calculus
  WarpPerspective warpPerspective = null;

  CornerPinSurface(int width, int height, int res) {
    res++;
    this.res = res;
    this.width = width;
    this.height = height;

    // initialize the point array
    mesh = new MeshPoint[res * res];
    for (int i = 0; i < mesh.length; i++) {
      float x = (i % res) / (float) (res - 1);
      float y = (i / res) / (float) (res - 1);
      mesh[i] = new MeshPoint(this, x * width, y * height, x * width, y * height);
    }

    // indices of the corner points
    TL = 0 + 0; // x + y
    TR = res - 1 + 0;
    BL = 0 + (res - 1) * (res);
    BR = res - 1 + (res - 1) * (res);

    // make the corners control points
    mesh[TL].setControlPoint(true);
    mesh[TR].setControlPoint(true);
    mesh[BL].setControlPoint(true);
    mesh[BR].setControlPoint(true);

    calculateMesh();

    this.gridColor = 128;
    this.controlPointColor = 0xFF00FF00;
  }

  public void setVisible(boolean value) {
    this.visible = value;
  }

  // ///////////////
  // MANUAL MESHPOINT MOVE FUNCTIONS
  // added by Daniel Wiedemann
  // to move meshpoints via keyboard for example (in OSX the mouse can not go
  // further then the screen bounds, which is obviously a very unpleasant
  // thing if corner points have to be moved across them)
  // ///////////////

  /**
   * Manually move one of the corners for this surface by some amount.
   * The "corner" parameter should be either: CornerPinSurface.TL, CornerPinSurface.BL,
   * CornerPinSurface.TR or CornerPinSurface.BR*
   */
  public void moveMeshPointBy(int corner, float moveX, float moveY) {
    mesh[corner].moveTo(mesh[corner].x + moveX, mesh[corner].y + moveY);
  }

  public void setMeshCorner(int corner, float x, float y) {
    mesh[corner].moveTo(x, y);
  }

  public void setMeshCorner(int corner, MeshPoint point) {
    mesh[corner].x = point.x;
    mesh[corner].y = point.y;
  }

  public MeshPoint getCorner(int corner) {
    return mesh[corner];
  }

  /**
   * @return The surface's mesh resolution, in number of "tiles"
   */
  public int getRes() {
    // The actual resolution is the number of tiles, not the number of mesh
    // points
    return res - 1;
  }

  /**
   * Renders and applies keystoning to the image using a specific renderer.
   */
  public void render(PGraphics g, PImage texture, boolean showGrid) {
    render(g, texture, 0, 0, texture.width, texture.height, showGrid);
  }

  /**
   * Renders and applies keystoning to the image using a specific render. The
   * tX, tY, tW and tH parameters specify which section of the image to render
   * onto this surface.
   */
  public void render(PGraphics g, PImage texture, int tX, int tY, int tW,
                     int tH, boolean showGrid) {
    int w = width;
    int h = height;

    g.pushMatrix();
    g.translate(x, y);
    if (showGrid)
      g.stroke(gridColor);
    else
      g.noStroke();
    g.fill(255);

//    g.image(texture, 0, 0);

    g.textureMode(PConstants.IMAGE);
    g.beginShape(PApplet.QUADS);
    g.texture(texture);
    float u, v = 0;
    for (int x = 0; x < res - 1; x++) {
      for (int y = 0; y < res - 1; y++) {
        MeshPoint mp;
        mp = mesh[(x) + (y) * res];
        u = PApplet.map(mp.u, 0, w, tX, tX + tW);
        v = PApplet.map(mp.v, 0, h, tY, tY + tH);
        g.vertex(mp.x, mp.y, u, v);
        mp = mesh[(x + 1) + (y) * res];
        u = PApplet.map(mp.u, 0, w, tX, tX + tW);
        v = PApplet.map(mp.v, 0, h, tY, tY + tH);
        g.vertex(mp.x, mp.y, u, v);
        mp = mesh[(x + 1) + (y + 1) * res];
        u = PApplet.map(mp.u, 0, w, tX, tX + tW);
        v = PApplet.map(mp.v, 0, h, tY, tY + tH);
        g.vertex(mp.x, mp.y, u, v);
        mp = mesh[(x) + (y + 1) * res];
        u = PApplet.map(mp.u, 0, w, tX, tX + tW);
        v = PApplet.map(mp.v, 0, h, tY, tY + tH);
        g.vertex(mp.x, mp.y, u, v);
      }
    }
    g.endShape(PApplet.CLOSE);
    g.resetShader();

    if (showGrid)
      renderControlPoints(g);

    g.popMatrix();

  }

  /**
   * This function will give you the position of the mouse in the surface's
   * coordinate system.
   *
   * @return The transformed mouse position
   */

  public PVector getTransformedCursor(int cx, int cy) {
    Point2D point = warpPerspective.mapSourcePoint(new Point(cx - (int) x,
        cy - (int) y));
    return new PVector((int) point.getX(), (int) point.getY());
  }

  // 2d cross product
  private float cross2(float x0, float y0, float x1, float y1) {
    return x0 * y1 - y0 * x1;
  }

  /**
   * Draws targets around the control points
   */
  private void renderControlPoints(PGraphics g) {
    g.stroke(controlPointColor);
    g.fill(255);
    for (int i = 0; i < mesh.length; i++) {
      if (mesh[i].isControlPoint()) {
        g.ellipse(mesh[i].x, mesh[i].y, 30, 30);
        g.ellipse(mesh[i].x, mesh[i].y, 10, 10);
      }
    }
  }

  /**
   * Sets the grid used for calibration's color
   */
  public void setGridColor(int newColor) {
    gridColor = newColor;
  }

  /**
   * Sets the control points color
   */
  public void setControlPointsColor(int newColor) {
    controlPointColor = newColor;
  }

  public PVector getPosition() {
    return new PVector(x, y);
  }

  public void setPosition(PVector pos) {
    x = pos.x;
    y = pos.y;
  }

  public PVector getCenter() {
    PVector center = new PVector();
    for (MeshPoint point : mesh) {
      center.x += point.x;
      center.y += point.y;
    }

    center.div(mesh.length);
    center.add(getPosition());
    return center;
  }

  public Draggable select(PVector point) {
    return select(point, true);
  }

  public Draggable select(PVector point, boolean controlPoints) {
    if (controlPoints) {
      // first, see if one of the control points are selected
      point.add(this.x, this.y);
      for (int i = 0; i < mesh.length; i++) {
        if (PApplet.dist(mesh[i].x, mesh[i].y, point.x, point.y) < 30
            && mesh[i].isControlPoint())
          return mesh[i];
      }
    }

    // then, see if the surface itself is selected
    if (isPointOver(point)) {
      clickX = point.x;
      clickY = point.y;
      return this;
    }
    return null;
  }

  /**
   * Returns true if the mouse is over this surface, false otherwise.
   */
  public boolean isPointOver(PVector point) {
    if (isPointInTriangle(point.x - x, point.y - y, mesh[TL],
        mesh[TR], mesh[BL])
        || isPointInTriangle(point.x - x, point.y - y,
        mesh[BL], mesh[TR], mesh[BR]))
      return true;
    return false;
  }

  /**
   * Used for mouse selection of surfaces
   */
  private boolean isPointInTriangle(float x, float y, MeshPoint a,
                                    MeshPoint b, MeshPoint c) {
    // http://www.blackpawn.com/texts/pointinpoly/default.html
    PVector v0 = new PVector(c.x - a.x, c.y - a.y);
    PVector v1 = new PVector(b.x - a.x, b.y - a.y);
    PVector v2 = new PVector(x - a.x, y - a.y);

    float dot00 = v0.dot(v0);
    float dot01 = v1.dot(v0);
    float dot02 = v2.dot(v0);
    float dot11 = v1.dot(v1);
    float dot12 = v2.dot(v1);

    // Compute barycentric coordinates
    float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
    float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
    float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

    // Check if point is in triangle
    return (u > 0) && (v > 0) && (u + v < 1);
  }

  /**
   * Interpolates the position of the points in the mesh according to the 4
   * corners TODO: allow for arbitrary control points, not just the four
   * corners
   */
  protected void calculateMesh() {
    int w = width;
    int h = height;
    // The float constructor is deprecated, so casting everything to double
    PerspectiveTransform transform = PerspectiveTransform.getQuadToQuad(0,
        0, w, 0, w, h, 0,
        h, // source to
        mesh[TL].x, mesh[TL].y, mesh[TR].x, mesh[TR].y, mesh[BR].x,
        mesh[BR].y, mesh[BL].x, mesh[BL].y); // dest

    warpPerspective = new WarpPerspective(transform);

    float xStep = (float) w / (res - 1);
    float yStep = (float) h / (res - 1);

    for (int i = 0; i < mesh.length; i++) {

      if (TL == i || BR == i || TR == i || BL == i)
        continue;

      float x = i % res;
      float y = i / res;

      x *= xStep;
      y *= yStep;

      Point2D point = warpPerspective.mapDestPoint(new Point((int) x,
          (int) y));
      mesh[i].x = (float) point.getX();
      mesh[i].y = (float) point.getY();
    }
  }

  private void calculateMeshOld() {
    for (int i = 0; i < mesh.length; i++) {
      int x = i % res;
      int y = i / res;
      float fX = (float) x / (res - 1);
      float fY = (float) y / (res - 1);
      MeshPoint bot = mesh[TL].interpolateTo(mesh[TR], fX);
      MeshPoint top = mesh[BL].interpolateTo(mesh[BR], fX);
      mesh[i].interpolateBetween(bot, top, fY);
    }
  }

  /**
   * @invisible
   *
   *            This moves the surface according to the offset from where the
   *            mouse was pressed when selecting the surface.
   */
  public void moveTo(float x, float y) {
    this.x = x - clickX;
    this.y = y - clickY;
  }

  public void translate(PVector t) {
    for (MeshPoint p : mesh) {
      p.x += t.x;
      p.y += t.y;
    }
  }

  public void scale(PVector origin, float scale) {
    translate(PVector.mult(origin, -1));
    for (MeshPoint p : mesh) {
      p.x *= scale;
      p.y *= scale;
    }
    translate(origin);
  }

  public void rotate(PVector origin, float theta) {
    translate(PVector.mult(origin, -1));
    for (MeshPoint p : mesh) {
      float newX = (float) (p.x * Math.cos(theta) - p.y * Math.sin(theta));
      float newY = (float) (p.x * Math.sin(theta) + p.y * Math.cos(theta));
      p.x = newX;
      p.y = newY;
    }
    translate(origin);
  }

  /**
   * @invisible
   *
   *            Populates values from an XML object
   */
  void load(XML xml) {

    this.x = xml.getFloat("x");
    this.y = xml.getFloat("y");
    // reload the mesh points
    XML[] pointsXML = xml.getChildren("point");
    for (XML point : pointsXML) {
      MeshPoint mp = mesh[point.getInt("i")];
      mp.x = point.getFloat("x");
      mp.y = point.getFloat("y");
      mp.u = point.getFloat("u");
      mp.v = point.getFloat("v");
      mp.setControlPoint(true);
    }
    calculateMesh();
  }

  XML save() {

    XML parent = new XML("surface");

    parent.setFloat("x", x);
    parent.setFloat("y", y);

    for (int i = 0; i < mesh.length; i++) {
      if (mesh[i].isControlPoint()) {
        // fmt = "point i=\"%d\" x=\"%f\" y=\"%f\" u=\"%f\" v=\"%f\"";
        // fmted = String.format(fmt, i, s.mesh[i].x, s.mesh[i].y,
        // s.mesh[i].u, s.mesh[i].v);
        XML point = new XML("point");
        point.setFloat("x", mesh[i].x);
        point.setFloat("y", mesh[i].y);
        point.setFloat("u", mesh[i].u);
        point.setFloat("v", mesh[i].v);
        point.setFloat("i", i);
        parent.addChild(point);
      }
    }
    return parent;
  }

  public boolean isVisible() {
    return visible;
  }
}