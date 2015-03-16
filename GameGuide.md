

# Mouse and keyboard #
## Keyboard shortcuts ##

| **Key** | **Screen** | **Description** |
|:--------|:-----------|:----------------|
| ESC | Movie | Skip current movie |
| ESC | Colony | Cancel build mode |
| Arrow keys | Colony, Starmap | Move the map |
| ALT+ENTER | Any | Toggle windowed and full screen mode |
| CTRL+1 | Bridge | Switch to Game Level 1 |
| CTRL+2 | Bridge | Switch to Game Level 2 |
| CTRL+3 | Bridge | Switch to Game Level 3 |
| CTRL+4 | Bridge | Switch to Game Level 4 |
| CTRL+5 | Bridge | Switch to Game Level 5 |
| CTRL+7 | Any | Set difficulty to EASY |
| CTRL+8 | Any | Set difficulty to NORMAL |
| CTRL+9 | Any | Set difficulty to HARD |
| CTRL+N | Colony | Toggle display of building names |
| CTRL+B | Colony | Toggle build mode |
| S | Colony, Ground war, Spacewar | Stop selected units |
| BACKSPACE | Ground war, Spacewar | TEST: Quit the battle instantly |
| A | Starmap | Fleet attack mode |
| M | Starmap | Fleet move mode |
| F1 | Any | Bridge screen |
| F2 | Any | Starmap screen |
| F3 | Any | Colony screen |
| F4 | Any | Display equipment overlay screen |
| F5 | Any | Display production overlay screen |
| F6 | Any | Display research overlay screen |
| F7 | Any | Display information overlay screen (empty) |
| F8 | Any | Database |
| F9 | Any | Bar (Game Level 2 or above) |
| F10 | Any | Diplomacy screen (Game Level 4 or above) |
| F11 | Any | Statistics screen |
| F12 | Any | Achievements screen |
| Plus/Minus | Any | Move to next/previous player colony |
| X | Any | Show or hide quick research panel. |
| C | Any | Show or hide quick production panel. |
| CTRL+X | Any | Toggle between fixed 30 frame/sec rendering and unrestricted frame/sec rendering modes |
| CTRL+Y | Any | Toggle UI debug mode. |
| CTRL+R | Colony | Toggle weather effects. |

## Mouse gestures ##

| **Gesture** | **Screen** | **Description** |
|:------------|:-----------|:----------------|
| Click | Any overlay screen | Click outside the overlay screen (e.g., the grayed area) to hide the overlay screen |
| CTRL+Mouse Wheel | Starmap<br>Colony <table><thead><th> Zoom In/Out </th></thead><tbody>
<tr><td> Mouse Wheel </td><td> Starmap<br>Colony </td><td> Scroll up/down vertically </td></tr>
<tr><td> Mouse Wheel </td><td> Information: Planets </td><td> Move to next/previous planet </td></tr>
<tr><td> Mouse Wheel </td><td> Information: Buildings </td><td> Move to next/previous building </td></tr>
<tr><td> Shift+Mouse Wheel </td><td> Starmap<br>Colony </td><td> Scroll left/right horizontally </td></tr>
<tr><td> Left Click </td><td> Minimap<br>Radar </td><td> Move viewport to the location </td></tr>
<tr><td> Drag with right mouse button </td><td> Starmap<br>Colony </td><td> Pan the viewport </td></tr>
<tr><td> Middle Click </td><td> Colony </td><td> Reset view to 100% zoom and center the view </td></tr>
<tr><td> Double Click </td><td> Starmap </td><td> Go to Colony </td></tr>
<tr><td> Shift+Right Click </td><td> Spacewar<br>Groundwar </td><td> Move to location </td></tr>
<tr><td> Ctrl+Right Click </td><td> Spacewar<br>Groundwar </td><td> Attack target </td></tr>
<tr><td> Right click </td><td> Starmap, Spacewar, Colony </td><td> <b>Classic mode</b>: Move to or attack enemy </td></tr>
<tr><td> Middle button drag </td><td> Starmap, Spacewar, Colony </td><td> <b>Classic mode</b>: pan the maps </td></tr>
<tr><td> CTRL+Middle button </td><td> Starmap, Spacewar, Colony </td><td> <b>Classic mode</b>: zoom to fit. </td></tr></tbody></table>

