![LITIENGINE Logo](https://github.com/gurkenlabs/litiengine/blob/master/resources/litiengine-logo-nopad.png "LITIENGINE Logo")

**LITIENGINE** is a free and open source Java 2D Game Engine. It provides all the infrastructure to create tile based 2D games with plain java, be it a platformer or a top-down adventure. 

[![Build Status](https://img.shields.io/travis/gurkenlabs/litiengine/master?style=flat-square)](https://travis-ci.com/gurkenlabs/litiengine)
[![Coverage](https://img.shields.io/sonar/coverage/de.gurkenlabs:litiengine?server=https%3A%2F%2Fsonarcloud.io&style=flat-square)](https://sonarcloud.io/dashboard?id=de.gurkenlabs%3Alitiengine)
[![Maven Central](https://img.shields.io/maven-central/v/de.gurkenlabs/litiengine.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/de.gurkenlabs/litiengine)
[![MIT License](https://img.shields.io/github/license/gurkenlabs/litiengine?style=flat-square)](https://github.com/gurkenlabs/litiengine/blob/master/LICENSE)


## Main Features

* Basic Game Infrastructure (GameLoop, Configuration, Resource Management, Logging, ...)
* 2D Render Engine (GUI Components, Spritesheet Animations, Ambient Lighting, Particle System, ...)
* 2D Sound Engine (support for .wav, .mp3 and .ogg)
* 2D Physics Engine
* Support for Tile Maps in .tmx format (e.g. made with [Tiled Editor](http://www.mapeditor.org/))
* Player Input via Gamepad/Keyboard/Mouse
* Entity Framework
* Message Based Networking Framework

> ## :construction: Important Note

> Currently the LITIengine is being actively developed and therefore some parts of the framework are not final yet. Be aware that the engine API might change over the course of the next releases up until beta (v0.5.0-beta).
Nonetheless, the LITIengine can of course already be used to make fully functioning **2D java games**.

## Installation
The library is distributed over the [Maven Central Repository](https://search.maven.org/artifact/de.gurkenlabs/litiengine/) and you can grab the necessary .jar-file(s) from there by using your favorite build automation tool or manually download the library.

### Gradle
```groovy
repositories {
  mavenCentral()
}

dependencies {
  compile 'de.gurkenlabs:litiengine:0.4.20'
}
```

### Maven
```xml
<dependency>
  <groupId>de.gurkenlabs</groupId>
  <artifactId>litiengine</artifactId>
  <version>0.4.20</version>
</dependency>
```
[More Installation Instructions](https://docs.litiengine.com/basics/getting-started/get-litiengine)
## Getting Started

1. [Setup the Game Project](https://litiengine.com/getting-started-setup-the-game-project/)
2. [Learning the Basics](https://litiengine.com/getting-started-learning-the-basics)
3. [Configuring the Game](https://litiengine.com/getting-started-configuring-the-game/)
4. [Loading a .tmx Map](https://youtu.be/RR3QxOhV8hM)

## Documentation
The [LITIengine documentation pages](https://docs.litiengine.com/) containing in-depth guides for LITIengine are currently in deployment. We're looking forward to your contributions!

> :warning: We are aware that there is currently a **lack of documentation**, but we're planning to document the most important parts of the library and use-cases with the first beta release to ensure that features don't get deprecated before they're even used.

If you are searching for a particular method or class within the API or just want to further explore the engine's possiblities, the Javadocs are a good place to start. 

[![Javadocs](http://www.javadoc.io/badge/de.gurkenlabs/litiengine.svg)](http://www.javadoc.io/doc/de.gurkenlabs/litiengine) 
## Questions?
Visit the official [LITIengine forum](https://forum.litiengine.com/) for troubleshooting or to learn about the LITIengine community. If you encounter bugs or want to request fancy new features, you can also open an issue in our [Issue Tracker](https://github.com/gurkenlabs/litiengine/issues).

## Libraries Used

* [JInput](https://github.com/jinput/jinput) for Gamepad support
* [MP3 SPI](http://www.javazoom.net/mp3spi/mp3spi.html) for .mp3 support
* [Ogg Vorbis SPI](http://www.javazoom.net/vorbisspi/vorbisspi.html) for .ogg support
* [Steamworks4j](https://github.com/code-disaster/steamworks4j) for supporting the steamworks SDK

## Contributing
* Agree to our [Code of Conduct](https://github.com/gurkenlabs/litiengine/blob/master/CODE_OF_CONDUCT.md)
* View our [Contribution guidelines](https://github.com/gurkenlabs/litiengine/blob/master/CONTRIBUTING.md)

## Cite the LITIengine
If you want to cite parts of the LITIengie in your academic work, you can use the following Digital Object Identifier:

[![DOI](https://zenodo.org/badge/87944612.svg)](https://zenodo.org/badge/latestdoi/87944612)

## Contacts 
* Website [litiengine.com](https://litiengine.com)
* Twitter [@gurkenlabs](https://twitter.com/gurkenlabs)
* Facebook [gurkenlabsofficial](https://www.facebook.com/gurkenlabsofficial/)
* YouTube [Gurkenlabs](https://www.youtube.com/channel/UCN7-9zYTxip_Hl1LvCQ8RBA)
* Bug Reports [Issue Tracker](https://github.com/gurkenlabs/litiengine/issues)
* E-Mail info@litiengine.com, info@gurkenlabs.de
* Forum [forum.litiengine.com](https://forum.litiengine.com/)

## Authors
![Gurkenlabs](https://gurkenlabs.de/wp-content/uploads/2018/12/logo-banner-website.png "Gurkenlabs")

**Gurkenlabs** is an indie game development project by two brothers from Bavaria:
* Steffen Wilke ([steffen-wilke](https://github.com/steffen-wilke))
* Matthias Wilke ([nightm4re94](https://github.com/nightm4re94))

## Support the devs

* [PayPal.me](https://www.paypal.me/gurkenlabsmatthias)
* [Patreon](https://www.patreon.com/gurkenlabs)
