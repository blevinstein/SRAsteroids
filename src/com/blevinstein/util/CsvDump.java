package com.blevinstein.util;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CsvDump {
  private static final String DELIM = ",";

  private PrintStream f;

  public CsvDump(String filename, String... headers) throws FileNotFoundException {
    this(new PrintStream(filename), headers);
  }
  public CsvDump(PrintStream f, String... headers) {
    this.f = f;
    f.println(String.join(DELIM, Arrays.asList(headers)));
  }

  public CsvDump addRow(Object... data) {
    f.println(String.join(DELIM,
        Arrays.stream(data).map((obj) -> obj.toString()).collect(Collectors.toList())));
    return this;
  }
}