<h1>Gameplay mechanics</h1>

<h2>Colony status icon</h2>

In order to help manage the colonies and their needs, status icons have been introduced. These icons are visible on the starmap, on the colony and on many information screen tabs. The following table summarizes these icons and their meaning.<br>
<br>
<table><thead><th> <b>Icon</b> </th><th> <b>Description</b> </th></thead><tbody>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/colony-hub-icon.png' /> </td><td> Missing colony hub </td></tr>
<tr><td><img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/colony-hub-icon-dark.png' /> </td><td> Inoperable colony hub </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/house-icon-dark.png' /> </td><td> Minor house shortage (<code>demand &lt; available * 2</code>) </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/house-icon.png' /> </td><td> Major house shortage (<code>demand &gt;= available * 2</code>) </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/energy-icon-dark.png' /> </td><td> Minor energy shortage (<code>demand &lt; available * 2</code>) </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/energy-icon.png' /> </td><td> Major energy shortage (<code>demand &gt;= available * 2</code>) </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/worker-icon-dark.png' /> </td><td> Minor worker shortage (<code>demand &lt; available * 2</code>) </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/worker-icon.png' /> </td><td> Major worker shortage (<code>demand &gt;= available * 2</code>) </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/food-icon-dark.png' /> </td><td> Minor food shortage (<code>demand &lt; available * 2</code>) </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/food-icon.png' /> </td><td> Major food shortage (<code>demand &gt;= available * 2</code>) </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/police-icon-dark.png' /> </td><td> Minor police shortage (<code>demand &lt; available * 2</code>) </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/police-icon.png' /> </td><td> Major police shortage (<code>demand &gt;= available * 2</code>) </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/hospital-icon-dark.png' /> </td><td> Minor hospital shortage (<code>demand &lt; available * 2</code>) </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/hospital-icon.png' /> </td><td> Major hospital shortage (<code>demand &gt;= available * 2</code>) </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/repair-icon-dark.png' /> </td><td> Construction in progress </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/repair-icon.png' /> </td><td> Damaged buildings </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/virus-icon.png' /> </td><td> Virus infection </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/stadium-icon.png' /> </td><td> Stadium required on human planets with <code>population &gt;= 50000</code> </td></tr>
<tr><td> <img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/fire-icon.png' /> </td><td> Fire department required on human planets with <code>population &gt;= 30000</code> </td></tr></tbody></table>

<h2>Building Upgrade System</h2>

The <b>new Building Upgrade System</b> has been introduced to solve two problems coming from the original game:<br>
<br>
<ul><li>Allow the player to meet demands (energy, food, etc.) even when the current planet has no more building space.<br>
</li><li>Fix the issue with the random power-outputs of power plants: Open-IG uses a constant for power and upgrades multiply the output to meet the initial demands of the alien planets.</li></ul>

A new panel has been added to the colony screen under the existing building information panel:<br>
<br>
<img src='http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-upgrade-panel.png' />

The user can click on the stars to upgrade a building to a certain level. You may only increase the upgrade level.<br>
<br>
Not all buildings offer four levels of upgrade.<br>
<br>
When an upgraded building is demolished, the upgrade level is considered: the player will receive the half of the total invested money.<br>
<br>
The general rule for the upgrade system is that each level will increase the output, the energy and worker demand of the building. Three exceptional case exist:<br>
<br>
<ul><li>Upgrading from Level 3 to Level 4 does not increase the energy and worker demand, but only the capacity.<br>
</li><li>Some buildings (e.g., Spaceship, Equipment, Weapons) increase capacity but their demands increase more rapidly: a necessary balance option.<br>
</li><li>Research centers do not increase their capacity, but instead, they reduce the energy and worker requirements: again, a necessary balance option.</li></ul>

<h2>Research</h2>

The research mechanics probably differs from the original game's behavior (I couldn't figure it out).<br>
<br>
Two independent factors are considered during the research.<br>
<br>
<ul><li>Research time depends on the research cost and allocated money. The default research time is the Research Cost value in game minutes. You may assing between 1/4 and 2 times the research cost, which will increase or reduce the research time inversely.</li></ul>

