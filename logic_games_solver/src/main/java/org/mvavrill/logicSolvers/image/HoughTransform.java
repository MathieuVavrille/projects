package org.mvavrill.logicSolvers.image;

import org.javatuples.Pair;
import org.javatuples.Quartet;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

public class HoughTransform implements EdgeDetection {

  // Number of values for theta
  private final int thetaNbValues = 51;
  // Range for theta [-range, +range]
  private final int thetaRange = 10;
  // The neighborhood to delete (in degrees)
  private final double thetaNeighborhood = 5;

  // Range and precision for the radius rho
  // The rounding of the factor will be done with Math.round(rho*rhoFactor)
  // i.e. a factor of 10 means a precision of 0.1
  private final double rhoFactor = 2.;
  // the neighborhood to delete (in rho)
  private final double rhoNeighborhood = 20;

  private final boolean[][] image;

  public HoughTransform(final boolean[][] image) {
    this.image = image;
  }

  public Quartet<Vertex,Vertex,Vertex,Vertex> getSudokuEdges() {
    List<Vertex> extractedPoints = extractPoints();
    Map<Double,Map<Integer,Integer>> accumulator0 = houghAccumulator(0, extractedPoints);
    Map<Double,Map<Integer,Integer>> accumulator90 = houghAccumulator(90, extractedPoints);
    List<Pair<Integer,Double>> lines0 = new ArrayList<Pair<Integer,Double>>();
    List<Pair<Integer,Double>> lines90 = new ArrayList<Pair<Integer,Double>>();
    for (int i = 0; i < 4; i++) {
      lines0.add(extractMaximum(accumulator0));
      lines90.add(extractMaximum(accumulator90));
    }
    List<Vertex> allIntersections = new ArrayList<Vertex>();
    for (Pair<Integer,Double> horizontalLine : lines0)
      for (Pair<Integer,Double> verticalLine : lines90)
        allIntersections.add(lineIntersection(horizontalLine, verticalLine));
    return null;
  }

  private List<Vertex> extractPoints() {
    List<Vertex> extracted = new ArrayList<Vertex>();
    for (int y = 0; y < image.length; y++)
      for (int x = 0; x < image[0].length; x++)
        if (image[y][x])
          extracted.add(new Vertex(x,y));
    return extracted;
  }

  private Map<Double,Map<Integer,Integer>> houghAccumulator(final double baseTheta, final List<Vertex> points) {
    double[] thetas = new double[thetaNbValues];
    for (int thetaInt = 0; thetaInt < thetaNbValues; thetaInt++) {
      thetas[thetaInt] = baseTheta+2*thetaRange*(thetaInt-thetaNbValues/2)/((double) thetaNbValues);
    }
    Map<Double,Map<Integer,Integer>> accumulator = new HashMap<Double,Map<Integer,Integer>>();
    for (double theta : thetas) {
      double thetaRad = Math.PI * theta / 180;
      double cosT = Math.cos(thetaRad);
      double sinT = Math.sin(thetaRad);
      Map<Integer,Integer> currentMap = new HashMap<Integer,Integer>();
      for (Vertex point : points) {
        int rhoValue = (int) Math.round(point.getX()*cosT + point.getY()*sinT);
        currentMap.put(rhoValue, 1+currentMap.getOrDefault(rhoValue,0));
      }
      accumulator.put(theta, currentMap);
    }
    return accumulator;
  }

  private Pair<Integer,Double> extractMaximum(final Map<Double,Map<Integer,Integer>> accumulator) {
    return null;
  }

  private Vertex lineIntersection(final Pair<Integer,Double> l1, final Pair<Integer,Double> l2) {
    return null;
  }

  private Quartet<Vertex,Vertex,Vertex,Vertex> getExtremalVertices(final List<Vertex> vertices) {
    return null;
  }
  
}
