package de.gurkenlabs.litiengine.environment.tilemap.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class IntegerArrayAdapter extends XmlAdapter<String, int[]> {
  @Override
  public int[] unmarshal(String v) throws Exception {
    if (v == null || v.isEmpty()) {
      return new int[0];
    }
    return Arrays.stream(v.split(",")).mapToInt(Integer::parseInt).toArray();
  }

  @Override
  public String marshal(int[] v) throws Exception {
    return Arrays.stream(v).mapToObj(String::valueOf).collect(Collectors.joining(","));
  }
}
