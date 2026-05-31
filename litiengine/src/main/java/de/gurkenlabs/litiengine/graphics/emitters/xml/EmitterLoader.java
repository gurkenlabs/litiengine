package de.gurkenlabs.litiengine.graphics.emitters.xml;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.xml.bind.JAXBException;

public class EmitterLoader {
  private static final Map<String, EmitterAttributes> loadedEmitters;
  private static final Logger log = Logger.getLogger(EmitterLoader.class.getName());

  private EmitterLoader() {}

  static {
    loadedEmitters = new ConcurrentHashMap<>();
  }

  public static EmitterAttributes load(String emitterXml) {
    if (loadedEmitters.containsKey(emitterXml)) {
      return loadedEmitters.get(emitterXml);
    }

    return load(Resources.getLocation(emitterXml));
  }

  public static EmitterAttributes load(URL emitterXml) {
    final String name = emitterXml.getFile();
    if (loadedEmitters.containsKey(name)) {
      return loadedEmitters.get(name);
    }

    EmitterAttributes loaded;
    try {
      loaded = XmlUtilities.read(EmitterAttributes.class, emitterXml);
    } catch (JAXBException e) {
      log.log(Level.SEVERE, String.format("Failed to load emitter data for %s", emitterXml), e);
      return null;
    }

    return load(loaded);
  }

  public static EmitterAttributes load(EmitterAttributes emitterData) {
    if (loadedEmitters.containsKey(emitterData.getName())) {
      return loadedEmitters.get(emitterData.getName());
    }

    loadedEmitters.put(emitterData.getName(), emitterData);
    return emitterData;
  }

  public static EmitterAttributes get(String name) {
    if (loadedEmitters.containsKey(name)) {
      return loadedEmitters.get(name);
    }
    return null;
  }
}