<pre><code>Research Time = (Research Cost / 10) / allocation multiplier<br>
</code></pre>

For example, a 6000 credit research would take 6000 game minutes. Game time advances in 10 minutes per step, and the screen displays 600 for convenience. On normal speed, this would take 600 seconds or 10 real minutes to complete. If you increase the research money to 12000, the research time decreases to 300 real seconds. If you decrease the money to 1500 credits, the research time jumps to 2400 seconds.<br>
<br>
<ul><li>The research progress depends on the percent of required and available research labs, but more labs of any kind will not mean greater percent.</li></ul>

For example, a technology uses 1 civil and 1 mechanical development centers. If you have 0 civil and 1 mechanical centers, the research will progress up to 50% and stop there. If you have 0 civil and 2 mechanical centers, the research will still go up to 50%.<br>
<br>
The Information / Research window offers several state information in the way the technology labels are colored. The meaning of the colors:<br>
<br>
<table><thead><th> <b>Color</b> </th><th> <b>Description</b> </th></thead><tbody>
<tr><td> Orange </td><td> Technology available for production / construction </td></tr>
<tr><td> Yellow </td><td> Research started (but may not be the active one) </td></tr>
<tr><td> Light green </td><td> The current amount of planets, if filled with proper labs would support the successful research </td></tr>
<tr><td> Green </td><td> There is simply not enough planets to complete the research if started </td></tr>
<tr><td> Light blue </td><td> There is enough labs built to start this research </td></tr>
<tr><td> Gray </td><td> Prerequisites of the research have been not met </td></tr></tbody></table>

<h2>Production</h2>

The production speed depends mainly on how many factories you have and what efficiency they operate. The production progress is proportional to this capacity.<br>
<br>
<pre><code>Production time = Cost * 50 / capacity<br>
</code></pre>

For example, you want to produce one fighter of cost 600 credits and you have 1000 spaceship capacity. The production will create a 20 credits worth of fighter per 10 ingame minutes, which translates to 30 real time seconds to complete a single fighter.<br>
<br>
Of course, if the capacity would be 60000, it would construct 2 fighters per real time seconds.<br>
<br>
<h2>Construction & Upgrading</h2>

The construction has a simple mechanic. A building has a hitpoint value equal to its cost. The construction increased 200 hit points per 10 ingame minutes.<br>
<br>
<pre><code>Construction time = Cost / 200<br>
</code></pre>

For example, constructing a colony hub of 40000 credits takes 200 seconds or 2000 ingame minutes (about 1 day and 9 hours).<br>
<br>
If you upgrade a building. It will switch back to 25% of construction level and is inoperable until it reaches its 100% completion again.<br>
<br>
<h2>Repairing</h2>

The repairing mechanics has a twist.<br>
<br>
If you have a <b>Fire Brigade</b> on your planet, it will repair your buildings up to 50% health (or more, depending on the ugrade level of the Fire Brigade) for free. If you have a heavily damaged building and turn the repair on manually, it won't cost you money until it reaches this 50% health. Starting from 50%, it works as a regular repair.<br>
<br>
The repair will improve 50 hitpoints for 20 credits per 10 ingame minutes.<br>
<br>
However, if the Fire Brigade is damaged itself, but still operational, it will only repair a proportion of this 50 hitpoints for free (at least 25 hitpoints, because it is the 50% efficiency mark).<br>
<br>
Regular repair is always fixing 50 hipoints per turn.<br>
<br>
The options screen features an option to automatically repair damaged buildings, therefore, you don't need to manually turn repairs on and off for each and every building. You may set a minimum required money amount for the automatic repair on this screen as well. This limit may help in resource constrained times when you want to preserve money for other tasks and not let the auto-repair drain your money.<br>
<br>
<h2>Auto-Build</h2>

