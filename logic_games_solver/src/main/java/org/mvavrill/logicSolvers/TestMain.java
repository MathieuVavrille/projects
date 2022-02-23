package org.mvavrill.logicSolvers;

import org.mvavrill.logicSolvers.image.*;

public class TestMain {
  public static void main (String[] argv) {
    new HoughTransform(null).getSudokuEdges();
    String fileName = "test_images/sudoku.png";
    ImageProcessing img = ImageProcessing.fromFile(fileName);
    img.gaussianFilter(9,2).sobelFilter().binarize(0.9).saveToFile(fileName + "_gray_weighted.png");
  }
}
