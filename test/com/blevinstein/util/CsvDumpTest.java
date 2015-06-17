package com.blevinstein.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CsvDumpTest {
  private ByteArrayOutputStream output;

  @Before
  public void setup() {
    output = new ByteArrayOutputStream();
  }

  @Test
  public void oneRow() {
    new CsvDump(new PrintStream(output), "x", "y", "z")
        .addRow(1, 2, 3);
    Assert.assertEquals(output.toString(), "x,y,z\n1,2,3\n");
  }
}