One of the old game's nasty property was that it became really tedious to manage a more than 20 planet empire later on within the game. Imperium Galactica 2 made improvements on this by introducing an auto-build feature. This feature removed the burden of micromanage the planets whenever they report a food or hospital shortage.<br>
<br>
Therefore, a similar auto-build has been introduced into Open Imperium Galactica. You can toggle the auto-build on the colony information screen by clicking on the label. You can hold down the SHIFT key while clicking on the label to set the state on all of your planets.<br>
<br>
The build strategy is as follows:<br>
<ul><li>If a construction or worker shortage is present on the planet, do nothing. The first constraint prevents building indefinitely due the lack of statistical evidence. The second one is a heuristic, because if a planet suffers from worker shortage, adding another building would usually make things worse (less men to operate more buildings, some may fall below the 50% operation limit, causing even more shortages).<br>
</li><li>Handle energy shortages first, and don't handle other shortages yet. Usually, the root cause of a shortage may be simply the lack of energy.<br>
</li><li>Handle other shortages later but simultaneously.<br>
</li><li>Upgrade first, construct second: space is limited, and in long term, using the upgrade path reserves more space for later problems.<br>
</li><li>Upgrade multiple levels if the player has 30 times the required cost, upgrade a single level otherwise.<br>
</li><li>Try upgrading or constructing expensive buildings first.<br>
</li><li>If a new construction is required, consider the available build space. For example: if you could afford an Arcology but you only have room for Prefab housing, the auto-build will choose the latter.</li></ul>

The downside of the auto-build is that it may hold your overall progress back in the early game time where your biggest shortage will be the money.<br>
<br>
To avoid unnecessary draining of credits by the auto-build feature, you can set the auto-build money limit in the options / gameplay page. This limit won't let the auto-build proceed if you have less than the specified money. The feature can be used to ensure the auto-build always chooses the most expensive (and most effective) buildings, basically "waiting" until you gather enough money to build them, and not building the cheapest one due money shortage.<br>
<br>
<h2>Satellite survival</h2>

Placing satellites around alien and enemy planets will take their attention sooner or later and destroy it. The same is true for alien satellites around the planets you own.<br>
<br>
The better radar a planet has the faster satellites will be detected. The following table displays the survival time (ingame minutes) for each satellite, radar type and difficulty level:<br>
<br>
<table><thead><th> <b>Satellite</b> </th><th> <b>No radar</b> </th><th> <b>Radar 1.0</b> </th><th> <b>Radar 2.0</b> </th><th> <b>Radar 3.0</b> </th><th> <b>Hubble 2</b> </th></thead><tbody>
<tr><td> Satellite (EASY) </td><td> infinite </td><td> 48 hours </td><td> 24 hours </td><td> 16 hours </td><td> 12 hours </td></tr>
<tr><td> Satellite (NORMAL) </td><td> infinite </td><td> 24 hours </td><td> 12 hours </td><td> 8 hours </td><td> 6 hours </td></tr>
<tr><td> Satellite (HARD) </td><td> infinite </td><td> 12 hours </td><td> 6 hours </td><td> 4 hours </td><td> 3 hours </td></tr>
<tr><td> Spy Satellite 1 (EASY) </td><td> infinite </td><td> 432 hours </td><td> 216 hours </td><td> 144 hours </td><td> 72 hours </td></tr>
<tr><td> Spy Satellite 1 (NORMAL) </td><td> infinite </td><td> 216 hours </td><td> 108 hours </td><td> 72 hours </td><td> 36 hours </td></tr>
<tr><td> Spy Satellite 1 (HARD) </td><td> infinite </td><td> 108 hours </td><td> 54 hours </td><td> 36 hours </td><td> 18 hours </td></tr>
<tr><td> Spy Satellite 2 (EASY) </td><td> infinite </td><td> 960 hours </td><td> 480 hours </td><td> 320 hours </td><td> 240 hours </td></tr>
<tr><td> Spy Satellite 2 (NORMAL) </td><td> infinite </td><td> 480 hours </td><td> 240 hours </td><td> 160 hours </td><td> 120 hours </td></tr>
<tr><td> Spy Satellite 2 (HARD) </td><td> infinite </td><td> 240 hours </td><td> 120 hours </td><td> 80 hours </td><td> 60 hours </td></tr></tbody></table>

<b>Hubble 2</b> orbital radars placed around neighboring planets won't detect the devices if the target planet has no radar by itself (<i>you could say, they are much smaller than a fleet to be detectable</i>).<br>
<br>
<i>I'm not sure whether the original game had any sanction for these satellites in terms of diplomatic relations, because there was no way to recall the devices. In the current version, they are just destroyed.</i>

