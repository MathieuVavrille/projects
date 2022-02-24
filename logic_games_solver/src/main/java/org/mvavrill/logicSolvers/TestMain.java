package org.mvavrill.logicSolvers;

import org.mvavrill.logicSolvers.image.*;

public class TestMain {
  public static void main (String[] argv) {
    //new HoughTransform(null).getSudokuEdges();
    String fileName = "test_images/sudoku_not_centered.jpg";
    boolean[][] img = ImageProcessing.fromFile(fileName).gaussianFilter(5,1.5).convolutionFilter(new double[][]{new double[]{1,1,1},new double[]{1,-8,1}, new double[]{1,1,1}}).binarizeBool(0.9);
    ImageProcessing.removeLonely(img);
    //binarizeBool(0.89);
    Vertex[][] vertices = new HoughTransform(img).getSudokuEdges();
    boolean[][][][] imageDigits = new DigitImageExtraction(img, vertices).extract();
    MnistDetection mnist = new MnistDetection("mnist/t10k-images.idx3-ubytebetter");
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        ImageProcessing.removeLonely(imageDigits[i][j]);
        System.out.println(mnist.digitsProbabilities(imageDigits[i][j]));
      }
    }

    //.saveToFile("test.png");
    //.saveToFile(fileName + "_gray_weighted.png");
  }
}
