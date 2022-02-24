package org.mvavrill.logicSolvers.image;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.javatuples.Quartet;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collections;

public class HoughTransform {

  // Number of values for theta
  private final int thetaNbValues = 51;
  // Range for theta [-range, +range]
  private final int thetaRange = 10;
  // The neighborhood to delete (in degrees)
  private final double thetaNeighborhood = 5;

  // Range and precision for the radius rho
  // The rounding of the factor will be done with Math.round(rho*rhoFactor)
  // i.e. a factor of 10 means a precision of 0.1
  private final int rhoFactor = 5;
  // the neighborhood to delete (in rho)
  private final double rhoNeighborhood = 20;

  private final boolean[][] image;

  public HoughTransform(final boolean[][] image) {
    this.image = image;
  }

  public Vertex[][] getSudokuEdges() {
    List<Vertex> extractedPoints = extractPoints();
    Map<Double,Map<Integer,Integer>> accumulator0 = houghAccumulator(0, extractedPoints);
    Map<Double,Map<Integer,Integer>> accumulator90 = houghAccumulator(90, extractedPoints);
    List<Pair<Integer,Double>> lines0 = new ArrayList<Pair<Integer,Double>>();
    List<Pair<Integer,Double>> lines90 = new ArrayList<Pair<Integer,Double>>();
    for (int i = 0; i < 4; i++) {
      lines0.add(extractMaximum(accumulator0));
      lines90.add(extractMaximum(accumulator90));
    }
    Collections.sort(lines0);
    Collections.sort(lines90);
    System.out.println(lines0);
    System.out.println(lines90);
    Vertex[][] allVertices = new Vertex[4][4];
    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        allVertices[i][j] = lineIntersection(lines90.get(i), lines0.get(j));
    return allVertices;
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
      thetas[thetaInt] =  Math.PI * (baseTheta+2*thetaRange*(thetaInt-thetaNbValues/2)/((double) thetaNbValues)) / 180.;
    }
    Map<Double,Map<Integer,Integer>> accumulator = new HashMap<Double,Map<Integer,Integer>>();
    for (double theta : thetas) {
      double cosT = Math.cos(theta);
      double sinT = Math.sin(theta);
      Map<Integer,Integer> currentMap = new HashMap<Integer,Integer>();
      for (Vertex point : points) {
        int rhoValue = (int) Math.round(rhoFactor*(point.getX()*cosT + point.getY()*sinT));
        for (int r = rhoValue-rhoFactor; r <= rhoValue+rhoFactor; r++) {
          currentMap.put(r, 1+currentMap.getOrDefault(r,0));
        }
      }
      accumulator.put(theta, currentMap);
    }
    return accumulator;
  }

  private Pair<Integer,Double> extractMaximum(final Map<Double,Map<Integer,Integer>> accumulator) {
    List<Triplet<Integer, Double, Integer>> scoreThetaRho = new ArrayList<Triplet<Integer, Double, Integer>>();
    accumulator.entrySet().stream()
      .forEach(entry -> entry.getValue().entrySet().stream()
               .forEach(entry2 -> scoreThetaRho.add(new Triplet<Integer, Double, Integer>(entry2.getValue(),entry.getKey(),entry2.getKey()))));
    Triplet<Integer, Double, Integer> best = Collections.max(scoreThetaRho);
    for (double theta : accumulator.keySet()) {
      if (Math.abs(theta-best.getValue1()) <= thetaNeighborhood) {
        Map<Integer,Integer> rhos = accumulator.get(theta);
        List<Integer> toDelete = new ArrayList<Integer>();
        for (int rho : rhos.keySet()) {
          if (Math.abs(rho-best.getValue2()) <= rhoNeighborhood)
            toDelete.add(rho);
        }
        toDelete.stream().forEach(r -> rhos.remove(r));
      }
    }
    return new Pair<Integer,Double>((int) Math.round(best.getValue2()/rhoFactor), best.getValue1());
  }

  private Vertex lineIntersection(final Pair<Integer,Double> l1, final Pair<Integer,Double> l2) {
    int r1 = l1.getValue0();
    int r2 = l2.getValue0();
    double cos1 = Math.cos(l1.getValue1());
    double sin1 = Math.sin(l1.getValue1());
    double cos2 = Math.cos(l2.getValue1());
    double sin2 = Math.sin(l2.getValue1());
    double det = 1/(cos1*sin2 - sin1*cos2);
    return new Vertex((sin2*r1-sin1*r2)/det, (cos1*r2-cos2*r1)/det);
  }

  private Vertex getMinimalVertexWeighted(final List<Vertex> vertices, final double xWeight, final double yWeight) {
    return vertices.stream().map(v -> new Pair<Double,Vertex>(xWeight*v.getX() + yWeight*v.getY(), v)).sorted().findFirst().get().getValue1();
  }

  private Vertex centerVertex(final Vertex vertex, final int x, final int y) {
    int vx = (int) Math.round(vertex.getX());
    int vy = (int) Math.round(vertex.getY());
    int blackCount = 0;
    Vertex firstWhite = null;
    Vertex lastWhite = null;
    int cpt = 0;
    while (blackCount < 3) {
      if (image[vy+cpt*y][vx+cpt*x]) {
        if (firstWhite == null)
          firstWhite = new Vertex(vx+cpt*x,vy+cpt*y);
        lastWhite = new Vertex(vx+cpt*x,vy+cpt*y);
        blackCount = 0;
      }
      else
        blackCount++;
      cpt++;
    }
    return vertex;//firstWhite.weightedAverage(lastWhite, 0.5);
  }
  
  private Quartet<Vertex,Vertex,Vertex,Vertex> getExtremalVertices(final List<Vertex> vertices) {
    return new Quartet<Vertex,Vertex,Vertex,Vertex>(centerVertex(getMinimalVertexWeighted(vertices, 1, 1), 1, 1),
                                                    centerVertex(getMinimalVertexWeighted(vertices,-1, 1),-1, 1),
                                                    centerVertex(getMinimalVertexWeighted(vertices,-1,-1),-1,-1),
                                                    centerVertex(getMinimalVertexWeighted(vertices, 1,-1), 1,-1));
  }
}
