# Creating a project

## Chose an IDE

We highly recommend you to develop your LITIengine game with an IDE (Integrated Development Environment). This will serve you with a ton of useful development tools, like *code completion*, *debugging*, *build tools*, *unit test execution* and much more that a plain text editor simply doesn't provide.

At gurkenlabs, we use the famous [Eclipse IDE](https://www.eclipse.org/downloads/packages/release/kepler/sr1/eclipse-ide-java-developers).
But [IntelliJ IDEA](https://www.jetbrains.com/idea/) is also a valid choice if you prefer it. **Both of them are free-to-use**.

Currently, there is no build-in support for any IDE but for future releases we plan to develop plugins that will help you creating and developing a LITIengine project. 
>We've already started development on a [LITIengine Eclipse Plugin](https://github.com/gurkenlabs/litiengine-eclipse-plugin).

## Chose a build system
We also recommend you to get familiar with [Gradle](https://gradle.org/) or [Maven](https://maven.apache.org/) because these tools can help you greatly when developing Java projects. LITIengine itself is build upon **Gradle** and uses it to seamlessly manage its build steps and dependent libraries.

Of course, it's still possible to just download the LITIengie .jar and import and use the library manually the "oldschool" way.
>We don't encourage using a manually downloaded .jar  for your game project. Trust us, it will be way easier for you in the future to update dependencies on e.g. the LITIengine and to better control and integrate the whole development life cycle.

## Initialize the project structure

Now, depending on the chosen build system, your project structure might look slightly different. The LITIengine doesn't restrict you in how you want to organize your project. However there are some common practices that we think are useful to apply for a Game project with the LITIengine:

* store your resources in `src` folders
* create multiple sub-folders for different types of resources
* save all the resources for your game within the project folder

### An example LITIengine project structure

```
game-project
└─── sprites
│   │─── sprite1.png
│   └─── ...
│─── audio
│   │─── sound1.ogg
│   └─── ...
│─── maps
│   │─── map1.tmx
│   │─── tileset.tsx
│   │─── tileset.png
│   └─── ...
│─── localization
│   │─── strings.properties
│   │─── strings_de_DE.properties
│   └─── ...
│─── src
│   └─── com
│        └─── mygame
│             │─── Program.java
│             └─── ...
│─── .classpath
│─── game.litidata
│─── config.properties
└───...
```