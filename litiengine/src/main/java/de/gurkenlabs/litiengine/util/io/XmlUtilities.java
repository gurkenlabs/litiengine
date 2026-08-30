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

/// JAXB helpers used to read and write engine XML resources.
///
/// JAXB contexts are cached by root type. Convenience save methods log marshalling failures and
/// return the requested path; [#read(Class, URL)] exposes unmarshalling failures to its caller.
///
/// @see de.gurkenlabs.litiengine.resources.Resources
public final class XmlUtilities {
  private static final Logger log = Logger.getLogger(XmlUtilities.class.getName());

  private static final Map<Class<?>, JAXBContext> jaxbContexts;

  private XmlUtilities() {
    throw new UnsupportedOperationException();
  }

  static {
    jaxbContexts = new ConcurrentHashMap<>();
  }

  /// Writes XML with custom indentation and closes the output stream after a successful transformation.
  ///
  /// If `input` is the result of JAXB marshalling, set [Marshaller#JAXB_FORMATTED_OUTPUT] to `false`
  /// so existing indentation does not interfere. Transformation, flushing, and closing failures are
  /// logged rather than propagated. The input stream is not closed.
  ///
  /// @param input       The input stream that contains the original XML.
  /// @param fos         The output stream that is used to save the XML.
  /// @param indentation The indentation with which the XML should be saved.
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

  /// Returns the cached JAXB context for a root type, creating it when necessary.
  ///
  /// Context-creation failures are logged.
  ///
  /// @param cls The JAXB root type.
  /// @param <T> The root type.
  /// @return The cached context, or `null` if it could not be created.
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

  /// Unmarshals an XML resource and resolves relative URLs against that resource.
  ///
  /// @param cls The expected root type.
  /// @param path The resource URL.
  /// @param <T> The root type.
  /// @return The unmarshalled object, or `null` if a JAXB context could not be created.
  /// @throws JAXBException if unmarshalling fails.
  public static <T> T read(Class<T> cls, URL path) throws JAXBException {
    final JAXBContext jaxbContext = getContext(cls);
    if (jaxbContext == null) {
      return null;
    }

    final Unmarshaller um = jaxbContext.createUnmarshaller();
    um.setAdapter(new URLAdapter(path));

    return cls.cast(um.unmarshal(path));
  }

  /// Marshals an object to a file using formatted XML output.
  ///
  /// Marshalling failures are logged rather than thrown.
  ///
  /// @param object The JAXB object to save.
  /// @param filePath The destination path.
  /// @return `filePath`, or `null` when the path or JAXB context is unavailable.
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

  /// Marshals an object after ensuring that the destination has an extension.
  ///
  /// @param object The JAXB object to save.
  /// @param path The destination path without or with an extension.
  /// @param extension The required extension, with or without a leading dot.
  /// @return The actual destination path, or `null` if saving could not be initialized.
  public static Path save(Object object, Path path, String extension) {
    String fullExtension = extension.startsWith(".") ? extension : "." + extension;
    Path fullPath = path;
    if (!fullPath.endsWith(fullExtension)) {
      fullPath = path.resolveSibling(path.getFileName().toString() + fullExtension);
    }
    return save(object, fullPath);
  }
}
