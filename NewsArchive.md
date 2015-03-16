

# Recent #

## August, 2013 ##

**August 16, 2013**

Note that starting from version 0.95.131 the game won't run on Java 6 anymore.

## May, 2013 ##

**May 9, 2013**

[Java 6 support will be dropped](http://open-ig-dev.blogspot.hu/2013/05/java-6-support-to-be-dropped.html) in the upcoming major version.

## April, 2013 ##

**ANNOUNCEMENT**

I'm happy to report that I signed a contract with Digital Reality (the original developers who own the rights to Imperium Galactica again) and I received the official permission to use the original Imperium Galactica resources in this Open Imperium Galactica project, and make them or any modified/derived resources **freely** downloadable from this site.

In other terms, Open Imperium Galactica is now a completely legal project and you, the users can freely and legally download and play Open-IG.

Soon I'll compose a FAQ about the subtle details of this contract and how it affects the LGPL part of the project.

## February, 2013 ##

**February 6, 2013**

I have to put Open-IG aside for a few months. I hope when I return to the development I'll be able to continue with the multiplayer feature. However, this will put the Campaign Editor aside.

## December, 2012 ##

**December 24, 2012**

Happy holidays!

## November, 2012 ##

**November 9, 2012**

I put the Android version of Open-IG on hold indefinitely. The decision came from considering several aspects:

  * I'm not sure they would let it into the online mobile store, even if it's free.
  * It's a significant undertaking with very little return. I can use the time to finish up the desktop version and/or my upcoming 4X game.
  * The game probably won't fit onto devices but the most expensive.
  * The questionare shows that about 93% of people voted for don't or low priority.

## September, 2012 ##

**September 18, 2012**

We have, again, entered into a slow-paced development phase. Expect only minor updates and bugfixes only, no "game changer" modifications for a month at least. I'll try to push out a simple campaign editor within 2 weeks.

If you haven't filled out the forms, please do so:

[English form](https://docs.google.com/spreadsheet/viewform?formkey=dE9uSTQ5RUlNUS14MTBzNmRRallOSlE6MQ#gid=0)

<img src='http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/hungarian.png' alt='Hungarian flag' width='32' />[Magyar űrlap](https://docs.google.com/spreadsheet/viewform?formkey=dHlxTFFPVHo3NmtQQlJJdW1WSkloQkE6MA#gid=0)

## August, 2012 ##

**August 22, 2012**

The skirmish is almost complete, you can now create a game, but victory conditions are not checked yet, and some advanced features (such as shared radar, non-attackable allies, pirate behavior) are not implemented yet. But still, you should be able to play, save and load the skirmish game.

Note that many labels are still not translated into german and french.

For documentation on the new Skirmish screen, please refer to these blog posts: [Galaxy, Economy and Victory](http://open-ig-dev.blogspot.hu/2012/08/some-skirmish-documentation.html) tabs and [Players](http://open-ig-dev.blogspot.hu/2012/08/skirmish-documentation-player-selection.html) tab.

Enjoy.

**August 18, 2012**

In order to give some flexibility to the skirmish players, I have added a basic trait system, commonly found in other 4X turn based games. This way, you can customize the parameters and capabilities of your race for the skirmish play beyond the default Open-IG values.

To make things fun, I made this trait selection available for the campaign. It is totally optional. Check out the <a href='https://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-traits-screen.png'>screenshot</a>.

If you have comments and suggestions about new traits or their point value, make a comment <a href='http://code.google.com/p/open-ig/issues/detail?id=642'>here</a>. If you happen to find a trait related bug, please report it in a new and separate issue.

**August 14, 2012**

We are making progress again! See the [release notes](https://code.google.com/p/open-ig/wiki/Game_Notes) for details.

In the mean time, you could help us by filling in the following questionare in either english or hungarian:

[English form](https://docs.google.com/spreadsheet/viewform?formkey=dE9uSTQ5RUlNUS14MTBzNmRRallOSlE6MQ#gid=0)

<img src='http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/hungarian.png' alt='Hungarian flag' width='32' />[Magyar űrlap](https://docs.google.com/spreadsheet/viewform?formkey=dHlxTFFPVHo3NmtQQlJJdW1WSkloQkE6MA#gid=0)

**August 8, 2012**

Sorry for the inactivity period. I was submerged into my professional job activities lately, but I have 2 weeks of free time coming up soon. The major things I planned:

  * Finish the quick production panel
  * Add weapon vs target type logic to space battle to overcome the HP and DPS issues, e.g., planetary gun is only 5% effective against fighters.
  * Start the campaign editor with a few important panels: battle settings, technology settings, labels & translation. These will help testers in balancing the space and ground battles more easily (e.g., without me to release a new data file every 5 minutes).

The upcoming **Multiplayer feature** is still in thinking phase. Primarily, I'm aiming for a 2 player competitive mode without additional AI players (nor Traders). More than 2 players or co-op against AI players seems to be technically too difficult for me now.

Any hints and tips about how to implement multiplayer network communication would be greatly appreciated.


## June, 2012 ##

**June 3, 2012**

Hi folks! We have reached the doorstep of the **Beta** release. If the following day's incubation period doesn't hatch any small issues, we will officially release the **Beta**. Stay tuned and keep testing!


## May, 2012 ##

**May 14, 2012**

I underestimated the work amount for the 0.95 issues. It may take some more time. Until then, here is a little teaser from our latest contributor, Norbert "Silver83" Pellny:

<a href='http://www.youtube.com/watch?feature=player_embedded&v=FnlIEwW2OkM' target='_blank'><img src='http://img.youtube.com/vi/FnlIEwW2OkM/0.jpg' width='425' height=344 /></a>

**May 4, 2012**

Due to the limited space here on Google Code, please compress any game save attachment and use external image hosting when reporting a new issue.

**May 3, 2012**

It's almost time to yell **BETA** at Open Imperium Galactica! We are almost there, just a few smaller features are missing. So here is the plan:

**May 13**: Implement everything marked in the issue list up to **Milestone-0.95** and release it as the **beta**. The aim of the **beta** is to have an end-to-end playable main campaign. For those who were afraid to play the game, this will be the right time to start playing.

The aim of the **beta** is to discover:

  * problems with the missions (timing, outcome, failure conditions),
  * issues with the interface,
  * difficulty problems (too many or to few AI attacks),
  * planet mechanics problems,
  * space and ground battle speed and balance issues,
  * etc.

Things that are deferred till **beta 2**:

  * resolving the mission differences between Open-IG and the original game
  * ingame skirmish match creator


## April, 2012 ##


**April 30, 2012**

Space chat has arrived.

**April 23, 2012**

If you run into a crash while contacting Achilles, please exit the game and select **Other options > Verify install** from the Launcher.

**April 21, 2012**

I've tried my best to translate Open-IG into German. Still, I'm sure many grammar errors remained in the text. Please, do not flood the issue list with individual error reporting, instead, use [this issue](http://code.google.com/p/open-ig/issues/detail?id=356) as gathering place. And yes, the subtitles aren't translated.

I can't say much about the French translation yet. I don't speak french at all, therefore, only things based on position (i.e. image coordinates, file name) can I successufully integrate.

**April 20, 2012**

German translation will take some time. I've already extracted voice and image data, but the labels with about 3000 entries is extremely tedious.

But you could help me in transcribing the videos (~140 of them).

**April 15, 2012**

This must be truly the end of times since I'm ill for the seventh time this year :(

Anyway, I'm looking for gamers who have the original game in German, [French](http://www.gametronik.com/site/fiche/abandonware_strategie/Imperium%2520Galactica/) or Spanish. If so, you could help me extend Open-IG with these languages by sending me the following files:

  * TEXT.PAC
  * `*`.PCX except from the folder STATPICS

This should be about 20MB of data.

Thanks.

**April 9, 2012**

I have released the improvements of the last months developments. Among these you'll find:

  * basic diplomacy: you can contact aliens, but their responses are randomized.
  * global speed control: speed up the simulation to 60 min/seconds, a day in super fast would last about 2 seconds
  * various visual improvements and bugfixes

**April 7, 2012**

I managed to reserve some time for development and did several improvements to the game. Most notably the inclusion of global speed settings. But the main task is with the diplomacy.

I've implemented the screen for outgoing negotiations and the only missing part is the way AI should react to diplomatic negotiations. Unfortunately, I couldn't find information about how to do it; most suggestions were to implement heuristics about how I would react to such scenarios.

If you have any idea about or experience with such diplomatic systems, or just want to help, please add your comment to [this issue](http://code.google.com/p/open-ig/issues/detail?id=319). The questions to answer are like:

`If the player offered me X credits, when would I say Yes/No?`


## March, 2012 ##

**March 11, 2012**

ME3? I'm speechless.

Anyway, back to implementing the diplomacy.

**March 6, 2012**

For those who have to wait until ME3 unlocks on friday, I'm glad to announce the mission-complete version of Open-IG. All missions on the 5 ranks have been implemented. What's remaining:

> - Diplomacy screen, AI diplomacy routines
> - Space chat
> - Minor grahpical and behavior fixes.

## February, 2012 ##


**February 27, 2012**

This weekend, I tried to run Imperium Galactica 2: Alliances to see how it works, looks and behaves compared to IG1. OMG it's so different; ugly, slow and doesn't always work. But the story is still attractive.

Let's make a mod/DLC from IG2 for Open Imperium Galactica. Of course, there are a few significant changes:

  * simplified production/research
  * ship design for fixed equipment configurations, lack of separate equipment management
  * spying
  * market
  * proprietary BIK videos
  * no bar

The spying (without the 3D effects) and market screens are relatively simple to implement, the rest is just technology tree and numbers.

The biggest roadblock is the BIK videos: they are much larger than IG1's, probably can't be republished and there is no embeddable Java player. One solution is to call an external multi-platform video player (e.g., VLC) for each cutscene and convert the smaller message portraits to the embedded format.

**To create this DLC, I need volunteers for the following tasks:**

  * For each race:
    * compose technology information, costs, strengths, etc.
  * The campaign races (Solarian, Kra'hen, Shinari)
    * list missions, conditions, rewards

Some of these information can be located in walkthroughs, but those are unfortunately written from the player's perspective, and not from a developer's perspective.

**February 23, 2012**

Level 3 is now complete and you'll be promoted to Admiral. Beyond that, nothing else now.

Test and enjoy.

**February 13, 2012**

Hi there! Finally, mission 17 is available which will warp you to Level 3!

I suggest you don't finish mission 18 by capturing all planets. Currently, the game won't progress any further and you would skip the yet-to-come missions 19-21 anyway.

**February 10, 2012**

"New DLC available for Open-IG!" - some publishers would advertise :)

Missions 13 and 16 are playable, should start 4 days after finishing the virus carrier mission 14.

I'll try to implement mission 17 and the jump into Level 3.

**February 4, 2012**

A new release with a few enhancements:

  * Mission 12
  * Mission 14
  * Groundwar AI
  * Wife

## January, 2012 ##

**January 29, 2012**

New release with fixes mostly. Unfortunately, the game experiences some issues under Linux and Mac lately, typically related to sound.

These are almost always problems with the installed Java Runtime. If you experience JVM crashes or hangs in space battle (where most of the sound FX happens at once), you can do any of the following:

  * Set the effect volume to zero while in space battle.
  * If running a 64 bit JVM, try switching to 32 bit JVM
  * Upgrade your JVM to the latest Java 6/7 version if available on your platform.
  * Make sure only a single vendor JVM is installed at once (either Oracle or OpenJDK)
  * Switch to Oracle JDK or switch to OpenJDK. Oracle JDK seems to work better on Linux at the moment.

In case of a JVM crash, you may attempt to report a bug in the respective JDK issue tracker.

**January 22, 2012**

I've hidden some bugs in the game, can you find them? :)

A few recent bug reports required to release a new version a bit early. Here, mission 9, 10 and 11 are available, although not well tested.

**January 21, 2012**

I guess you were eager to grab onto more missions. Now here they are:

  * Level 1 (Lieutenant) can be considered done.
  * Level 2 (Captain) mission 6, 7, 8 and 15, but mission 7 part 2 won't trigger yet

Enjoy.

**January 16, 2012**

Már egy ideje terveztem új videó készítését. Íme:

<a href='http://www.youtube.com/watch?feature=player_embedded&v=qHBUdUqqI-k' target='_blank'><img src='http://img.youtube.com/vi/qHBUdUqqI-k/0.jpg' width='425' height=344 /></a><a href='http://www.youtube.com/watch?feature=player_embedded&v=lImUf9gHWAs' target='_blank'><img src='http://img.youtube.com/vi/lImUf9gHWAs/0.jpg' width='425' height=344 /></a>

**January 15, 2012**

Thanks to the great work of Joe, we can finally announce the version 0.94, where the AI and some missions are in place. Enjoy.

Notable improvements:

  * Introduction of the Objectives system, accessible through the TAB key or the checkmark/cross icons in the top-middle statusbar.
  * Missions 1, 2 and 3 playable
  * Introduction of the Main Campaign Freeplay (previously the Main Campaign)
  * The bridge is now fully functional. You can send and receive messages.
  * Game over is now possible.

**January 11, 2012**

Hi there! New release with several bug fixes and improvements to UI and AI.
One of the long awaited features is the ability to switch to classical right-click-action mode. In this mode, you can pan the maps with the middle mouse button. Right clicking on space/ground moves the units, right clicking on enemies issues attack order.

**January 2, 2012**

The latest version of the game may cause some inconveniences, namely unexpected hang and disappearance of saves. You may let the Launcher to update itself to version 0.13.

We recommend that you backup your **save/default** directory, as the game now limits the saves to 5 quick- and 5 autosave, and all pre v0.93.510 versions' save is considered autosave.

**January 1, 2012**

Happy new year! Four years ago I thought I can simply reimplement Imperium Galactica within 6 months...

Anyway, here is a new release with improved AI.

**Note: if the game hangs, quit the Launcher.**

# Earlier #

## December, 2011 ##

**December 30, 2011**

We've developed and tested the first version of the AI. The AI is capable to manage colonies, explore the galaxy, spy on other races, produce colonization fleets and colonize empty planets. The AI is disabled for the alien races at the moment.

I've added a feature to enable AI support for the human player. By pressing CTRL+J (outside of battle), you can enable and disable this feature. When enabled, the AI will manage your entire realm. You can still build, produce, research and issue orders to fleets. You don't need to click-race with the AI, just pause the game. (This test feature lets us observe the decisions of the AI in a convenient way.)

In addition, the "AI managed" auto-build option has been added. It uses the same AI planning algorithm, as the full AI. Switching to this mode will keep your colony healthy, so long there is enough funding.

Finally, I've added a "From scratch" game mode. Start a new game and press CTRL+K. This will remove all of your colonies except Achilles, gives you some money and allows you to build a Colony Ship immediately (but you need factories for that). Note that this mode is extremely slow to ramp up. You'll need to spend months in fast forward. Try enabling the AI for this case and let's see what happens.

Oh, and if you are desperately in need for money, press CTRL+G several times :)

**December 27, 2011**

Fixes, fixes, fixes. Anyway, the AI still complicated thing. I'm still struggling with goal arbitration, alternative actions and parallel actions.

**December 15, 2011**

A small release with a more lively galaxy.

I'm still reading about AI techniques. Most sources just throw up the planning algorithm: Knapsack solver, Heuristic solver, Goal Oriented Action Planning, A`*`, etc. These are fine, but the never talk about how to determine the value/score/priority of the entities statically and dinamically.

**December 7, 2011**

After a big rush in another open source [project](https://sourceforge.net/projects/advance-project/) of mine (which finally contained real computer science!) I'm back to Open-IG and doing the next big thing: AI for general operations. I found a reasonable [explanation](http://www.gamasutra.com/view/feature/1535/designing_ai_algorithms_for_.php) of how to do it in principle.

## October, 2011 ##

Finally! The basic ground battle simulation is integrated into the game, but with two major shortcomings:
  * Vehicles move over each other
  * Firepower vs. Hitpoints are not properly adjusted, but Joe is working on it.

Note also the memory requirements are now at least 768MB.

## September, 2011 ##

**September 21, 2011**

The ground and space battles are almost playable. The notable missing feature is the manual deployment of vehicles and the proper damage / hitpoint adjustments.

## August, 2011 ##

**August 25, 2011**

József "Joe" Torma officially joined the project, as you might have already noticed. He is doing an excellent job in discovering missing features or bugs in the game, which is good for the game quality, but bad for my schedule.

I need to spend more time on fixing bugs and introducing convenience improvements instead of doing the next big thing implementation.

Anyway, the feature-complete beta release will come hopefully in the beginning of October, 2011. Until then, keep testing!

**August 18, 2011**

A developer build has been released (we are working on the game sound effects). It contains some bug fixes and gui improvements, and a small space battle implementation. If you attack an alien planet, the game switches to the spacewar screen and displays ground and space defenses. That's all. Just press F2 or F3 to return to the normal view. If you are lucky and the targeted planet has no defenses at all, you'll conquer the planet without fight.

**August 12, 2011**

Finally, I have some free time and willingness to resume the development. The next big thing is surface and space battles.

## July, 2011 ##

**July 29, 2011**

[Java 7 is out](http://www.oracle.com/technetwork/java/javase/downloads/index.html). On Windows, the new version makes the game run much faster due the improved 2D rendering pipeline. Consider upgrading to it.

In the meanwile, I start working on the basic space and surface battle system, a feature much more awaited than diplomacy I guess.

**July 19., 2011**

Version 0.92 is out. I decided to release it due some small but annoying bugs in 0.91 and 0.92b.

**July 17., 2011**

Development resumes, but a bit slowly due the heat.

On a different note, I always wanted to create some "Let's play Open Imperium Galactica" videos. They could be handy tutorials for the game. Unfortunately, creating such videos takes too much time away from development tasks.

Therefore, here is an open call to anybody who has the time and willingness to **record and narrate one or more**. Language should be mainly English or Hungarian, but you  could just record it in your own language and (I, we, etc.) create English subtitles later on. Upload it to youtube and notify me. As the "reward", the video will be linked here and the author(s) get a nice place in the Acknowledgements and the in-game Credits.

Note that by Murphy's Law we may expect some fine bugs or functional inconveniences to pop up during the recording.


## June, 2011 ##

**June 17, 2011**

It's deliverable time again for FP7; and I'm a bit tired these days. Hopefully, I will be able to continue development in July (see the updated project plan).


## May, 2011 ##

**May 25, 2011**

It's conference time again, therefore, I had to pause the development.

The following additional changes are planned:

  * Remove upgrade options from morale-boosting buildings (Church, Bar, etc.). Their effect on the population and tax income make the late game too simple.
  * Reduce the morale effect of the Police station on planets where the owner has the same race as the population.
  * Remove upgrade options of research laboratories.
  * Introduce population demand for a fire brigade.
  * Introduce earthquake on earth-like and rocky planets.
  * Add Time-to-Live for satellites.


## April, 2011 ##

**April 22, 2011**

Going through the hundreds of diplomatic options reminded me about some testing related to-do where the community could help:
  * the consistent naming of the other races in various contexts (database, alien info, etc.),
  * typos or grammar errors, or
  * other kind of inconsistencies.

Some of these issues may originate from the original game, others are new, but I'd like to have a better game clone.

**April 21, 2011**

Here comes the next release of the game: version 0.91! It contains various bugfixes and improvements of the user interface, some gameplay mechanics adjustments. The Options, Credits and Test screens have been implemented. Please see the [release notes](http://code.google.com/p/open-ig/wiki/Game_Notes#Version_0.91_-_General_improvements) for further details.

**April 16., 2011**

Now that Open-IG is playable, I wonder whether the [gameplay mechanics](http://code.google.com/p/open-ig/wiki/GameGuide#Gameplay_mechanics) are working as one would expect. I have doubts about the following properties:

  * Building construction speed (200 hp / 10 game minutes): maybe too fast
  * Building repair speed (50 hp / 10 game minutes, 20 credits / minutes): maybe slow
  * Research speed (e.g., a 100.000 credit research taking 50000 game minutes): maybe too slow
  * Production speed (e.g., with 1000 capacity, 20 credits / 10 game minutes worth of product): maybe slow
  * Colony morale effects: medium and large shortages decrease, extra living space increase, morale buildings increase
  * Upgrade system: you can upgrade banks and churches, but doesn't it ridiculously increase the money income later?

What do you think? What was your gameplay experience?

**April 15., 2011**

Yet another weekend and yet another playable version to test. Please welcome version 0.90 with the long awaited fleet management features. You can now create, move and manage fleets, colonize empty planets.

[See the release notes.](http://code.google.com/p/open-ig/wiki/Game_Notes#Version_0.90_-_Fleets)

**April 14. 2011**

The equipment screen is complete and it is possible now to manage the fleets and move them around on the starmap.

However, we reached the point where the default quicksave/quickload is not enough to help in testing various scenarios. Therefore, I moved the Load/Save/Options screen development earlier and will release the new v0.90 with it.

Consequently, the game becomes more and more playable, which puts on the demand for thorough testing. Unfortunately, the gameplay mechanics lets you develop your first colony ship around after 3 hours of playtime; can't develop and play at the same time. This is where you, the community can help to diverse and multiply the testing power and coverage.

**April 10. 2011**

I've implemented the bar and database screens, improved and fixed the presentation on many screens.

Reminder: hit CTRL+O to take ownership of an empty or enemy planet, if you need to build more laboratories or factories.

[Release Notes](http://code.google.com/p/open-ig/wiki/Game_Notes#Version_0.89_-_Bar_and_Database)

**April 8. 2011**

Improvements to the game mechanics and user interface. Quick save (CTRL+S) and quick load (CTRL+L) feature added. See the [release notes](http://code.google.com/p/open-ig/wiki/Release_Notes) and the [Screenshots](http://code.google.com/p/open-ig/wiki/Screenshots#Version_0.88_screenshots) for further details.

A long standing issue will be the morale and population calculation of the game. It is hard to design a proper mechanics which considers the tax, shortages, stadiums, etc.

Please, start playing Open Imperium Galactica and if you encounter some strange situation, report it via the [issues](http://code.google.com/p/open-ig/issues/list) page.

**April 6. 2011**

I'm happy to announce that Open-IG is now playable - sort of. I've implemented most information screen tabs (except the aliens and fleets tabs). The rest of the tabs should work as expected.

I've also updated the research and production screens to reflect the true status of the world in terms of capacities and technologies.

The third major improvements is the introduction of simulation: you can now build, upgrade, repair and demolish buildings; research a technology if you have enough laboratories; build ships and equipment; see how the planet morale evolves over the days; see the day-night cycles.

A temporary cheat: hit CTRL+O to take ownership of an empty or enemy planet, if you need to build more laboratories or factories.

**Note:** no options or load/save implemented yet.

## March, 2011 ##

**March 30. 2011**

The colony screen mostly working. You cannot build yet (the backing model is still incomplete), but you can demolish/disable buildings. See [the Game Guide](http://code.google.com/p/open-ig/wiki/GameGuide) for a list of available keyboard/mouse options.

There are two other notable changes in the application:
  * A **Console Watcher** window should pop up on any program error. You can use it to report an issue.
  * The memory requirements are now set to 512 MB.

**March 22. 2011**

Open Imperium Galactica [helps](http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7029238) the Java 7 development :)

**March 20. 2011**

The new research and production screens are available as well as a hopeful fix for the infinitie restart bug on Linux machines.

**March 18. 2011**

Please use the project's [Issues](http://code.google.com/p/open-ig/issues/list) page if you have trouble starting or running the game. Without feedback, I might think everything works if the game runs on my Windows 7 x86 machines. (It turns out the game has startup problems on Ubuntu machines.)

**March 6. 2011**

Equipment screen created. Not 100% functional as many things will depend on the actual data model coming later.

## February, 2011 ##

**February 27. 2011**

I added the promised new mini UI framework, which should allow me to implement screens more conveniently. In addition, I've improved the Testbed application with a proper ship walk screen. The equipment screen will come within a week.

**February 24. 2011**

I face two major inconvenience with the current screen design:

  * long resource loading times -> deferred or lazy (image) resource loading,
  * lack of composition -> component based approach instead of screen based approach.

I'll have to design a component system similar to Swing containers to handle events, rendering and layout, but with less overhead and complexity (due Swings aims to be a generic UI).

This way, building the remaining screens and reusing features such as arbitrarily labeled buttons and scroll-boxes should be much more convenient.

**February 12. 2011**

I've completed the Testbed application. In addition, the loading times should be now faster on multi-core systems. The testbed application is also available among the launcher downloadable modules.

I strongly recommend to use/download the launcher application which will take care of updating your existing installments.

**February 8. 2011**

I'm feeling a bit uneasy about neglecting this nice project lately. It seems so easy to do it since I converted and grouped the resource files: all I have to do is draw them on screen or play it through the speakers, right?

After mining 1.7 million m<sup>3</sup> cobblestone, dirt and sand, completing a major industrial software [development](http://www.reliawind.eu/), I feel ready to continue with research, and of course, hobby development of Open Imperium Galactica.

I'll try to set up some milestones about the following 6 months. See the
[project plan for 2011](http://code.google.com/p/open-ig/wiki/ProjectPlan2011).

## October, 2010 ##

**October 31. 2010**

I've integrated the starmap and planet rendering into the main Open Imperium Galactica. You can now download the version 0.81 file.

Requirements:

  * At least 384 MB of memory
  * The [open-ig-upgrade-20100917a.zip](http://open-ig.googlecode.com/files/open-ig-upgrade-20100917a.zip) upgrade pack.

I'm still working (thinking about) the promised launcher for Open-IG.

**October 3. 2010**

It's the yearly article time for my PhD. Therefore, I am unable to do effective work in Open-IG right now.

The next step in the development will be a Launcher with ability to automatically update the game files. In addition, the new Starmap and Planet screens will be integrated with the main game till the end of october.

## September, 2010 ##

**September 18. 2010**

Just two Youtube videos about the MapEditor v0.4: [480p](http://www.youtube.com/watch?v=mzrY9bO7tk8) and [720p](http://www.youtube.com/watch?v=x2g76kjZDzc).

**September 17. 2010**

The newest MapEditor with version 0.4 is here!

  * Ability to Cut, Copy and Paste the selected buildings, surfaces or both.
  * Ability to create any custom sized map and resize the current map
  * Ability to clear buildings/surfaces that fell outside the map's current bounds
  * New visual option: Use a standard font (e.g. Courier New) for text instead of the classical font
  * New visual option: display locations where buildings cannot be placed

NOTE: the editor requires the [open-ig-upgrade-20100917a.zip](http://open-ig.googlecode.com/files/open-ig-upgrade-20100917a.zip) upgrade pack.

**September 16. 2010**

The newest MapEditor with version 0.3 is here! [Screenshot](http://darksideunderflow.com/images/open-ig-mapeditor-p09.png)

  * Improved resource management: the game/editor will automatically locate files and upgrades of interest
  * English and Hungarian text!
  * Ability to Undo and Redo operations on the map
  * Ability to Import, Open and Save maps
  * Toolbar with easy access functionality.
  * The name cells in the surface and building tables can be renamed and the editor remembers them.
  * The editor remembers the settings of its window and restores itself to it.
  * The editor maintains a recent list to easy access earlier maps

For the last time, it is advised to delete the **open-ig-config.xml** file before running the v0.3 version.

NOTE: the editor requires the [open-ig-upgrade-20100916a.zip](http://open-ig.googlecode.com/files/open-ig-upgrade-20100916a.zip) upgrade pack.

To run the MapEditor, there is no need to download the rest of the game resources, only the latest upgrade pack along with the JAR file is necessary (32MB total).

The associated wiki page will be updated soon.

**September 15. 2010**

The next version of the MapEditor is released: version 0.2. Changes include:

  * Bugfixes in editing behavior
  * Rendering building with status information: powered, damaged, repairing, worker shortage, etc.
  * Ability to save and load the map.
  * Adjust the properties of the buildings: allocated energy & worker amount, upgrade level

Running the new map editor requires the [latest 20100915a upgrade pack](http://open-ig.googlecode.com/files/open-ig-upgrade-20100915a.zip).

NOTE: The game resource file management has become a bit problematic. In the next versions, the program will automatically detect and utilize the resource ZIP files: there will be no need to manually specify them in general. For the current version. It is advised to delete the **open-ig-config.xml** before running the newest version of this map editor.

**September 13. 2010**

The first upgrade pack is now available. It contains fixes for various building images and their night-appearances.

In addition, a pre-alpha version of the MapEditor has been uploaded. You can play around a bit. For the usage, please refer to the [Map Editor](http://code.google.com/p/open-ig/wiki/MapEditor) wiki page.

**September 10. 2010**

I'm working on the new Planet surface renderer and a cool [Map Editor](http://code.google.com/p/open-ig/wiki/MapEditor). It will be available later in this month for download along with the first upgrade pack.

## August, 2010 ##

**August 21. 2010**

I've freed up some time and continue the development of the 0.8 branch. Until the next release, please check out [how to upgrade the game resource files](http://code.google.com/p/open-ig/wiki/GameUpgradeSystem).

## April, 2010 ##

**April 8. 2010**

All resource files should be available now. [Installation](http://code.google.com/p/open-ig/wiki/Install_Instructions_08).

**April 7. 2010**

Unfortunately, I have little time to develop Open-IG at the moment. I plan to upload the new game resource files and publish the available parts of version 0.8. See the [install instructions](http://code.google.com/p/open-ig/wiki/Install_Instructions_08) page for further details.

I'm still uploading the resource files from a slow connection. Please be patient.

## January, 2010 ##

**January 18. 2010**

I've created a small video presenting the recently developed screens: http://www.youtube.com/watch?v=qRSTZnWWGfY

**January 14. 2010**

Just a few [screenshots](http://code.google.com/p/open-ig/wiki/Screenshots) containing the newly developed screens.

**January 6. 2010**

I hope I'll be able to do the development more frequently now. As the game's (now independent) resources are practically available, all I need to do is to rebuild and rewire every screen. Still a tedious task because of those numerous and small pieces...

Until then, you can enjoy the screenshot for the new [Main Menu](http://code.google.com/p/open-ig/wiki/MainMenu).

## November, 2009 ##

**November 20. 2009**

I have composed a small demo from the bar talk game mode. Please see the [wiki page](http://code.google.com/p/open-ig/wiki/BarTalksDemo) for how to use it.

If someone has the original English Imperium Galactica, you could help me with the proper english talk topics from the original.

**November 16. 2009**

I've been able to make some progress with the new resource location and load mechanisms. It will surely take a week until I completely associate each image with a field. If done, it will be much faster to (re-)compose the game screens.

I made the decision to include a new feature: [Building Upgrade Slots](http://code.google.com/p/open-ig/wiki/FeaturePlans#Building_upgrade_slots).

There is still one thing bugging me: how the remaining research time is computed, based on the available research labs? I can't figure it out. For example, a (1,1,0,0,0) research with (0,0,0,0,0) available labs will go up to 60% completeness before stopping. Is somebody willing to try to figure out the algorithm for it? It might need several trial and error runs with the original game.

**November 2. 2009**

I've uploaded a small sample of the new video files, so those who downloaded the new development player won't get bored. When you start the player and the setup screen arrives (or click on file-setup) you need to select the ig-video-sample.zip file then click Save and Run. See [Setup](http://code.google.com/p/open-ig/wiki/Setup) for details.


**November 1. 2009**

There is a slow and hidden progress behind the [scenes](http://code.google.com/p/open-ig/wiki/Screenshots): the rendering of the ship walk, the bar and most of the database screens are ready, but not integrated in any way.

To make thinks work standalone, I have to re-design all existing screens and re-model the backing data structures. There are simply too many pieces to compose and test.

## October, 2009 ##

**October 7. 2009**

Note again, that downloads marked as Deprecated and named with Developer is not meant for general download.

**October 1. 2009**

Note that the new VideoPlayer and video packs are currently for development purposes: this allows us to test the multilingual video playback and the creation of subtitles for the videos.

## September, 2009 ##

**September 27. 2009**

I must admit, we are stepping on an uncertain field with these lots of resource files. On two fronts: uploading 1GB resources into Google Code is rare, and they tend to post fix-things: delete without a single warning. The second thing is ownership. I do it for fun and for free, but there are forces on earth who like to disrupt such kind of deeds.

Please be patient and don't download the game resources yet. Currently, Gergő Harrach and I are working on the subtitles for the videos, and it might happen that some videos and audio files need extra corrections to be made.

**September 26. 2009**

I've completed the packaging of the new video. The new VideoPlayer will be able to play the new (and only the new) video files along with selectable audio and subtitle. The new file distribution requires an [Setup](http://code.google.com/p/open-ig/wiki/Setup) program. Uploading all the audio and video takes some time, and the documentation is also coming for these new windows.

Note for the files: you'll always need the `*`generic`*` named files, as they contain language-independent data: e.g effect sound or audio for non-speaking videos. In practice, this means you don't need to download open-ig-audio-hu.zip and open-ig-images-hu.zip if you want only the English language (~65MB less to download).

**September 19. 2009**

Note for the audio pack: Unfortunately, I put them up too early and without the music files. When version 0.8 arrives, please re-download all of them again, even if their version number won't change. Anyway, the audio format is in my [Audio\_2009\_Format](http://code.google.com/p/open-ig/wiki/Audio_2009_Format) which allows better lossless compression with ZIP than the original WAV.

**September 17. 2009**

The resource extraction goes well, in couple of days I'll finish it and create (english+generic+hungarian) downloadable packages for the images (10+10+10MB), audio (70+10+70 MB) and video (950 MB).

There are two subtasks, which could be performed by anyone willing to help:

  * Figure out the Bar discussion graph: e.g there are 4-6 response videos in the LOCAL directory of the game. One should run the original game and note the dialog options that can be clicked by the user and record which video gets played in response. You can use the  AnimPlay to compare the in-game and file videos.

  * Create subtitles for the videos (once the audio package is ready, it can be done for both english and hungarian). Unfortunately, there is no subtitle support yet, but I guess I can extend the AnimPlay to look for a subtitle file along the animation file and display it - this way you can adjust the subtitle position based on the spoken dialog. Currently at least, you will be able to write down the dialog itself - we will time it later. The subtitle file format should be plain simple:

```
minute:second.millisecond-minute:second.millisecond The spoken text.
minute:second.millisecond-minute:second.millisecond The spoken text 2.
```

```
00:00.000-00:03.500 Meséljen, hogyan zajlik egy ilyen kutatás?
00:04.500-00:06.000 A kolóniákon kiemelt szerepe van az ilyen kutatóközpontoknak.
```

No in-text newlines and the file should contain text in UTF-8.

I also updated the AnimPlay with the latest hungarian audio-delay data. And don't worry about the ending of some videos - I made a small mistake in the auto-fade-out.

**September 3. 2009**

Thank you for the help in the identification. See the results on the <a href='http://code.google.com/p/open-ig/wiki/Credits'>Credits</a> page.
I started to split and transcode the game images - I hope it will clear my resource-whereisit-paralysis.

I will create a package to contain common and language specific images. Expected combined size: ~50MB. Language specific audio: ~100MB per language. Animations: ~750MB total. I need to change the resource loading mechanism and the planned 0.8 version will be completely independent of the original game resources.

If someone is willing to donate his German, French or other language version, I can create a small utility program, which extracts the language specific part from your version (~180MB) and you can submit that instead of the entire 1.2GB game.

## August, 2009 ##

**August 31. 2009**

In Hungarian

**FELHÍVÁS**

Feltöltöttem jó pár videót a <a href='http://www.youtube.com/results?search_query=lgc314&search_type=&aq=f'>Youtube</a>-ra, amelyek az IG eredeti szereplőiből mutat egy-egy videót. Szeretném őket azonosítani, hogy Credits-ben felvehessem a nevüket. Köszönet.

In english: call for identifying the voice actors of the Hungarian game videos.

**August 30. 2009**

I figured out why some videos show anomalous pixels when playing - I misinterpreted the differential coding and palette usage. I'll post the fixed player tomorrow.

I started a [discussion group](http://groups.google.com/group/open-ig-discussion), because the game mechanic, multiplayer and other new features will need tough decisions, and I don't want to cast those decision on my own - we should discuss the options.

**August 28. 2009**

### TESTERS NEEDED! ###

But not for Open-IG at the moment! I'm about to implement the fleet management screen and I need volunteers, who extract the weapon statistics of the original game. The extraction is tedious, because:

  * you need to create a fleet with one type of ship
  * attack somebody
  * on the space war screen, read the firepower value

For larger, mutli-hub spaceships, you need to do it multiple times with different weapon settings. Unfortunately, determining the aliens' ship firepower is much harder, as they are usually in pack and you get only an aggregated firepower value - not a detailed one.

Contact me to organize who tests what ships to avoid unnecessary overlapping.

Thank you.

**August 27. 2009**

I have created a new package with version 0.72 . It contains the movie player fix for missing fade-outs and sound cuts. Plus, it contains an **experimental** feature: music. Of course, if you put any OGG file into the game's `MUSIC` directory, Open-IG is still happy to play it for you - in an uncontrollable, pseudo-random order. I plan to introduce an in-game or out-of-game (e.g plain old Java Swing window) jukebox sooner or later.

**August 25. 2009**

Upgraded the SpidyAniDecoder to insert an end fade-out into those videos, which have longer audio track than the number of frames/fps would indicate - similarly as the original game played it back. Because this 'hack' is in the decoder, all use places should automatically benefit from it: the in-game videos, the AnimPlay (now upgraded to 0.55) playback and conversion.

**August 24. 2009**

I uploaded two in-game videos from the current version: <a href='http://www.youtube.com/watch?v=cpZPm0AZPT8'>Howto build a Hospital Planet to support 500 000+ people</a> and a more <a href='http://www.youtube.com/watch?v=nSJ3vZCLfE4'>simplistic feature demo</a>. I advise to watch it in HD and on full screen.

**August 21. 2009**

I put the four wife videos on youtube: <a href='http://www.youtube.com/watch?v=CxofBDueRWk'>1</a>, <a href='http://www.youtube.com/watch?v=kbm1-bq4eRg'>2</a>, <a href='http://www.youtube.com/watch?v=_9oHxEJsbkE'>3</a>, <a href='http://www.youtube.com/watch?v=7kRQde8hufs'>4</a>. Unfortunately, youtube has decreased the video quality - but the hungarian sound acting is what matters!

I also updated the AnimPlay to have a playlist automatically filled in from the ANI files from a given directory, plus added a batch conversion to PNGs & WAV files. Just select the files you want to convert and hit the button. If you have multiple CPUs, the conversion will be even faster!

## July, 2009 ##

**July 9. 2009**

Unfortunately, I have to do my regular job and don't have time for Open-IG at the moment. It could take 2-3 months to develop it further. Good news is that I have now a pretty solid view about how I will implement the simulation part. Until then, the biggest unfinished properties are:

  * Equipment management screen
  * Space combat - ships, rotation, projectiles, etc.
  * Surface combat - tanks, radar, damage system
  * Diplomacy
  * Bridge + wandering around in the ship
  * Technology dependant rules - radar level implied detail display, fleet visibility, planet discovery
  * AI
  * Achievement system and display
  * Multiplayer

## June, 2009 ##

**June 19. 2009**

  * Added the ability to place/remove items into the production lines and adjust the production properties.
  * CTRL+A will increase the inventory count of the currently selected technology.
  * Some management related information display. See features.
  * For my testing purposes, the game jumps right into the starmap after startup. If you want to watch the intro or title animation, hit ESC then click Quit/Exit and you get back t the main menu.
  * The game now complains if you try run it with less than 160MB JVM memory instead of just crashing silently.

**June 11. 2009**
  * Added the [new features](http://code.google.com/p/open-ig/wiki/NewFeatures) wiki page which describes the enhancements in Open-IG
  * Added information about the technical details behind the [planetary surface maps](http://code.google.com/p/open-ig/wiki/Surface_Maps)

**June 1. 2009**
  * Added production (F5) and research (F6) screens with animation
  * Unresearched buildings do not show up on the buildings listing
  * User CTRL+R to completely research the currently selected technology
  * You might need to run the game with at least 160MB JVM memory: -Xmx160M

## May, 2009 ##

**May 29. 2009**
  * Some navigation and UI behavior fixes
  * Buildings now consume energy and workers, can be deactivated.
  * Added inventions information screen
  * CTRL+SHIFT+O own every planet on the starmap, CTRL+I own enemy occupied planets, CTRL+M takeover the current planet, CTRL+B toggle build phases, CTRL+D toggle building damaged status


**May 23. 2009**
  * Added ability to place buildings onto planets
  * Added building listing to the information screen
  * CTRL+C clears all buildings from the current planet, DEL deletes the selected building

**May 20. 2009**
  * Updated the AnimPlay application. Under the hood, now it uses the same method for rendering images and audio. Better yet, I've added 3 options to
    * save the animation as an animated GIF file,
    * save a series of PNG files containing every frame
    * save the animation sound as a WAV file

**May 18. 2009**
  * Updated the Wiki pages.
  * Released version 0.67. Major enhancements:
    * Display planets, colony info, fleets on the information panel
    * Planet/Colony button displays the appropriate surface type and variant of the selected planet
    * CTRL+. to toggle between default/neighbor/bilinear/bicubic interpolation of the starmap background
  * Added screenshots of the new screens

**May 16. 2009**
  * Updated the Wiki pages.
  * Released version 0.65. Major enhancements:
    * Game model for world, planet, fleet
    * Render fleets
    * Render minimap
    * Navigate using minimap
    * List player's planets
    * List player's fleets, cycle through the fleets
    * Display planet/fleet info
  * Check out the new CTRL+K, CTRL+N and CTRL+F 'cheats'

**May 4. 2009**
  * Updated the Wiki pages.
  * Progress is slower than I expected. Creating a good and solid data model supporting skirmish, campaign and multiplayer is hard. But I uploaded the 0.6 version, which includes some style and bug fixes.
  * Planet surface rendering is now correctly displaying multi-tile elements. Added the original campaign's ~100 planets with radar enabled. I also added the open source [JOrbis](http://www.jcraft.com/jorbis/) OGG player library. I had to slightly modify its source code to adhere the checkstyle rules used in the project and removed lots of unused codes from it. When the application will support custom campaigns, it will allow custom music/sound playback from .ogg or .wav files.

## March, 2009 ##

**March 7. 2009**
  * Created a package for the newest version of the program. Now, along with the JAR file, I've put the source code into a ZIP file. As the program evolves, unfortunately, the default 64 MB memory for double-clicked JAR programs becomes too sort. There are several options:
    * Create/use a command line script with -Xmx128M parameter
    * Change the JAR file extension assignment and include the -Xmx128M parameter

## February, 2009 ##

**February 19. 2009**
  * Thanks to Open-IG and a new wide screen monitor, I was able to find two bugs in the latest JDK regarding the new D3D rendering. See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6801614 and http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6804860 for further details as soon as they are available.

**February 3. 2009**

  * Due some strange rendering behavior in 6u11 I strongly advise to use 6u7 on Windows for running Open-IG. The strange rendering occurs when the game windows are resized to a very large size (>1500 pixels). On these resolutions, the graphics gets misplaced, the dotted lines get garbled. Possible reason might be in the new D3D rendering pipeline (or just my videocard driver is too old).

## January, 2009 ##

**January 28. 2009**

  * Made some enhancements in audio, screen switching and on information screen.

**January 19. 2009**

  * Updated the AnimPlay utility. Now it allows to stop the playback, and resize the playback window.

**January 18. 2009**

  * Fixed some minor issues in the AnimPlay utility.
  * Created a demonstration application to show the current state of the game.