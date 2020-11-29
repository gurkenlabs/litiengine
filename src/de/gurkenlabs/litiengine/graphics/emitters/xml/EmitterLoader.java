package de.gurkenlabs.litiengine.graphics.emitters.xml;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

public class EmitterLoader {
  private static final Map<String, EmitterData> loadedEmitters;
  private static final Logger log = Logger.getLogger(EmitterLoader.class.getName());

  private EmitterLoader() {
  }

  static {
    loadedEmitters = new ConcurrentHashMap<>();
  }

  public static EmitterData load(String emitterXml) {
    if (loadedEmitters.containsKey(emitterXml)) {
      return loadedEmitters.get(emitterXml);
    }

    return load(Resources.getLocation(emitterXml));
  }

  public static EmitterData load(URL emitterXml) {
    final String name = emitterXml.getFile();
    if (loadedEmitters.containsKey(name)) {
      return loadedEmitters.get(name);
    }

    EmitterData loaded;
    try {
      loaded = XmlUtilities.read(EmitterData.class, emitterXml);
    } catch (JAXBException e) {
      log.log(Level.SEVERE, String.format("Failed to load emitter data for %s", emitterXml), e);
      return null;
    }

    return load(loaded);
  }

  public static EmitterData load(EmitterData emitterData) {
    if (loadedEmitters.containsKey(emitterData.getName())) {
      return loadedEmitters.get(emitterData.getName());
    }

    loadedEmitters.put(emitterData.getName(), emitterData);
    return emitterData;
  }

  public static EmitterData get(String name) {
    if (loadedEmitters.containsKey(name)) {
      return loadedEmitters.get(name);
    }
    return null;
  }

}