<h2>Earthquake</h2>

Earthquakes may happen on Earth-like or Rocky planets last <b>60 ingame minutes</b> (so you might catch it and see the shaking :). Earthquake affects buildings by reducing their hitpoints. The amount of hitpoints reduced depends on the building type:<br>
<br>
<ul><li>For energy producing buildings, the earthquake reduces their status by 20% of their total hitpoints. For example, a Nuclear Plant having 8000 points will be reduced to 6400 points.<br>
</li><li>Factory buildings loose 15% of their total hitpoints.<br>
</li><li>Other buildings take 10% damage, e.g., a Colony hub of 40000 points will be reduced to 36000 points.</li></ul>

Earthquakes will affect the late gameplay (currently, after 6 real hours) and its occurrence is governed by the difficulty level:<br>
<br>
<ul><li>EASY: 1 / year<br>
</li><li>NORMAL: 2 / year<br>
</li><li>HARD: 4 / year</li></ul>

When an earthquake happens, the player is notified and double clicking on the bottom status bar should bring up the colony affected.<br>
<br>
It is possible, that unattended planets might be completely destroyed by earthquakes.<br>
<br>
<i>Depending on playtester's response, the mechanics might change. Having earthquakes tear down the initial colonies at the start might be too much.</i>

<h2>Radar</h2>

<table><thead><th> <b>Radar type</b> </th><th> <b>Alien planet</b> </th><th> <b>Alien fleet</b> </th></thead><tbody>
<tr><td> No radar </td><td> Displays gray label,<br> display black surface and text about "Install satellite"  </td><td> Invisible </td></tr>
<tr><td> Radar 1 </td><td> Display "Occupied",<br> display building base only </td><td> display owner,<br> display count of non-fighter ships exactly </td></tr>
<tr><td> Radar 2 </td><td> ? </td><td> ? </td></tr>
<tr><td> Radar 3 </td><td> ? </td><td> ? </td></tr>
<tr><td> Survey satellite </td><td> Display owner,<br> display "Alien Empire",<br> display Space stations,<br> display buildings base only  </td><td> - </td></tr>
<tr><td> Spy satellite 1 </td><td> Installable on nameless planets,<br> display owner,<br> display building base only,<br> display space stations,<br> display population number </td><td> - </td></tr>
<tr><td> Spy satellite 2 </td><td> Installable on nameless planets,<br> display owner,<br> display buildings with name,<br> display space stations,<br> display population number,<br> display approximate vehicle and fighter counts </td><td> - </td></tr>
<tr><td> Hubble </td><td> ? </td><td> ? </td></tr>
<tr><td> Fleet without radar </td><td> Discover planet only </td><td> Show fleet owner as "Alien Fleet". </td></tr>
<tr><td> Fleet radar 1 </td><td> display gray label,<br> display "Install satellite" </td><td> display owner,<br> display count of non-fighter ships exactly,<br> display fighters as range </td></tr>
<tr><td> Fleet radar 2 </td><td> display gray label,<br> display "Install satellite" </td><td> display owner,<br>  display count of non-fighter ships exactly,<br> display fighters as range<br> </td></tr>
<tr><td> Fleet radar 3 </td><td> display owner,<br> display building bases only </td><td> display owner,<br> display count of non-fighter ships exactly,<br> display fighters as range </td></tr></tbody></table>

<h2>Attacking</h2>

(Since v0.94)<br>
<br>
You can attack alien planets and fleets on the starmap. You should select a fleet and click on the ATTACK button, hit the A key and left-click on the target or CTRL + Right Click on the target.<br>
<br>
The original game had a small inconvenience: if fleets and planets overlapped on screen, you could not attack and got a nice warning most of the time. In current Open-IG, you click will select an alien fleet/planet under the mouse pointer, or do nothing. (<i>Later on, I plan to show a small menu listing all the fleets and planets at the spot and you may choose one of them more easily.</i>)<br>
<br>
<h2>Taxation</h2>

