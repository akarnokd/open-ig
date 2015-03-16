
# Introduction #

I collected some (not yet frequently asked) questions and answers about the Open-IG project.

# Questions & Answers #

## Why did you start this project? ##
Recently I pulled my old Imperium Galactica game and played with it using DOSBox. I noticed the age of this game, how the 640x480 resolution was too small and how unresponsive it worked. I figured out that maybe I could create a resizable game
interface. Luckily, the images, sound and animation is present, therefore I just need to create the engine to play it.

## Where did the inspiration came from? ##
My another old-time favorite is Open Transport Tycoon Deluxe, which started out as a modern time version of the old DOS program. Luckily, the rendering in Imperium Galactica much simpler.

## What new features are you talking about? ##
At this stage, these are only plans:

  * Resizable game window.
  * Load savegame files from the original game
  * Extension points to GUI, Tech tree, AI and whatnot.
  * Introduction of Easy and Custom difficulties
  * Multi-slot autosave support
  * Skirmish and multiplayer game

These features might seem a bit too large, but because the original game is a
very simple 2D game with well predictable data modell, it wouldn't be hard in my opinion.

## What version of the game does Open-IG support? ##

Once upon a time, the aim was to create an engine to run with the original game resources. But as time progressed, inconsistencies, bugs and other issues were encountered, which forced to project to permanently fix half of the original content. The game is standalone now with all resources included in the downloads.

## Isn't the game copyrighted? ##

~~The copyright belongs to Atari when I last checked. Recently, Digital Reality has released IG2 for iPad, which might mean they have some rights retained all these years.~~

~~The game doesn't depend on the original distribution. You can feel safer if you own the original but you can't purchase it first hand today to my best knowledge.~~

~~The original game is now abandonware and my actions are all fair-use.~~


Yes, the original game is copyrighted, along with all of its resources. However, - as of 3rd April, 2013 - I signed a contract with Digital Reality, the developer and current owner of the copyrights, and they gave permission to rework and/or publish any necessary files from the original for free as part of this project.

For further details, please see LegalFAQ page.


## Why Java? ##
I'm a Java developer so it was a simple decision. As a side effect, the Open-IG will be playable on any platform which supports the recent versions of Java.

## When will we see the source? ##
Source code of the latest stable build is available as download. The most recent changes can be checked out from the SVN. There are no plans for nightly builds.

## What kind of resources does the game use? ##
We are using PNG files for images, WAV and OGG files for sound effects and music, XML for data files and [custom video](http://code.google.com/p/open-ig/wiki/ANI_2009_Format) format.

## Can I comment/join? ##
You need to register with Google to be able to comment or report an issue (not my policy).

Joining the project is possible. I'm always looking for talents in the following topics:

  * Graphical works
  * Audio works
  * Translation, subtitling
  * Balance testing and adjustments
  * Java programmer for upcoming contents (e.g., Campaing editor, IG2 DLC, Spying screen, etc.)
  * Android programmer for porting
  * Java programmer for the multiplayer
  * Gameplay testers, who play the game end-to-end, over and over again.

## What about donations? ##
**Never.** Spend your money for something useful.

## Why does Open-IG take up so much resources? ##
The original game ran on a 486 with 16 MB of memory in DOS. But these hardware requirements made the developers have some compromises:
  * Simpler UI animations, graphical shortcuts
  * Graphic resources are cached and loaded on demand, causing delays on screen switches
In reflection of that, Open-IG is written in Java for modern machines and operating systems. But these have some effects on the game components:
  * The code runs in a virtual machine, which has some memory and runtime overhead
  * All game resources except the animations are loaded into memory, using 70MB of memory
  * The in memory representation of the game images use 32bit RGBA. Compared to the 8 bit compressed PCX images, this is a 4 - 10 times of increase
But these properties on todays computers are not bad at all. Here is why:
  * Plenty of memory is available in modern computers, 1GB on average. Open-IG consumes below 128MB, therefore, there remains enough room for the OS, your browser etc.
  * Java uses JIT to compile the hot spots of the code, eliminating any interpretation overhead
  * Rendering is mostly accelerated by GPU which likes 32 bit images
  * Threading is heavily used which might utilize multiple cores
  * Event and timer driven rendering instead of a main loop to consume all CPU.

## Why is the development slow? ##
It has some aspects:
  * I develop it basically alone.
  * Each screen has enormous amount of small pieces: buttons, text, clickable obects etc. These have to be placed, wired together manually to resemble to the original screen and behavior. Most screens are not even buildable/testable until a real data model is under it (this is the case with the recently enhanced information screen). This needs lots of testing and comparison with the original game.
  * Some issues require small developments but require lots of explaining.
  * Some issues look simple but require complicated developments.

## What languages are supported? ##

English, German, French, Russian and Hungarian are supported at the moment. These include fixed graphics text (on buttons) and dynamic text on other labels.

## Can I change the language? ##
Yes. The Launcher lets you choose a language and the game itself lets you do that on the main menu. Currently, there is no easy way to change the language besides hitting CTRL+1 thru CTRL+5 in the main menu.

## What differences exist between the varius language versions of the original game? ##
Apart from having different language labels, there is a really subtle difference in the game's animation files. Strangely, in the english version these animations are in lower resolution, about 2/3 of the hungarian version. The reason is unknown. Certainly, the size gain is neglectible as the english version is still occupying 2 CDs with total size of 1.1GB.

## Will the Open-IG contain the famous "á la Bokros" tax setting? ##
Yes it will/does already. For non-hungarian players to understand, "á la Bokros" tax settings is a practical joke about a hungarian economic minister who was in charge during the development of the original game around 1995. This minister has increased the taxing level beyond anything seen before to 'fight' the economic crisis of that time. The game developers wanted to capture this not so famous deed in the game. In the english version, the top tax level was simply named Oppressive.

## How can I use the checked-out source code? ##
I advise to use an IDE and import the source code as a Java project. You can also directly check out code from the SVN repository in most IDEs (you might need third party SVN plugins).

In addition, the I use the following tools and plugins with [Eclipse](http://eclipse.org/) to ensure the quality of the code:

  * [Eclipse-CS](http://eclipse-cs.sourceforge.net/downloads.html)
  * [FindBugs](http://findbugs.sourceforge.net/)
  * [JDepend4Eclipse](http://andrei.gmxhome.de/jdepend4eclipse/index.html)
  * [Memory Analizer](http://www.eclipse.org/mat/)
  * [VisualVM](http://visualvm.java.net/)

## We are in 2011, what about the copyright now? ##

~~This is a fully legitimate question. The original game is now 14 years old and nobody else seems to care about it or Open-IG at the current phase. Still, depending on how much success it will become once it reaches a playable state, the story might end. Based on experiences happening these days in software industry, these are the options:~~

  * ~~Nothing happens, everyone can use it.
  *~~Oracle sues me because I dared to mention the word "Java".
  * ~~Atari (the current owner of the original license) gives an official supportive statement (e.g., they won't sue me).
  *~~Atari sues me because if the success (and money) is not theirs, then noone should have it. If money = free, then especially likely.
  * ~~Before I finish, Atari creates a nostalgic (and working) version from the original game, taking away the fun.~~

Please see the LegalFAQ for an updated description of this and similar questions.