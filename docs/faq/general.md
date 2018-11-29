---
description: This page contains answers to frequently asked questions that are broadly related to Java Game Development.
---

## Libraries 
This section contains useful information for external libraries related to Java Game Development.

### How to use steamworks4j SteamAPI from Eclipse?

LITIengine uses the **steamworks4j** wrapper for the SteamAPI to grant access to Steam features from java.
When developing a game that uses these features, you need to execute a few extra steps in order to support the functionality from the IDE.

1. You need to have created the game on [Steamworks](https://partner.steamgames.com) in order to have an _appID_
2. Create an `steam_appid.txt` file containing only the _appID_ of your game
3. Copy the `steam_appid.txt` to the _working directory_ of your app.

   > For debugging and running your app from Eclipse (or other IDEs), the application will be run, using the `javaw.exe`. Your _working directory_ will typically be something like _C:\Program Files\Java\jdkX.X.X_XXX\bin_, which is where your `javaw.exe` is located. This, of course, depends on your environment (workspace/project) _Java Build Path_ configuration of the editor
4. From here on, you can just follow the original tutorial [here](http://code-disaster.github.io/steamworks4j/getting-started.html#initialization).
   