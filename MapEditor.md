

# Map Editor Version 0.4 Introduction #

The aim of this Map Editor program is to help convert the original surface and building maps convert to a version used by Open IG v0.8 and above. In addition, the tool allows creating completely new and mixed surface maps and building layouts for any further custom games. The third role is to help me develop the new planet surface rendering module.

# System Requirements #

  * Java JRE version 6 or later (Java 7 preferred, see https://jdk7.dev.java.net/ ).
  * 384 MB of memory (512 MB or more preferred)
  * 3MB disk space for the pack, 1.2 GB for the Open-IG resource files.

# Installation #

  * Download the JAR file and place it where the other Open-IG ZIP files are located. (See [Install Instructions](http://code.google.com/p/open-ig/wiki/Install_Instructions_08) for details on the resource files)
  * Download any `open-ig-upgrade-*.zip` files
  * Delete the `open-ig-config.xml` file
  * Optionally: delete the `open-ig-mapeditor-config.xml` file
  * Run the MapEditor by double clicking on its JAR file, or from command line: `java -Xmx512M -jar open-ig-mapeditor-0.4.jar`
  * If you don't want to customize any settings, click Save & Run.

# Menus #

## File ##

![http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-file.png](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-file.png)

The file menu contains the usual operations:

| _Menu item_ | _Shortcut_ | _Description_ |
|:------------|:-----------|:--------------|
| **[New...](#File_/_New.md)** |  | create a new empty map. The user can specify the size of the map |
| **[Open...](#File_/_Open.md)** | `CTRL+O` |  open an existing map definition. |
| **Recent** |  | the list of previously opened or saved maps |
| **Recent / Clear recent** |  | removes the list of recently opened files |
| **[Import...](#File_/_Import.md)** |  | use a map or planet from the original Imperium Galactica |
| **Save** | `CTRL+S` | save the current map, for new maps, it is equal to **Save as...** |
| **[Save as...](#File_/_Save.md)** |  | save the current map |
| **Exit** |  | quit the application without saving |

### File / New ###

Brings up the **New Map** dialog.

![http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-filenew.png](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-filenew.png)

The user can enter the width and height of the new map. Width and height of the map is considered as how many tiles can be placed edge-by-edge. For example:

![http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-filenew-tiles.png](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-filenew-tiles.png)

has width of 3 and height of 2. The tiles are twice as wide as they are height, therefore, a square-like map has width **w** and height **2 x w**.

The default map size is 33 x 66 which corresponds to the original game's map size.

### File / Open ###

Brings up the **Load Map** dialog.

![http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-open.png](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-open.png)

The user can type in a path to the file or browse for it.

The user can load only the surface features or only the buildings from the file.

The dialog remembers the last filename entered.

### File / Save as ###

Brings up the **Save Map** dialog.

![http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-save.png](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-save.png)

The user can type in a path to the file or browse for it.

The user can save only the surface features or only the buildings into the file.

The dialog remembers the last filename entered.

### File / Import ###

Brings up the **Import a map or planet settings** dialog.

![http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-import.png](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-import.png)

Import an original Imperium Galactica surface or planet.

If there is no map active in the editor, the approval of the dialog creates the default 33 x 66 map.

The **Original map** lists all available surface types and their variants, e.g. desert 1, neptoplasm 3 etc.

The **Original planet** lists all planets from  the original game as they are present at the start of the main campaign. The list contains their names, their surface type and their current owner race, e.g. Achilles |Desert: Empire|.

The **Shift X** and **Shift Y** lets the user adjust the placement of the imported map relative to the main coordinate system. Original maps and planets require to be shifted by (-1, -1) to confront with the new planet rendering routines (otherwise, it will clip the first column or row of the original map). Combined with the **Replace current surface** option disabled, the user can merge several different maps together.

The **Replace current surface** clears any existing surface settings and puts the original map onto the current map. Can be used to switch a base surface below a built colony.

The **Replace current building** clears any existing building and places the original buildings onto the map. Can be used to populate a newly built map with the original layout of buildings.

The **Replace surface along with the buildings** basically loads the original planet with surface and building contents replacing the current map.

Click **Import** to proceed. The dialog remembers its last settings and can be used to import maps subsequently with ease.

## Edit ##
![http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-edit.png](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-edit.png)

The **Edit** menu contains operations for manipulating the map contents.

| _Menu item_ | _Shortcut_ | _Description_ |
|:------------|:-----------|:--------------|
| **Undo** | CTRL+Z | Undo the last change to the map |
| **Redo** | CTRL+Y | Redo the last change to the map |
| **Cut** | CTRL+X | Cut the surface and buildings from the current selection |
| **Copy** | CTRL+C | Copy the surface and buildings from the current selection |
| **Paste** | CTRL+V | Paste the surface and buildings (if any) from the system clipboard relative to the currently selected cell. If multiple cells are selected, the most-left cell is used for origo |
| **Cut: building** | CTRL+SHIFT+X | Cut only the buildings from the current selection |
| **Copy: building** | CTRL+SHIFT+C | Copy only the buildings from the current selection |
| **Paste: building** | CTRL+SHIFT+V | Paste only the buildings (if any) from the system clipboard relative to the currently selected cell |
| **Cut: surface** |  | Cut only the surface from the current selection |
| **Copy: surface** |  | Copy only the surface from the current selection |
| **Paste: surface** |  | Paste only the surface (if any) from the system clipboard relative to the currently selected cell |
| **[Object placement mode](#Selection_/_Placement_mode.md)** | F3 | Toggle between selection mode and object placement mode. |
| **Delete building** | DELETE | Delete buildings from the current selection rectangle |
| **Delete surface** | CTRL+DELETE | Delete surface features from the current selection rectangle |
| **Delete both** | CTRL+SHIFT+DELETE | Delete both surface features and buildings from the current selection rectangle |
| **Clear buildings** |  | Remove all buildings from the current map |
| **Clear surface** |  | Remove all surface features from the current map |
| **Place roads** |  | Place roads specific to a race |
| **[Resize map...](#Resize_map.md)** |  | Resize the current map. All building and surface features remain intact, but might get beyond the allowed map cells |
| **Remove outbound objects** |  | Remove any building or surface feature which is partially or completely outside of the map boundary. Might be used after a map resize or fill operation. The operation displays a message box about how many buildings and surface features have been deleted. |

The cut, copy functions store an XML description of the selected elements on the System Clipboard.

### Selection / Placement mode ###

The map can be used in two modes: _selection mode_ and _placement mode_.

_Selection mode_ allows the user to select a rectangular region (in map coordinate terms) or select a building. This mode can be used to cut, copy, paste, delete or fill objects. In this mode, any placement started from the **Surfaces & Buildings** tables will ignore placeability constraints (e.g. you cannot place buildings on each other or on a multi-tile surface).

_Placement mode_ allows the user to place the selected surface feature or building onto the map. In this mode the placeability constraints apply, e.g. you cannot place buildings onto each other, beyond the map's edges or onto a multi-tile surface feature; you cannot place surface features beyond the map edges. Placing a multi-tile surface on another multi-tile surface removes the latter.

You can toggle between these modes with F3, menu or toolbar ( ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Down24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Down24.gif) ).


### Resize map ###

Brings up the **Resize Map** dialog.

![http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-resize.png](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-resize.png)

The user can enter the width and height of the new map. Width and height of the map is considered as how many tiles can be placed edge-by-edge. For example:

![http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-filenew-tiles.png](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-filenew-tiles.png)

has width of 3 and height of 2. The tiles are twice as wide as they are height, therefore, a square-like map has width **w** and height **2 x w**.

## View ##

![http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-view.png](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-view.png)

## Language ##

![http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-language.png](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-language.png)

## Help ##

![http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-help.png](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-help.png)


## Toolbar ##

The toolbar houses some quickly accessible operations.

![http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-toolbar.png](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-mapeditor-0.4-toolbar.png)

| _Symbol_ | _Description_ | _Menu_ | _Shortcut_ |
|:---------|:--------------|:-------|:-----------|
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/New24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/New24.gif) | Create a new empty map with custom size | **File** / **New...** |  |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Open24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Open24.gif) | Open an existing map | **File** / **Open...** | CTRL+O |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Save24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Save24.gif) | Save the current map | **File** / **Save** | CTRL+S |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Import24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Import24.gif) | Import a surface map or planet from the original game | **File** / **Import...** |  |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/SaveAs24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/SaveAs24.gif) | Save the current map into another name or location | **File** / **Save as...** |  |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Cut24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Cut24.gif) | Cut the selected area to the clipboard with both buildings and surface features  | **Edit** / **Cut** | CTRL+X |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Copy24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Copy24.gif) | Copy the selected area to the clipboard with both buildings and surface features | **Edit** / **Copy** | CTRL+C |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Paste24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Paste24.gif) | Paste surface and building information relative to the currently selected cell | **Edit** / **Paste** | CTRL+V |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Remove24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Remove24.gif) | Remove buildings from the currently selected area | **Edit** / **Delete building** | DELETE |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Undo24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Undo24.gif) | Undo the last map editing operation | **Edit** / **Undo** | CTRL+Z |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Redo24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Redo24.gif) | Redo the last map editing operation | **Edit** / **Redo** | CTRL+Y |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Down24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Down24.gif) | Toggle between object placement mode and selection mode | **Edit** / **Object placement mode** | F3 |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Zoom24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Zoom24.gif) | Zoom back to normal level | **View** / **Zoom normal** | CTRL+NUMPAD 0 |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/ZoomIn24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/ZoomIn24.gif) | Zoom in by 10% | **View** / **Zoom in** | CTRL+NUMPAD 9 |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/ZoomOut24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/ZoomOut24.gif) | Zoom out by 10% | **View** / **Zoom out** | CTRL+NUMPAD 3 |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/TipOfTheDay24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/TipOfTheDay24.gif) | Switch to full daylight | **View** / **Zoom normal** | CTRL+NUMPAD 7 |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/TipOfTheDayDark24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/TipOfTheDayDark24.gif) | Switch to night | **View** / **Zoom normal** | CTRL+NUMPAD 1 |
| ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Help24.gif](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/editors/res/Help24.gif) | Jump to this help in your default browser | **Help** / **Online help...** |  |


