# Create Maps with Tiled
## What is '*Tiled*'?
From the official [Tiled docs page](https://doc.mapeditor.org/en/stable/manual/introduction/):
> Tiled is a 2D level editor that helps you develop the content of your game. 
> Its primary feature is to edit tile maps of various forms, but it also supports free image placement 
> as well as powerful ways to annotate your level with extra information used by the game. 
> Tiled focuses on general flexibility while trying to stay intuitive.

With Tiled editor, you can create orthogonal, hexagonal, and isometric Tile maps in no time.\
It is the absolute go-to tool for 2D Tile-based mapping.

## How do I use Tiled?
For a general understanding of the mapping process with Tiled editor, we encourage you to have a look at its [plentiful documentation](https://doc.mapeditor.org/en/stable/manual/introduction/#creating-a-new-map).\
However, we will refer to a few LITIengine specific aspects of creating maps with Tiled in the sections about [Map Objects](https://docs.litiengine.com/basics/manage-maps/map-objects) and [custom properties](https://docs.litiengine.com/basics/manage-maps/custom-properties)

* First, you need to paint your Tileset in the [pixel painting program of your choice](https://www.slant.co/topics/1547/~best-pixel-art-sprite-editors).
* Then, import the Tileset image into Tiled editor to create a [.tsx Tileset](https://doc.mapeditor.org/en/stable/reference/tmx-map-format/#tileset).
* Create [layers](https://doc.mapeditor.org/en/stable/manual/layers/) containing Tiles, Objects, and Images.
* Save your map.

## Why is tile mapping not a part of the utiLITI editor?
LITIengine had its origin in the idea of writing a Java 2D game engine without including *any* external libraries (what were we *thinking*?).
Yet, we believe that it is key to success to be able to sometimes rely on other people's expertise, especially when having limited resources.
The Tiled editor has been developed by a vivid community for approximately ten years, offering a universal standard for tile maps.
In other words, *it just works*. There is simply no need to reinvent the wheel when it comes to tile mapping.
