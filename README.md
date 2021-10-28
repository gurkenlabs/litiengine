![LITIENGINE Logo](core/src/main/resources/resources/litiengine-banner.png "LITIENGINE Logo")

**LITIENGINE** is a free and open source Java 2D Game Engine. It provides a comprehensive Java library and a dedicated map editor to create tile-based 2D games.

[![Build](https://github.com/gurkenlabs/litiengine/actions/workflows/build.yml/badge.svg)](https://github.com/gurkenlabs/litiengine/actions/workflows/build.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=de.gurkenlabs%3Alitiengine&metric=coverage)](https://sonarcloud.io/dashboard?id=de.gurkenlabs%3Alitiengine)
[![Maven Central](https://img.shields.io/maven-central/v/de.gurkenlabs/litiengine.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/de.gurkenlabs/litiengine)
[![MIT License](https://img.shields.io/github/license/gurkenlabs/litiengine?style=flat)](https://github.com/gurkenlabs/litiengine/blob/master/LICENSE)
[![Discord chat](https://img.shields.io/discord/326074836508213258?style=flat&logo=discord)](https://discord.gg/rRB9cKD)
[![Supporters](https://img.shields.io/opencollective/all/litiengine?label=supporters&style=flat)](https://opencollective.com/litiengine)



## Main Features

* Basic Game Infrastructure (GameLoop, Configuration, Resource Management, Logging, ...)
* 2D Render Engine (GUI Components, Spritesheet Animations, Ambient Lighting, Particle System, ...)
* 2D Sound Engine (support for .wav, .mp3 and .ogg)
* 2D Physics Engine
* Support for Tile Maps in .tmx format (e.g. made with [Tiled Editor](http://www.mapeditor.org/))
* Player Input via Gamepad/Keyboard/Mouse
* Entity Framework

## Installation
The library is distributed over the [Maven Central Repository](https://search.maven.org/artifact/de.gurkenlabs/LITIENGINE/) and you can grab the necessary .jar-file(s) from there by using your favorite build automation tool or manually download the library.

### Gradle
```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation 'de.gurkenlabs:litiengine:0.5.1'
}
```

### Maven
```xml
<dependency>
  <groupId>de.gurkenlabs</groupId>
  <artifactId>litiengine</artifactId>
  <version>0.5.1</version>
</dependency>
```
[More Installation Instructions](https://litiengine.com/download/)
## Getting Started

1. [Setup the Game Project](https://litiengine.com/docs/getting-started/)
2. [Running the Game](https://litiengine.com/docs/getting-started/run-the-game/)
3. [Configuring the Game](https://litiengine.com/docs/configuration/)
4. [Loading a .tmx Map](https://youtu.be/RR3QxOhV8hM)

## Documentation
The [LITIENGINE documentation pages](https://litiengine.com/docs/) containing in-depth guides for LITIengine are currently in deployment. We're looking forward to your contributions!

> :warning: We are aware that there is currently a **lack of documentation**, but we're planning to document the most important parts of the library and use-cases with the first beta release to ensure that features don't get deprecated before they're even used.

If you are searching for a particular method or class within the API or just want to further explore the engine's possiblities, the Javadocs are a good place to start. 

[![Javadocs](http://www.javadoc.io/badge/de.gurkenlabs/litiengine.svg)](https://litiengine.com/api/) 
## Questions?
Visit the official [LITIENGINE forum](https://forum.litiengine.com/) for troubleshooting or to learn about the LITIENGINE community. If you encounter bugs or want to request fancy new features, you can also open an issue in our [Issue Tracker](https://github.com/gurkenlabs/litiengine/issues).

## Libraries Used

* [JInput](https://github.com/jinput/jinput) for Gamepad support
* [MP3 SPI](http://www.javazoom.net/mp3spi/mp3spi.html) for .mp3 support
* [Ogg Vorbis SPI](http://www.javazoom.net/vorbisspi/vorbisspi.html) for .ogg support
* [Steamworks4j](https://github.com/code-disaster/steamworks4j) for supporting the steamworks SDK

### utiLITI 
* [Darklaf](https://github.com/weisJ/darklaf) for theming support

## Contributing
* Agree to our [Code of Conduct](https://github.com/gurkenlabs/litiengine/blob/master/CODE_OF_CONDUCT.md)
* View our [Contribution guidelines](https://github.com/gurkenlabs/litiengine/blob/master/CONTRIBUTING.md)

## Citations
If you want to cite parts of the LITIengie in your academic work, you can use the following Digital Object Identifier:

[![DOI](https://zenodo.org/badge/87944612.svg)](https://zenodo.org/badge/latestdoi/87944612)

## Contacts 
* Website [litiengine.com](https://litiengine.com)
* Twitter [@gurkenlabs](https://twitter.com/gurkenlabs)
* Facebook [gurkenlabsofficial](https://www.facebook.com/gurkenlabsofficial/)
* YouTube [Gurkenlabs](https://www.youtube.com/channel/UCN7-9zYTxip_Hl1LvCQ8RBA)
* Bug Reports [Issue Tracker](https://github.com/gurkenlabs/litiengine/issues)
* E-Mail info@litiengine.com
* Forum [forum.litiengine.com](https://forum.litiengine.com/)

## Authors
![Gurkenlabs](https://gurkenlabs.de/wp-content/uploads/2018/12/logo-banner-website.png "Gurkenlabs")

**Gurkenlabs** is an indie game development project by two brothers from Bavaria:
* Steffen Wilke ([steffen-wilke](https://github.com/steffen-wilke))
* Matthias Wilke ([nightm4re94](https://github.com/nightm4re94))

## Support the devs

[![](https://opencollective.com/litiengine/individuals.svg)](https://opencollective.com/litiengine#support)