Total tax income is the sum of planet tax incomes. The planet tax calculation depends on several factors:<br>
<br>
<ul><li>population count,<br>
</li><li>taxation level (0..100),<br>
</li><li>morale (0..100),<br>
</li><li>owner's total number of planets and<br>
</li><li>game difficulty (0, 1, 2).</li></ul>

There are 10 levels of taxation ranging from 0% to 100%, each with about 11% increase to the previous. For example, MODERATE level is 33%, VERY_HIGH is 66%.<br>
<br>
The planetary tax is calculated via the following formulas:<br>
<pre><code>base tax = population * taxation % * morale %<br>
<br>
(player) planet compensation = Max(planet count, 3) + difficulty - 2<br>
<br>
(ai) planet compensation = Math(planet count, 3) - difficulty<br>
<br>
tax compensation = 3 / Sqrt(planet compensation)<br>
<br>
tax = base tax * tax compensation<br>
</code></pre>

The following table lists a few compensation values for the player:<br>
<br>
<table><thead><th> <b>Planet count</b> </th><th> <b>EASY</b> </th><th> <b>NORMAL</b> </th><th> <b>HARD</b> </th></thead><tbody>
<tr><td> <= 3 </td><td> 3 </td><td> 2.12 </td><td> 1.73 </td></tr>
<tr><td> 4 </td><td> 2.12 </td><td> 1.73 </td><td> 1.5 </td></tr>
<tr><td> 5 </td><td> 1.73 </td><td> 1.5 </td><td> 1.34 </td></tr>
<tr><td> 6 </td><td> 1.5 </td><td> 1.34 </td><td> 1.22 </td></tr>
<tr><td> 10 </td><td> 1.1 </td><td> 1 </td><td> 0.95 </td></tr>
<tr><td> 20 </td><td> 0.7</td><td> 0.68 </td><td> 0.67 </td></tr>
<tr><td> 50 </td><td>  0.43 </td><td> 0.42 </td><td> 0.42 </td></tr>
<tr><td> 100 </td><td> 0.3 </td><td> 0.3 </td><td> 0.3 </td></tr></tbody></table>

This compensation feature should ensure a good money-bootstrap at the beginning and reduce the practically unusable money-boost at the end of the game.<br>
<br>
<h2>Rocket vs. ECM</h2>

You can equip your more advanced ships with ECM (Electronic Countermeasure), which has a chance to divert incoming rockets. The same ECM is available to most alien races.<br>
<br>
Note that scrambled rockets may target your own fleet even if you fired them and may devastate your fleet instead of the enemy. In this case, avoid firing rockets at the larger ships which may contain these ECM.<br>
<br>
Each rocket type has its own Anti-ECM measure which modifies the probabilities of such diversion. The following table summarizes the relation:<br>
<br>
<table><thead><th> <b>Anti-ECM \ ECM</b>  </th><th> <b>0</b>  </th><th>  <b>1</b>  </th><th>   <b>2</b> </th></thead><tbody>
<tr><td> 0 (e.g., Rocket 1)            </td><td> 100% </td><td> 15% </td><td>  0% </td></tr>
<tr><td> 1 (e.g., Rocket 2)             </td><td> 100% </td><td> 50% </td><td>  15% </td></tr>
<tr><td> 2 (e.g., Multi-headed rocket)            </td><td> 100% </td><td> 85% </td><td>  50% </td></tr></tbody></table>

For example, if you fire with Rocket 1 at an enemy without ECM, you have a 100% hit chance. However, if the enemy has ECM 2, your rocket will be always diverted.<br>
<br>
<h2>Trait system</h2>

<img src='https://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-traits-screen.png' />

The trait system is planned to provide some customization and replay values for those who do not want to customize a campaign via the Campaign Editor.<br>
<br>
<h1>Gameplay tips</h1>

<ul><li>Planetary accounting and statistics gathering are performed at midnight. Morale affecting factors such as shortages are considered exactly at this moment. Therefore, plan your building upgdares and reconstruction accordingly, i.e., do not start upgrading your Arcologies at 23:50, because you may get a massive morale loss due the temporary living space shortage.<br>
</li><li>If you have the money but not enough planets, you should start as many research as possible. They may not run to completion but you get a headstart.<br>
</li><li>If you want to place multiple buildings of the same type, hold SHIFT while placing a building.<br>
</li><li>Most listing offers the ability to scroll them via the mouse wheel. For example, you may scroll the building panel on the colony screen instead of clicking on the up or down button.</li></ul>

