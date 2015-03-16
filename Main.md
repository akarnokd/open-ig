
# Introduction #
Open-IG is an open source Java<sup>TM</sup> clone of the [Digital Reality](http://www.digitalreality.hu) game [Imperium Galactica](http://en.wikipedia.org/wiki/Imperium_Galactica), a popular real time space strategy game developed around 1997 in Hungary. Open-IG attempts to mimic the original game as closely as possible while extending it with new features. It needs the original Imperium Galactica files to run.

Open-IG is licensed under the GNU Lesser General Public License.

Some teaser images can be found on the [screenshots](http://code.google.com/p/open-ig/wiki/Screenshots) page.

Check out the [Questions and answers](http://code.google.com/p/open-ig/wiki/Questions) page.

# Installation #

  1. Install Java JRE/JDK 1.6.X (preferrably X >= 10) or later.
  1. Copy the contents of the original Imperium Galactica CD1 and CD2 into one directory (duplicate files can be simply overwritten).
  1. Put the `open-ig-0.72.jar` file into that directory. Note that the application requires at least 160 MB of memory. You can use the -Xmx command line parameter or change the .jar file extension assignment settings in your operating system to include it.
  1. To run the game double click on this file or use the command line

`java -Xmx160M -jar open-ig-0.72.jar`

> If you don't want to put the JAR with the game files, you could use an extra command line parameter to specify the home of the IG files (the path might be case sensitive on your operating system):

`java -Xmx160M -jar open-ig-0.72.jar c:\games\ig\`

Java 1.6u10 thru 1.6u14 seems to have some performance issues with the newly introduced D3D pipeline, causing the game to consume lots of CPU resources and rendering becomes slow. In this case, you could run the program with the `-Dsun.java2d.d3d=false` command line parameter and everything should be fast again. My experience is that running the game on JDK7 does not suffer this performance problem but runs even faster with lower CPU consumption.

On Windows you can change the default memory allocated for a Java program run from a JAR file by browsing to the `HKEY_CLASSES_ROOT\jarfile\shell\open\command\(default)` key in **Regedit** and set it to

`"C:\Program Files\Java\jre6\bin\javaw.exe" -Xmx256M -jar "%1" %*`

by adding the `-Xmx256M` before the `-jar` parameter.

Please see this page later on or the [documentation](Documentation.md) page for what can you do within the application at the moment.

## System requirements ##
  * Java JRE/JDK 1.6.X or 1.7.X
  * Any operating system with graphical user interface
  * A mouse with scroll wheel.
  * 160MB RAM
  * 2-15 MB for Open-IG, 1.2 GB for original Imperium Galactica
  * 1 GHz P4 or equivalent (estimated)
  * Optional: GPU with DirectX/OpenGL hardware acceleration

# Documentation #

## Features ##
  * Main menu screen
    * Click on the five options to jump to Starmap, Options screens; play the Title and Intro animations or exit the program
  * Options screen, accessible through the Load main menu option or ESC
    * Click on the varius checkboxes to toggle checked options (no effect yet)
    * Move the Music and Sound sliders to adjust volume
  * Starmap screen
    * Accessible through F2 or via the Starmap buttons
    * Hold down the right mouse button in the starmap area to pan the viewport
    * You could drag the vertical and horizontal scrollbar with the left mouse button
    * Use CTRL + Mouse Scroll to zoom in/out of the viewport
    * Use Mouse scroll to scroll up/down
    * Use SHIFT + Mouse scroll to scroll left/right
    * Resize the window to see how the various fields grow
    * Use CTRL+K to show non-player planets on the starmap, as they where just discovered
    * Use CTRL+N to display names/owners of non-player planets on the starmap as they where having spy satelite v2
    * Use CTRL+F to display non-player fleets
    * Right click on the minimap to see the scroll animate
    * Use left click to select a planet/fleet
    * Use left click on the equipment (fleets) name to select the fleet
    * Use right click to instantly jump to the equipmnet (fleet)
    * Use middle mouse click to scroll-animate to the equipment (fleet)
    * Use + or - to cycle through the player's planets
    * CTRL+. to toggle between default/neighbor/bilinear/bicubic interpolation of the starmap background
  * Production screen
    * Accessible through F5 or the production buttons
  * Research screen
    * Accessible through F6 or the research buttons
    * Use CTRL+R to research the currently selected technology
  * Planet screen
    * Accessible through F3 or via the Planet/Colony buttons
    * Hold down the right mouse button over the planet surface to pan the viewport
    * Click on the Buildings, Radar, Building Info and Lower left buttons to toggle the panels
    * There are 7 surface types (Desert, Ice, Crater, Rocky, Water, Earth, Nectoplasm) and several variations for each.
    * Use + or - to cycle through the player's planets
    * Use minimap to select a planet
    * Use the Tax+ Tax- buttons to set tax level on the selected planet
  * Infromation screen
    * Accessible through F7 or via the Info buttons.
    * Click on the buttons to show various information tabs.
    * Planets screen shows known planets, click on the planet names or the minimap to display details
    * Colony info screen shows information about the currently selected colony
    * Fleets shows information about known fleets

## Differences from the original game ##
Apart from the tons of missing screens and behaviors, there are few deliberate changes:
  * Starmap and planet screens are occupying the entire window, the rest are centered and shadow out the big screens behind them. The original game was constrained to 640x480.
  * Scrolling the starmap and planet is done using right-mouse pan. In the original game, you had to hold the right mouse button near the edges of the view to scroll it. I think this is a bit cumbersome for a modern UI game.
  * Zoom is done with CTRL+Mouse Wheel. You can also left/right click on the zoom button to zoom in/out. The original game, you could click on the zoom button, or switch to zoom mode and click on the starmap. This is understandable, as in 1996 there were not many mouses with scroll wheel.

See the [documentation](Documentation.md) and [new features](NewFeatures.md) wiki page.

# Development #

Currently, not much info can I provide.

## Developer resources ##

  * [PAC file format](PAC_Format.md) specification
  * [ANI file format](ANI_Format.md) specification
  * [LZSS decompression algorithm](Custom_LZSS.md) used in the ANI files
  * [RLE decompression algorithm](Custom_RLE.md) used in the ANI files
  * [Surface map files](Surface_Maps.md)

# Tools #

The Open-IG will play the original game's ANI files. To play these files separately, please use the [AnimPlay](http://open-ig.googlecode.com/files/animplay-0.55.jar) utility. Double click on this file or use the command line `java -jar animplay-0.55.jar`. The utility will allow you to select a file to play. You can find more info on the AnimPlay page.
(The source code is available from the SVN.)

## External tools ##

There exist an unpacker for the game PAC files. You can downoad it from [here](http://bgafc.t-hosting.hu/data/ig1pac_h.exe).

# About the author #
I'm a Ph.D student specializing in Production Informatics, Supply Chains and Manufacturing Execution Systems.