package org.mvavrill.logicSolvers.image;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;



public class ImageProcessing {

  private final double[][] image;

  public ImageProcessing(final double[][] image) {
    this.image = image;
  }

  public static ImageProcessing fromFile(final String fileName) {
    BufferedImage img = null;
    try {
      img = ImageIO.read(new File(fileName));
    }
    catch (IOException e) {
      System.out.println(e);
      System.exit(0);
    }
    return new ImageProcessing(toGrayScale(img));
  }
  
  public static double[][] toGrayScale(final BufferedImage image) {
  
    // get image's width and height
    int width = image.getWidth();
    int height = image.getHeight();
    double[][] gray = new double[height][width];
  
    // convert to grayscale
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int p = image.getRGB(x, y);
        int r = (p >> 16) & 0xff;
        int g = (p >> 8) & 0xff;
        int b = p & 0xff;
        gray[y][x] = 0.299*r + 0.587*g + 0.114*b;
      }
    }
    return gray;
  }


  public ImageProcessing sobelFilter() {
    double[][] sobelNorm = new double[image.length][image[0].length];
    ImageProcessing gx = convolutionFilter(new double[][]{new double[]{-1,-2,-1},new double[3], new double[]{1,2,1}});
    ImageProcessing gy = convolutionFilter(new double[][]{new double[]{-1,0,1},new double[]{-2,0,2}, new double[]{-1,0,1}});
    for (int i = 1; i < image.length-1; i++) {
      for (int j = 1; j < image[i].length-1; j++) {
        sobelNorm[i][j] = Math.sqrt(gx.image[i][j]*gx.image[i][j]+gy.image[i][j]*gy.image[i][j]);
      }
    }
    return new ImageProcessing(sobelNorm);
  }

  public ImageProcessing vertexFilter() {
    return convolutionFilter(new double[][]{new double[]{2,2,2,2,2},new double[]{2,1,1,1,1}, new double[]{2,1,0,0,0}, new double[]{2,1,0,-4,-4}, new double[]{2,1,0,-4,-8}});
  }

  public ImageProcessing gaussianFilter(final int size, final double sigma) {
    if (size%2 == 0)
      throw new IllegalStateException("The size of the filter has to be odd");
    double[][] filter = new double[size][size];
    int halfSize = size/2;
    double sigmaSquared = sigma*sigma;
    for (int i = -halfSize; i <= halfSize; i++) {
      for (int j = -halfSize; j <= halfSize; j++) {
        filter[i+halfSize][j+halfSize] = Math.exp(-(i*i+j*j)/(2*sigmaSquared))/(2*Math.PI*sigmaSquared);
      }
    }
    return convolutionFilter(filter);
  }

  public ImageProcessing binarize(final double ratioRemoved) {
    List<Double> values = new ArrayList<Double>();
    for (int i = 0; i < image.length; i++)
      for (int j = 0; j < image[i].length; j++)
        values.add(image[i][j]);
    double[] sortedValues = values.stream().mapToDouble(v -> v).sorted().toArray();
    double threshold = sortedValues[(int) (ratioRemoved*sortedValues.length)];
    System.out.println(threshold);
    double[][] binarized = new double[image.length][image[0].length];
    for (int i = 0; i < image.length; i++)
      for (int j = 0; j < image[i].length; j++)
        binarized[i][j] = image[i][j] < threshold ? 0 : 1;
    return new ImageProcessing(binarized);
  }

  private static double valueOrDefault(int I,int J,int i,int j, double[][] mat) {
    if (I < 0 || I >= mat.length || J < 0 || J >= mat[i].length)
      return mat[i][j];
    return mat[I][J];
  }

  public ImageProcessing convolutionFilter(final double[][] filter) {
    if (filter.length != filter[0].length)
      throw new IllegalStateException("Filter is not square");
    int filterSize = filter.length/2;
    double[][] output = new double[image.length][image[0].length];
    for (int i = 0; i < image.length; i++) {
      for (int j = 0; j < image[i].length; j++) {
        double filter_value = 0;
        for (int iF = -filterSize; iF <= filterSize; iF++) {
          for (int jF = -filterSize; jF <= filterSize; jF++) {
            filter_value += filter[filterSize+iF][filterSize+jF]*valueOrDefault(i+iF, j+jF, i, j, image);
          }
        }
        output[i][j] = filter_value;
      }
    }
    return new ImageProcessing(output);
  }

  public static int[][] normalize(final double[][] image) {
    double max = Arrays.stream(image).mapToDouble(line -> Arrays.stream(line).max().getAsDouble()).max().getAsDouble();
    double min = Arrays.stream(image).mapToDouble(line -> Arrays.stream(line).min().getAsDouble()).min().getAsDouble();
    int[][] normalized = new int[image.length][image[0].length];
    for (int i = 0; i < normalized.length; i++)
      for (int j = 0; j < normalized[i].length; j++)
        normalized[i][j] = (int) Math.round((255*(image[i][j]-min))/(max-min));
    return normalized;
  }

  public void saveToFile(final String fileName) {
    BufferedImage output = new BufferedImage(image[0].length, image.length, BufferedImage.TYPE_INT_RGB);
    int[][] normalized = normalize(image);
    for (int i = 0; i < normalized.length; i++)
      for (int j = 0; j < normalized[i].length; j++) {
        int p = (normalized[i][j] << 24) | (normalized[i][j] << 16) | (normalized[i][j] << 8) | normalized[i][j];
        output.setRGB(j,i,p);
      }
    try {
      ImageIO.write(output, "png", new File(fileName));
    }
    catch (IOException e) {
      System.out.println(e);
      System.exit(0);
    }
  }
  
}
