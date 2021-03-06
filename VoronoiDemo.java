import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.File;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.Locale;

import DCEL.*;
import Utils.*;

public class VoronoiDemo extends JPanel {
  private static VoronoiDiagram voronoi;
  private static Point[] points;
  private static final int window_width = 700;
  private static final int window_height = 500;
  // The farest x,y-cords of scene (to draw axes) 
  private static final int scene_inf_x = 1000;
  private static final int scene_inf_y = 1000;
  // The offset of (0,0) point of cartesian coord system
  private static final int scene_offset_x = 60;
  private static final int scene_offset_y = 400;
  // Diameter of point (for render)
  private static final double point_diameter = 2.5;
  
  @Override  
  public void paintComponent(Graphics g){
    super.paintComponent(g);
    
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(
      RenderingHints.KEY_ANTIALIASING, 
      RenderingHints.VALUE_ANTIALIAS_ON
    );
    AffineTransform oldAT = g2.getTransform();

    // 1. Flip Y axis and translate coords to go from
    // screen coords system to cartesian.
    // Now we draw in cartesian system
    g2.setTransform(invertYAxisAffineTransform());

    // Draw coordinate axes
    drawCoordinateAxes(g2); 
    // Draw convex hull
    drawFilledPolygon(g2, voronoi.getConvexHull(), Color.gray);
   
    // 2. Flip back to screen cords system to draw text 
    // Now we draw in Screen-coordinate system
    g2.setTransform(oldAT);

    // Draw input points and labels 
    drawPoints(g2, points, Color.black, true);
    g2.dispose();
  }

  /**
   * Trasform to flip Y axis and translate coords from
   * screen coords system to cartesian one
   */
  private AffineTransform invertYAxisAffineTransform() {
    AffineTransform newAT = new AffineTransform();
    // Move center of coords by offset
    newAT.translate(scene_offset_x, scene_offset_y);
    // Flip Y axis
    newAT.scale(1.0, -1.0);
    return newAT; 
  }

  /**
   * Draws coordinate axes 
   */
  private void drawCoordinateAxes(Graphics2D g) {
    g.setPaint(Color.gray);
    g.draw(new Line2D.Double(0, -scene_inf_y, 0, scene_inf_y));
    g.draw(new Line2D.Double(-scene_inf_x, 0, scene_inf_x, 0));
  }

  /**
   * Draws points and labels with their coords 
   */
  private void drawPoints(
    Graphics2D g, 
    Point[] points, 
    Color color, 
    boolean labels 
  ) {
    FontMetrics fm = g.getFontMetrics();
    double point_radius = point_diameter / 2.0;
    g.setPaint(color);
    
    for(int i = 0; i < points.length; i++) {
      Point2D.Double point = 
        new Point2D.Double(points[i].getX(), points[i].getY());
      // Apply flip Y transform to point (not to text label)
      invertYAxisAffineTransform().transform(point, point);
      
      g.fill(
        new Ellipse2D.Double(
          point.getX() - point_radius, 
          point.getY() - point_radius, 
          point_diameter, 
          point_diameter
        )
      );

      if (labels) {
        String text = points[i].toString(); 
        g.drawString(
          text,
          (float)(point.getX() - fm.stringWidth(text) / 2.0), 
          (float)(point.getY() - fm.getHeight()) 
        );
      }
    }
  }

  /**
   * Draws polygon filled with color from points
   */
  private void drawFilledPolygon(Graphics2D g, Point[] points, Color color) {
    Path2D polygon_path = new Path2D.Double();

    polygon_path.moveTo(points[0].getX(), points[0].getY());
    for(int i = 1; i < points.length; i++) {
      polygon_path.lineTo(points[i].getX(), points[i].getY());
    }
    polygon_path.closePath();

    g.setPaint(color);
    g.fill(polygon_path);
  }

  /**
   * Loads points from file into points[]
   * And do some preprocessing
   */
  private static void load_and_preprocess_data(String file_name) {
    File points_file = new File(file_name);
    try {   
      Scanner sc = new Scanner(points_file);
      sc.useLocale(Locale.ENGLISH);

      // Read points
      int n = sc.nextInt();
      points = new Point[n];
      for(int i = 0; i < n; i++) {
        double x = sc.nextDouble();
        double y = sc.nextDouble();
        points[i] = new Point(x,y);
      }
 
      // Sort points with custom comparator
      Arrays.sort(points, new PointComparator());

      sc.close();
    } catch(Exception e) {
      System.out.println("IO error:" + e.toString());    
    }
  }
  
  public static void main(String []args) {
    load_and_preprocess_data(args[0]);
    
    voronoi = (new VoronoiBuilder(points)).getVoronoi();
    Debug.log("Done.");    

    // Display graphics
    JFrame frame = new JFrame();
    frame.getContentPane().add(new VoronoiDemo());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(window_width, window_height);
    frame.setVisible(true);
  }

}
