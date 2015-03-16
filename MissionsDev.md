**Note that this page is a developer checklist for the missions and may not reflect the actual implemented gameplay.**



# Level 1: Lieutenant #
## Mission 1 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Objective: Rebuild Achilles

Success condition: **original: N/A**

Failure condition: Achilles gets destroyed. **Fired!**

## Mission 2 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Objective: Protect the random trader ships from pirates

Success condition: **original: all traders saved?**

Failure condition: **more than one trader lost?**

**Fired when all traders lost?**

Video: random trader under attack

Remark: attack probably happens once per week, has TTL of several days

### Task 1..3 ###

Individual tasks for traders.

Rewards: 500 cr, 1000 cr, 5000 cr

Last attack will contain a pirate destroyer

Remark: interleaved with Mission 3 and 4

## Mission 3 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Objective: Escort a cargo ship "Transport"

Success condition: **ship not destroyed**, reward: Shield1

Failure condition: **ship destroyed**

Video: Escort cargo ; Merchant attacked ; Merchant destroyed

Remark: Pirate attack near San Sterling

## Mission 4 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Objective: Resolve pirate battle near Naxos

Success condition: **single pirate ship survived**

Failure condition: **single pirate ship destroyed**

Video: Naxos unknown ships?

Remark: the single "Rebel" pirate ship will be your ally in the battle.

## Mission 5 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Objective: Cover the Thorin's retreat from Garthog forces

Success condition: **Thorin survives**

Failure condition: **Thorin lost**, **Fired!**

Video: Request reinforcements: Success: +3 Fighter 1
douglas\_thorin\_escort, douglas\_thorin\_escort\_reinforcements

Movie: thorin\_escort

Remark: if you saved the pirate in Mission 4, you get single-time help in the defense.

# Level 2: Captain #

  * + Cruiser 1 without radar
  * + 2 Fighter 2
  * + Centronom
  * + New Caroline

## Mission 6 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Sequence: 1

Objective: Defend Achilles from Garthog surprise attack

Notes:
  * scripted fleet attacks Achilles after a few days in the rank
  * Ground units will attack cheapest buildings first
  * Do not let the user produce Cruiser 1

Success condition: Garthogs defeated (either in orbit or on ground)

Failure condition: Achilles taken by Garthog. **Fired!**

## Mission 7 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

### Task 1 ###

Sequence: 2

Objective: Defend trader from Garthog pirates

Video: merchant\_under\_attack\_garthog

Reward: 5000 cr


### Task 2 ###

Sequence: after first virus infection

Objective: Defend trader from Garthog pirates again

Reward: Good relations with FreeTraders later on in the game

## Mission 8 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Sequence: 3

Objective: Complete the test

Notes:
  * Activated by going to the bridge
  * Flashbacks starts after this by 2 months

## Mission 9 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Sequence 4:

Objective: Deal with the San Sterling Smuggler

Success condition: either destroyed or smuggler flees

Failure condition:
  * smuggler reaches the planet **san\_sterling\_smuggler\_escaped**
  * any other trader destroyed by you **san\_sterling\_smuggler\_killed\_innocent**


Video: san\_sterling\_smuggler, san\_sterling\_smuggler\_killed, merchant\_in

## Mission 10 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Sequence: 5

Objective: Escort governor of Centronom

Success condition: **original: always**, ensure safe arrival

Failure condition: governor's fleet destroyed

Video: douglas\_escort\_centronom\_governor ; colony\_ship\_arrival

Notes:
  * Appears at San Sterling, heading to Naxos

## Mission 11 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Sequence: 6

Objective: Defend the planet from Garthog attack

Success condition: planet saved

Failure condition: planet lost

Notes:
  * Garthog will choose the least defended planet
  * Display send messages according to the chosen planet

## Mission 12 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Sequence: 7

Objective: Virus infection on New Caroline

Success condition: planet survived

Failure condition: planet lost

Video: new\_caroline\_virus, new\_caroline\_virus\_again, etc.

Notes:
  * Report the infection to colonel message
  * Receive hubble2 on Naxos and San Sterling
  * Turn back traders. **Need ability to choose a target from overlapping units!**

### Task 1 ###
First virus infection

### Task 2 ###
Subsequent virus infection

### Task 3 ###
Subsequent virus infection, unless reported to colonel

### Task 4 ###
Subsequent virus infection, unless reported to colonel

### Task 5 ###
Subsequent virus infection, unless reported to colonel

## Mission 13 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Sequence: 9

Objective: Escort admiral Benson to New Caroline

Failure condition: Not met with Benson

Video: douglas\_escort\_admiral\_benson ; douglas\_escort\_admiral\_benson\_failed ; colony\_ship\_arrival\_2

Reward: 5 Laser ?

Notes:
  * Admiral's fleet starts off from Achilles
  * Attacked by Garthog fleet containing one Battleship


## Mission 14 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Sequence: 10

Objective: Destroy the virus carriers

Success: virus carriers destroyed

Failure: **N/A**

Notes:
  * Becomes available after Task 2 and report to colonel, or after task 5

## Mission 15 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Notes:
  * See spy movie
  * Talk to Kelly in the bar

## Mission 16 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Objective: Escort money carrier

Notes:
  * 2 weeks later
  * New Caroline -> San Sterling
  * Attacked by few Garthog fighters and destroyers

video: flagship\_arrival (maybe swap with Benson's video, as this is not a flagship as banson's ship is it)

## Mission 17 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Objective: Stop the stolen prototype

Reward: Move to level 3

Notes:
  * 1 week after money escort
  * Appears near New Caroline -> Garthog N?
  * Garthog: 2 Battleships, few destroyers, fighters, 1 Human Destroyer 2

# Level 3: Commander #

Notes:

  * +1 Flagship with 4 light tanks

## Mission 18 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Objective: Defeat the Garthogs

### Task 1..5 ###

Objective: Capture Garthog N

## Mission 19 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Objective: Blockade of Zeuson

Success condition: Rebel governor turned back

Failure condition: Rebel governor escaped or **killed?**

Notes:

  * After 4-5 days, a FreeTraders "Unknown" will leave Zeuson, should flee

Video: take\_prisoner

## Mission 20 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Objective: Talk to Brian

Notes:
  * Flashback after zeuson blockade

## Mission 21 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Objective: Escort prototype

Reward: Destroyer 2 available

Notes:
  * 2 weeks after blockade
  * Zeuson -> somewhere right
  * Attacked by 1+10+30 garthog fleet

# Level 4: Admiral #

Notes:
  * +Thorin, 6 light tanks, 1 Cruiser 1, 6 Fighter 2

## Mission 22 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Objective: build, research, colonize

Success condition: Own 7 more planets (either colonization or capture)

Reward: Promotion to Grand admiral

## Mission 23 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

### Task 1 ###
Objective: Talk to the doctor in the Bar

### Task 2 ###

Objective: Contact the Earth development center of robotics

Notes:
  * Flashback after 2-3 days of promotion

## Mission 24 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Objective: Make first contact with the Dargslan

Reward: Promotion to Grand admiral

Notes:
  * either the planet count, or the first battle with the Dargslan will trigger Level 5

# Level 5: Grand Admiral #

## Mission 25 ##

Status: ![http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png](http://open-ig.googlecode.com/svn/trunk/open-ig/src/hu/openig/gfx/ok.png)

Objective: Defeat the Dargslan

Success condition: Dargslans own 0 planets