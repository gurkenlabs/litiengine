package de.gurkenlabs.util.io;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

public final class XmlUtilities {
  private XmlUtilities() {
  }

  /**
   * Saves the xml, contained by the specified input with the custom
   * indentation. If the input is the result of jaxb marshalling, make sure to
   * set Marshaller.JAXB_FORMATTED_OUTPUT to false in order for this method to
   * work properly.
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
    } catch (TransformerConfigurationException e) {
      e.printStackTrace();
    } catch (TransformerFactoryConfigurationError e) {
      e.printStackTrace();
    } catch (TransformerException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
