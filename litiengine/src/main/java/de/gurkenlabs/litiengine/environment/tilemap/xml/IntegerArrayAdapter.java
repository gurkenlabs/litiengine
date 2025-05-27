package de.gurkenlabs.litiengine.environment.tilemap.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.Arrays;
import java.util.stream.Collectors;


public class IntegerArrayAdapter extends XmlAdapter<String, int[]> {
  @Override
  public int[] unmarshal(String v) throws Exception {
    if (v == null || v.isBlank()) {
      return new int[0];
    }
    String[] split = v.split(",");
    if (split.length == 0) {
      return new int[0];
    }
    return Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
  }

  @Override
  public String marshal(int[] v) throws Exception {
    if( v == null ){
      return null;
    }
    if(v.length == 0){
      return "";
    }
    return Arrays.stream(v).mapToObj(String::valueOf).collect(Collectors.joining(","));
  }
}
