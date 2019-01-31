# The Game API

![Game API](../images/api-game.png)

The static `Game` class is undoubtedly one of the classes that you will call the most when creating a game with LITIengine.
It is designed to be the static container that provides access to all important aspects of the engine., e.g. it holds the GameInfo, the RenderEngine, the SoundEngine and many other major components.

We designed the API such that all important parts that make up the game are directly accessible via the Game class statically.
To be more technical, it is essentially a collection of core Singleton instances.

The Game class will also be your starting point when setting up a new LITIengine project.
In order to launch your game,  you need to at least call `Game.init(String... args)` and `Game.start()` from your program's `main(String[] args)` method.

Additionally, event listeners for the most basic operations of a Game life cycle can be registered in the Game class, as can be seen in the following example.

Example snippet:
```java
Game.init();
Game.start();
Game.addTerminatedListener(() -> 
{
  // do sth when game is shut down
});

System.out.println("Game version is: " + Game.info().getVersion());
```
## Major Components
 * `Game.graphics()`
 * `Game.audio()`
 * `Game.physics()`

## Meta Components
 * `Game.config()`
 * `Game.info()`
 * `Game.metrics()`
 * `Game.time()`

## Game Loops
 * `Game.loop()`
 * `Game.renderLoop()`
 * `Game.inputLoop()`

## Composition
 * `Game.world()`
 * `Game.window()`
 * `Game.screens()`