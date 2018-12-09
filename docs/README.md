## About LITIengine

The **LITIengine** is a free, open source and easy to learn **2D Java Game Engine**. It provides the infrastructure to create a 2D tile-based Java Game, be it a platformer, a top-down shooter or an RPG.
The main features include a **2D Physics Engine**, a **2D Render Engine**, a **2D Sound Engine**, a **Particle System**, support for **Tiled Maps (.tmx)** and a clean API for the **Basic Game Infrastructure**.
Originally it was written by the two bavarian brothers **Steffen and Matthias** and now it has become an [rapidly developed open source project](https://github.com/gurkenlabs/litiengine) with a raising number or contributors and an active [Community](https://forum.litiengine.com/).

One major difference to other engines is that the **2D Render Engine** is entirely based on plain **Java AWT Graphics**. If you've learned or starting to learn Java this will instantly give you great results and highly optimized rendering performance with what you already know. We think that this is a great and simple way to start making video games without having to care about a lot of vector math or "OpenGL shenanigans".
The graphics can then be further enhanced by the **Particle System** to create beautiful visual effects (like fire or smoke).

The `Environments` in the LITIengine are based on `.tmx` maps which can be created and edited with the well known [Tiled Level Editor](https://www.mapeditor.org/) and further enhanced by entities of the engine. 

Moreover, the `SoundEngine` support **two dimensional audio** that can be played relative to a position in the environment.