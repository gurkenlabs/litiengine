package de.gurkenlabs.litiengine.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class AttributeMarshalTest {

  @Test
  void serializesValueAttribute() throws Exception {
    Attribute<Float> a = new Attribute<>(0.5f);
    String xml = marshal(Attribute.class, a);
    assertTrue(xml.contains("value=\"0.5\""), xml);
  }

  @Test
  void roundTripPreservesValue() throws Exception {
    Attribute<Float> a = new Attribute<>(1.25f);
    String xml = marshal(Attribute.class, a);

    Attribute<?> parsed = unmarshal(Attribute.class, xml);
    assertEquals(1.25f, parsed.getValue().floatValue());
  }

  @Test
  void omitsValueAttributeWhenValueIsNull() throws Exception {
    Attribute<Float> a = new Attribute<>();
    String xml = marshal(Attribute.class, a);
    // a null boxed value must not be serialized as the "value" XML attribute
    assertTrue(!xml.contains("value="), "expected no value attribute for null base value: " + xml);

    Attribute<?> parsed = unmarshal(Attribute.class, xml);
    assertNull(parsed.getValue());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static <T> String marshal(Class<T> type, T value) throws Exception {
    JAXBElement<T> e = new JAXBElement<>(new QName("a"), type, value);
    JAXBContext ctx = JAXBContext.newInstance(type);
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

