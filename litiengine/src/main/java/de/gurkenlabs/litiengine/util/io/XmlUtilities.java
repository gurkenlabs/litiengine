package de.gurkenlabs.litiengine.util.io;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

public final class XmlUtilities {
  private static final Logger log = Logger.getLogger(XmlUtilities.class.getName());

  private static final Map<Class<?>, JAXBContext> jaxbContexts;

  private XmlUtilities() {
    throw new UnsupportedOperationException();
  }

  static {
    jaxbContexts = new ConcurrentHashMap<>();
  }

  /**
   * Saves the XML, contained by the specified input with the custom indentation. If the input is the result of jaxb marshalling, make sure to set
   * Marshaller.JAXB_FORMATTED_OUTPUT to false in order for this method to work properly.
   *
   * @param input       The input stream that contains the original XML.
   * @param fos         The output stream that is used to save the XML.
   * @param indentation The indentation with which the XML should be saved.
   */
  public static void saveWithCustomIndentation(ByteArrayInputStream input, OutputStream fos, int indentation) {
    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
      transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, ""); // Compliant
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indentation));
      Source xmlSource = new SAXSource(new org.xml.sax.InputSource(input));
      StreamResult res = new StreamResult(fos);
      transformer.transform(xmlSource, res);
      fos.flush();
      fos.close();
    } catch (TransformerFactoryConfigurationError | TransformerException | IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public static <T> JAXBContext getContext(Class<T> cls) {
    try {
      final JAXBContext jaxbContext;
      if (jaxbContexts.containsKey(cls)) {
        jaxbContext = jaxbContexts.get(cls);
      } else {
        jaxbContext = JAXBContext.newInstance(cls);
        jaxbContexts.put(cls, jaxbContext);
      }
      return jaxbContext;
    } catch (final JAXBException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return null;
  }

  public static <T> T read(Class<T> cls, URL path) throws JAXBException {
    final JAXBContext jaxbContext = getContext(cls);
    if (jaxbContext == null) {
      return null;
    }

    final Unmarshaller um = jaxbContext.createUnmarshaller();
    um.setAdapter(new URLAdapter(path));

    return cls.cast(um.unmarshal(path));
  }

  public static Path save(Object object, Path filePath) {
    if (filePath == null) {
      return null;
    }
    JAXBContext jaxbContext = getContext(object.getClass());
    if (jaxbContext == null) {
      return null;
    }
    try {
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      jaxbMarshaller.marshal(object, filePath.toFile());
    } catch (JAXBException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
    return filePath;
  }

  public static Path save(Object object, Path path, String extension) {
    String fullExtension = extension.startsWith(".") ? extension : "." + extension;
    Path fullPath = path;
    if (!fullPath.endsWith(fullExtension)) {
      fullPath = path.resolveSibling(path.getFileName().toString() + fullExtension);
    }
    return save(object, fullPath);
  }
}
