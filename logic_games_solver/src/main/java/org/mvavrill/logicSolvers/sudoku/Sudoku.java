package org.mvavrill.logicSolvers.sudoku;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

public class Sudoku {

  
  public static int[][] solve(final int[][] grid) {
    Model model = new Model("Sudoku");
    IntVar[][] vars = model.intVarMatrix(9,9,1,9);
    for (int i = 0; i < 9; i++)
      for (int j = 0; j < 9; j++)
        if (grid[i][j] != 0)
          vars[i][j].eq(grid[i][j]);
    for (int line = 0; line < 9; line++)
      model.allDifferent(vars[line]).post();
    for (int column = 0; column < 9; column++)
      model.allDifferent(ArrayUtils.getColumn(vars, column)).post();
    for (int I = 0; I < 3; I++)
      for (int J = 0; J < 3; J++) {
        model.allDifferent(new IntVar[]{vars[3*I][3*J], vars[3*I+1][3*J], vars[3*I+2][3*J],vars[3*I][3*J+1], vars[3*I+1][3*J+1], vars[3*I+2][3*J+1],vars[3*I][3*J+2], vars[3*I+1][3*J+2], vars[3*I+2][3*J+2]}).post();
      }
    Solution solution = model.getSolver().findSolution();
    int[][] solArray = new int[9][9];
    for (int i = 0; i < 9; i++)
      for (int j = 0; j < 9; j++)
        solArray[i][j] = solution.getIntVal(vars[i][j]);
    return solArray;
  }
  
}
