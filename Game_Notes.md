

See the [Development Blog](http://open-ig-dev.blogspot.com) for more frequent changelog.

# Version 0.95.148 - Mostly bugfixes #
  * Released: 2013-09-03 11:11 CET
  * **This note contains all changes since the version 0.95.102**.
  * Bridge screen's send/receive status is now remembered across screens and saves. ([Issue #664](https://code.google.com/p/open-ig/issues/detail?id=#664))
  * Fixed initial orbital factory not deployed to the initial planets.
  * Fixed planet inventory deployment starting off with zero HP in some cases.
  * Fixed AI not rebuilding its orbital factory.
  * Fixed AI planet evaluation and ordering crash.
  * Modified AI behavior: fleets with no firepower (e.g., all rockets fired, all weapons destroyed or missing) will retreat.
  * Java version displayed in the game window's title.
  * Fixed a label ([Issue #670](https://code.google.com/p/open-ig/issues/detail?id=#670))
  * Fixed crash when the user quickly cancels a video about to play ([Issue #671](https://code.google.com/p/open-ig/issues/detail?id=#671))
  * Fixed crash due wrong label on the fleet info panel. ([Issue #646](https://code.google.com/p/open-ig/issues/detail?id=#646))
  * Fixed another label formatting related crash. ([Issue #677](https://code.google.com/p/open-ig/issues/detail?id=#677))
  * Fixed save corruption when chosing traits for skirmish. ([Issue #678](https://code.google.com/p/open-ig/issues/detail?id=#678))
  * Fixed crash if planet surfaces were randomized in skirmish. ([Issue #678](https://code.google.com/p/open-ig/issues/detail?id=#678))
  * Fixed crash when clicking Send on a skirmish bridge ([Issue #679](https://code.google.com/p/open-ig/issues/detail?id=#679))
  * Fixed cases where a game save could not properly incorporate pending operations, such as the announcement of an objective or mission completion rewards. Usually, these where cases where the player saved in the middle of some audio announcement (for example, when the first trader is attacked in M2 and you saved while the computer told you about an incoming message, the mission was not showing up in the reload) ([Issue #681](https://code.google.com/p/open-ig/issues/detail?id=#681)).
  * The Skirmish / Galaxy screen features an extra field where you can set up the maximum technology level. This was formerly the only level settings. The current initial tech level now specifies the availability of technologies right from the start. For example, setting the first value to 3 will make everything up to level 3 available for production or construction. The scale goes from 0 to 6, where 0 means there could be level 1 researches to be done. ([Issue #683](https://code.google.com/p/open-ig/issues/detail?id=#683))
  * Replaced the French audio and video files with the proper versions, however, they could be still incorreclty timed.
  * Russian language support. Russian audio has been integrated, although some of them might not be properly synchronized to the videos.
  * Fixed some missing labels across languages.
  * Added support for Russian version.
  * Russian language related changes. Many labels have been extracted from the original version in addition to Kirill Grigoriev's diligent work in translating the new labels. Note that due the automated nature of the extraction, labels might be off.
  * Partial fixes for [Issue #684](https://code.google.com/p/open-ig/issues/detail?id=#684). Playing in skirmish with non-human races now enables a few technologies even when their level is above the specified initial level. These technologies were forcibly enabled in the main campaign (e.g., PhoodFactory). Note, however, that alien ships have fixed weapons and equipment, which can't be changed by the player at the moment. Later developments will make them as customizable as the human ships now.
  * Fixed russian labels causing crashes (The %s parameters were somehow stripped). ([Issue #685](https://code.google.com/p/open-ig/issues/detail?id=#685))
  * Fixed rendering of a scaled Achievements/Statistics screen. ([Issue #662](https://code.google.com/p/open-ig/issues/detail?id=#662), [Issue #686](https://code.google.com/p/open-ig/issues/detail?id=#686))
  * More Russian labels translated and fixed. Thanks to Grigoriev Kirill!
  * Fixed assertion error in Mission 13 (Defend Benson) in case when the player attacked the incoming Garthogs before they reached Benson. ([Issue #688](https://code.google.com/p/open-ig/issues/detail?id=#688))
  * Fixed mislabeling in the shipwalk ([Issue #687](https://code.google.com/p/open-ig/issues/detail?id=#687))
  * Added rain and snow effects to most planet types. They are randomized in terms of frequency and duration, but usually last 2-3 ingame hours. The rendering is not final yet. Development by Bajor "Danyetz" Dániel.
  * More Russian translation by Grigoriev Kirill.
  * Fixed crash in case of certain skirmish game setups. ([Issue #690](https://code.google.com/p/open-ig/issues/detail?id=#690)).
  * Balance: reduced Garthog force strength on Mission 5: Escort Admiral Tullen for easy and normal players. ([Issue #691](https://code.google.com/p/open-ig/issues/detail?id=#691))
  * Admiral Tullen's battleship should now show up with 100% health, although its hitpoints are far less than the regular Thorin.
  * Added Spy Center graphics and is buildable, although not doing anything and AI is ignoring it.
  * Factories now show their production output in the Building Info panel. ([Issue #699](https://code.google.com/p/open-ig/issues/detail?id=#699)) Equipment management did not work properly ([Issue #700](https://code.google.com/p/open-ig/issues/detail?id=#700)). Several issues were identified and fixed:
    * If the reequip tanks or rockets were selected, these were constantly re-equipped, and didn't let the user change the values. Now, these items will be reequipped only once the fleet moves into range of a planet with military spaceport.
    * Removing battleships and/or vehicle storage units left the vehicles in the fleet. Now, the excess vehicles are moved back into the inventory.
    * Global unit limits (3, 25, 30) were still hardcoded at some locations. (The definition.xml contains the option to change various limits.)
  * Improved pathfinding performance for larger battles and when selecting buildings as targets. ([Issue #702](https://code.google.com/p/open-ig/issues/detail?id=#702))
  * Modified AI to attack its enemies even before it finishes the galaxy exploration.
  * Added attack-move to ground battles. In classic mode, hold CTRL and click on the ground. In modern mode, hold CTRL+SHIFT. If the command debug is enabled via CTRL+C, the attack-move paths are shown in red.
  * Fixed subtitle for Wife #1 message. Fixed title and subtitle for escort mission messages. ([Issue #674](https://code.google.com/p/open-ig/issues/detail?id=#674))
  * Updated skirmish to accept radically different galaxy and technology models. ([Issue #690](https://code.google.com/p/open-ig/issues/detail?id=#690))
  * Reduced frequency of thunder audio effects.
  * During spacewar, switching to chat on one panel while the other already shows chat will switch that chat panel to something else. ([Issue #543](https://code.google.com/p/open-ig/issues/detail?id=#543))
  * Fixed chat when shooting at traders in the middle of a peaceful conversation not switching to flee text. ([Issue #660](https://code.google.com/p/open-ig/issues/detail?id=#660), [Issue #546](https://code.google.com/p/open-ig/issues/detail?id=#546), [Issue #696](https://code.google.com/p/open-ig/issues/detail?id=#696)). (Luckily, it was just a missing case in the chat def)
  * Changed tank speeds, light tanks go faster and each newer tank gets slower. ([Issue #589](https://code.google.com/p/open-ig/issues/detail?id=#589))
  * Fixed vehicle movement getting interrupted at each cell if the movement is linear (i.e., if the current target can be reached under 0.5 step, the remaining 0.5 worth of movement is deferred till the next step).
  * Changed the AI to build as many military spaceports as the player can afford with 50% money excess relative to the spaceport cost and autobuild limit. The equation is: spaceport\_count = money / (spaceport\_cost `*` 1.5 + autobuild\_limit) + 1  ([Issue #509](https://code.google.com/p/open-ig/issues/detail?id=#509))
  * You can make alliance with the Dargslan if you are extremely lucky. The war threshold has been lovered to 90. ([Issue #561](https://code.google.com/p/open-ig/issues/detail?id=#561))
  * Changed vehicle firepower and HP. ([Issue #574](https://code.google.com/p/open-ig/issues/detail?id=#574))
  * Added nicknames to ships ([Issue #572](https://code.google.com/p/open-ig/issues/detail?id=#572)). These nicknames are listed in the players.xml file for each player. The nicknames/nickname entries reference a label.
  * Ships now register kill counts and kill values. Kill counts are visible in the ship information subpanel during spacewar, in the hp/dps box of an individual ship in equipment, and the total kills in the equipment screen totals area.
  * Moved the Sell button on the equipment screen further to the left to avoid accidental sell instead of remove. ([Issue #621](https://code.google.com/p/open-ig/issues/detail?id=#621))
  * Added difficulty dependent ECM vs Anti-ECM hit probabilities along with probabilities that the scrambled rocket will target the same fleet it was fired from. (Note that the battle.xml structure changed, see battle.xsd for details). ([Issue #607](https://code.google.com/p/open-ig/issues/detail?id=#607))
  * Fixed incorrect Hungarian subtitle. ([Issue #634](https://code.google.com/p/open-ig/issues/detail?id=#634))
  * Added options to hide various panels on the starmap screen. ([Issue #641](https://code.google.com/p/open-ig/issues/detail?id=#641))
  * Updated the Free Play version of the main campaign:
    * enabled Colony Ships for all parties
    * removed colonization limits from all parties
    * the human player now starts with a fleet with all ships and vehicles granted in the original through levels 1-5 (including Thorin).
  * Fixed missing parameters from diplomacy in Russian translation ([Issue #704](https://code.google.com/p/open-ig/issues/detail?id=#704))
  * Added Orbital Factory as available tech to all non-human races in the Main campaign.
  * Added the ability to colonize a planet by clicking on the Colonize button, now shown when the planet is suitable (known to be empty). The AI assistance will produce military spaceport, orbital factory and the colony ships if necessary, deploys into new fleets and sends them to the targets. ([Issue #296](https://code.google.com/p/open-ig/issues/detail?id=#296))
  * A few missing French button graphics added.
  * French graphics cleanup.
  * Fixed fleet and shield regeneration not working properly.
  * Fixed some UI errors in case of main menu language change (CTRL+1...CTRL+5)
  * Modified skirmish initial planet count minimum to 1 to avoid crashes ([Issue #705](https://code.google.com/p/open-ig/issues/detail?id=#705)) due zero total lab required. I will extend the skirmish to handle 0 initial planets later on where the player's fleet is just deployed somewhere and may colonize a planet he/she wishes at start.
  * Fixed case when mission objectives were not properly triggered when the computer audio notification was turned off ([Issue #705](https://code.google.com/p/open-ig/issues/detail?id=#705)).
  * The planet management AI now won't produce Orbital Factory if there is none in the empire ([Issue #705](https://code.google.com/p/open-ig/issues/detail?id=#705)).
  * Modified the save system to use XML+GZIP instead of plaintext XML. Existing saves will be automatically converted to the new format.
  * Population loss on planets where the owner's race is different from the population race is now 3 times as fast.
  * Tanks now try to get closer to their targets in ground battles. If you give them an explicit attack order, they will approach the target, fire one round, then move one cell closer if possible. If you give a move or stop order, they go into a form of guard mode and won't try to get closer to their target (i.e., they will hold formation). In attack-move this behavior is not enabled. You can disable this in the open-ig-config.xml under aiGroundAttackGetCloser.
  * Showing the bar and diplomacy room is now dependent on the walks.xml definition of the current level's ship design, not the level number. In addition, diplomacy was moved to the level 5 ship.
  * Aliens have now a separate police station (in the data files) which is cheaper and provides morale boost. This should improve their numbers and economy.
  * Campaign editor improvements. You can now create a copy of the existing campaign and adjust a few properties. However, I suggest using the copy feature only and make changes by the old fashioned  XML editing.
  * Aliens now produce their special units.
  * Fixed AI static defense planning for races who don't have banks and trader spaceports.
  * Aliens are now able to colonize any number of planets after 2 weeks into rank 4.
  * Fixed diplomacy screen becoming inaccessible. ([Issue #708](https://code.google.com/p/open-ig/issues/detail?id=#708))
  * Fixed missing letter in the Russian font ([Issue #711](https://code.google.com/p/open-ig/issues/detail?id=#711)).
  * Fixed some labels.
  * Fixed some trader chats ([Issue #711](https://code.google.com/p/open-ig/issues/detail?id=#711)).
  * Modified the loading to fix older saves where the aliens shared the same Police Station as the human player.
  * Fixed the equipment screen to prevent adding technology to slots which don't support that particular technology (e.g., rockets to laser slot). ([Issue #712](https://code.google.com/p/open-ig/issues/detail?id=#712))
  * Space station equipment status is now saved and loaded properly. ([Issue #715](https://code.google.com/p/open-ig/issues/detail?id=#715))
  * Fleets are auto-reequipped only once after a battle (not on every approach to a planet with a spaceport).
  * Space stations are auto-reequipped only once after a battle (not constantly, which made equipment management non-operational).
  * Holding down the mouse over +1 and -1 buttons to change equipment counts works again.
  * Changed timing of mission 7 (protect traders) and mission 13  (escort Benson): there is less chance they overlap and the player can reach both parties if playing with pre-hyperdrive trait ([Issue 718](https://code.google.com/p/open-ig/issues/detail?id=718))
  * Fixed issue with non-human police stations not getting workers and power in case an earlier game was  loaded with a newer Open-IG version. ([Issue #719](https://code.google.com/p/open-ig/issues/detail?id=#719))
  * Fixed the overlapping of Hubble 1 and Hubble 2 in the production screen, preventing the latter to be built if run with skirmish set to level 6. Note that Hubble 1 is a campaign-scripted satellite, and can't be really deployed. ([Issue #720](https://code.google.com/p/open-ig/issues/detail?id=#720))
  * Players can now build previously non-buildable tech in skirmish (e.g., Thorin @ lvl 6).
  * Added option to campaign editor's copy feature to make copy of the default labels.
  * If the player goes into a trade agreement with another empire, the Trader ships travel to those planets as well (cosmetic) ([Issue #596](https://code.google.com/p/open-ig/issues/detail?id=#596))
  * Intro English subtitle corrections ([Issue #725](https://code.google.com/p/open-ig/issues/detail?id=#725))
  * Fixed crash when ship-walking on level 4 ([Issue #727](https://code.google.com/p/open-ig/issues/detail?id=#727))
  * Fixed the research tip on lab reorganization ([Issue #733](https://code.google.com/p/open-ig/issues/detail?id=#733))
  * Fixed earthquake frequency ([Issue #737](https://code.google.com/p/open-ig/issues/detail?id=#737))
  * Fixed hyperdrive availability problem in case of the Pre-Hyperdrive trait ([Issue #747](https://code.google.com/p/open-ig/issues/detail?id=#747))
  * Fixed some campaign start and game load issues. Should fix the cases where attacking enemy planets wouldn't work.
  * Fixed the Coffee Break achievement and Oldest Man achievements.
  * Fixed issue with attacking other fleets ([Issue #753](https://code.google.com/p/open-ig/issues/detail?id=#753)).
  * Fixed crash due invalid current research ([Issue #754](https://code.google.com/p/open-ig/issues/detail?id=#754)).
  * Fixed issue with adding larger ships to fleets or stations to planets. ([Issue #755](https://code.google.com/p/open-ig/issues/detail?id=#755))
  * Fixed transfer of large ship between fleets ([Issue #756](https://code.google.com/p/open-ig/issues/detail?id=#756)).
  * Fixed ship transfer again: moving ships between fleets now retains all equipment, statistics and health information.
  * Fixed several bugs preventing the AI to upgrade its fleet or colonize planets. ([Issue #758](https://code.google.com/p/open-ig/issues/detail?id=#758))
  * Fixed auto-colonization not using existing colony ships at the start of the skirmish
  * Colony ships are no longer added to the starting fleet, but only to separate fleets.
  * Fixed auto-colonization not available until the colony ship has been researched even if there was a colony ship in fleet.
  * Fixed fleet movement-task tracking related issues, interfering with scripting and auto-colonization.
  * Fixed AI trying to remove the same building multiple times ([Issue #762](https://code.google.com/p/open-ig/issues/detail?id=#762))
  * Fixed earthquake occurrence time to be the ingame-time instead of the total time (which includes pauses).
  * Fixed earthquake damage over time for non-default game speeds.
  * Fixed Pre-Warp trait not properly disabling hyperdrives. ([Issue #764](https://code.google.com/p/open-ig/issues/detail?id=#764))
  * Fixed traders not catcheable on levels 1 and 2 if Pre-Warp trait was enabled ([Issue #736](https://code.google.com/p/open-ig/issues/detail?id=#736))
  * Fixed equipment showing actual numbers > 0 even if the slot was empty. ([Issue #764](https://code.google.com/p/open-ig/issues/detail?id=#764))
  * Fixed NPE when finishing a fleet split ([Issue #765](https://code.google.com/p/open-ig/issues/detail?id=#765))
  * Fixed AI Managed planets overfill vehicles if the player changed the vehicle mix manually. ([Issue #763](https://code.google.com/p/open-ig/issues/detail?id=#763))
  * Reduced speed of Garthog virus carriers in case the player has the Pre-Warp trait. ([Issue #736](https://code.google.com/p/open-ig/issues/detail?id=#736))
  * Changed rocket movement to avoid the stable-orbit around a target. ([Issue #766](https://code.google.com/p/open-ig/issues/detail?id=#766))
  * Use PgUp and PgDown keys to jump between planets. ([Issue #721](https://code.google.com/p/open-ig/issues/detail?id=#721))
  * Fixed auto-colonization not working properly in some cases.
  * Modified vehicle pathfinding to reduce the number of turn-arounds when the command was issued in-between two cells.
  * Fixed AI patrol not moving anywhere and producing small zig-zag like movements.
  * Fixed database screen's information button: now it opens the planet list instead of alien races.
  * Fixed the production line's button tooltips.
  * Added button sounds to some colony info buttons. ([Issue #723](https://code.google.com/p/open-ig/issues/detail?id=#723))
  * Added tooltip text to the auto-build settings on the colony info screen, explaining each settings. Note that due to the large tooltip, they might not be fully readable below 600 pixel window height.
  * New gameplay option: allow the AI managed planets to produce items, default true. Some expressed annoyance with the original always-true mode. Note that the AI will still deploy items available in the inventory.
  * AI: will build equipment factory to build radars to improve on exploration. ([Issue #770](https://code.google.com/p/open-ig/issues/detail?id=#770))
  * AI: will build military spaceport much earlier.
  * Spacewar: fixed display of DPS, it no longer shows the fraction. ([Issue #771](https://code.google.com/p/open-ig/issues/detail?id=#771))
  * Spacewar: fixed select all button selecting rockets and kamikaze units.
  * Modified diplomatic responses to allow positive AI answers to happen more likely.
  * Groundwar: Units and barracks now prefer the more damaged enemy units in their fire range.
  * Groundwar: Mines are now properly triggered by passing vehicles.
  * Fixed mission 17 - prototype not catcheable with Pre-Warp trait. ([Issue #772](https://code.google.com/p/open-ig/issues/detail?id=#772))
  * Starmap: added indicator below the planets telling if there is an operational Military Spaceport on the planet:  . Due to lack of space, it is not shown on any of the information screens, and besides, it is meant to be a starmap aid.
  * Fixed transfer of Fighters incorrectly changing the counts. ([Issue #773](https://code.google.com/p/open-ig/issues/detail?id=#773))
  * Fixed selling fighters not reducing the fleet numbers. ([Issue #773](https://code.google.com/p/open-ig/issues/detail?id=#773))
  * Fixed French text for the antidote-trader spacewar chat. ([Issue #645](https://code.google.com/p/open-ig/issues/detail?id=#645))
  * Skirmish: The starting fleet now has one equipped radar if the technology settings allow it.
  * AI: AI will now build spaceship factory earlier; satellites will be produced much sooner, allowing earlier colonization.
  * Profile selection screen implemented. Existing profile can be selected either via typing in the name or clicking on the list entry. Double click or press ENTER to accept the selection (or click the button). You can't delete a profile from within the game, but you can delete the directory in the "save" subfolder manually.
  * Fixed satellite deployment/destruction related issues.
  * Fixed unit selection via SHIFT+Click not adding units to the selection.
  * Fixed some erratic fleet-AI behavior on higher game speeds.
  * Modified damage calculation to a sublinear function of the number of attackers in space battles. For example, attacking with 10 units with 100 dps/unit yields an effective 400 dps. This new behavior can be disabled in the open-ig-config.xml under the key "spacewarDiminishingAttach" set to false.
  * The battleship received after promotion to commander is no longer equipped with hyperdrive if the pre-hyperdrive trait is selected. ([Issue #774](https://code.google.com/p/open-ig/issues/detail?id=#774))
  * Fixed AI behavior if the player attacks a Garthog fleet while the destroy the virus carriers mission is active. ([Issue #776](https://code.google.com/p/open-ig/issues/detail?id=#776))
  * Fixed issue with transferring ships between fleets, allowing more ships to be moved into a single fleet than the capacity limit. ([Issue #775](https://code.google.com/p/open-ig/issues/detail?id=#775))

# Version 0.95.102 - Skirmish and bugfixes #
  * Released: 2012-09-18 10:30 CET
  * French language support related changes
  * A quick fix for a crash and mission no-progression bug ([Issue #644](https://code.google.com/p/open-ig/issues/detail?id=#644)).
  * Skirmish screen. You can find some ideas about it in [Issue #638](https://code.google.com/p/open-ig/issues/detail?id=#638).
  * Fixed crashes caused by missing video references in forced messages (such as fired) ([Issue #647](https://code.google.com/p/open-ig/issues/detail?id=#647)).
  * Fixed missing bridge message panel when the Psychologist test is completed.
  * Fixed crash when user clicks on the Game over screen, and the bridge screen still interprets this click as in-game.
  * Fixed test questionare state when reloading a save.
  * Added logic for shared-radar for campaign and skirmish. You need to have a relation of 80 with the aliens along with an alliance against someone else. The settings can be changed in the definition.xml.
  * Modified AI diplomatic reaction to consider the current relation with you (and the third party in ally case), therefore, if you are good friends with the alien, they will accept money in P = relation %.
  * Starmap modified do disallow attacking fleets and planets of the same group in skirmish.
  * The game now can play external OGG music. Place your music under /audio/generic/music and name your files Music4.ogg, Music5.ogg etc. You don't need to padd the numbers. ([Issue #639](https://code.google.com/p/open-ig/issues/detail?id=#639))
  * Fixed case where the new trait system did not properly load old saves. ([Issue #643](https://code.google.com/p/open-ig/issues/detail?id=#643))
  * Fixed a no-progression bug. ([Issue #644](https://code.google.com/p/open-ig/issues/detail?id=#644))
  * Fixed NPE crash due the new message-history behavior on the bridge ([Issue #647](https://code.google.com/p/open-ig/issues/detail?id=#647))
  * Added new failsafe cases for level 3 when you manage to desert a Garthog planet, a colony ship will automatically recolonize it for you. ([Issue #649](https://code.google.com/p/open-ig/issues/detail?id=#649))
  * Skirmish victory conditions fully implemented. The new win screen, loading and saving a skirmish definition will be handled later ([Issue #638](https://code.google.com/p/open-ig/issues/detail?id=#638), [Issue #259](https://code.google.com/p/open-ig/issues/detail?id=#259), [Issue #637](https://code.google.com/p/open-ig/issues/detail?id=#637))
  * Fixed NPE crash in space battle ([Issue #654](https://code.google.com/p/open-ig/issues/detail?id=#654))
  * Fixed Technology victory checking to ignore Pirate and Trader AI players.
  * ixed quick-research panel button visible in levels 1-2 in some cases.
  * Fixed cases where the AI could attack strong allies.
  * Added trade agreement management: trader AI starts to visit the other planets as well. Updated the diplomacy screen to allow trade agreement to be established only once. Going to war with the other will clear the trade agreement.
  * Added more traits. ([Issue #642](https://code.google.com/p/open-ig/issues/detail?id=#642))
  * Fixed traits not working in skirmish mode.
  * Fixed victory condition checking.
  * Fixed initial fleet not equipped properly.
  * Added mouse scroll and keyboard navigation to information/inventions screen ([Issue #651](https://code.google.com/p/open-ig/issues/detail?id=#651)).
  * Buildings list on the info panel now get a light-red color when the player doesn't have enough money to build them.
  * Quick research now displays the research cost under the description.
  * Fixed cases where the money can go below zero.
  * AI will now explicitly build Water Vaporator even when the morale is above 50.
  * Some German translation fixes (external contributor).
  * Fixed virus bomb missing battle parameters. ([Issue #655](https://code.google.com/p/open-ig/issues/detail?id=#655))
  * Fixed crash in the Achievements screen when showing/hiding achievements in the list. ([Issue #659](https://code.google.com/p/open-ig/issues/detail?id=#659))
  * Error logging into file open-ig.log in the game's main directory.
  * Fixed mission objective representation and behavior issue during M12: New Caroline Virus. ([Issue #661](https://code.google.com/p/open-ig/issues/detail?id=#661))
  * AI now executes unlimited actions on all difficulties. Fleet strength limits still apply on EASY and NORMAL.
  * Fixed cases where the AI paused all its actions until the colonization completed on a planet.
  * Fixed skirmish not letting the AI attack or colonize before the player colonized at least one planet.
  * Fixed Trader AI related crash.
  * Fixed case when the skirmish settings doesn't contain a player ("You"). You'll get assigned to a random AI faction. You can just observe or give commands, however, the AI might overrule you in some situations.
  * Changed the buildings.xml & xsd format to reference the building image resources directly and not through a filename pattern.
  * Fixed AI colonization not working in skirmish.
  * Fixed AI colonization in general: if the AI has colony ships in inventory or deployed, it will colonize a planet if available.
  * Fixed skirmish screen mod and label related errors.
  * Fixed missing labels for AI fleet names in skirmish mode.
  * Fixed music not playing.
  * Fixed Garthog fleet labels in the main campaign.
  * The diplomacy panel on the information screen no longer lists aliens as allies if the relation is above 90. The new logic considers only players whom which the player has active alliance agains another player.
  * ixed the initial stance settings for the skirmish screen. Alliances and enemies are now correctly established.

# Version 0.95.043 - Battle balance and bugfixes #
  * Released: 2012-08-17 17:30 CET
  * Reduced fighter hitpoints into the 200-1000 range. ([Issue #614](https://code.google.com/p/open-ig/issues/detail?id=614))
  * Added weapon-efficiency battle.xml parameter, which can reduce the weapon effectivenes against a target matching a filter criterion.
  * Added 5%-20% efficiency values to all destroyers, cruisers and battleships against fighters, except Destroyer 1 (due to level 1). Fighter vs. Fighter goes at 100%.
  * Changed AI and auto-targeting to select nearby targets based on the against-efficiency metrics, instead of just pure randomly. I.e., larger ships are unlikely to target fighters when they can match another larger ship.
  * Changed when the first wife video message arrives. ([Issue #414](https://code.google.com/p/open-ig/issues/detail?id=414))
  * Fixed cases when escorting a mission fleet via the follow mode the defender fleet did not attack the offender automatically. ([Issue #536](https://code.google.com/p/open-ig/issues/detail?id=536))
  * Quick production panel is populated with initial technologies and is updated once a research has completed. ([Issue #627](https://code.google.com/p/open-ig/issues/detail?id=627))
  * Fixed groundwar defensive structures not firing and not properly reacting to energy shortage ([Issue #630](https://code.google.com/p/open-ig/issues/detail?id=630))
  * AI and quick panel don't allow producing technologies marked as nobuild. ([Issue #633](https://code.google.com/p/open-ig/issues/detail?id=633))
  * Fixed several battle and mission issues due the mechanics changes and other latent circumstances. ([Issue #635](https://code.google.com/p/open-ig/issues/detail?id=635))

# Version 0.95.040 - Quick production #
  * Released: 2012-08-14 21:30 CET
  * Fixed several internal bugs.
  * Added explicit mute options to audio settings ([Issue #75](https://code.google.com/p/open-ig/issues/detail?id=75))
  * Added more diagnostic options. Repeating crashes won't flood the log anymore. ([Issu #116](https://code.google.com/p/open-ig/issues/detail?id=116))
  * Added quick production panel. ([Issue #311](https://code.google.com/p/open-ig/issues/detail?id=311))
  * Reduced the number of night-shades which should reduce stuttering of the planet surface during day-night transitions. ([Issue #359](https://code.google.com/p/open-ig/issues/detail?id=359))
  * Incoming messages are now historized, however, you'll need to start a new game to get older messages. ([Issue #394](https://code.google.com/p/open-ig/issues/detail?id=394))
  * Achivement screen shows the got/total number of achievements. You can hide got/missing achievements. ([Issue #431](https://code.google.com/p/open-ig/issues/detail?id=431))
  * Fixed cases where high speed settings would prevent mission progression. ([Issue #433](https://code.google.com/p/open-ig/issues/detail?id=433))
  * Changed the time where the second dream occurs to avoid overlapping with missions. ([Issue #447](https://code.google.com/p/open-ig/issues/detail?id=447))
  * New Caroline virus infections reduced to 3 and related visualization bugs fixed. (Issues [#470](https://code.google.com/p/open-ig/issues/detail?id=470), [#475](https://code.google.com/p/open-ig/issues/detail?id=475))
  * Fixed planet management AI working too slow on high speed settings ([Issue #487](https://code.google.com/p/open-ig/issues/detail?id=487))
  * Changed level 1 mission timings ([Issue #535](https://code.google.com/p/open-ig/issues/detail?id=535))
  * Game will resume on normal speed when a bridge-message is finished, and was not manually paused before. (Issues [#537](https://code.google.com/p/open-ig/issues/detail?id=537), [#557](https://code.google.com/p/open-ig/issues/detail?id=557))
  * Fixed crash and timing of mission 5 (Tullen). ([Issue #556](https://code.google.com/p/open-ig/issues/detail?id=556))
  * Changed the ground war logic to consider power levels of buildings and defensive structures. ([Issue #570](https://code.google.com/p/open-ig/issues/detail?id=570))
  * Changed multi-missile behavior and visual effects. ([Issue #577](https://code.google.com/p/open-ig/issues/detail?id=577))

# Version 0.95.031 - Bugfixes #
  * Released: 2012-07-21 20:00 CET
  * Campaign editor: added missing XML schemas of game files. This will help verify the data files in the future. ([Issue #67](http://code.google.com/p/open-ig/issues/detail?id=67))
  * Moved simulation constants into the definition.xml. You can change the build and repair speed, fleet ship limits, etc. ([Issue #70](http://code.google.com/p/open-ig/issues/detail?id=70))
  * Fixed an AI exploration crash ([Issue #590](http://code.google.com/p/open-ig/issues/detail?id=590))
  * Fixed destroyed shields not properly dropping the entire planet shielding. ([Issue #609](http://code.google.com/p/open-ig/issues/detail?id=609))
  * Fixed case where AI and user action could result in more than 3 space stations being added to a planet. ([Issue #611](http://code.google.com/p/open-ig/issues/detail?id=611))
  * Added the Anti-ECM vs. ECM hit probability matrix to the battle.xml. ([Issue #612](http://code.google.com/p/open-ig/issues/detail?id=612))

# Version 0.95.030 - Beta + bugfixes #
  * Released: 2012-07-13 15:00 CET
  * **Gergely Harrach** is now an official contributor, welcome!
  * Added a quick-research panel to the game where you can pick a research, stop a running research, adjust money on the current research and check the current and required lab numbers of technologies.
  * Fixed research/production label images to be consistent across languages ([Issue #63](http://code.google.com/p/open-ig/issues/detail?id=63))
  * Added tooltips to many buttons and labels. ([Issue #76](http://code.google.com/p/open-ig/issues/detail?id=76))
  * Clicking on "Other..." in the options screen now exits fullscreen mode so the dialog becomes visible. ([Issue #337](http://code.google.com/p/open-ig/issues/detail?id=337))
  * Ability to override existing save "slots" ([Issue #341](http://code.google.com/p/open-ig/issues/detail?id=341))
  * The game now requires confirmation on returning to the main menu to avoid accidental quits ([Issue #342](http://code.google.com/p/open-ig/issues/detail?id=342))
  * Credits scrolls in double speed ([Issue #368](http://code.google.com/p/open-ig/issues/detail?id=368))
  * AI now performs more actions on higher game speeds and does not limit itself on hard (Issues [#452](http://code.google.com/p/open-ig/issues/detail?id=452), [#551](http://code.google.com/p/open-ig/issues/detail?id=551))
  * Fixed alien races not having access to certain leveled technologies such as tanks and vehicles ([Issue #454](http://code.google.com/p/open-ig/issues/detail?id=454))
  * Scaling up the UI now resizes the main window (but only when scaling is done in windowed mode) ([Issue #516](http://code.google.com/p/open-ig/issues/detail?id=516))
  * Fixed case where the space chat achievement was triggered through non-chat means ([Issue #541](http://code.google.com/p/open-ig/issues/detail?id=541))
  * Trader ships are no longer affected by ship formations during defend missions ([Issue #544](http://code.google.com/p/open-ig/issues/detail?id=544))
  * Double clicking on a game to load no longer gives 3 click-sounds ([Issue #545](http://code.google.com/p/open-ig/issues/detail?id=545))
  * Fixed some trader fleet history tracking causing error messages ([Issue #548](http://code.google.com/p/open-ig/issues/detail?id=548))
  * Fixed some NPE-related crashes and slowdowns ([Issue #549](http://code.google.com/p/open-ig/issues/detail?id=549))
  * Fixed the on-bridge repeated appearance of the Doctor for the test ([Issue #550](http://code.google.com/p/open-ig/issues/detail?id=550))
  * Fixed issue with subtitles not being displayed beyond a certain time into videos. ([Issue #552](http://code.google.com/p/open-ig/issues/detail?id=552))
  * Fixed issue when user toggled fullscreen mode during video playback and the screen remained blacked out, allowing no interaction with the game. ([Issue #552](http://code.google.com/p/open-ig/issues/detail?id=552))
  * Fixed crash in space battles ([Issue #553](http://code.google.com/p/open-ig/issues/detail?id=553))
  * Fixed many translation errors and typos in the german translation. Thanks **Silverntiger**. ([Issue #560](http://code.google.com/p/open-ig/issues/detail?id=560))
  * Changed how building upgrade information is displayed. The cost, description and state is now readable in a tooltip if hovered over the panel or the stars. ([Issue #564](http://code.google.com/p/open-ig/issues/detail?id=564))
  * Fixed rockets and bombs retreating with the fleet ([Issue #565](http://code.google.com/p/open-ig/issues/detail?id=565))
  * Fixed multi-rocket behavior and animation ([Issue #565](http://code.google.com/p/open-ig/issues/detail?id=565))
  * If a ship destroys its target, it goes automatically into guard mode ([Issue #565](http://code.google.com/p/open-ig/issues/detail?id=565))
  * Fixed cases when AI exploration fleets leave the starmap ([Issue #566](http://code.google.com/p/open-ig/issues/detail?id=566))
  * Added diplomatic options to the Garthog race (accessible in freeplay only). Note that the options are not yet translated into german. ([Issue #567](http://code.google.com/p/open-ig/issues/detail?id=567))
  * Picture of the Free Traders was missing from the database ([Issue #569](http://code.google.com/p/open-ig/issues/detail?id=569))
  * Changed the behavior of the selection box in ground battles. This should allow more precise single and multiple unit selection ([Issue #587](http://code.google.com/p/open-ig/issues/detail?id=587))
  * Fixed case where unfinished ground defense buildings received guns as well ([Issue #592](http://code.google.com/p/open-ig/issues/detail?id=592))
  * Water waporators now provide 100% increase for population growth, e.g., when built, desert planets have 100% population growth instead of 50%. Upgrades can increase this to much higher level. ([Issue #599](http://code.google.com/p/open-ig/issues/detail?id=599))
  * Fixed tax morale never going above 96% no matter how happy the colony was. ([Issue #600](http://code.google.com/p/open-ig/issues/detail?id=600))
  * Fixed cases where more ships could be transferred into a fleet than allowed ([Issue #601](http://code.google.com/p/open-ig/issues/detail?id=601))
  * Fixed issue with guns and starbases not re-targeting after their current target went out of range ([Issue #602](http://code.google.com/p/open-ig/issues/detail?id=602))
  * Modified behavior in case multiple ground shields are participating in  a space battle. ([Issue #603](http://code.google.com/p/open-ig/issues/detail?id=603))
  * Fixed crashes and display inconsistencies on the information screen. ([Issue #604](http://code.google.com/p/open-ig/issues/detail?id=604))

# Version 0.95 - Beta #
  * Released: 2012-06-09 12:30 CET
  * **Missions from earlier saves might not work properly. I suggest you start a new game, especially if you are still on levels 1 or 2.**
  * Level 1 missions happen much sooner than before. Watch out in case you run on higher simulation step speeds.
  * If the game crashes during gameplay, you'll get a crash save. Please include it along with any relevant autosaves when you report an issue.
  * Added group selection buttons to spacewar and groundwar. Use CTRL+0..9 to create, SHIFT+0..9 to recall, CTRL+A to select all. Left click on the star button to select all, right click to deselect all. Once a group exists, you can dismiss the group by right clicking on the shield. (Issues [#108](http://code.google.com/p/open-ig/issues/detail?id=108), [#409](http://code.google.com/p/open-ig/issues/detail?id=409))
  * Added spacewar subsystem damage. Slots and ship health regenerates over a planet with military spaceport.  ([Issue #184](http://code.google.com/p/open-ig/issues/detail?id=184))
  * Added zoom button to colony screen. Left click to zoom in, right click to zoom out, middle click to reset to 100%. ([Issue #219](http://code.google.com/p/open-ig/issues/detail?id=219))
  * Equipment screen now displays defensive and offensive values of selected items and the fleet in total ([Issue #297](http://code.google.com/p/open-ig/issues/detail?id=297))
  * Some colony needs are now displayed textually in the colony info screen and panel. (Issues [#304](http://code.google.com/p/open-ig/issues/detail?id=304), [Issue #533](http://code.google.com/p/open-ig/issues/detail?id=533))
  * Two more achievements for level 1 (Issues [#324](http://code.google.com/p/open-ig/issues/detail?id=324), [#529](http://code.google.com/p/open-ig/issues/detail?id=529))
  * In production/research screen, selecting a main or subcategory now jumps to the first technology or display the not-available animation if the list is empty. (Issues [#389](http://code.google.com/p/open-ig/issues/detail?id=389), [#527](http://code.google.com/p/open-ig/issues/detail?id=527))
  * Added landing marker for space battle ([Issue #390](http://code.google.com/p/open-ig/issues/detail?id=390))
  * Improved hostility of AI players ([Issue #404](http://code.google.com/p/open-ig/issues/detail?id=404))
  * Added missing space-chat cases with traders and planet blockades ([Issue #445](http://code.google.com/p/open-ig/issues/detail?id=445))
  * Added statistical counters for battle performance ([Issue #494](http://code.google.com/p/open-ig/issues/detail?id=494))
  * Fixed a few button related errors ([Issue #500](http://code.google.com/p/open-ig/issues/detail?id=500))
  * Fixed rendering anomalies if primary and secondary UI scaling was enabled ([Issue #501](http://code.google.com/p/open-ig/issues/detail?id=501))
  * Added missing sound effect when selecting a planet on the starmap ([Issue #502](http://code.google.com/p/open-ig/issues/detail?id=502))
  * Battle statistics screen mostly won't popup if you attack a trader ([Issue #503](http://code.google.com/p/open-ig/issues/detail?id=503))
  * Fixed rendering anomalies with bridge/diplomacy projectors ([Issue #504](http://code.google.com/p/open-ig/issues/detail?id=504))
  * Music gets muted when a satellite deploy animation is running. ([Issue #505](http://code.google.com/p/open-ig/issues/detail?id=505))
  * Fixed issues with Garthogs not reaching Benson's fleet ([Issue #506](http://code.google.com/p/open-ig/issues/detail?id=506))
  * Fixed wrong space-chat options in certain cases ([Issue #507](http://code.google.com/p/open-ig/issues/detail?id=507))
  * AI now properly refills its stations with rockets ([Issue #508](http://code.google.com/p/open-ig/issues/detail?id=508))
  * Fixed cases where ships could be added beyond the limits of the fleets ([Issue #510](http://code.google.com/p/open-ig/issues/detail?id=510))
  * Added indicator in equipment screen to note you need a battleship in order to add tanks to the fleet. ([Issue #512](http://code.google.com/p/open-ig/issues/detail?id=512))
  * AI will now build defenses and tanks after the planet's economic and factory buildings have been built. ([Issue #513](http://code.google.com/p/open-ig/issues/detail?id=513))
  * Groundwar AI now targets buildings and deploys its units at a random position. ([Issue #514](http://code.google.com/p/open-ig/issues/detail?id=514))
  * Fixed mission specific space-chat errors ([Issue #519](http://code.google.com/p/open-ig/issues/detail?id=519))
  * Offline buildings under repair now display the wrench animation ([Issue #520](http://code.google.com/p/open-ig/issues/detail?id=520))
  * Mission time management diagnostics added ([Issue #522](http://code.google.com/p/open-ig/issues/detail?id=522))
  * Starmap planet information now displays the population delta ([Issue #523](http://code.google.com/p/open-ig/issues/detail?id=523))
  * Fixed trade income is not always showing up in the statusbar ([Issue #524](http://code.google.com/p/open-ig/issues/detail?id=524))
  * The Title and Intro videos now play once if you start the game. Skip with ESC or SPACE if you want. ([Issue #525](http://code.google.com/p/open-ig/issues/detail?id=525))
  * Double click selects the same type units in space and groundwar. Triple clicks select the same categor of units. ([Issue #526](http://code.google.com/p/open-ig/issues/detail?id=526))
  * Statistics related crash in autobattles fixed ([Issue #528](http://code.google.com/p/open-ig/issues/detail?id=528))
  * Notification history is now always ordered by date ([Issue #530](http://code.google.com/p/open-ig/issues/detail?id=530))
  * You can now attempt to call for reinforcements anytime during level 2 ([Issue #531](http://code.google.com/p/open-ig/issues/detail?id=531))
  * Main menu language selection buttons removed for now ([Issue #532](http://code.google.com/p/open-ig/issues/detail?id=532))
  * New main menu logo "Open Imperium Galactica" by Norbert ([Issue #534](http://code.google.com/p/open-ig/issues/detail?id=534))

# Earlier #

See [earlier notes](ReleaseNotesPre093.md).