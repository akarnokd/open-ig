# Introduction #

This page describes the file format and rendering of the game's planetary surface maps.

# Details #
The surface map for various planets are located in the `DATA/MAP.PAC` package. There are 7 surface types and 5-9 variants for each in this file:

| **Index** | **Surface type** | **Variants** | **Example** |
|:----------|:-----------------|:-------------|:------------|
| 1 | Desert | 9 | MAP\_A1.MAP |
| 2 | Frozen | 9 | MAP\_B2.MAP |
| 3 | Cratered | 9 | MAP\_C3.MAP |
| 4 | Rocky | 8 | MAP\_D4.MAP |
| 5 | Liquid | 8 | MAP\_E5.MAP |
| 6 | Earth | 8 | MAP\_F6.MAP |
| 7 | Neptoplasm | 6 | MAP\_G1.MAP |

Each map file is 8454 bytes long and has the following format:

| **Offset** | **Description** |
|:-----------|:----------------|
| 0000-0001 | A word describing the width of the usable region |
| 0002-0003 | A word describing the height of the usable region |
| 0004-2105 | the actual map bytes |

Note: words are in little endian format

The logical map size is 65\*65 tiles, but the map file contains 2 bytes for each tile cell. The first byte selects the tile image and the second selects a strip for multi-tile images.

The actual tile images are located in the `DATA/FELSZINx.PAC` as PCX images where `x` denotes the index of the surface. Surface tiles are named for example as `021.PCX` but the numbers are non-continuous.

The map contains the biased values for the tile images using the following rule:
  * if the surface type index is less than 7, then the actual image index can be obtained by subtracting 41 from the byte.
  * If the surface type index is 7, then 84 needs to be subtracted.

The file contains the tile bytes in the following visual order:

![http://karnokd.uw.hu/mapdesc.png](http://karnokd.uw.hu/mapdesc.png)

Where the top left corner (considered the origo in Open-IG) with coordinates 0,0 starts on byte 0x0004 in the map file (red line). As the byte offset increases, the tiles are going downwards up to 65 times. Note that the coordinate system in Open-IG points top-right for the X axis and top-left for the Y axis. Note also that the tiles themselves are not exactly rotated by 45 degrees, therefore, the slopes are visible on the rendered map.

The display order of the map tiles (especially multi-sized tiles) requires some care. The proper Z order display is not the byte sequence in the map (e.g. up-down as the red line indicates) but diagonal, starting at the top tiles for each column and going in the left-down direction (e.g. the minus X direction).

Translation between screen, tile and map coordinates are as follows:

#### Translate from tile coordinate system to screen coordinate system used to render the tile images (`hu.openig.core.Tile class`) ####
(Note: the screen origo is considered that position, where the (0, 0) tile will be drawn, not considering the potential rendering offsets for scroll)
```
public static int toScreenX(int x, int y) {
  return x * 30 - y * 28;
}
public static int toScreenY(int x, int y) {
  return -12 * x - 15 * y;
}
```
#### Translate from screen coordinates back to tile coordinates used to locate tiles based on screen coordinates ####
```
public static float toTileX(int x, int y) {
  return (x + toTileY(x, y) * 28) / 30f;
}
public static float toTileY(int x, int y) {
  return -(30 * y + 12 * x) / 786f;
}
```
Unfortunately, the reverse direction required floating point arithmetic for sub-tile coordinates.
#### Translate from tile coordinates to map coordinates (in `hu.openig.render.PlanetRenderer`) ####
```
public int toMapOffset(int x, int y) {
  return (x - y) * 65 + (x - y + 1) / 2 - x;
}
```

Unfortunately, currently these algorithms are hard-coded into the Open-IG which has the drawback of not supporting arbitrary large maps. But luckily, the rendering module seems to be in a good shape for extending it into this direction, and also will allow surface editing (just like building placement).