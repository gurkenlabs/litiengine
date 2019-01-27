# 2D Audio

## The Sound Engine - `Game.audio()`
The 2D `SoundEngine` provides all methods to playback sounds and music in your game. It allows to define the 2D coordinates of the sound or even pass in the source entity of the sound which will adjust the position according to the position of the `Entity`. 

The LILIengine sound engine supports **.wav**, **.mp3** and **.ogg** by default. If you need to support other audio codecs, you have to write an own SPI implementation and inject it to your project.

For more information, read the [Official Java Documentation on Service Provider Interfaces](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html).


```java
Sound mySound = Resources.sounds().get("my-sound.ogg");

// play the sound
Game.audio().playSound(mySound);

// play the sound at environment location (50/50)
Game.audio().playSound(mySound, 50, 50);

// play the sound at location of an entity 
Game.audio().playSound(mySound, myEntity);

// play background music
Game.audio().playMusic(Resources.sounds().get("my-music.ogg"));

// use the SoundPlayback to react to events
ISoundPlayback playback = Game.audio().playSound(mySound);
playback.addSoundPlaybackListener(new SoundPlaybackListener() {

  @Override
  public void finished(SoundEvent event) {
  }
  @Override
  public void cancelled(SoundEvent event) {
  }
});
```

If a location (or related `Entity`) is specified when playing a `Sound`, the engine will adjust the `pan` and `volume` relative to the current **listener location**. By default, this location will be the focus of the game's `Camera`.