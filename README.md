# LITIengine

[![Build Status](https://travis-ci.org/gurkenlabs/litiengine.svg?branch=master)](https://travis-ci.org/gurkenlabs/litiengine)
[![Quality Gate](https://sonarcloud.io/api/badges/gate?key=de.gurkenlabs:litiengine)](https://sonarcloud.io/dashboard?id=de.gurkenlabs%3Alitiengine)
[![Maven Central](https://img.shields.io/maven-central/v/de.gurkenlabs/litiengine.svg)](https://maven-badges.herokuapp.com/maven-central/de.gurkenlabs/litiengine)
[![Javadocs](http://www.javadoc.io/badge/de.gurkenlabs/litiengine.svg)](http://www.javadoc.io/doc/de.gurkenlabs/litiengine)

![LITIengine Logo](https://github.com/gurkenlabs/litiengine/blob/master/resources/LITIEngine_Logo_big.png "LITIengine Logo")

LITIengine is the pure 2D free java game engine. Written in plain Java 8 it provides all the infrastructure to create a 2D tile based java game, be it a platformer or a top-down adventure.

> ## Important Note

> Currently the LITIengine is being actively developed and therefore some parts of the framework are not final yet. Be aware that the engine API might change over the course of the next releases up until beta (v0.5.0-beta).
Nonetheless, the LITIengine can of course already be used to make fully functioning **2D java games**, or at least we've done so quite successfully :smile:.

> We are aware that there is currently a **lack of documentation**, but for the mentioned reasons, we're planning to document the most important parts of the library and use-cases with the first beta release to ensure that features don't get deprecated before they're even used.

> If any **questions occur**, please don't hesitate to contact us, preferably by opening an issue at our [Issue Tracker](https://github.com/gurkenlabs/litiengine/issues).

> We're looking forward for your feedback on the engine!


## Getting Started

* [Setup Eclipse Project manually](https://github.com/gurkenlabs/litiengine/wiki/Setup-Eclipse-Project-Manually)
* *More Documentation coming soon...*

## Features

* Basic Game Infrastructure (GameLoop, Configuration, Resource Management, Logging, ...)
* 2D Physics Engine
* 2D Render Engine (plain Java)
  * GUI Components
  * Static and Dynamic Shadows
  * Dynamic Lighting
  * ...
* 2D Sound Engine (support for .wav, .mp3 and .ogg)
* Support for Tile Maps in .tmx format (e.g. made with [Tiled Editor](http://www.mapeditor.org/))
* Message Based Networking Framework
* Player Input via Gamepad/Keyboard/Mouse
* Entity Framework
* Ability Framework
* Particle System

## Libraries Used

* [JInput](https://github.com/jinput/jinput) for Gamepad support
* [MP3 SPI](http://www.javazoom.net/mp3spi/mp3spi.html) for .mp3 support
* [Ogg Vorbis SPI](http://www.javazoom.net/vorbisspi/vorbisspi.html) for .ogg support
* [Steamworks4j](https://github.com/code-disaster/steamworks4j) for supporting the steamworks SDK

## Games made with LITIengine

### DR.LEPUS

![DR.LEPUS - The Last Rabbit on Earth](https://gurkenlabs.de/wp-content/uploads/2017/04/page-title.png "DR.LEPUS")

### DR.LEPUS - The Last Rabbit on Earth ([Ludum Dare 35](http://ludumdare.com/compo/ludum-dare-35/?action=preview&uid=67508))

![DR.LEPUS - The Last Rabbit on Earth](https://gurkenlabs.de/wp-content/uploads/2016/09/page-title.png "DR.LEPUS - The Last Rabbit on Earth")

### LITI - Stoneage Brawl

![LITI - Stoneage Brawl](https://gurkenlabs.de/wp-content/uploads/2017/01/liti-stoneage-brawl-banner.png "LITI - Stoneage Brawl")

### Naughty Gnomes
![Naughty Gnomes](https://gurkenlabs.de/wp-content/uploads/2017/04/banner.png "Naughty Gnomes")

### Naughty Elves
![Naughty Elves](https://gurkenlabs.de/wp-content/uploads/2018/02/banner.png "Naughty Elves")

## Contacts 
* Website [litiengine.com](https://litiengine.com)
* Twitter [@gurkenlabs](https://twitter.com/gurkenlabs)
* Facebook [gurkenlabsofficial](https://www.facebook.com/gurkenlabsofficial/)
* Bug Reports [Issue Tracker](https://github.com/gurkenlabs/litiengine/issues)
* E-Mail info@gurkenlabs.de

## Authors

* Steffen Wilke ([steffen-wilke](https://github.com/steffen-wilke))
* Matthias Wilke ([nightm4re94](https://github.com/nightm4re94))

## Support the devs

* [PayPal.me](https://www.paypal.me/gurkenlabsmatthias)
* [Patreon](https://www.patreon.com/gurkenlabs)
