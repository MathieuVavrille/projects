package org.mvavrill.logicSolvers.image;

import org.javatuples.Quartet;

public interface EdgeDetection {
  /** In the order, returns the top left, top right, bottom left bottom right vertices.*/
  public Quartet<Vertex,Vertex,Vertex,Vertex> getSudokuEdges();
}
