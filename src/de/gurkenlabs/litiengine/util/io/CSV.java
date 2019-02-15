package de.gurkenlabs.litiengine.util.io;

import java.io.*;

public class CSV {

  public static void write(int branches[], int functionNumber) throws Exception {
    try {
      FileWriter fw = new FileWriter("branchtest/test_" + functionNumber + ".csv", true);
      BufferedWriter bw = new BufferedWriter(fw);
      PrintWriter pw = new PrintWriter(bw);
      pw.print(branches[0]);
      for (int i = 1; i < branches.length; i++) {
        pw.print(",");
        pw.print(branches[i]);
      }
      pw.println();
      pw.flush();
      bw.close();
      fw.close();
    } catch (Exception e) {
      System.exit(1);
    }
  }
}
