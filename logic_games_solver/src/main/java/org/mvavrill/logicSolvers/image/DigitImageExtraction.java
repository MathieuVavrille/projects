package org.mvavrill.logicSolvers.image;

public class DigitImageExtraction {

  public final double cellReduction = 0.9;

  public final boolean[][] image;
  public final Vertex[][] vertices;

  public DigitImageExtraction(final boolean[][] image, final Vertex[][] vertices) {
    this.image = image;
    this.vertices = vertices;
  }

  /** Intersection between segments AB and CD */
  private Vertex segmentIntersection(final Vertex A, final Vertex B, final Vertex C, final Vertex D) {
    double a = B.getX()-A.getX();
    double b = C.getX()-D.getX();
    double c = B.getY()-B.getY();
    double d = C.getY()-D.getY();
    double det = 1/(a*d-b*c);
    double t = det*(d*(C.getX()-A.getX())-b*(C.getY()-A.getY()));
    return A.weightedAverage(B, t);
  }
  
  private boolean[][] extractCell(final int i, final int j, final Vertex topLeft, final Vertex topRight, final Vertex bottomRight, final Vertex bottomLeft) {
    Vertex left = topLeft.weightedAverage(bottomLeft, (2.*i+1.)/6.);
    Vertex right = topRight.weightedAverage(bottomRight, (2.*i+1.)/6.);
    Vertex top = topLeft.weightedAverage(topRight, (2.*j+1.)/6.);
    Vertex bottom = bottomLeft.weightedAverage(bottomRight, (2.*j+1.)/6.);
    Vertex middle = left.weightedAverage(right, (2.*j+1.)/6.);
    int x = (int) Math.round(middle.getX());
    int y = (int) Math.round(middle.getY());
    int cellSize = (int) Math.round(cellReduction*(left.distance(right)+top.distance(bottom))/12);
    boolean[][] cell = new boolean[2*cellSize+1][2*cellSize+1];
    for (int k = 0; k < 2*cellSize+1; k++) {
      for (int l = 0; l < 2*cellSize+1; l++) {
        cell[k][l] = image[y+k-cellSize][x+l-cellSize];
      }
    }
    return cell;
  }
  
  private Vertex center(final int i, final int j, final Vertex topLeft, final Vertex topRight, final Vertex bottomRight, final Vertex bottomLeft) {
    Vertex left = topLeft.weightedAverage(bottomLeft, (2.*i+1.)/6.);
    Vertex right = topRight.weightedAverage(bottomRight, (2.*i+1.)/6.);
    return left.weightedAverage(right, (2.*j+1.)/6.);
  }

  public boolean[][][][] extract() {
    boolean[][][][] res = new boolean[9][9][][];
    for (int I = 0; I < 3; I++) {
      for (int J = 0; J < 3; J++) {
        for (int i = 0; i < 3; i++) {
          for (int j = 0; j < 3; j++) {
            res[3*I+i][3*J+j] = extractCell(i,j,vertices[I][J],vertices[I][J+1],vertices[I+1][J+1],vertices[I+1][J]);
          }
        }
      }
    }
    return res;
  }

  private void addVertexToImage(final double[][] grid, final Vertex vertex) {
    int vx = (int) Math.round(vertex.getX());
    int vy = (int) Math.round(vertex.getY());
    for (int k = vy-2; k < vy+3; k++) {
      for (int l = vx-2; l < vx+3; l++) {
        if (0 <= k && k < grid.length && 0 <= l && l < grid[k].length)
          grid[k][l] = 255;
      }
    }
  }

  private void addGridToImage(final double[][] grid, final Vertex vertex, final double width) {
    System.out.println(2*width);
    for (int i = (int) Math.round(vertex.getX()-width); i < vertex.getX()+width; i++) {
      grid[(int)Math.round(vertex.getY()-width)][i] = 255;
      grid[(int)Math.round(vertex.getY()+width)][i] = 255;
    }
    for (int i = (int) Math.round(vertex.getY()-width); i < vertex.getY()+width; i++) {
      grid[i][(int)Math.round(vertex.getX()-width)] = 255;
      grid[i][(int)Math.round(vertex.getX()+width)] = 255;
    }
  }

  /*public double[][] extract() {
    double[][] newGrid = new double[image.length][image[0].length];
    for (int i = 0; i < image.length; i++) {
      for (int j = 0; j < image[i].length; j++) {
        if (image[i][j])
          newGrid[i][j] = 128;
      }
    }
    for (int I = 0; I < 3; I++) {
      for (int J = 0; J < 3; J++) {
        for (int i = 0; i < 3; i++) {
          for (int j = 0; j < 3; j++) {
            double side = vertices[I][J].distance(vertices[I][J+1]);
            addGridToImage(newGrid, center(i,j,vertices[I][J],vertices[I][J+1],vertices[I+1][J+1],vertices[I+1][J]), cellReduction*side/6);
          }
        }
      }
    }
    /*addVertexToImage(newGrid, topLeft);
    addVertexToImage(newGrid, topRight);
    addVertexToImage(newGrid, bottomLeft);
    addVertexToImage(newGrid, bottomRight);
    addVertexToImage(newGrid, topLeft.weightedAverage(topRight, 1/3.));
    addVertexToImage(newGrid, topLeft.weightedAverage(topRight, 2/3.));
    addVertexToImage(newGrid, bottomLeft.weightedAverage(bottomRight, 1/3.));
    addVertexToImage(newGrid, bottomLeft.weightedAverage(bottomRight, 2/3.));
    addVertexToImage(newGrid, topLeft.weightedAverage(bottomLeft, 1/3.));
    addVertexToImage(newGrid, topLeft.weightedAverage(bottomLeft, 2/3.));
    addVertexToImage(newGrid, topRight.weightedAverage(bottomRight, 1/3.));
    addVertexToImage(newGrid, topRight.weightedAverage(bottomRight, 2/3.));
    return newGrid;
    }*/
}
