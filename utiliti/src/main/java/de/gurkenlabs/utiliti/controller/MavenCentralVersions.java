package de.gurkenlabs.utiliti.controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Loads published LITIENGINE versions from Maven Central's canonical repository. */
public final class MavenCentralVersions {
  public static final URI METADATA_URI = URI.create(
    "https://repo.maven.apache.org/maven2/de/gurkenlabs/litiengine/maven-metadata.xml");
  private static final Pattern VERSION = Pattern.compile("<version>\\s*([^<]+?)\\s*</version>");

  private MavenCentralVersions() {}

  public static List<String> load() throws IOException, InterruptedException {
    HttpClient client = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(8))
      .followRedirects(HttpClient.Redirect.NORMAL)
      .build();
    HttpRequest request = HttpRequest.newBuilder(METADATA_URI)
      .timeout(Duration.ofSeconds(12))
      .header("Accept", "application/xml")
      .GET()
      .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() != 200) {
      throw new IOException("Maven Central returned HTTP " + response.statusCode());
    }
    List<String> versions = parse(response.body());
    if (versions.isEmpty()) {
      throw new IOException("Maven Central returned no published LITIENGINE versions.");
    }
    return versions;
  }

  static List<String> parse(String metadata) {
    if (metadata == null || metadata.isBlank()) {
      return List.of();
    }
    Matcher matcher = VERSION.matcher(metadata);
    List<String> versions = new ArrayList<>();
    while (matcher.find()) {
      String version = matcher.group(1).trim();
      if (!version.isBlank() && !version.toUpperCase(java.util.Locale.ROOT).endsWith("-SNAPSHOT")) {
        versions.add(version);
      }
    }
    Collections.reverse(versions);
    return List.copyOf(versions);
  }
}