<h2>Cheats</h2>

If you run into difficult situations, want to skip parts or just want to poke around, here are some keyboard shortcuts that can help you. These are called cheats because it can break the balance or natural progress of the game.<br>
<br>
<table><thead><th> <b>Shortcut</b> </th><th> <b>Description</b> </th></thead><tbody>
<tr><td> CTRL+G </td><td> Add 10.000 credits </td></tr>
<tr><td> CTRL+SHIFT+G </td><td> Add 100.000 credits </td></tr>
<tr><td> CTRL+Z </td><td> Toggle virus infection on the current planet (own or enemy) </td></tr>
<tr><td> CTRL+H </td><td> Display mission debug window (might not show up properly in fullscreen) </td></tr>
<tr><td> CTRL+P </td><td> Switch to the player of the selected planet, i.e., you can play as Dargslan: build, produce, deploy, etc. </td></tr>
<tr><td> CTRL+V </td><td> Show all planets and fleets on the starmap </td></tr>
<tr><td> CTRL+D </td><td> Deploy all kinds of vehicles </td></tr>
<tr><td> CTRL+J </td><td> Toggle complete AI assist for the current player on or off </td></tr>
<tr><td> CTRL+I </td><td> Make the currently selected technology researched (if not already) and add 1 unit to the inventory </td></tr>
<tr><td> CTRL+O </td><td> Take ownership of the selected planet </td></tr>
<tr><td> CTRL+B </td><td> All known races will offer alliance (debug) </td></tr>
<tr><td> CTRL+SHIFT+B </td><td> All known races will ask for/demand surrender (debug) </td></tr>
<tr><td> CTRL+SHIFT+J </td><td> All known races will demand 1000 credits (debug) </td></tr>
<tr><td> CTRL+SHIFT+W </td><td> Destroy enemy ships during spacewar (except Traders) </td></tr></tbody></table>


<h2>Mission tips</h2>

<h3>Mission 1: Rebuild Achilles</h3>

<ul><li>Try not to sell any radar or the damaged buildings.<br>
</li><li>Repair the Fire Brigade, Nuclear Plant, Church and Prefab Housing first<br>
</li><li>Reduce tax until the morale grows back to 50%<br>
</li><li>Repair the military spaceport as it will repair your damaged fleets</li></ul>

<h3>Mission 2: Protect the traders</h3>

<ul><li>Easy except the third task.<br>
</li><li>Complete Mission 3 to receive a shield and use it against the last attackers.<br>
</li><li>In the last task, if the Pirate destroyer targets your Fighter, move the fighter around it in a circle so the laser doesn't hit it. Use the remaining ships to destroy the pirates.<br>
</li><li>Regenerate your fleet over a planet with military spaceport (e.g., Achilles if you kept the building)</li></ul>

<h3>Mission 3: Escort cargo</h3>

<ul><li>Easy.<br>
</li><li>Don't forget to equip the bonus shield.</li></ul>

<h3>Mission 4: Pirate battle</h3>

<ul><li>Make sure the center Pirate 1 survives the fight. He will help you in Mission 5, making the space battle there much easier.</li></ul>

<h3>Mission 5: Protect the Thorin</h3>

<ul><li>Pause after the cutscene and go immediately to the bridge to ask the Colonel for reinforcements.<br>
</li><li>Thorin has 4 rockets, use them<br>
</li><li>If you saved the pirate in Mission 4, he will bring in help.<br>
</li><li>Try not to lose your fighters (you should have 6), since it could take time to replenish them in Level 2.</li></ul>

<h3>Mission 6: Defend Achilles</h3>

<ul><li>Pause the game immediately after the cutscene.<br>
</li><li>Select the other planets, go into equipment (F4).<br>
</li><li>Undeploy a few tanks.<br>
</li><li>Switch to achilles via +/- keys. Deploy tanks to maximum.<br>
</li><li>Unpause the game and let the Garthog land its force