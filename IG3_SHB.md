I always wanted to make my own (IP) 4X game similar to Imperium Galactica. Unfortunately, I'm a software developer, and a game of such magnitude requires graphic designers, sound designers and story designers besides the general UI and mechanics. Of course, it can't be called IG3 or similar because of the name rights.

This page summarizes some of my thoughts and requirements of the game.



# Game technology #

  * Multi-platform: Windows, Linux & Mac
  * 3D
  * Scalable UI (don't want to read 5 pixel labels)
  * modding and customization
  * DLC-like system for complete new campaigns
  * Campaign editor, map editor

# Story, races #

  * ?
  * Unique ships and buildings (e.g., no cheap IG2)
  * Bar (Starcraft 2 style), to pick up unique missions
  * Unique missions like in IG1 and IG2
  * Skirmish, multiplayer
  * Ability to choose perks and traits
    * limited in the campaign
  * Campaign for multiple races (at least 3)

# Resources #

  * Primary resource is credits
  * Asteroid & Planet mining
    * Metal: for ship construction
    * Precious metals: for electronic equipment
    * Light elements: for fusion/anti material?
    * Water?
  * Food/Water?

# Model, mechanics #

  * Money:
    * Taxation of colonists
      * Non-working population provides only consumption tax
      * Workers provide more tax
    * Taxation of traders (e.g., if they land, not on a daily basis)
    * Finer grained tax percentages.
    * Income is computed on the integral of morale (e.g., not just the last second demand stances, if you have temporary energy shortage, you get a bad evaluation in IG)
  * Population
    * More realism: only certain percent of the population is available for work
  * Own trading fleets???
    * transfer of resources and population
  * Ships and fleets require personnel
  * Orbital factories (capacity) for large ship construction
  * Realism: Solar plants work only in daylight, see building shifts

# Planets #

  * Usual: Earth, Rocky, Desert, Mars-like, Icy
  * New: volcanic
    * rich in resources
  * New: Gas giants
    * mining light elements
  * New: Water planets
    * no buildable surface, restricted buildings
  * New: Machine-planets?
    * have a mechanical look
  * Moons, asteroids
    * Mining
    * Space defenses only
  * Individual planet sizes with different gravity, affects population
    * Individual day/night cycles
      * Galactic standard time and local time
      * Effect: infra view for night attacks?
  * Proper solar system with planets, asteroid belts and moons
    * Planets may be discovered similarly while moving within the star system
  * Planets may become unstable, see colony transfer ships
  * Explicitly abandon planets
  * Abandoned/Died planets retain their buildings for some time
    * new colonizers may get technology and resources

# Buildings #

  * Usual: colony hub, housing, food, entertainment, energy, factory, research, barracks, guns, shields
  * New: school -> improve production performance
  * New: university -> improve research performance
  * New: outpost -> smaller defense building
  * New: Ability to manually set the energy and worker level per building
  * New: Mining station
  * New: some underwater buildings for full-water planets, mostly mining/research planets?!
    * guns, military and traders spaceport have surface ports
  * New: Windfarm
  * New: orbital solar power station and ground station?
  * Operational shifts: set when the building should be operational???
    * production may be 24/7, but research is not

# Ships #

  * Equipment customization, more arbitrary, e.g., create a ship with 50 lasers so long the power holds
  * New: reactor, provides power to equipment and drive
  * New: hull strength improvements
    * double plating, triple plating, ablative plating
    * hull repair, hull regenerator
    * may consume energy, increases mass -> combat speed
  * Personnel, personnel loss on damage
    * transfer personnel?
    * Ship may be inoperational if low on personnel
  * Fleet leaders with bonuses (IG2 style)
  * Main fleet as you
  * Salvage fleets for resources and technology?
  * Special ship types:
    * Colony ship (medium, large)
    * Colony transfer ships: move large population from one planet to other
      * In case a planet becomes unlivable
    * Planet explorer
      * detect solar systems, planets

# Warfare #

  * Space battle
  * Ground battle
    * Orbital strikes, damages area!
      * COD AC 130 style FPS mode???
    * Air units (e.g., fighters)
    * Anti air units
  * Underwater battle?

# Diplomacy #

  * Trade agreements over resource exchange
  * Ask ally for help attack or defend a planet
    * i.e., attack space defenses and nearby enemy fleets
    * joint space battle
    * joint ground battle

# Other #

  * Don't overwhelm the player with economic complexity
  * AI support for most areas (construction, planet management, trade routes, etc.)
  * Less cluttered interface, more generic UI controls