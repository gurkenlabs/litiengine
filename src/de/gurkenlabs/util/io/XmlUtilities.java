package de.gurkenlabs.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

public final class XmlUtilities {
  private static final Logger log = Logger.getLogger(XmlUtilities.class.getName());

  private XmlUtilities() {
  }

  /**
   * Saves the xml, contained by the specified input with the custom indentation.
   * If the input is the result of jaxb marshalling, make sure to set
   * Marshaller.JAXB_FORMATTED_OUTPUT to false in order for this method to work
   * properly.
   * 
   * @param input
   * @param fos
   * @param indentation
   */
  public static void saveWithCustomIndetation(ByteArrayInputStream input, FileOutputStream fos, int indentation) {
    try {
      Transformer transformer = SAXTransformerFactory.newInstance().newTransformer();
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

  public static <T> T readFromFile(Class<T> cls, String path) {
    try {
      final JAXBContext jaxbContext = JAXBContext.newInstance(cls);
      final Unmarshaller um = jaxbContext.createUnmarshaller();

      InputStream stream = ClassLoader.getSystemResourceAsStream(path);
      if (stream == null) {
        stream = new FileInputStream(path);
      }

      return (T) um.unmarshal(stream);
    } catch (final JAXBException | IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return null;
  }

  public static <T> String save(T object, String fileName, String extension) {
    if (fileName == null || fileName.isEmpty()) {
      return null;
    }

    String fileNameWithExtension = fileName;
    if (!fileNameWithExtension.endsWith("." + extension)) {
      fileNameWithExtension += "." + extension;
    }

    File newFile = new File(fileNameWithExtension);

    try (FileOutputStream fileOut = new FileOutputStream(newFile)) {
      JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);

      final ByteArrayOutputStream out = new ByteArrayOutputStream();

      // first: marshal to byte array
      jaxbMarshaller.marshal(object, out);
      out.flush();

      // second: postprocess xml and then write it to the file
      XmlUtilities.saveWithCustomIndetation(new ByteArrayInputStream(out.toByteArray()), fileOut, 1);
      out.close();

      jaxbMarshaller.marshal(object, out);
    } catch (JAXBException | IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return newFile.toString();
  }

}
