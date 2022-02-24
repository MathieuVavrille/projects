package org.mvavrill.logicSolvers.image;

import org.javatuples.Pair;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.util.Scanner;

public class MnistDetection {

  public final int[] trainingLabels;
  public final List<List<Integer>> trainingValues;

  public MnistDetection(final String fileName) {
    int[] tl = null;
    List<List<Integer>> tv = null;
    try {
      Scanner scanner = new Scanner(new File(fileName));
      int N = scanner.nextInt();
      tl = new int[N];
      tv = new ArrayList<List<Integer>>();
      for (int i = 0; i < N; i++) {
        tl[i] = scanner.nextInt();
        int l = scanner.nextInt();
        String s = scanner.next();
        List<Integer> currentTrainingValues = new ArrayList<Integer>();
        for (int j = 0; j < l; j++) {
          currentTrainingValues.add(Integer.valueOf(""+s.charAt(j)));
        }
        tv.add(currentTrainingValues);
      }
    } catch (Exception e) {
      System.out.println(e);
      System.exit(0);
    }
    trainingLabels = tl;
    trainingValues = tv;
  }

  public List<Pair<Integer,Double>> digitsProbabilities(final boolean[][] image) {
    for (int i = 0; i < image.length; i++) {
      for (int j = 0; j < image[i].length; j++) {
        if (image[i][j])
          System.out.print("X");
        else
          System.out.print("·");
      }
      System.out.println("");
    }
    List<Integer> repr = extractRepr(image);
    System.out.println(repr);
    if (repr == null || repr.size() < 20)
      return null;
    return null;
  }

  private List<Integer> extractRepr(final boolean[][] image) {
    int j = image[0].length/2;
    List<Integer> bestRepr = new ArrayList<Integer>();
    boolean previousIsBlack = true;
    for (int i = 0; i < image.length; i++) {
      if (image[i][j]) {
        if (previousIsBlack) {
          System.out.println(i + " "+  j + " " + image[i][j]);
          List<Integer> currentRepr = extractRepr(image, i, j);
          if (bestRepr.size() < currentRepr.size())
            bestRepr = currentRepr;
        }
        previousIsBlack = false;
      }
      else
        previousIsBlack = true;
    }
    return bestRepr;
  }

  private static final int[] dirI = new int[]{-1,-1,0,1,1,1,0,-1};
  private static final int[] dirJ = new int[]{0,1,1,1,0,-1,-1,-1};
  // A direction containing a black point
  private int directionToSafe(final int d) {return ((d+6)%8)/2*2;}
  private boolean  is_inside(final boolean[][] image, final int i, final int j) {return 0 <= i && i < image.length && 0 <= j && j < image[i].length;}
  
  private List<Integer> extractRepr(final boolean[][] image, final int startI, final int startJ) {
    System.out.println(startI + " " + startJ);
    List<Integer> repr = new ArrayList<Integer>();
    int prevI = startI; // previous point
    int prevJ = startJ;
    int prevDir = 3;
    int currI = -1; // current point
    int currJ = -1;
    while (currI != startI || currJ != startJ) {
      int newDir = (directionToSafe(prevDir)+1)%8;
      currI = prevI+dirI[newDir];
      currJ = prevJ+dirJ[newDir];
      while (!is_inside(image, currI, currJ) || !image[currI][currJ]) {
        newDir = (newDir+1)%8;
        currI = prevI+dirI[newDir];
        currJ = prevJ+dirJ[newDir];
      }
      repr.add(newDir);
      prevDir = newDir;
      prevI = currI;
      prevJ = currJ;
      if (repr.size() > 5000)
        throw new IllegalStateException("truc");
      //System.out.println(repr);
    }
    return repr;
  }
  
  private int levenshteinDistance(final List<Integer> s1, final List<Integer> s2) {
    int[][] l = new int[s1.size()+1][s2.size()+1];
    for (int i = 0; i< s1.size()+1; i++)
      l[i][s2.size()] = s1.size()-i;
    for (int i = 0; i < s2.size()+1; i++)
      l[s1.size()][i] = s1.size()-i;
    for (int i = s1.size()-1; i >= 0; i++) {
      for (int j = s2.size()-1; j >= 0; j++) {
        int substitutionCost = s1.get(i) == s2.get(j) ? 0 : 1;
        l[i][j] = Math.min(l[i+1][j+1]+substitutionCost, 1+Math.min(l[i-1][j], l[i][j-1]));
      }
    }
    return l[0][0];
  }

  
  
}
