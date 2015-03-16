

# Version 0.94.425 - Fixes #
  * Released: 2012-05-29 14:10 CET
  * Added sort indicator to Info/Planets/Details view .([Issue #138](http://code.google.com/p/open-ig/issues/detail?id=138))
  * Added buttons to change the auto-build settings (+/-), added "All" button to set the same settings on all your planets.([Issue #142](http://code.google.com/p/open-ig/issues/detail?id=142))
  * Added "All" button to set the same taxation level on all your planets.
  * Achievements viewable from main menu, temporarily through an ugly button. ([Issue #221](http://code.google.com/p/open-ig/issues/detail?id=221))
  * Slowed down fleets by 33% (Issues [#258](http://code.google.com/p/open-ig/issues/detail?id=258), [#436](http://code.google.com/p/open-ig/issues/detail?id=436))
  * Infected planets now suffer -5 morale penalty. ([Issue #326](http://code.google.com/p/open-ig/issues/detail?id=326))
  * In shipwalk, bridge, bar, cabin and diplomacy screens you can right click on a destination to go there instantly without the animation. ([Issue #335](http://code.google.com/p/open-ig/issues/detail?id=335))
  * Error message and sound is played when the player has zero capacity and adds a production order (the order placement always succeeds). ([Issue #386](http://code.google.com/p/open-ig/issues/detail?id=386))
  * Traders now flee when they get to 75% health. Turning them back now skips the battle statistics screen ([Issue #392](http://code.google.com/p/open-ig/issues/detail?id=392))
  * The spacewar ship listing now contains the HP + SP = Defense values of the ships ([Issue #408](http://code.google.com/p/open-ig/issues/detail?id=408))
  * Building names and statuses can be hidded via the options screen. ([Issue #410](http://code.google.com/p/open-ig/issues/detail?id=410))
  * Changed rocket and ECM behavior to give higher chance of hitting the target ([Issue #430](http://code.google.com/p/open-ig/issues/detail?id=430))
  * Most non-full screens can now be scaled to fit the window independently of the global scaling, enabled via an options setting ([Issue #442](http://code.google.com/p/open-ig/issues/detail?id=442))
  * Display of current and delta relation during diplomatic conversation with the aliens ([Issue #455](http://code.google.com/p/open-ig/issues/detail?id=455))
  * Reaching an attack target in space now starts the battle much sooner ([Issue #462](http://code.google.com/p/open-ig/issues/detail?id=462))
  * Trader fleet behavior changes and fixes ([Issue #473](http://code.google.com/p/open-ig/issues/detail?id=473))
  * The game now creates a named save on each level start ([Issue #474](http://code.google.com/p/open-ig/issues/detail?id=474))
  * Fixed some space-chat related problems ([Issue #476](http://code.google.com/p/open-ig/issues/detail?id=476))
  * Level 2 now allows calling the Colonel for reinforcements anytime (but might not grant them) ([Issue #477](http://code.google.com/p/open-ig/issues/detail?id=477))
  * Fixed video messages  in case the virus missions fail ([Issue #478](http://code.google.com/p/open-ig/issues/detail?id=478))
  * One of the traders can now instantly heal any infected colonies ([Issue #479](http://code.google.com/p/open-ig/issues/detail?id=479))
  * Fixed missing red blinking boxes around certain fleets ([Issue #481](http://code.google.com/p/open-ig/issues/detail?id=481))
  * Fixed some scripting issues ([Issue #482](http://code.google.com/p/open-ig/issues/detail?id=482))
  * Fixed virus mission finish not detected when multiple planets were infected ([Issue #483](http://code.google.com/p/open-ig/issues/detail?id=483))
  * Fixed prototype fleet not appearing in level 2 ([Issue #484](http://code.google.com/p/open-ig/issues/detail?id=484))
  * Traders should now properly remember where they came from (but clean new game might be required) ([Issue #485](http://code.google.com/p/open-ig/issues/detail?id=485))
  * Fixed many diplomatic option display and selection issues ([Issue #486](http://code.google.com/p/open-ig/issues/detail?id=486))
  * Level 2 now lists an unresearched Cruiser 1 and Starbase 1 is not buildable any more (new game required) ([Issue #490](http://code.google.com/p/open-ig/issues/detail?id=490))
  * Record message on the database screen now features a red blinking circle indicating the ongoing record ([Issue #491](http://code.google.com/p/open-ig/issues/detail?id=491))
  * Fixed termination issues with the smuggler mission ([Issue #492](http://code.google.com/p/open-ig/issues/detail?id=492))


# Version 0.94.410 - More bugfixes #
  * Released: 2012-05-22 16:30 CET
  * Welcome **Sigee15** in the project as committer.
  * Diplomacy screen is now fully functional ([Issue #210](http://code.google.com/p/open-ig/issues/detail?id=210))
  * AI players now nag you with diplomatic messages ([Issue #211](http://code.google.com/p/open-ig/issues/detail?id=211))
  * Added generic objectives for level 2 to set the main course ([Issue #325](http://code.google.com/p/open-ig/issues/detail?id=325))
  * Financial information sums added ([Issue #407](http://code.google.com/p/open-ig/issues/detail?id=407))
  * Fixed scrolling of messages in the bridge message panel ([Issue #437](http://code.google.com/p/open-ig/issues/detail?id=437))
  * Fixed notification about research being stopped due lack of something ([Issue #438](http://code.google.com/p/open-ig/issues/detail?id=438))
  * Fixed AI managed player planets not replacing and slowly adding buildings ([Issue #439](http://code.google.com/p/open-ig/issues/detail?id=439))
  * Fixed missing traders in protect mission 2 ([Issue #440](http://code.google.com/p/open-ig/issues/detail?id=440))
  * Fixed crash in smuggler mission 9 ([Issue #441](http://code.google.com/p/open-ig/issues/detail?id=441))
  * Fixed cases where the Thorin mission 5 would leave the controllable area before the garthogs catch up, leaving the player unable to attack ([Issue #443](http://code.google.com/p/open-ig/issues/detail?id=443))
  * Fixed issue where trader fleets forgot where they came from ([Issue #444](http://code.google.com/p/open-ig/issues/detail?id=444))
  * Fixed bridge animation not properly showing the message panel after closing it ([Issue #450](http://code.google.com/p/open-ig/issues/detail?id=450))
  * Clicking on record message during mission 15 (spy) now instantly completes the mission instead of the need to resume the game ([Issue #451](http://code.google.com/p/open-ig/issues/detail?id=451))
  * Fixed the Launcher reporting file access errors in certain cases ([Issue #457/1](http://code.google.com/p/open-ig/issues/detail?id=457))
  * Fixed cases where planet progress simulation crashed the game ([Issue #457/2](http://code.google.com/p/open-ig/issues/detail?id=457))
  * Fixed the crash due invalid chat references during virus mission 12 ([Issue #458](http://code.google.com/p/open-ig/issues/detail?id=458))
  * Trader #4 now cures an infected planet when allowed to land ([Issue #460](http://code.google.com/p/open-ig/issues/detail?id=460)). Use the chat to figure out which one is it :)
  * Fixed missing or wrong chat labels (Issues [#461](http://code.google.com/p/open-ig/issues/detail?id=461), [#469](http://code.google.com/p/open-ig/issues/detail?id=469))
  * Added error notification for the case when the build limit is reached ([Issue #464](http://code.google.com/p/open-ig/issues/detail?id=464))
  * Additional planet infections in mission 12 now send a message as well ([Issue #467](http://code.google.com/p/open-ig/issues/detail?id=467))
  * Fixed carrier mission 3 not working properly ([Issue #471](http://code.google.com/p/open-ig/issues/detail?id=471))
  * Fixed issue when auto-equipping planets and the space stations were not properly deduced from inventory ([Issue #472](http://code.google.com/p/open-ig/issues/detail?id=472))


# Version 0.94.400 - Bug fixes #
  * Released: 2012-05-13 18:00 CET
  * This is **NOT** the Beta yet.
  * Higher quality music by Norbert "Silver83" Pellny ([Issue #428](http://code.google.com/p/open-ig/issues/detail?id=428))
  * Notification if a research stops due lack of money or lab ([Issue #295](http://code.google.com/p/open-ig/issues/detail?id=295))
  * AI now sends diplomatic messages ([Issue #321](http://code.google.com/p/open-ig/issues/detail?id=321))
  * Spacewar Grid rendering ([Issue #323](http://code.google.com/p/open-ig/issues/detail?id=323))
  * Mission 7 related crashes fixed ([Issue #340](http://code.google.com/p/open-ig/issues/detail?id=340))
  * Added missing cutscene for Mission 2 ([Issue #343](http://code.google.com/p/open-ig/issues/detail?id=343))
  * Potential interesting fleets now receive red blinking box in missions ([Issue #347](http://code.google.com/p/open-ig/issues/detail?id=347))
  * Bridge on Level 2 now has equipment and information areas ([Issue #352](http://code.google.com/p/open-ig/issues/detail?id=352))
  * Mission 9 (smuggler) adjusted ([Issue #362](http://code.google.com/p/open-ig/issues/detail?id=362))
  * Pending battles are now saved and loaded (e.g., AI vs. AI or multiple attacks happening at the same time) ([Issue #372](http://code.google.com/p/open-ig/issues/detail?id=372))
  * Trader ship names now change with the game language settings ([Issue #377](http://code.google.com/p/open-ig/issues/detail?id=377))
  * Fixed bad original audio for the conversation with Kelly ([Issue #378](http://code.google.com/p/open-ig/issues/detail?id=378))
  * Mission 11 attack incoming message now happens earlier ([Issue #382](http://code.google.com/p/open-ig/issues/detail?id=382))
  * Garthog fleet now flies away in Mission 11 instead of just disappearing. ([Issue #385](http://code.google.com/p/open-ig/issues/detail?id=385))
  * Fixed cases when a game save may be corrupted and cause error reports ([Issue #388](http://code.google.com/p/open-ig/issues/detail?id=388))
  * Mission 11 now allows two planets to be infected before you get fired ([Issue #398](http://code.google.com/p/open-ig/issues/detail?id=398))
  * Added more space chat cases for traders leaving and arriving at an infected planet ([Issue #399](http://code.google.com/p/open-ig/issues/detail?id=399))
  * Launcher is now able to start the game without Internet connection ([Issue #400](http://code.google.com/p/open-ig/issues/detail?id=400))
  * Bar link back to the corridor is not anymore overlapping with the conversation link ([Issue #401](http://code.google.com/p/open-ig/issues/detail?id=401))
  * Subsequent statusbar messages now show up without interruption ([Issue #402](http://code.google.com/p/open-ig/issues/detail?id=402))
  * Building fractional production display corrected ([Issue #406](http://code.google.com/p/open-ig/issues/detail?id=406))
  * Label typo fixes ([Issue #412](http://code.google.com/p/open-ig/issues/detail?id=412))
  * Mission 2 pirate attacks now timeout properly ([Issue #416](http://code.google.com/p/open-ig/issues/detail?id=416))
  * The X key resets the UI scaling only when the visual page is shown on the options screen ([Issue #418](http://code.google.com/p/open-ig/issues/detail?id=418))
  * Mission 4 and Mission 19 chat now affects the attackers by switching targets ([Issue #419](http://code.google.com/p/open-ig/issues/detail?id=419))
  * Audio warning added to Mission 3 ([Issue #421](http://code.google.com/p/open-ig/issues/detail?id=421))
  * Crash when joining fleets ([Issue #422](http://code.google.com/p/open-ig/issues/detail?id=422))
  * Toggle fullscreen mode checkbox added to options screen ([Issue #424](http://code.google.com/p/open-ig/issues/detail?id=424))
  * Fixed AI attacking a demolished building indefinitely ([Issue #425](http://code.google.com/p/open-ig/issues/detail?id=425))
  * Fixed crash in battle statistics after ground battle ([Issue #429](http://code.google.com/p/open-ig/issues/detail?id=429))
  * Traders now have more speed, fleets without hyperdrive travel now faster ([Issue #432](http://code.google.com/p/open-ig/issues/detail?id=432))
  * Added option to allow skipping cutscenes with the mouse click (default off) ([Issue #434](http://code.google.com/p/open-ig/issues/detail?id=434))
  * Toggle fullscreen from options caused menu and control problems ([Issue #435](http://code.google.com/p/open-ig/issues/detail?id=435))

# Version 0.94.370 - Space chat #
  * Released: 2012-04-30 20:30 CET
  * Menu background screens cleared by **Norbert "Silver83" Pellny** ([Issue #367](http://code.google.com/p/open-ig/issues/detail?id=367))
  * Space chat implemented (Issues [#215](http://code.google.com/p/open-ig/issues/detail?id=215), [#396](http://code.google.com/p/open-ig/issues/detail?id=396), [#322](http://code.google.com/p/open-ig/issues/detail?id=322))
  * Diplomacy language specific fixes ([Issue #135](http://code.google.com/p/open-ig/issues/detail?id=135))
  * Mission 5 internal cleanup ([Issue #327](http://code.google.com/p/open-ig/issues/detail?id=327))
  * Added user interface scaling and full screen cutscene options ([Issue #329](http://code.google.com/p/open-ig/issues/detail?id=329))
  * Launcher's german language integration was incomplete ([Issue #357](http://code.google.com/p/open-ig/issues/detail?id=357))
  * Missing animations fixed (Issues [#358](http://code.google.com/p/open-ig/issues/detail?id=358), [#360](http://code.google.com/p/open-ig/issues/detail?id=360))
  * Level 2 shipwalk animation fixed ([Issue #363](http://code.google.com/p/open-ig/issues/detail?id=363))
  * Mission 8 timeout management fixed ([Issue #364](http://code.google.com/p/open-ig/issues/detail?id=364))
  * Double clicking on fleets opens wrong screen ([Issue #365](http://code.google.com/p/open-ig/issues/detail?id=365))
  * Production screen had missing labels ([Issue #366](http://code.google.com/p/open-ig/issues/detail?id=366))
  * Mission 5 timeout management fixed ([Issue #369](http://code.google.com/p/open-ig/issues/detail?id=369))
  * Battle finish statistics assigns wrong counts to the player and enemy (Issues [#370](http://code.google.com/p/open-ig/issues/detail?id=370), [#374](http://code.google.com/p/open-ig/issues/detail?id=374))
  * Loading a save sometimes failed ([Issue #371](http://code.google.com/p/open-ig/issues/detail?id=371))
  * Fixed case where loading a save in game would clear attack orders.
  * Missing Achilles video ([Issue #373](http://code.google.com/p/open-ig/issues/detail?id=373))
  * Highlighting of traders in level 2 is working now ([Issue #375](http://code.google.com/p/open-ig/issues/detail?id=375)).
  * AI sometimes could not fire its rockets ([Issue #376](http://code.google.com/p/open-ig/issues/detail?id=376))
  * AI fighters, when low on health, will kamikaze into its target ([Issue #376](http://code.google.com/p/open-ig/issues/detail?id=376))
  * Mission 11 crash fixed ([Issue #380](http://code.google.com/p/open-ig/issues/detail?id=380))
  * Enemy fleets shouldn't be moveable via keyboard commands ([Issue #381](http://code.google.com/p/open-ig/issues/detail?id=381))
  * Fixed missions where enemies had to catch up might not progress as intended ([Issue #383](http://code.google.com/p/open-ig/issues/detail?id=383))
  * Traders are now limited to the player's empire and travel to visible planets when infected ([Issue #385](http://code.google.com/p/open-ig/issues/detail?id=384))
  * Battleship 2 did not support hyperdrives v1-v4 ([Issue #397](http://code.google.com/p/open-ig/issues/detail?id=397))


# Version 0.94.360 - German language support #
  * Released: 2012-04-21 11:00 CET
  * German language support
  * Promotion to Commander message and videos now always play ([Issue #331](http://code.google.com/p/open-ig/issues/detail?id=331))
  * Phsychologist name fix ([Issue #313](http://code.google.com/p/open-ig/issues/detail?id=313))
  * Options screen is restructured ([Issue #303](http://code.google.com/p/open-ig/issues/detail?id=303))
  * Crash when switching to fullscreen or back ([Issue #330](http://code.google.com/p/open-ig/issues/detail?id=330))
  * Missing Achilles video added ([#328](http://code.google.com/p/open-ig/issues/detail?id=328))
  * Fleet control issues, **Note: Level 1-3 players should consider starting a completely new game.** ([Issue #336](http://code.google.com/p/open-ig/issues/detail?id=336))
  * Tax calculation fixed ([Issue #320](http://code.google.com/p/open-ig/issues/detail?id=320))
  * Load game problem fixed ([Issue #338](http://code.google.com/p/open-ig/issues/detail?id=338))
  * Colony fire animation FPS increased ([Issue #346](http://code.google.com/p/open-ig/issues/detail?id=346))
  * The production screen was incorrectly accessible from Level 1 ([Issue #349](http://code.google.com/p/open-ig/issues/detail?id=349))
  * Garthog pirates sometimes fly away ([Issue #351](http://code.google.com/p/open-ig/issues/detail?id=351))
  * Added screen-regions to the Level 1 bridge ([Issue #350](http://code.google.com/p/open-ig/issues/detail?id=350))
  * Music stop under dream cutscene ([Issue #354](http://code.google.com/p/open-ig/issues/detail?id=354))
  * Tile caching caused uneven lighting for certain objects ([Issue #353](http://code.google.com/p/open-ig/issues/detail?id=353))

# Version 0.94.350 - Basic diplomacy #
  * Released: 2012-04-09 10:30 CET
  * **I recommend starting a new game as some gameplay mechanic changes and fixes might not take effect under existing saves**.
  * Added smoke and fire to buildings ([Issue #73](http://code.google.com/p/open-ig/issues/detail?id=73))
  * Added ability to set the game base speed ([Issue #153](http://code.google.com/p/open-ig/issues/detail?id=153))
  * Few settings has been moved from Gameplay to Sound page in Options.
  * Multi headed rocket have an area effect ([Issue #180](http://code.google.com/p/open-ig/issues/detail?id=180))
  * Rockets and bombs have cooldown now as well ([Issue #192](http://code.google.com/p/open-ig/issues/detail?id=192))
  * Groundwar damaged units now emit smoke and fire ([Issue #274](http://code.google.com/p/open-ig/issues/detail?id=274))
  * AI now uses its rockets, but won't target campaign related ships ([Issue #300](http://code.google.com/p/open-ig/issues/detail?id=300))
  * Space stations have now specific equipment slot for rockets, which must be manually refilled or let the auto-refill take care of it.
  * Information screen tabs can be accessed via QWERASDF keys ([Issue #309](http://code.google.com/p/open-ig/issues/detail?id=309))
  * Debug functionality CTRL+M has been removed ([Issue #312](http://code.google.com/p/open-ig/issues/detail?id=312))
  * Skirmish games may now be resumed after the win condition ([Issue #305](http://code.google.com/p/open-ig/issues/detail?id=305))
  * Added extra video message to Mission 21 ([Issue #306](http://code.google.com/p/open-ig/issues/detail?id=306))
  * Changed "AI managed" auto-build settings to consider all such planets instead of individually manage them (similarly to regular AI players) ([Issue #316](http://code.google.com/p/open-ig/issues/detail?id=316))
  * Ability auto-upgrade planet defenses, similar to fleets ([Issue #318](http://code.google.com/p/open-ig/issues/detail?id=318))
  * The player's main fleet is now named according to the current language when a new game is started.
  * Fixed production screen showing wrong capacity in case no orbital factory was deployed yet.
  * Changed tax mechanics to boost taxes when the player has few planets and reduce taxes when the player has 10+ planets.


# Version 0.94.320 - Campaign finished #
  * Released: 2012-03-06 15:30 CET
  * Added planet dependant population growth and display of the rate in the information panels ([Issue #69](http://code.google.com/p/open-ig/issues/detail?id=69))
  * Added demand for fire brigade, look for the small fire icons ([Issue #71](http://code.google.com/p/open-ig/issues/detail?id=71))
  * Missions of level 4 and 5 implemented ([Issue #216](http://code.google.com/p/open-ig/issues/detail?id=216))
  * Reduced the effects of battle on the planet population ([Issue #186](http://code.google.com/p/open-ig/issues/detail?id=186))
  * All game videos have been accounted for (Issues [#194](http://code.google.com/p/open-ig/issues/detail?id=194), [#193](http://code.google.com/p/open-ig/issues/detail?id=193))
  * Added ability to deploy ground units continuously by holding the left mouse button ([Issue 261](http://code.google.com/p/open-ig/issues/detail?id=261))
  * Idle ships will fire at rockets and bombs ([Issue #298](http://code.google.com/p/open-ig/issues/detail?id=298))
  * Player's stations and guns now fire automatically on closest enemy target ([Issue 299](http://code.google.com/p/open-ig/issues/detail?id=299))
  * Building under construction now uses mixed scaffolding ([Issue #272](http://code.google.com/p/open-ig/issues/detail?id=272))
  * The main campaign and single player campaign features the Game over screen with option to resume the current gameplay ([Issue #301](http://code.google.com/p/open-ig/issues/detail?id=301)).
  * Disable subtitles in options ([Issue #302](http://code.google.com/p/open-ig/issues/detail?id=302))
  * Settings to slow down the game in case of an incoming attack ([Issue 282](http://code.google.com/p/open-ig/issues/detail?id=282))

# Version 0.94.300 - More missions #
  * Released: 2012-02-23 16:00 CET
  * Missions 18, 19, 20 and 21 added ([Issue #214](http://code.google.com/p/open-ig/issues/detail?id=214))
  * Fixed Mission 16 crash ([Issue 292](http://code.google.com/p/open-ig/issues/detail?id=292))
  * Fixed issues due non-unique fleet ids ([Issue #293](http://code.google.com/p/open-ig/issues/detail?id=293))
  * When playing as non-human, alien special units are displayed in the equipment screen ([Issue #293](http://code.google.com/p/open-ig/issues/detail?id=293))
  * Fixed mission overlappings ([Issue #293](http://code.google.com/p/open-ig/issues/detail?id=293))
  * Mission 8 talk to the phsychologist now happens after two visions as intended.
  * Fixed Mission 5 mentioning Admiral Benson instead of Admiral Tullen ([Issue #294](http://code.google.com/p/open-ig/issues/detail?id=294))
  * Fixed issues with newly deployed Destroyer 1s not properly equipped, however, you need to start a new game. ([Issue #294](http://code.google.com/p/open-ig/issues/detail?id=294))
  * Fixed infinite money glitch due selling equipment ([Issue #294](http://code.google.com/p/open-ig/issues/detail?id=294)).
  * Fixed Mission 16 reinforcements not arriving ([Issue #294](http://code.google.com/p/open-ig/issues/detail?id=294)).


# Version 0.94.200 - More missions #
  * Released: 2012-02-13 21:10 CET
  * Mission 17 added, level 2 complete ([Issue #213](http://code.google.com/p/open-ig/issues/detail?id=213))
  * Mission 18 added
  * Achievement sound ([Issue #238](http://code.google.com/p/open-ig/issues/detail?id=238))
  * Mission 2 reward text fixes ([Issue #256](http://code.google.com/p/open-ig/issues/detail?id=256))
  * Added mission 13 reward ([Issue #289](http://code.google.com/p/open-ig/issues/detail?id=289))
  * Fixed mission 5 unreachable garthog fleet ([#291](http://code.google.com/p/open-ig/issues/detail?id=291))

# Version 0.94.190 - More missions #
  * Released: 2012-02-10 14:00 CET
  * Added missions 13 and 16
  * Reduced population loss due space and ground battles (Issues [#254](http://code.google.com/p/open-ig/issues/detail?id=254), [#279](http://code.google.com/p/open-ig/issues/detail?id=279))
  * Added subtitles to Achilles-Check message ([Issue #276](http://code.google.com/p/open-ig/issues/detail?id=276))
  * Fixed label for police station unit of measure ([Issue #280](http://code.google.com/p/open-ig/issues/detail?id=280))
  * Fleets on the starmap now move smoother ([Issue #273](http://code.google.com/p/open-ig/issues/detail?id=273))
  * Attack notification improvements ([Issue #281](http://code.google.com/p/open-ig/issues/detail?id=281))
  * Barracks had incorrect space health ([Issue #283](http://code.google.com/p/open-ig/issues/detail?id=283))
  * Fixed achievement "Defense contract" ([Issue #285](http://code.google.com/p/open-ig/issues/detail?id=285))
  * Fixed Map Editor initialization crash ([Issue #286](http://code.google.com/p/open-ig/issues/detail?id=286))
  * Fixed mission 12 stages and completion not triggering if the user didn't watch the briefing/debriefing messages. ([Issue #287](http://code.google.com/p/open-ig/issues/detail?id=287))
  * Mission 6 starts immediately after promotion and your fleet is moved far away, just like in the original.

# Version 0.94.180 - Few enhancements #
  * Released: 2012-02-04 20:00 CET
  * Added Missions 12 and 14
  * Added triggers for Wife videos
  * Groundwar AI units now attack ([Issue #277](http://code.google.com/p/open-ig/issues/detail?id=277))
  * Corrected missing Achilles-Check video and sound ([Issue #275](http://code.google.com/p/open-ig/issues/detail?id=275))

# Version 0.94.160 - Fixes #
  * Released: 2012-01-29 17:00 CET
  * Fixed ([Issue #190](http://code.google.com/p/open-ig/issues/detail?id=190), [Issue #267](http://code.google.com/p/open-ig/issues/detail?id=267)), auto-refill of bombs and rockets settings did not work correctly.
  * Implemented virus infection and propagation ([Issue #125](http://code.google.com/p/open-ig/issues/detail?id=125))
  * Fixed [Issue #191](http://code.google.com/p/open-ig/issues/detail?id=191): audiovisual feedback if a planet can't be attacked
  * Fixed Mission 1 and consequences: Mission 5 will start only if you completed Mission 1. ([Issue #197](http://code.google.com/p/open-ig/issues/detail?id=197))
  * Fixed [Issue #109](http://code.google.com/p/open-ig/issues/detail?id=109), ships without firepower won't respond to attack command (own and AI side inclusive)
  * Follow fleet working again and "attacks the attacker". ([Issue 265](http://code.google.com/p/open-ig/issues/detail?id=265))
  * Fixed Mission 3 ([Issue #209](http://code.google.com/p/open-ig/issues/detail?id=209)), if you follow the fleet with one of your own and the attack happens, your fleet will automatically attack the pirates. If you managed to do a close escort, this will surely result in a space battle within a few seconds.
  * Fixed issues related to Mission 8. ([Issue #242](http://code.google.com/p/open-ig/issues/detail?id=242), [Issue #225](http://code.google.com/p/open-ig/issues/detail?id=225))
  * Fixed issues with Mission 9. ([Issue #244](http://code.google.com/p/open-ig/issues/detail?id=244))
  * Fixed issues with some missions restarting once completed ([#247](http://code.google.com/p/open-ig/issues/detail?id=247))
  * Fixed [Issue #246](http://code.google.com/p/open-ig/issues/detail?id=246), unable to create and equip new fleets.
  * Fixed ([Issue #246](http://code.google.com/p/open-ig/issues/detail?id=248), [Issue #255](http://code.google.com/p/open-ig/issues/detail?id=255)), Mission 7 trader protection problems
  * Fixed [Issue #260](http://code.google.com/p/open-ig/issues/detail?id=260), fire rockets from starbases, auto-refill of starbases too
  * Fixed [Issue #250](http://code.google.com/p/open-ig/issues/detail?id=250), battle finish screen ally losses.
  * Fixed [Issue #251](http://code.google.com/p/open-ig/issues/detail?id=251), AI deployed starbases had no weapons.
  * Fixed [Issue #252](http://code.google.com/p/open-ig/issues/detail?id=252), kamikaze management and crash
  * Fixed [Issue #264](http://code.google.com/p/open-ig/issues/detail?id=264), rockets were selectable and stoppable in spacewar.
  * Fixed [Issue #257](http://code.google.com/p/open-ig/issues/detail?id=257), AI managed planets should not produce stations and vehicles in Level 1.
  * Fixed [Issue #268](http://code.google.com/p/open-ig/issues/detail?id=268), error messages on the statusbar stay longer visible.
  * Added the game option to switch the mouse left-right button interpretation
  * Fixed [Issue #270](http://code.google.com/p/open-ig/issues/detail?id=270), attack command in spacewar affected all ships, not just the selected ones.


# Version 0.94.100 - More missions #
  * Released: 2012-01-21 22:00 CET
  * **You may need to start a new game!**. The remaining two missions from level 1 should start correctly, but if you ran ahead, you might get both of them at once.
  * Level 1 missions implemented ([Issue #212](http://code.google.com/p/open-ig/issues/detail?id=212))
  * Mission 1, 2 and 3 corrections and missing videos ([Issue #209](http://code.google.com/p/open-ig/issues/detail?id=209))
  * Missing "message from X" on the battle finish screen ([Issue #217](http://code.google.com/p/open-ig/issues/detail?id=217))
  * Mission 4 implemented ([#Issue #223](http://code.google.com/p/open-ig/issues/detail?id=223))
  * Fixed [Issue #229](http://code.google.com/p/open-ig/issues/detail?id=229): ground units did not move correctly
  * Fixed [Issue #230](http://code.google.com/p/open-ig/issues/detail?id=230): playing messages and quitting from bridge did not stop the message correctly
  * Fixed issues with full screen videos not stopping music. ([Issue #232](http://code.google.com/p/open-ig/issues/detail?id=232))
  * Mission 5 ([Issue #224](http://code.google.com/p/open-ig/issues/detail?id=224))
  * Mission 6 ([Issue #227](http://code.google.com/p/open-ig/issues/detail?id=227))
  * Mission 7 ([Issue #233](http://code.google.com/p/open-ig/issues/detail?id=233), [Issue #235](http://code.google.com/p/open-ig/issues/detail?id=235))
  * Mission 8 ([Issue #225](http://code.google.com/p/open-ig/issues/detail?id=225))
  * Mission 15 ([Issue #226](http://code.google.com/p/open-ig/issues/detail?id=226))
  * Fixed sound playback inconsistencies ([Issue #218](http://code.google.com/p/open-ig/issues/detail?id=218))

# Version 0.94 - Missions #
  * Released: 2012-01-15 13:00 CET
  * **Acknowledgement: Joe did a great job for this release!**
  * **You may need to start a new game!**. If you want to carry on with your previous Main Campaign progress, you can edit your save XML and change the `<world game='campaign/main'` to `<world game='campaign/main2'` in the root node manually.
  * Improvements to the AI ([Issue #118](http://code.google.com/p/open-ig/issues/detail?id=118))
  * Bug fixes (Issues [#185](http://code.google.com/p/open-ig/issues/detail?id=185), [#188](http://code.google.com/p/open-ig/issues/detail?id=188), [#189](http://code.google.com/p/open-ig/issues/detail?id=189), [#199](http://code.google.com/p/open-ig/issues/detail?id=199), [#200](http://code.google.com/p/open-ig/issues/detail?id=200), [#201](http://code.google.com/p/open-ig/issues/detail?id=201), [#202](http://code.google.com/p/open-ig/issues/detail?id=202), [#203](http://code.google.com/p/open-ig/issues/detail?id=203), [#208](http://code.google.com/p/open-ig/issues/detail?id=208))
  * Added: mission scripting framework
  * Created missions 1, 2 and 3, see the Missions page. (Issues [#197](http://code.google.com/p/open-ig/issues/detail?id=197), [#207](http://code.google.com/p/open-ig/issues/detail?id=207), [#209](http://code.google.com/p/open-ig/issues/detail?id=209))
  * Added: missing lightmaps ([Issue #62](http://code.google.com/p/open-ig/issues/detail?id=62))
  * Added: Use TAB key to display objectives (Issues [#110](http://code.google.com/p/open-ig/issues/detail?id=110), [#198](http://code.google.com/p/open-ig/issues/detail?id=198), [#206](http://code.google.com/p/open-ig/issues/detail?id=206))
  * Balance updates (Issue [#204](http://code.google.com/p/open-ig/issues/detail?id=204))

# Version 0.93.540 - Improvements #
  * Released: 2012-01-11 21:10 CET
  * Several AI improvements and fixes
  * Ability to switch to right-click-action controls (starmap, spacewar, groundwar). If enabled, simple right click moves or attacks. Panning is done with the middle mouse button. Zoom to fit is CTRL+Middle button in this mode.
  * Grouping units in space and ground battles. Use CTRL+0 .. CTRL+9 to assign, SHIFT+0 .. SHIFT+9 to recall a group. (Issues [#105](http://code.google.com/p/open-ig/issues/detail?id=105), [#106](http://code.google.com/p/open-ig/issues/detail?id=106))
  * Display selected unit information in the bottom-middle section of the spacewar screen. ([Issue #107](http://code.google.com/p/open-ig/issues/detail?id=107))
  * Balance related code and model improvements (Issues [#155](http://code.google.com/p/open-ig/issues/detail?id=155), [#170](http://code.google.com/p/open-ig/issues/detail?id=170))
  * General bug fixes (Issues [#156](http://code.google.com/p/open-ig/issues/detail?id=156), [#161](http://code.google.com/p/open-ig/issues/detail?id=161), [#163](http://code.google.com/p/open-ig/issues/detail?id=163), [#165](http://code.google.com/p/open-ig/issues/detail?id=165), [#166](http://code.google.com/p/open-ig/issues/detail?id=166), [#167](http://code.google.com/p/open-ig/issues/detail?id=167), [#168](http://code.google.com/p/open-ig/issues/detail?id=168), [#169](http://code.google.com/p/open-ig/issues/detail?id=169), [#172](http://code.google.com/p/open-ig/issues/detail?id=172), [#174](http://code.google.com/p/open-ig/issues/detail?id=174), [#175](http://code.google.com/p/open-ig/issues/detail?id=175))
  * Improved rocket and bomb behavior. (Issues [#176](http://code.google.com/p/open-ig/issues/detail?id=176), [#178](http://code.google.com/p/open-ig/issues/detail?id=178), [#179](http://code.google.com/p/open-ig/issues/detail?id=179))
  * Rockets and bombs can be targeted. (Issues [#177](http://code.google.com/p/open-ig/issues/detail?id=177), [#182](http://code.google.com/p/open-ig/issues/detail?id=182))
  * Bombs fired at ground structures damages other nearby structures. ([Issue #183](http://code.google.com/p/open-ig/issues/detail?id=183))
  * Fixed issues [#171](http://code.google.com/p/open-ig/issues/detail?id=171) and [#173](http://code.google.com/p/open-ig/issues/detail?id=173): adding production to wrong category.
  * Changed disable order for buildings in case of worker shortage ([Issue #164](http://code.google.com/p/open-ig/issues/detail?id=164))
  * Water vaporator now boosts the growth instead of providing housing. ([Issue #148](http://code.google.com/p/open-ig/issues/detail?id=148))
  * Fixed [issue #181](http://code.google.com/p/open-ig/issues/detail?id=181):  add more vehicles to fleets/planets than the max allowed.

# Version 0.93.530 - More AI #
  * Released: 2012-01-06 19:20 CET
  * Spacewar rockets implemented ([Issue #131](http://code.google.com/p/open-ig/issues/detail?id=131))
  * Groundwar minelayer controllable. Press D to deploy a mine. ([Issue #132](http://code.google.com/p/open-ig/issues/detail?id=132))
  * Several AI fixes
  * AI now builds defensive structures, guns, shields and stations
  * AI now constructs fleets based on best available technology
  * Autobattle now working (Issues: [#113](http://code.google.com/p/open-ig/issues/detail?id=113), [#154](http://code.google.com/p/open-ig/issues/detail?id=154))
  * Added new skirmish map: Human vs. AI. Two players, both with human technology.
  * Label fixes (Issues [#157](http://code.google.com/p/open-ig/issues/detail?id=157), [#158](http://code.google.com/p/open-ig/issues/detail?id=158), [#159](http://code.google.com/p/open-ig/issues/detail?id=159))

# Version 0.93.510 - Fixes #
  * Released: 2012-01-01 01:40 CET
  * Improved AI
  * Enabled AI for all races
  * Technology fixes
  * Ability to switch players. CTRL+V shows all planets and fleets. Select an alien planet, then press CTRL+P. You can issue orders, build, research, etc.
  * Added achievements: checking, display, save into profile
  * Fixed several bugs

# Version 0.93.500 - Fixes #
  * Released: 2011-12-30 18:30 CET
  * Added peaceful AI. Use CTRL+J to toggle it on the current user
  * Added some performance and trouble shooting settings UI.
  * Fixed [Issue #18](http://code.google.com/p/open-ig/issues/detail?id=18), Autosave and Quicksave slots are limited to 5 each
  * Ability to save via custom name (unlimited)
  * Officially fixed [Issue #111](http://code.google.com/p/open-ig/issues/detail?id=111)
  * Fixed [Issue #129](http://code.google.com/p/open-ig/issues/detail?id=129)
  * Fixed [Issue #130](http://code.google.com/p/open-ig/issues/detail?id=130)
  * Fixed [Issue #143](http://code.google.com/p/open-ig/issues/detail?id=143): Roads after ground war
  * Fixed [Issue #144](http://code.google.com/p/open-ig/issues/detail?id=144): AI retry blocked research
  * Fixed [Issue #145](http://code.google.com/p/open-ig/issues/detail?id=145)
  * Fixed [Issue #146](http://code.google.com/p/open-ig/issues/detail?id=146): Save exploration map
  * Fixed [Issue #150](http://code.google.com/p/open-ig/issues/detail?id=150): Wrong battle conclusions
  * Fixed [Issue #151](http://code.google.com/p/open-ig/issues/detail?id=151): AI autobuild to many buildings
  * Fixed [Issue 152](http://code.google.com/p/open-ig/issues/detail?id=152): Assertion error when concluding battle
  * Fixed various other bugs
  * Fixed information screen to display "Colonizable" in the right subpanels.
  * Reduced upgrade level to 2 for Bank, Traders Spaceport, Trade Center

# Version 0.93.492 - Fixes #
  * Released: 2011-12-27 13:30 CET
  * Fix for [Issue #133](http://code.google.com/p/open-ig/issues/detail?id=133)
  * Fix for [Issue #141](http://code.google.com/p/open-ig/issues/detail?id=141)
  * Fix for [Issue #137](http://code.google.com/p/open-ig/issues/detail?id=137)
  * Fix for [Issue #128](http://code.google.com/p/open-ig/issues/detail?id=128)
  * Fix for [Issue #139](http://code.google.com/p/open-ig/issues/detail?id=139)
  * Enhancement [#140](http://code.google.com/p/open-ig/issues/detail?id=140): immediately researchable technology appears light-blue in the information-technology screen.
  * Added small arrow to the top-right statusbar to indicate the presence of the screen-menu.
  * Save now produces an `info-*.xml` file. Hopefully, next time I ask for a save, they send me the `save-*.xml` without confusion.

# Version 0.93.490 - Quick fixes #
  * Released: 2011-12-25 20:20 CET
  * Fleet inventory issues ([Issue #133](http://code.google.com/p/open-ig/issues/detail?id=133))
  * Ground unit pathfinding improvements ([Issue #121](http://code.google.com/p/open-ig/issues/detail?id=121))

# Version 0.93.480 - Quick fix #
  * Released: 2011-12-21 14:30 CET
  * Fixed NPE on loading a save ([Issue # 127](http://code.google.com/p/open-ig/issues/detail?id=127))

# Version 0.93.475 - Traders #
  * Released: 2011-12-15 11:30 CET
  * **You may need to start a new game!**
  * Fixed sound issues and hangs on Mac systems ([Issue #114](http://code.google.com/p/open-ig/issues/detail?id=114)).
  * Added AI framework
  * Added trader fleets: they wander around, can be attacked. If attacked and get below 50% health, they will turn around, return to their last visited planet, and stay there for 10 ingame hours. ([Issue #117](http://code.google.com/p/open-ig/issues/detail?id=117))
  * Space battle against aliens now considers their attack/defense/social preferences, which determines when they switch tactics between attack any/attack costly ships/flee.
  * Fix: If a tracked fleet moves outside the radar zone, the follower goes to its last known location and stops.
  * Added game option to display the radar coverage area similarly to Imperium Galactica 2. ([([Issue #124](http://code.google.com/p/open-ig/issues/detail?id=124))
  * Label fixes ([Issue #112](http://code.google.com/p/open-ig/issues/detail?id=112))
  * Right clicking on a fleet or planet on the Starmap screen now jumps to that object ([Issue #115](http://code.google.com/p/open-ig/issues/detail?id=115))
  * CTRL+Middle click now zooms the view to fit the screen. When starting a new game, the view is also fit to screen. ([Issue #120](http://code.google.com/p/open-ig/issues/detail?id=120))


# Version 0.93.450 - Ground battles #
  * Released: 2011-10-01 14:30 CET
  * **You may need to start a new game!**
  * Added ground battles.
  * Improved the battle music quality.
  * Small UI improvements.
  * Added _statistics_ and _achievements_ button to the starmap screen.
  * Fixed [Issue #103](http://code.google.com/p/open-ig/issues/detail?id=103) Sullep destroyer image.

# Version 0.93.425 - Fixes #
  * Released: 2011-08-31 14:00 CET
  * **You may need to start a new game!**
  * Added ship and defense firepowers and updated alien ship configurations.
  * Added option to upgrade the entire fleet's equipment with by clicking on the [+++] button in the equipment screen. The button is only visible if there is opportunity to replace any equipment or add more to the current equipment counts. Any obsolete equipment is placed back into the main inventory ([Issue #102](http://code.google.com/p/open-ig/issues/detail?id=102)).
  * Added option to fully fill or empty an inventory slot by SHIFT+Clicking on the +1 and -1 buttons ([Issue #102](http://code.google.com/p/open-ig/issues/detail?id=102)).
  * Fixed [Issue #88](http://code.google.com/p/open-ig/issues/detail?id=88) clicking on invisible planets was possible.
  * Fixed [Issue #89](http://code.google.com/p/open-ig/issues/detail?id=89) missing or incorrect button click sounds.
  * Fixed [Issue #90](http://code.google.com/p/open-ig/issues/detail?id=90) error messages now appear directly on the statusbar instead of a regular message.
  * Fixed [Issue #92](http://code.google.com/p/open-ig/issues/detail?id=92) always show equipment configuration when a slot is selected
  * Fixed [Issue #93](http://code.google.com/p/open-ig/issues/detail?id=93) fleet speed computation.
  * Fixed [Issue #95](http://code.google.com/p/open-ig/issues/detail?id=95) empty fleets get removed properly now.
  * Fixed [Issue #96](http://code.google.com/p/open-ig/issues/detail?id=96) Deployed satellites do not decrease the available amount.
  * Fixed [Issue #97](http://code.google.com/p/open-ig/issues/detail?id=97) crash while removing equipment.
  * Fixed [Issue #98](http://code.google.com/p/open-ig/issues/detail?id=98) for firepower, count only beam-type weapons of ships.
  * Fixed [Issue #99](http://code.google.com/p/open-ig/issues/detail?id=99) cost of Spy Satellite v2.0
  * Fixed [Issue #100](http://code.google.com/p/open-ig/issues/detail?id=100) destruction of satellites was not properly reported.
  * Fixed [Issue #101](http://code.google.com/p/open-ig/issues/detail?id=101) colonized planets with satellites orbiting them no longer gain radar. In addition, any satellites will be placed back into the inventory.


# Version 0.93.400 - Button sounds #
  * Released: 2011-08-24 15:45 CET
  * Added button sounds ([Issue #28](http://code.google.com/p/open-ig/issues/detail?id=28), [Issue #53](http://code.google.com/p/open-ig/issues/detail?id=53), [Issue #54](http://code.google.com/p/open-ig/issues/detail?id=54), [Issue #57](http://code.google.com/p/open-ig/issues/detail?id=57), [Issue #77](http://code.google.com/p/open-ig/issues/detail?id=77)).
  * Fixed [Issue #81](http://code.google.com/p/open-ig/issues/detail?id=81) Fleet speed computation in case of multiple hyperdrive configurations.
  * Fixed [Issue #80](http://code.google.com/p/open-ig/issues/detail?id=80) when replacing an installed equipment on a ship via the +1 button, the original equipment is removed and added to the available inventory.
  * Fixed [Issue #82](http://code.google.com/p/open-ig/issues/detail?id=82) hungarian surface type labels.
  * Fixed [Issue #84](http://code.google.com/p/open-ig/issues/detail?id=84) Garthogs are Empire in hungarian version.
  * Fixed [Issue #83](http://code.google.com/p/open-ig/issues/detail?id=83) Keyboard arrows pan the starmap in the opposite direction.
  * Fixed [Issue #85](http://code.google.com/p/open-ig/issues/detail?id=85) if a building can't be placed due money, play sound and send an error message.
  * Added [Issue #86](http://code.google.com/p/open-ig/issues/detail?id=86) Message for yesterday's trade income.
  * Added option to limit the auto-repair by setting a minimum required money amount.

# Version 0.93.375 - Game mechanics fixes #
  * Released: 2011-08-24 15:45 CET
  * Fix [Issue #43](http://code.google.com/p/open-ig/issues/detail?id=43) Auto-build turns off even if there is opportunity for upgrading
  * Fix [Issue #26](http://code.google.com/p/open-ig/issues/detail?id=26) population growth reduced.
  * Fix [Issue #46](http://code.google.com/p/open-ig/issues/detail?id=46) if the satellite deployment video was disabled in the options, the game didn't install the satellite.
  * Fix [Issue #45](http://code.google.com/p/open-ig/issues/detail?id=45) Video volume was incorrectly set.
  * Fix multiple issues related to displaying information about planets, fleets and surfaces in face of various radar coverage configurations. ([Issue #48](http://code.google.com/p/open-ig/issues/detail?id=48), [Issue #49](http://code.google.com/p/open-ig/issues/detail?id=49), [Issue #50](http://code.google.com/p/open-ig/issues/detail?id=50), [Issue #58](http://code.google.com/p/open-ig/issues/detail?id=58))
  * Fix [Issue #55](http://code.google.com/p/open-ig/issues/detail?id=55) alien buildings could be turned on-off by the player.
  * Fix [Issue #56](http://code.google.com/p/open-ig/issues/detail?id=56) missing label for ship-walk research screen.
  * Fix [Issue #47](http://code.google.com/p/open-ig/issues/detail?id=47) Hubble deployment video was incorrect (it played a campaign video instead of the regular).
  * Fixed issue with alien fleets controllable via keyboard shortcuts on the starmap (temporarily, moving them with M still available for testing purposes).
  * Fixed crash when displaying equipment for an alien fleet.
  * Fix [Issue #59](http://code.google.com/p/open-ig/issues/detail?id=59) uninhabited planets now display "Empty - Colonizable" on the starmap screen.
  * Fix [Issue #60](http://code.google.com/p/open-ig/issues/detail?id=60) audio problem when showing or hiding the comm panel on the bridge.
  * Fix [Issue #61](http://code.google.com/p/open-ig/issues/detail?id=61) The main menu button in the options screen has been moved back to its original place in the upper right corner.
  * Fix [Issue #78](http://code.google.com/p/open-ig/issues/detail?id=78) audio support is sometimes strangely implemented on Linux or in OpenJDK.

# Version 0.93.350 - Fixes and small improvements #
  * Released: 2011-08-20 18:00 CET
  * **Update** the Equipment screen now resizes vertically with the game window, making it easier to manage a larger list of diverse ships
  * **Fix** [Issue #42](http://code.google.com/p/open-ig/issues/detail?id=42) planetary shield build limit changed to one-of-a-kind (like the research labs)
  * **Fix** Initial fleet ships of a new campaign now display equipment slots on equipment screen.
  * **Fix** [Issue #39](http://code.google.com/p/open-ig/issues/detail?id=39) Meson Projector research cost fixed
  * **Update** planetary population growt now fixed between -2000 and +1000 per day.
  * **Update** research time computation is now 75% of the research cost in game minutes
  * **Fix** [Issue #34](http://code.google.com/p/open-ig/issues/detail?id=34) on the research screen, the running research status was sometimes truncated
  * **Update** The load/save/settings screen changed and contains three tabs: load & save, audio, gameplay
  * **New** [Issue #37](http://code.google.com/p/open-ig/issues/detail?id=37) option to set the research money percentage on newly started researches, e.g., when set to 200%, all new research will start with double money.
  * **Fix** [Issue #35](http://code.google.com/p/open-ig/issues/detail?id=35)in planetary equipment, adding fighters to a planet requires a space station first (instead of military spaceport).
  * **Fix** adding vehicles and space stations to planets do not require military spaceport
  * **Fix** [Issue #40](http://code.google.com/p/open-ig/issues/detail?id=40) switching to the equipment screen while a building research is selected now changes the selection to tanks (in case of a fleet) or to fighters (in case of a planet)
  * **Fix** fixed some UI behavior on the equipment screen
  * **Fix** [Issue #41](http://code.google.com/p/open-ig/issues/detail?id=41) when upgrading equipment on a ship, you can now move to the next ship in the selected group by hitting ENTER (SHIFT+ENTER goes backwards)
  * **Update** [Issue #25](http://code.google.com/p/open-ig/issues/detail?id=25) buildings operational level now requires 50% or more energy and less than 50% damage, previous 50% worker limit has been removed. This implies that undermanned buildings may still operate on efficiency such as 12%.

# Version 0.93.300 - Mostly fixes #
  * Released: 2011-08-19 17:30
  * **Fix** sound and music volume adjustment was incorrect.
  * **Fix** Ship walk elements, Walking around the ship no longer displays a half-transparent white poligon. Instead, move around the mouse cursor to see walk options shown by popup labels.
  * **Update** error console now includes system information which could be used to report an issue more accurately.
  * **New** added option to disable animation when deploying a satellite.
  * **New** added option to disable button sound effects (effects to be added later on).
  * **New** game options are now saved with the game you are playing. Settings accessible from the main menu affects only new gameplay.


# Version 0.93.200 - Small improvements #
  * Released: 2011-08-17 17:00
  * **New** Player now starts with a small fleet when starting a new campaign.
  * **New** Alien planets now have space stations deployed to them like in the original game. You have to start a new campaign.
  * **New** scrollbars to the starmap screen.
  * **Fix** [Issue #16](http://code.google.com/p/open-ig/issues/detail?id=16): Auto-repair buildings consumed money even when no building had damage.
  * **Fix** presentation issue when deleting a save failed.
  * **Update** [Issue #20](http://code.google.com/p/open-ig/issues/detail?id=20) earthquakes occur rarer and do less damage.
  * **Fix** [Issue #21](http://code.google.com/p/open-ig/issues/detail?id=21) Fusion Plant buildable except desert, rocky, neptoplasm; Solar Plant buildable except frozen, liquid.
  * **Fix** [Issue #23](http://code.google.com/p/open-ig/issues/detail?id=23) Free Nations and Free Traders name was too long on the information screen in Hungarian.
  * **Fix** [Issue #24](http://code.google.com/p/open-ig/issues/detail?id=24) You should be able to deploy space stations and vehicles to a planet without Military spaceport.
  * **Fix** [Issue #29](http://code.google.com/p/open-ig/issues/detail?id=29) Hospitals and Stadiums had too few worker demands (80 -> 280, 350 -> 860).
  * **Fix** [Issue #31](http://code.google.com/p/open-ig/issues/detail?id=31) San Sterling was to small, increased the vertical row number by 5 cells.

# Version 0.92 - Music, few fixes and improvements #
  * Released: 2011-07-19 23:00
  * Removed upgrade options from Church, Bar, Recreation Center, Park, Stadium, Civil Lab, Mechanical Lab, Computer Lab, AI Lab and Military Lab. When you load a save, these buildings will return to their non-upgraded status (without money reclaim).
  * Decreased the morale boosting effect of Police stations where the owner race is the same as the population race.
  * **New:** Added time-to-live options to planetary inventory where satellites reside.
    * If a planet does not have any radar building or Hubble2 satellite, the satellites of other races may remain there forever.
    * Time-to-live is defined in 10s of game minutes; Survey satellite = 12 hours, Spy satellite = 24 hours, Spy satellite 2 = 96 hours.
    * The better radar is available to the planet the faster it discovers a radar, e.g., a planet with hubble 2 (radar = 4) and an enemy survey satellite (12 hours) will be discovered within 3 hours.
  * Improvements to the Load/Save screen to use a smaller XML file for the list of available saves which will reduce the population time for longer lists. Previously existing saves will become a companion XML on the next screen display.
  * The Main Menu button is now hidden on the settings part of the load/save screen to avoid accidentally leaving the game instead of returning to the previous screen.
  * **New:** Added music playback with the original 3 tunes.
  * **Fix:** Some platforms may not support the MUTE audio control in Java which caused NPE.
  * **Fix:** The Windows feature of treating ZIP files as folders caused some resource loading problems. From now, the resource locator checks whether a .zip file is actually a directory or not and reads files accordingly.
  * **New:** Added earthquake effect and notifications.

# Version 0.91 - General improvements #
  * Released: 2011-04-21 17:30
  * **Credits** screen added. The main menu has been reordered to contain a label for it. To stop the credits, hit ESC.
  * [Test screen](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/questionare.png) added. You can only see it from the **Testbed** application. You may scroll the list via the mouse wheel.
  * Created an [Auto-Build](http://code.google.com/p/open-ig/wiki/GameGuide#Auto-Build) option.
  * The **Options** screen has been implemented. Reachable through the main menu or via **ESC** or **O** key within the game.
    * You can adjust the volume of effects, music and video.
    * You can turn on auto-repair just like in the original
    * **New:** Computer voice is fun but only for a few hours. After that, it becomes annoying. However, it is still useful if the computer reports a production or research to be completed or warns about an enemy fleet. Therefore, you may disable the screen switch commentary and notifications separately.
    * **New:** adjust the credit limit for the Auto-Build option.
  * In the colony screen within the building panel, the current count of the selected building type is displayed.
  * Various building and research related amount fixes (money, upgrade, etc.)
  * **Fix:** The waypoints and targets of the fleet are now saved.
  * **New:** [Jump screen menu](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/message_history_screen_menu.png). Hold any mouse button down at the top-right corner of the status bar to display the list of available screens (just like in Imperium Galactica 2)
  * Bottom status bar notifications implemented.
    * If a message is visible, double click on it to go to its destination (a planet, a production or a research).
    * **New:** Right click on this bar to show the [message history](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/message_history_screen_menu.png). The history shows the event text, an icon and the game time when it happened. You may double click on any item to go to its target if applicable. You can hide the list by right clicking on the statusbar again. You may use the mouse scroll wheel over the list.
  * Gameplay changes:
    * Lower than 50% morale now reduces the population twice as fast
    * You may only build 1 Water vaporator per planet but gives you +10 morale.
    * Added shortage notification icons for police.
  * The planet list table can now be sorted by the columns both ascending and descending: planets, population, morale and problems.

# Version 0.90 - Fleets #
  * Released: 2011-04-15 23:30
  * Added functionality to the Equipment screen. [Screenshot](http://code.google.com/p/open-ig/source/browse/trunk/open-ig/doc/equipment-07.png).
    * You may now split fleets, transfer between fleets.
    * **Cheat:** CTRL+I will make the currently selected item researched and adds one to the inventory
    * **New feature:** Sell a fleet member. You may now sell even the larger ships from the fleet. You'll get back half of the production cost of the ship and its equipment.
    * Heavier ships are now presented in a scrollable list instead of randomly placing them around. The list shows the health, shield level.
    * To rename a fleet, double click on its name. To stop renaming, hit ENTER.
  * Added functionality to the starmap screen.
    * Move fleets, attack with the fleet (just visual indicator, no space battle so far)
    * **New feature:** Waypoint mode. Click on the MOVE command, then hold SHIFT and left click to chain up movements.
    * **New feature:** Direct move; Hold the SHIFT key and RIGHT click on the map. Direct attack; Hold CTRL and RIGHT click on the map. (You don't need to select the move or attack button.)
  * Information/Fleets should work now
  * Colonize an empty planet: build a colony ship and approach an empty planet. Note that some enemy planets may appear empty if you don't have them in a range of a v2.0 radar.
  * Load and Save screen added. You may now load any previous saves. Saving is just simply adding a new save with the current time, similarly to CTRL+S. Note that the Settings view is not implemented.
    * You may now return to the Main menu to start a new game instead of relaunching the application
  * The old setup screen is now hidden. Will be replaced by the new ingame Settings screen mentioned before.
  * Issue fixes
    * [Issue #6](http://code.google.com/p/open-ig/issues/detail?id=6) bugs and most missing features
    * [Issue #7](http://code.google.com/p/open-ig/issues/detail?id=7) bugs and missing features
    * [Issue #9](http://code.google.com/p/open-ig/issues/detail?id=9) database screen missing labels
    * [Issue #10](http://code.google.com/p/open-ig/issues/detail?id=10) starmap screen planet listing ordering
    * [Issue #11](http://code.google.com/p/open-ig/issues/detail?id=11) crash on the information planet listing table

# Version 0.89 - Bar and Database #
  * Released: 2011-04-10 21:00
  * Various data model fixes
  * Statistics screen now working
  * Database screen added
  * Bar screen added. Use CTRL+2, CTRL+3 and CTRL+4 to talk to 3 persons (Kelly is not currently available due the lack of story scripting).
  * Information: Aliens screen added
  * The new game does not place satellites onto random planets. You have to develop Radar 2.0 to see the nearby planets. Use CTRL+O to take over and start researching Radar 3.0, you should see enough planets in chain to develop Hubble 2, which is the longest range radar.
  * Various visual and behavior fixes.

# Version 0.88 - Behavior changes #
  * Released: 2011-04-08 21:00
  * Gameplay fixes: the simulation now affects the population count
  * Starmap screen improvements: added a zoom button, added visibility toggle buttons.
  * Added the option to deploy a satellite: build a survey satellite and deploy it to Center 16 for example.
  * Fixed the CTRL+O planet takeover to put an initial 5000 person population onto empty planets
  * Planets information tab: added a new list view which shows the planets in a table: name, population and delta, morale and delta and current problems
  * A blinking icons now indicate warning conditions (e.g., living space is a bit low), steady icons represents significant problems
  * Quick save  (CTRL+S) and quick load (CTRL+L) feature added. Saves are stored as `save/default/save-YYYY-MM-DD-HH-mm-ss-SSS.xml`
  * Autosave at midnight
  * The main menu screen has a disabled **Load** option but a working **Continue** option which loads the last save.

# Version 0.87 - Information screen #
  * Released: 2011-04-06 23:30
  * Information screen added
  * Research and production screens updated
  * Added simulation for:
    * Research progress
    * Production progress
    * Building construction
    * Building repair
    * Building upgrade
    * Day-by-day taxation and morale
    * Day and night cycles

# Version 0.86 - Colony screen #
  * Released: 2011-03-30 22:00
  * Colony screen integrated
  * Rendering speed improvements
  * Bug fixes

# Version 0.85 - Research & Production screen #
  * Released: 2011-03-20 14:30
  * Research and production screen added
  * Some fixed in the UI framework
  * Fixed some image resources
  * Fixed the infinite restart bug mostly occurring on Linux

# Version 0.84 - Equipment screen #
  * Released: 2011-03-06 11:30
  * Equipment screen added.
  * Rendering fixes.
  * Resource improvements

# Version 0.83 - New UI framework #
  * Released: 2011-02-27 11:45
  * Introduced a new and more composable UI framework for the screens
  * Shipwalk fixes.
  * Resource load time small improvements.

# Version 0.82 - Small improvements #

  * Improved the resource loading speed for multi-core systems. This affects the Game, the Map Editor and the Testbed applications too.