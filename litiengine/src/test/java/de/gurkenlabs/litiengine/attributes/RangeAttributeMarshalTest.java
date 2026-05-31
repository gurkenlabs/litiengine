package de.gurkenlabs.litiengine.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;

class RangeAttributeMarshalTest {

  @Test
  void doesNotSerializeValue() throws Exception {
    RangeAttribute<Float> a = new RangeAttribute<>(-0.1f, 0.1f);
    String xml = marshal(a);
    assertTrue(xml.contains("min=\"-0.1\""), xml);
    assertTrue(xml.contains("max=\"0.1\""), xml);
    assertFalse(xml.contains("value="), "RangeAttribute must not serialize value: " + xml);
  }

  @Test
  void marshalDoesNotMutateInMemoryValue() throws Exception {
    RangeAttribute<Float> a = new RangeAttribute<>(0.25f, -1f, 1f);
    marshal(a);
    // beforeMarshal/afterMarshal must restore the original base value
    assertEquals(0.25f, a.getValue().floatValue());
  }

  @Test
  void roundTripPreservesRangeAndDropsValue() throws Exception {
    RangeAttribute<Float> a = new RangeAttribute<>(0.25f, -1f, 1f);
    String xml = marshal(a);

    @SuppressWarnings("rawtypes")
    RangeAttribute parsed = unmarshal(RangeAttribute.class, xml);
    assertEquals(-1f, parsed.getMin().floatValue());
    assertEquals(1f, parsed.getMax().floatValue());
    // value was not serialized, so the parsed instance has no base value
    assertNull(parsed.getValue());
  }

  private static String marshal(RangeAttribute<?> value) throws Exception {
    @SuppressWarnings({"unchecked", "rawtypes"})
    JAXBElement<RangeAttribute> e =
        new JAXBElement<>(new QName("range"), RangeAttribute.class, value);
    JAXBContext ctx = JAXBContext.newInstance(RangeAttribute.class);
    Marshaller m = ctx.createMarshaller();
    StringWriter sw = new StringWriter();
    m.marshal(e, sw);
    return sw.toString();
  }

  private static <T> T unmarshal(Class<T> type, String xml) throws Exception {
    JAXBContext ctx = JAXBContext.newInstance(type);
    Unmarshaller u = ctx.createUnmarshaller();
    return u.unmarshal(new StreamSource(new StringReader(xml)), type).getValue();
  }
}
