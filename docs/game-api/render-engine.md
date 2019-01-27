# 2D Graphics

## `java.awt.Graphics2D`
All of LITIengine's rendering happens on a **Java AWT** `Canvas` component and there's **no expicit OpenGL** involved. By that, the engine is one of the very few on the market that achieves an efficient rendering process with **plain Java**.

The `Canvas` provides a `Graphics2D` object which is passed though the engine on every frame and receives all the drawing operations. This object is basically an empty canvas we're drawing the elements of our game on.

For more information, read the [Official Java Documentation on Graphics2D](https://docs.oracle.com/javase/7/docs/api/java/awt/Graphics2D.html).

## The Render Engine - `Game.graphics()`
The 2D `RenderEngine` is used to render texts, shapes and entities at their location in the
`Environment` and with respect to the `Camera`'s location and zoom. Notice that the location lies within the coordinate space of the current `Environment`. The `RenderEngine` will translate the coordinates to a location on the screen.

Internally, it uses the static renderer implementations to actually execute the drawing operations on the `Graphics2D` object. This class basically prepares the specified render subject and passes them to a renderer with the current correct context.

```java
// render "my text" at the environment location (50/100)
Game.graphics().renderText(g, "my text", 50, 100);

// render "my text" at the location of an entity
Game.graphics().renderText(g, "my text", myEntity.getX(), myEntity.getY());

// render a rectangle (50x50 px) at the environment location (50/100)
Rectangle2D rect = new Rectangle2D.Double(50, 100, 50, 50);
Game.graphics().renderShape(g, rect);
```

> Rendering an `Entity` explicitly over the `RenderEngine` should never be necessary as long as the Entity was added to the game's current `Environment`. The rendering process of the current `Environment` takes care of drawing all the entities and implicitly calls these methods on the `RenderEngine`.

### Available Renderers
These classes can be useful when composing a GUI with images, text or shapes which are rendered at a certain location on the screen.
  * **Image Renderer** (renders `Images`)
  * **Shape Renderer** (renders `Shapes`)
  * **Text Renderer** (renders `Strings`)