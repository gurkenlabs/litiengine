![LITIENGINE Logo](litiengine/src/main/resources/litiengine-banner.png "LITIENGINE Logo")

**LITIENGINE** is a free and open source Java 2D Game Engine. It provides a comprehensive Java library and a dedicated map editor to create tile-based 2D games.

[![Build](https://github.com/gurkenlabs/litiengine/actions/workflows/build.yml/badge.svg)](https://github.com/gurkenlabs/litiengine/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=de.gurkenlabs.litiengine&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=de.gurkenlabs.litiengine)
[![Maven Central](https://img.shields.io/maven-central/v/de.gurkenlabs/litiengine.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/de.gurkenlabs/litiengine)
[![MIT License](https://img.shields.io/github/license/gurkenlabs/litiengine?style=flat)](https://github.com/gurkenlabs/litiengine/blob/master/LICENSE)
[![Discord chat](https://img.shields.io/discord/326074836508213258?style=flat&logo=discord)](https://discord.gg/rRB9cKD)
[![Financial Supporters](https://img.shields.io/opencollective/all/litiengine?label=financial%20supporters&style=flat)](https://opencollective.com/litiengine)

## :video_game: Main Features

* Basic Game Infrastructure (GameLoop, Configuration, Resource Management, Logging, ...)
* 2D Render Engine (GUI Components, Spritesheet Animations, Ambient Lighting, Particle System, ...)
* 2D Sound Engine (support for .wav, .mp3 and .ogg)
* 2D Physics Engine
* Support for Tile Maps in .tmx format (e.g. made with [Tiled Editor](http://www.mapeditor.org/))
* Player Input via Gamepad/Keyboard/Mouse
* Entity Framework

## :gear: Installation

### :elephant: Gradle
The LITIENGINE Java library is hosted on the [Maven Central Repository](https://search.maven.org/artifact/de.gurkenlabs/litiengine), i.e. fetching it with Gradle is as simple as configuring your source repository and defining the dependency as shown below.
#### Groovy syntax:
```groovy
repositories {
    mavenCentral()
}

dependencies {
  implementation 'de.gurkenlabs:litiengine:0.5.2'
}
```

#### Kotlin Syntax:
```kotlin
repositories {
    mavenCentral()
}

dependencies {
  implementation("de.gurkenlabs:litiengine:0.5.2")
}
```
### Other ways of installing and using LITIENGINE
For alternative ways of downloading and applying LITIENGINE to your project, visit our [Getting Started Guide](https://litiengine.com/docs/getting-started/).

## :books: Documentation
The [LITIENGINE documentation](https://litiengine.com/docs/) contains in-depth explanations, guides, and tutorials for general concepts of the engine.
### Javadocs
If you are searching for a particular method or class within the API or just want to further explore the engine's capabilities and structure, the Javadocs are a good place to start:
[![Javadocs](http://www.javadoc.io/badge/de.gurkenlabs/litiengine.svg)](https://litiengine.com/api/) 
### Questions
You've found yourself trying to work out a feature of the engine that is not yet documented?
Join our community in the official [LITIENGINE forum](https://forum.litiengine.com/) or on [Discord](https://discord.gg/rRB9cKD) for troubleshooting.

### Bugs and Issues
You've encountered an obvious issue or bug with LITIENGINE or want to request enhancements and features? File an issue in our [Issue Tracker](https://github.com/gurkenlabs/litiengine/issues).

## :package: Libraries Used

* [JInput](https://github.com/jinput/jinput) for Gamepad support
* [MP3 SPI](https://mvnrepository.com/artifact/com.googlecode.soundlibs/mp3spi/1.9.5.4) for .mp3 support
* [Ogg Vorbis SPI](https://mvnrepository.com/artifact/com.googlecode.soundlibs/vorbisspi/1.0.3.3) for .ogg support

### utiLITI 
* [Darklaf](https://github.com/weisJ/darklaf) for theming support

### Other Recommended Libraries
* [Steamworks4j](https://github.com/code-disaster/steamworks4j) for supporting the steamworks SDK

## :handshake: Contributing
If you've decided to help out with LITIENGINE's development - you're awesome!
And here's what you need to to:
* Agree to our [Code of Conduct](https://github.com/gurkenlabs/litiengine/blob/master/CODE_OF_CONDUCT.md)
* View our [Contribution guidelines](https://github.com/gurkenlabs/litiengine/blob/master/CONTRIBUTING.md)

## Star History (Compared with other 2D java game libraries / frameworks / engines)

[![Star History Chart](https://api.star-history.com/svg?repos=gurkenlabs/litiengine,AlmasB/FXGL,magefree/mage,b3dgs/lionengine,fastjengine/FastJ,cping/LGame,LWJGL/lwjgl3,playn/playn,mini2Dx/mini2Dx&type=Timeline)](https://star-history.com/#gurkenlabs/litiengine&AlmasB/FXGL&magefree/mage&b3dgs/lionengine&fastjengine/FastJ&cping/LGame&LWJGL/lwjgl3&playn/playn&mini2Dx/mini2Dx&Timeline)

## Contact

LITIENGINE is created by two Bavarian brothers known as [gurkenlabs](https://gurkenlabs.de/):

| ![](https://avatars.githubusercontent.com/u/7015370?s=64) | ![](https://avatars.githubusercontent.com/u/26114385?s=64) |
| :-----------: | :------------: |
| Steffen Wilke | Matthias Wilke |
| [steffen-wilke](https://github.com/steffen-wilke) | [nightm4re94](https://github.com/nightm4re94) |

### :speech_balloon: Links and Social Media
[![](https://img.shields.io/badge/website-litiengine.com-00a5bc)](https://litiengine.com)
[![](https://img.shields.io/badge/forum-forum.litiengine.com-00a5bc)](https://forum.litiengine.com)
[![](https://img.shields.io/badge/mail-info%40litiengine.com-00a5bc)](mailto:info@litiengine.com?subject=[LITIENGINE])

[![](https://img.shields.io/badge/twitter-%40gurkenlabs-51963a?style=social&logo=twitter)](https://twitter.com/gurkenlabs)
[![](https://img.shields.io/badge/instagram-%40gurkenlabs-51963a?style=social&logo=instagram)](https://www.instagram.com/gurkenlabs)
[![](https://img.shields.io/badge/facebook-gurkenlabsofficial-51963a?style=social&logo=facebook)](https://www.facebook.com/gurkenlabsofficial)
[![](https://img.shields.io/badge/youtube-gurkenlabs-51963a?style=social&logo=youtube)](https://www.youtube.com/channel/UCN7-9zYTxip_Hl1LvCQ8RBA)

## Sponsors and supporters
[![Individuals](https://opencollective.com/litiengine/individuals.svg?button=false) ![Organizations](https://opencollective.com/litiengine/organizations.svg) ](https://opencollective.com/litiengine#support)

![JProfiler](https://litiengine.com/wp-content/uploads/2022/01/xjprofiler_large.png.pagespeed.ic.uajXHJCvPb.webp)