# Panels #

## Preview ##

## Surfaces & Buildings ##

## Building properties ##

## Allocation ##

# The Map #

# Notes #

### Mixed planets ###

The new rendering allows the placement of surface features and buildings from various types at once. You can create a half rocky, half snowy map with Garthog and Human buildings on it...

The only drawback is the type of the roads to apply. In the game, the planet's population race will always determine the type of the road graphics for simplicity. In the editor, the roads are rendered based on the last building's race that was placed on the surface.


# Screenshots #

<a href='http://darksideunderflow.com/images/open-ig-mapeditor-p09.png'><img width='250' height='250' src='http://darksideunderflow.com/images/open-ig-mapeditor-p09.png' /></a>


<a href='http://darksideunderflow.com/images/open-ig-mapeditor-0.1-p07.png'><img width='250' height='250' src='http://darksideunderflow.com/images/open-ig-mapeditor-0.1-p07.png' /></a>


<a href='http://karnokd.uw.hu/images/open-ig-mapeditor-02.png'><img width='250' height='250' src='http://karnokd.uw.hu/images/open-ig-mapeditor-02.png' /></a>

<a href='http://karnokd.uw.hu/images/open-ig-mapeditor-01.png'><img width='250' height='250' src='http://karnokd.uw.hu/images/open-ig-mapeditor-01.png' /></a>