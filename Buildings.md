# Introduction #

How buildings (should) work in Open-IG.


# Details #

## build phase ##

The `Building.buildProgress` defines the current buildup level. It goes from 0 to `Building.type.hitpoints` with a build speed constant `Building.BUILD_SPEED` per game seconds.

Uncertain: `Building.type.hitpoints` equals to `Building.type.cost`?

During buildup, the surface map displays the scaffolding tiles for the particular `Building.techId`.

During construction, the Building.hitpoints and Building.buildProgress increases together

The in-construction buildings can be damaged. This is indicated in hitpoints less than buildProgress.

Damage indicator tiles are shown when the hitpoints reaches half of the building progress.

A building is completed when buildProgress reaches the Building.type.hitpoints

## Operation phase ##

Buildings operational resources might be `percentable`, meaning that the output depends on the amount of supplied worker and energy.

The exception are the energy production buildings, where only the assigned worker count is considered.

A building is considered inoperable when either the assigned energy or the assigned worker amount is less than half of the nominal values.

The building efficiency is the smaller percentage of the assigned/required.

Efficiency is considered zero when

  * `buildingProgress < type.hitpoints`; e.g., the building is incomplete
  * `hitpoints < type.hitpoints / 2`; e.g. the building is more than half damaged
  * `assignedWorker < workerDemand / 2`; e.g. less than half of the worker demand is present
  * `assignedEnergy < energyDemand / 2`; e.g. less than half of the energy demand is present (except for power plants)

In other case, the efficiency is the minimum of

  * `assignedWorker / workerDemand `
  * `assignedEnergy / energyDemand `
  * `hitpoints / type.hitpoints `

for example, a 100% worker and 100% energy but 75% health gives 75% efficiency.