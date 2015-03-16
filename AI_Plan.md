Just a bunch of rules, ideas, requirements to properly design a Hybrid GOAP system. The original game's AI behavior and algorithm is unknown. Probably it used some state machine and random task arbitration, along with cheating the planet and world simulations.



# Score/priority system evaluation considerations #

## Fleets ##

  * hitpoints damage ratio (ignoring shields)
  * attack/defense ratio
  * speed
    * -> time to meet with the target
      * planet: distance/speed
      * fleet: need for good orbit estimation

## Buildings ##
  * cost
    * -> build time
  * available space
  * when built, what will be its efficiency
    * for energy, build power plant(s) before?

## Production ##
  * cost
  * speed
    * -> time of completion
  * case when build capacity / step > cost -> wasted capacity if build order is 1

## Research ##
  * base cost
  * speed 12.5% - 200% based on investment
    * -> completion time can be adjusted

## Colony health ##

  * current production efficiency on the planet
  * expected tax and population values

Note: maybe exact effect-functions can be derived from the simulation mechanics to support this (e.g., f(Tax, Population) -> NewPopulation

# Enemy attacking fleet detected #

Remark: Target is known from the fleet itself. Need to compensate the human player

## Targets a planet ##

Own fleet actions:
  * Intercept fleet in space
    * avoid meeting point over one of the enemies fleet
    * potentially meet over one of our planet with space ground defenses
  * Reinforce target planet in orbit
  * Use multiple fleets

Planet's actions:
  1. Deploy tanks from inventory
    * and/or Produce if enough time
  1. Deploy stations from inventory
    * if military spaceport available
      * build station if enough time
    * if no military spaceport available
      * (build stations and military spaceport) if enough time
  1. Build more guns if enough time
  1. Build a shield if enough time
  1. Ensure there is enough power to the defenses
    * build power plant
    * turn off other buildings

## Targets a fleet ##

Own fleet actions:
  * Do not react
  * Calculate interception with current headings
    * if over one of the enemy's planet, choose flee...
  * If already damaged, move to repair
  * Flee to a planet with strong defenses
    * Consider upgrading the target planet's defenses in the mean time (see planet actions above)
  * Another fleet move to intercept (see fleet actions above)

# Peace time #

## Research ##

  * list of technology with tech prerequisites met
  * if enough labs, do it
  * if total lab count <= planet count
    * demolish and rebuild labs according to it
      * A`*` or another planning layer would be beneficial here
        * reduce rebuild amount
        * anticipate new tech becoming researchable
  * else colonize new planet(s)
  * or else capture planet(s)
    * having the lab already built
  * consider partial research
  * consider mixing the rebuild, capture and colonize

## Production ##

  * limits on concurrent production:
    * maybe limit to 5 like in the UI
    * unlimited parallelism? -> speed distribution still in effect. Creating 10 items parallel will be very slow
    * always single product?
      * preemption via zero priority (pauses running production)

## Exploration ##

  * deployed fleet with colony ship
    * produced colony ship
      * researched colony ship
  * do we need to have mixed fleets?
  * algorithm to discover the galaxy:
    * split the galaxy map into squares, move to each of these squares
      * radial-circular exploration?
      * square size is sqrt(2) `*` radar1 radius

## Diplomacy ##

I = incoming call, O = outgoing call

  * Ask for money (I/O)
    * cause: won't consider an attack for X days
    * Maybe goal: get more money
  * Improve relations (I/O)
    * reduce likelihood of attack
  * Trading agreement (I/O)
    * reduce likelihood of attack
  * Ally against (I/O)
    * focus on common enemy when deciding who to attack
  * Ally against dargslan (I)
    * focus on dargslans specifically
  * Ask for surrender (O)
    * objective: keep as many of the enemy's defensive structures intact
  * Resign (O)
    * cosmetic response? what is the gain of this decision?
  * Ask for peace (O)
    * reduce likelihood of attack, can focus on other enemies
    * if overwhelmed
  * Declare war (O)
    * issued if the given enemy seems to be weak enough
    * compute how many fleets and tanks to produce to take most of the enemy's planets

## Colony ##

  * Counteract population decrease
    * reduce taxes
    * build morale improving buildings (Police for aliens, Bar&Stadium for humans)
  * Build for special demands
    * Fire department
    * Stadium
    * Colony Hub
  * Build for common shortages
    * energy
    * living space
    * food
    * police

# Attacking enemies #

  * target's attack/defense ratio
  * decision
    * attack with current forces
      * multiple waves
    * buildup forces first
  * Note: human player weak at the beginning, too easy target