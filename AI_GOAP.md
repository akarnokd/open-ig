

# Goals and actions #

## Goal: Discover a planet ##
Options:
  * Explore via fleet X
    * Preconditions:
      * fleet has radar >= 1
    * Actions:
      1. select an undiscovered cell
      1. move fleet X to cell center
    * Effects:
      * Planet(s) X... discovered
  * Surface radar of planet Y
    * Preconditions:
      * current radar doesn't reach into an undiscovered cell
      * can afford radar type R
      * has room for radar type R
    * Actions:
      * Build radar type R
      * Wait one radar turn
    * Effects:
      * Planet(s) Z... discovered
  * Orbital radar over Y
    * Preconditions
      * Y has no orbital radar yet
      * orbital radar in inventory
    * Actions:
      * deploy orbital radar
      * wait one radar turn
    * Effects:
      * Planet(s) Z... discovered

## Goal: Own planet X ##
Options:
  * Colonize with fleet Y
    * Preconditions:
      * has fleet with Colony ship
      * planet X is empty
    * Actions:
      * move fleet to planet
      * onFleetArrivedAtPlanet
        * if goal state still satisfied, do colonization
      * build Colony hub
    * Effects:
      * planet X owned
      * has room for lab Z<sub>i</sub>
      * has room for factory F<sub>i</sub>
  * Conquer with fleet Y
    * Preconditions:
      * has surface vehicles
      * can defeat space defenses
    * Actions:
      * attack planet with fleet
    * Effects
      * planet X owned
      * (depending on X and knowledge)
        * has room for lab Z<sub>i</sub>
        * provides lab Z
        * has room for factory F<sub>i</sub>
        * provides factory F<sub>i</sub>

## Goal: Have +1 of product type X ##
Options:
  * Produce X
    * Preconditions
      * Technology X known
    * Actions:
      1. Place build order
      1. onProductionComplete
    * Effects
      * Inventory of X +1
      * X available in inventory

## Goal: Know technology X ##
Options:
  * Conquer a planet Y (for building technologies only)
    * Preconditions:
      * Planet Y buildings known
      * Planet Y has building X
    * Actions:
      * own planet Y
    * Effects
      * technology X known
  * Perform research
    * Preconditions:
      * technology prerequisites met
      * has lab configuration of Z<sub>i</sub>
    * Actions:
      * start research X
      * onResearchStateChange == COMPLETE -> Success
    * Effects:
      * technology X known

## Goal: Have lab configuration of Z<sub>i</sub> ##
Options:
  * Add lab Y to planet X
    * Preconditions
      * planet X lab limit not reached
    * Actions:
      * Build lab Y on planet X
      * onBuildingComplete -> Success
    * Effects
      * Lab count of Y +1
  * Replace lab on planet X with Y
    * Preconditions
      * Existing lab X is excess in the target configuration
    * Actions:
      * Demolish existing lab
      * Build lab Y
      * onBuildingComplete -> Success
    * Effects:
      * Lab count of Y +1
      * Lab count of X -1;

## Goal: have fleet with colony ship ##
Options:
  * Deploy into new fleet
    * Preconditions:
      * Colony ship available in inventory
      * Military spaceport built
    * Actions
      * Create new fleet
      * Deploy one colony ship into the fleet
    * Effects
      * Fleet with colony ship
  * Deploy into existing fleet
    * Preconditions:
      * Fleet with room
      * Military spaceport built
    * Actions:
      * Move fleet into orbit of a planet with military spaceport
      * Deploy one colony ship into the fleet
    * Effects
      * Fleet with colony ship

## Goal: have fleet which can defeat space defenses of X ##
Options:
  * Join fleets
    * Preconditions:
      * Has multiple fleets
    * Actions:
      * Move fleets together
      * Transfer units
    * Effects
      * Fleet attack increased
      * Fleet defense increased
  * Create new fleet
    * Preconditions:
      * Inventory available
      * Military spaceport available
    * Actions
      * Create new fleet
      * Deploy units
    * Effects
      * Fleet attack increased
      * Fleet defense increased
  * Deploy into existing fleet
    * Preconditions:
      * A fleet
      * Inventory available
      * Military spaceport available
    * Actions:
      * Move fleet into orbit
      * Deploy units
    * Effects
      * Fleet attack increased
      * Fleet defense increased