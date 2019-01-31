# Get LITIengine

## Get the Java library

So you want to build a 2D Java game with the *LITIengine*, that's great! Now, the first thing you want to do is to actually download the library.
There are multiple ways to achieve this. The library is distributed over the [Maven Central Repository](https://search.maven.org/artifact/de.gurkenlabs/litiengine/) and you can grab the necessary .jar-file(s) from there by using your favorite build automation tool or manually download the library.

### Gradle (Groovy)
```groovy
compile 'de.gurkenlabs:litiengine:0.4.15'
```

A basic example for a Gradle based LITIengine project can be found [HERE](https://github.com/gurkenlabs/litiengine/tree/master/examples/hello-liti-gradle).

### Apache Maven
```xml
<dependency>
  <groupId>de.gurkenlabs</groupId>
  <artifactId>litiengine</artifactId>
  <version>0.4.15</version>
</dependency>
```

A basic example for a Maven based LITIengine project can be found [HERE](https://github.com/gurkenlabs/litiengine/tree/master/examples/hello-liti-maven).

### Manual Download
[litiengine-0.4.15.jar](https://search.maven.org/remotecontent?filepath=de/gurkenlabs/litiengine/0.4.15/litiengine-0.4.15.jar)
> **Note:** This download will not provide you with any referenced native assemblies (e.g. for Gamepad integration).

[litiengine-v0.4.15-alpha.zip](https://github.com/gurkenlabs/litiengine/releases/download/v0.4.15-alpha/litiengine-v0.4.15-alpha.zip)
> **Note:** This `.zip` archive contains all the required libraries and native assemblies

## Get the utiLITI editor
The LITIengine comes with an editor that supports you with creating game environments and managing your resources. It is a stand alone product which produces a `.litidata` game project files that can then be loaded to your game. 

> **Note:** The editor is not an IDE for Java development.

[utiLITI for Windows](https://github.com/gurkenlabs/litiengine/releases/download/v0.4.15-alpha/utiliti-v0.4.15-alpha-win.zip)

[utiLITI for Linux / MacOS](https://github.com/gurkenlabs/litiengine/releases/download/v0.4.15-alpha/utiliti-v0.4.15-alpha-linux-mac.zip)
