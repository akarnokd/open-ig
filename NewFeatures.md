

# Introduction #

This page describes the newly added features of the Open Imperium Galactica. This includes graphical and game enhancements.


# Details #

## Planet status icons, taxation level ##
![http://karnokd.uw.hu/feat-icons-1.png](http://karnokd.uw.hu/feat-icons-1.png)

To allow easy management of planets the status icons have been introduced on various screens. These status icons allow the player to instantly recognize the problems with various planets.

![http://karnokd.uw.hu/feat-icons-2.png](http://karnokd.uw.hu/feat-icons-2.png)

Also, the starmap screen's information area contains the same icons. Also the taxation level is displayed in the population info line.

![http://karnokd.uw.hu/feat-icons-3.png](http://karnokd.uw.hu/feat-icons-3.png)

The planetary surface screen includes these icons along with the continuous display of the current planet.

The Open-IG utilizes the following icons to signal some errors:
| **Icon** | **Description** |
|:---------|:----------------|
| ![http://karnokd.uw.hu/energy-icon-b.png](http://karnokd.uw.hu/energy-icon-b.png) | The planet has some energy issues |
| ![http://karnokd.uw.hu/food-icon-b.png](http://karnokd.uw.hu/food-icon-b.png) | The planet lacks food |
| ![http://karnokd.uw.hu/hospital-icon-b.png](http://karnokd.uw.hu/hospital-icon-b.png) | There are not enough hospitals |
| ![http://karnokd.uw.hu/house-icon-b.png](http://karnokd.uw.hu/house-icon-b.png) | The planet has not enough living space |
| ![http://karnokd.uw.hu/worker-icon-b.png](http://karnokd.uw.hu/worker-icon-b.png) | There is not enough population to operate all buildings perfectly |

## Colony info on the planet surface ##

![http://karnokd.uw.hu/feat-colinfo-1.png](http://karnokd.uw.hu/feat-colinfo-1.png)

Displays all information available on the Colony info screen in a compact format. The semi-transparent panel can be toggled on/off by clicking on the bar right to it.

The status coloring is similar to the original game:
| **Color** | **Description** |
|:----------|:----------------|
| Green | Everything is OK |
| Yellow | The demand is higher than the available amount, but still less thant two times the available amount (e.g buildings can be operated at least 50%) |
| Red | The demand is more than twice the available amount |

## Worker demand ##

![http://karnokd.uw.hu/feat-workers-1.png](http://karnokd.uw.hu/feat-workers-1.png)

Displays the amount of demanded / available workers on a planet.

## Colony info problems ##

![http://karnokd.uw.hu/feat-colprobl-1.png](http://karnokd.uw.hu/feat-colprobl-1.png)

The small planetary info area on various information screens now includes the list of problems on that planet. The coloring is the same as for the **Colony info on the planet surface** above, except the OK cases are not listed

## Problematic planet highlight on the Planets info page ##

![http://karnokd.uw.hu/feat-blink-1.png](http://karnokd.uw.hu/feat-blink-1.png)

Planet names with any problem blinks on the Planets information page.

## Customizable energy and worker allocation ##

![http://karnokd.uw.hu/feat-alloc-1.png](http://karnokd.uw.hu/feat-alloc-1.png)

In the original game, if there was a worker or energy shortage, the game automatically distributed the available resources equally among buildings, resulting in the same operation level for every one. This allocation strategy might be sub-optimal in various cases. Therefore, new and selectable strategies have been introduced as follows:

| **Name** | **Description** |
|:---------|:----------------|
| Uniform | The original game strategy, applies the same allocation percentage to every building |
| Living conditions | Prefers life enhancing buildings (housing, entertainment, hospitals etc.). Boosts morale. |
| Production | Moves resources to the production buildings to maximize capacity in space, equipment and weapons factories |
| Science | Moves resources to scientific buildings to ensure research is not interrupted |
| Economy | Moves resources to the financial and economic buildings to maximize trade income |
| Military | Allocates resources to military and defensive buildings (barracks, radars, guns, shields, etc.) to maximize defensive capability of the planet |
| Operation | Uses heuristic approach to maximize the number of operational buildings on the planet. |

## Fleet firepower ##

![http://karnokd.uw.hu/feat-firepower-1.png](http://karnokd.uw.hu/feat-firepower-1.png)

Beneath the fleet speed value, the current firepower is displayed for the currently selected fleet. Later on, it might be possible to see the enemy fleet's firepower if it is in the range of any Radar3.

## Player planet listing on starmap ##

![http://karnokd.uw.hu/feat-planets-1.png](http://karnokd.uw.hu/feat-planets-1.png)

Due the resizable window of Open-IG, the space gained by resizing the starmap vertically allows to display and choose from a list of planets instead of just one planet name.

## Research info enhancements ##

![http://karnokd.uw.hu/feat-res-1.png](http://karnokd.uw.hu/feat-res-1.png)

Added some color coded info about the currently operational and total amounts of research centers to the research information screen's lab level property. Yellow number means that there are some non-operational research centers on the player's planets. Beneath the lab level, the total number of research centers are displayed for each kind.

![http://karnokd.uw.hu/feat-res-2.png](http://karnokd.uw.hu/feat-res-2.png)

The coloring is also visible on the Research screen.

In addition, the requirements listing received coloring to, which resembles to the coloring of the main research item listings:

| **Color** | **Description** |
|:----------|:----------------|
| Orange | Already researched |
| Yellow | Research in progress |
| Green | All prerequisite technologies have been researched, research can be started |
| Gray | Not all prerequisite technology is available |

This coloring is also used on the Research screen's **Needed** listing. Plus, the user can now click on the listed prerequisite and it jumps automatically to that research.

## Available/Total production capacity ##

![http://karnokd.uw.hu/feat-prod-1.png](http://karnokd.uw.hu/feat-prod-1.png)

On the production screen, the capacity display now contains the total-theoretical capacity of the manufacturing. The actual capacity is color coded and might be lower due some inoperable buildings. The same color coding concept is used as elsewhere:

| **Color** | **Description** |
|:----------|:----------------|
| Green | The maximum capacity is available |
| Yellow | More than half of the maximum capacity is available |
| Red | Less than half of the maximum capacity is available |

## Keyboard shortcuts ##
The game uses several keyboard shortcuts and has some new ones for testing purposes. Most of these shortcuts will not be available in the final release of Open-IG.

| **Shortcut** | **Description** |
|:-------------|:----------------|
| F2 | Go to the Starmap screen |
| F3 | Go to the Planet surface screen |
| F5 | Go to the production screen |
| F6 | Go to the research screen |
| F7 | Go to the information screen. The displayed tab depends on the original screen |
| ESC | Pause and go to the options screen or cancel movie playback |
| CTRL+K | Display all planets on the starmap in the discovered state (e.g. no name and limited planet info) |
| CTRL+N | Display all planets on the starmap in the well known state (e.g. name, population, surface, etc.) |
| CTRL+F | Display all currently existing enemy fleets on the starmap |
| NUM+ | Go to next planet |
| NUM- | Go to previous planet |
| CTRL+O | Take ownership of all empty planets |
| CTRL+. | Toggle rendering interpolation between linear, bilinear and bicubic |
| CTRL+C | Remove all buildings from the current planet |
| DEL | Delete (demolish) the currently selected building |
| CTRL+S | Save the current building layout into an XML file named by the planet |
| CTRL+D | Toggle between 0% and 60% damage level for the currently selected building |
| CTRL+M | Take over the currently selected planet |
| CTRL+P | Save building list into a text file used for resource allocation optimization |
| CTRL+SHIFT+O | Take over all planets on the starmap (enemy and empty too) |
| CTRL+I | Take over all enemy planets on the starmap |
| CTRL+B | Toggle building completion level between 0% and 100% for the currently selected building |
| CTRL+R | Make available (researched) the currently selected technology |
| CTRL+A | Increase the inventory amount by 1 of the currently selected and available technology |