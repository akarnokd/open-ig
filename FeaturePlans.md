

# Introduction #

On this page, I collect ideas about enhancing the gameplay with more modern concepts, better management ways or nicer gameplay mechanics. Suggestions and comments welcome.

# Quicksave with automatic versioning #

When using the quicksave function the game will automatically version the save files, e.g. no single quicksave slot as in many other games. This will be true for the daily autosave feature too. This helps during the development and testing a lot, plus, it might come in handy for the regular gamers too.

# Allow save during battles #

In the original version the player was not able to save an ongoing battle. Theoretically, it is as simple as the regular save. It might, however, affect the sense of the gameplay.

# Multiple release versions #

Watching this 1GB of data makes me think: It's nice to have them all, but might not be ideal to host that much amount of data.

Therefore, I already thought about a reduced version of the data files: Basically have a key picture(s) of each, and let the subtitle do its job telling the story.

This way, the game occupies about 70-90 MB of disk space and on-line storage.

The drawback is that I need to work more on the videos and add a separate code path for the reduced version.

# Vehicles move backwards #

In the original game, if you give a move command in the opposite direction the turret is facing, the vehicles start to turn and then move - leaving a window for enemy hits without returning fire. By allowing altering the carryout of the movement, vehicles can decide to rotate less and move backwards. Perhaps adding separate 'Move backwards' command instead of changing the default move command.

# No groud battle AI #

Basically it means that after the AI deploys its vehicles (for defense: around the military buildings, for offense: on the map edges), no further command is issued to them and left alone to defend themselves. The player should go in/out to destroy them. Drawback is that the AI won't react to artillery fire at all, leaving the player to use only long range equipment. However, this latter can be solved by introducing an automatic move-away from attack if the attacker is not in range: move 1-3 slots into the direction of fire to potentially get into fire range. This approach requires only simple reactive agent behavior. AI vs AI battles will be non-visible and played out only in numbers.

# Building upgrade slots #

Basically this idea is for a small anomaly fix and for more RPG elements. In the original game, the power output of the power plants are completely randomized, and mostly way over the nominal output. I discovered it while I was constructring the initial alien planets - almost all of them started with power shortage.

To compensate this deterministically (e.g. not introducing a randomization with unknown outcome), I came to the idea of upgrade slots. These are plain simple efficiency multipliers for the buildings' outputs, e.g.:

150%, 200%, 300%, 400%

This way, I can give free upgrade points to the alien and own structures to match the initial required power output.

Upgrade slots should cost you some money, for example, the cost of the original building. If you have room, you might build more of the base structure (if it is not limited), however, if you start to run out of it, these slots will come in handy.

It is also possible, that not all buildings will receive this kind of boost, or this number of boost - only the power plants are for sure, maybe the hospitals too. Here are my plans for the buildings. (increasing the output will increase the energy and worker requirements as well. The final upgrade slot will not increase the demand part further than the previous one).

| **Name** | **Default output** | **Build cost** | **Slots** |
|:---------|:-------------------|:---------------|:----------|
| Colony hub | 5k food, 5k hospital | 40k | - |
| Prefab shelter<br>Apartment block<br>Arcology<br>Water waporator <table><thead><th> 7k<br>15k<br>30k<br>12k living space </th><th> 5.6k<br>12k<br>16k<br>45k </th><th> living space 125%, 150%, 200%; 250%<br>workers: 125%, 150%, 175%, 175%<br>energy: 125%, 150%, 200%, 200% </th></thead><tbody>
<tr><td> Nuclear plant<br>Fusion Plant<br>Solar plant </td><td> 3.5MW<br>9MW<br>6MW<sup>1</sup> energy </td><td> 8k, 24k, 16k </td><td> energy: 150%, 200%, 250%, 300%<br>worker: 125%, 150%, 200%, 200% </td></tr>
<tr><td> Hydroponic farm<br>Phood factory </td><td> 8k<br>20k food </td><td> 4.2k, 10k </td><td> food: 125%, 150%, 200%, 250%,<br>energy: 125%, 150%, 200%, 200%<br>worker: 125%, 150%, 200%, 200% </td></tr>
<tr><td> Spaceship factory<br>Equipment factory<br>Weapons factory </td><td> 1000 spaceship cap<br>1000 equipment cap<br>1000 equipment cap </td><td> 20k, 20k, 24k </td><td> caps: 125%, 150%, 175%, 200%<br>energy: 150%, 200%, 250%, 250%<br>worker: 150%, 200%, 250%, 250% </td></tr>
<tr><td> Civil lab<br>Mechanic lab<br>Computer lab<br>AI lab<br>Military lab </td><td> 1 civ<br>1 mech<br>1 comp<br>1 ai<br>1 mil </td><td> 30k<br>34k<br>42k<br>46<br>52k </td><td> energy: 75%, 50%, 25%, 5%<br>workers: 75%, 50%, 25%, 5% <sup>2</sup> </td></tr>
<tr><td> Traders spaceport </td><td> 600 cr/day </td><td> 10k </td><td> credits: 125%, 150%, 175%, 200%<br>energy: 125%, 150%, 175%, 175%<br>worker: 125%, 150%, 175%, 175% </td></tr>
<tr><td> Military spaceport </td><td> orbital support </td><td> 75k </td><td> - </td></tr>
<tr><td> Bank </td><td> credit duplication </td><td> 16k </td><td> multiplier: 2.5x, 3x, 3.5x, 4x </td></tr>
<tr><td> Trade center </td><td> 1200 cr/day </td><td> 17k </td><td> credits: 125%, 150%, 175%, 200%<br>energy: 125%, 150%, 175%, 175%<br>worker: 125%, 150%, 175%, 175% </td></tr>
<tr><td> Hospital<br>Police </td><td> 20000 persons<br>50000 persons covered </td><td> 13k<br>26k </td><td> 125%, 150%, 175%, 200%<br>energy: 150%, 175%, 200%, 200%<br>worker: 150%, 175%, 200%, 200%<sup>3</sup> </td></tr>
<tr><td> Fire brigade </td><td> 50% free repair </td><td> 12k </td><td> 55% repair, 61% repair, 68% repair, 77% repair<br>energy: 150%, 175%, 200%, 200%<br>worker: 150%, 175%, 200%, 200% </td></tr>
<tr><td> Radar telescope<br>Field telescope<br>Phased telescope</td><td> 1 parsec<br>2 parsec<br>3 parsec coverage </td><td> 4k<br>10k<br>25k </td><td> - </td></tr>
<tr><td> Bunker </td><td> 50% less causality </td><td> 18k </td><td> 55% less, 61% less, 68% less, 77% less<br>energy: 150%, 200%, 250%, 250%<br>worker: 150%, 200%, 250%, 250% </td></tr>
<tr><td> Ion projector<br>Plasma projector<br>Particle projector<br>Meson projector<br> </td><td> 50, 75, 90, 105 hp/hit<sup>4</sup> </td><td> 10k<br>17.8<br>32k<br>40k </td><td> Firepower: 115%, 130%, 145%, 160%<br>Hitpoints: 125%, 150%, 175%, 200%<br>Worker: 150%, 200%, 250%, 250%<br>Energy: 150%, 200%, 250%, 250%</br> </td></tr>
<tr><td> Inversion shield<br>Hypershield </td><td> 50%<br>100% of hitpoints as extra shield  </td><td> 15k<br>35k </td><td> Shielding: 120%, 140%, 160%, 180%<br>Hitpoints: 110%, 120%, 130%, 140%<sup>5</sup><br>Worker: 150%, 200%, 250%, 250%<br>Energy: 150%, 200%, 250%, 250%</br> </td></tr>
<tr><td> Barracks<br>Fortress<br>Stronghold </td><td> 2 firepower<br>4 firepower<br>6 firepower </td><td> 15k<br>30k<br>50k </td><td> Firepower: 115%, 130%, 145%, 160% rounded up<br>Hitpoints: 110%, 120%, 130%, 140%<br>Worker: 150%, 200%, 250%, 250%<br>Energy: 150%, 200%, 250%, 250% </td></tr>
<tr><td> Recreation center<br>Park<br>Church<br>Bar<br>Stadium </td><td> 5<br>10<br>10<br>15<br>20 morale boost </td><td> 7.5k<br>14k<br>4k<br>6.3<br>50k </td><td> Morale boost: 120%, 140%, 160%, 200%<br>Worker: 150%, 200%, 250%, 250%<br>Energy: 150%, 200%, 250%, 250%</td></tr></tbody></table>


<sup>1</sup> I would arbitrarily decrease the default power output of the fusion and solar plant to allow it to play nicely with the upgrade slot.<br>
<br>
<sup>2</sup> It is pointless to increase the research capacity, as it would allow the player to research everything with less planets - just decrease the power and worker requirement on these buildings - might come in handy for the factories. The drastic improvement is due the high cost of these buildings to be justified.<br>
<br>
<sup>3</sup> Coverage increase is more costly in terms of workers and energy.<br>
<br>
<sup>4</sup> Exact firepower values not yet determined.<br>
<br>
<sup>5</sup> Don't give too much hitpoints to the shield itself, as it could render a planet completely undefeatable. I must adjust the weapon strength upgrades accordingly, and perhaps modify the campaign AI to not go over level 2.<br>
...<br>
<br>
<h1>Build any building on planets</h1>

The current implementation resembles to the original game: there is a race per planet, and the building rendering is selected based on that information. It is possible to make this per building instance based (decoupling the strong connection between building and building prototype) to allow displaying buildings of different races at once.<br>
<br>
I see two options:<br>
<br>
<ul><li>Allow building anything everywhere (human and captured alien planets), for example, having a Human nuclear plant along with a Garthog nuclear plant. As they are equal in terms of parameters, this makes no sense other than visual niceness.<br>
</li><li>Allow building those human buildings, which are not available in the alien arsenal: e.g fusion plant, stadium, church. Of course, the it must be rectified why aliens would use a church? One drawback is that it is asymmetric for the alien races. If you play with alien races, you need to capture human colonies or other alien planets which contain human structures - hard to imagine.</li></ul>

<h1>New tax income source: bars, recreation centers, stadions</h1>

In the original game, building these structures had only effect on the morale (a thing I still need to think about). It feels natural, the more people use these buildings, the more income we should get as tax. It should directly increase the tax income per day based on a function yet to be designed (e.g function of population size, number of these buildings).<br>
<br>
<h1>Symmetric skirmish mode</h1>

I already plan a skirmish mode, where you can unbounded conquer the galaxy - no hideous calls from the admiral or your wife! (plus testing the AI and other things is much easier in this mode :). However, the original game's technology tree is unbalanced and incomplete - missing buildings and ship types. I think it is possible to create a skirmish mode (using the tech.xml only, no actual change to the code is needed) to use the common subset for all aliens and humans (or perhaps, keep all the human vessels but scale their power to equal to the others?).<br>
<br>
<h1>New experience management</h1>

The original game had a practically useless feature: the more enemies your ships destroyed, the more experience they gathered - improving their properties (exactly how exactly). I want to keep this experience aquisition but have a more usable and RPG-ish style.<br>
<br>
First of all, Introduce an experience pool. You can then assign and unassign experience to/from your ships at will. If you upgrade your fleet, you unassign the current XP, then assign it to the new vessels. We can conceptionalize it as not moving XP, but rather moving the experienced crew between ships... :)<br>
<br>
The second thing is how XP should (did in original game) affect the ship properties. I see some options:<br>
<br>
<ul><li>Have it alter the firepower of the ship based on its value (probably a degressive function, e.g. logarithmic)<br>
</li><li>Have it alter all the ships properties together / on different scale: firepower, shields, hull, movement.<br>
</li><li>Have fixed slot of enhancement options per type / slot and have a fixed effect on them. Maybe even name them or have a Mass Effect style enhancement: one after one, with some special mid elements<br>
<ul><li>Movement speed<br>
</li><li>Rotation speed<br>
</li><li>Lasers (power, range)<br>
</li><li>Cannons (power, range)<br>
</li><li>Rockets<br>
</li><li>Bombs<br>
</li><li>Shields<br>
</li><li>ECMs (range)<br>
</li><li>Hull</li></ul></li></ul>

For example, Hull enhancement points can go to:<br>
<br>
<blockquote>+5% -> +10% -> +25% -> Regenerativity +5% -> +30% -> +50% -> Regenerativity + 10% -> +75% -> +90% -> +100% -> Regenerativity +25%</blockquote>

Where regenerativity means hull integrity regeneration rate on a time unit.<br>
<br>
I think, there is a lot of room on the equimpent screen to have UI for this. Perhaps a tabbed panel on the right side where currently you can equip / unequip a medium or large ship.<br>
<br>
The subsystem enhancement concept, however, is too strong to allow it to be unassigned. We will only know when the first real tests and usage patterns get fed back.<br>
<br>
<h1>Badge/Achievement system</h1>

The game is full of statistics: number of planets, income, power production, enemies destroyed, etc. I want to add some achievements for making progress in the game. Some ideas this far:<br>
<br>
<table><thead><th> <b>Short name</b> </th><th> <b>Description</b> </th></thead><tbody>
<tr><td> Conqueror </td><td> Conquered the first enemy planet </td></tr>
<tr><td> Millionaire </td><td> You collected 1.000.000 credits </td></tr>
<tr><td> Student of Bokros </td><td> All your planets sustained the Oppressive taxation for a week </td></tr>
<tr><td> Pirate Bay </td><td> You lost a planet to the Pirates </td></tr>
<tr><td> Dargslayer </td><td> You eliminated all Dargslan from the galaxy </td></tr>
<tr><td> Energizer </td><td> Your total energy production reached 10.000.000 kW </td></tr>
<tr><td> Death Star </td><td> You left your colony to be destroyed </td></tr>
<tr><td> Resear assistant </td><td> You researched 5 technologies </td></tr>
<tr><td> Scientist </td><td> You researched 30 technologies </td></tr>
<tr><td> Nobel prize </td><td> You researched all technologies </td></tr>
<tr><td> Popular </td><td> All your colonies are happy with you </td></tr>
<tr><td> Happy clients of APEH </td><td> All your colonies have above 95% tax morale </td></tr>
<tr><td> Ultimate leader </td><td> All your colonies support you </td></tr>
<tr><td> Revolutioner </td><td> One of your colony has revolved </td></tr>
<tr><td> Mass effect </td><td> Your total population got above 1.000.000 </td></tr>
<tr><td> Defender of the crown </td><td> You successfully defended your colony </td></tr>
<tr><td> Embargo </td><td> You destroyed your first Trader ship </td></tr>
<tr><td> Colombus </td><td> You colonized your first planet </td></tr>
<tr><td> Quarter of the pie </td><td> You own 25 planets </td></tr>
<tr><td> Manufacturer </td><td> You produced a total of 1.000 items </td></tr>
<tr><td> Salvage </td><td> You destroyed 1.000 enemy ships </td></tr>
<tr><td> Living space </td><td> You discovered your first empty planet. </td></tr>
<tr><td> Food for thought </td><td> Your food production is above 1.000.000 </td></tr>
<tr><td> A decade in the service </td><td> You played 10 game years </td></tr>
<tr><td> Oldest man </td><td> You played 100 game years </td></tr>
<tr><td> All your base are belong to us </td><td> Your enemies captured one of your planets </td></tr>
<tr><td> E.T. exists </td><td> You discovered a non-human civilization </td></tr>
<tr><td> Defense contract </td><td> All your planets are equipped with at least 3 planetary guns and one shield. </td></tr>
<tr><td> Coffee break </td><td> You paused your game for 30 minutes </td></tr>
<tr><td> All seeing eye </td><td> At least 80% of the galaxy is covered by radar </td></tr>
<tr><td> Newbie </td><td> Started a new game </td></tr>
<tr><td> Commander </td><td> You have been promoted to commander </td></tr>
<tr><td> Admiral </td><td> You have been promoted to admiral </td></tr>
<tr><td> Grand Admiral </td><td> You have been promoted to grand admiral </td></tr>
<tr><td> Influenza no more </td><td> You successfully eliminated the source of the virus infection </td></tr></tbody></table>

Of course, the limits for numerical achievements can change, depending on how open-ig is played (e.g maybe 10M kW for the Energizer achievement is too low or too high for a normal play?)<br>
<br>
<h1>Retreat from gound battle</h1>

By Gergő Harrach.<br>
<br>
A really simple scheme: retreat from ground battle if you feel you won't win it. There are things to decide:<br>
<br>
<ul><li>Should the retreat an instant loss of your vehicles?<br>
</li><li>Should we automatically move out of range and save the remaining troops?<br>
</li><li>If you are the defender, should the retreat mean you give off your planet?<br>
</li><li>If you retreat from defense, should the remaining barracks...strongholds be left intact: e.g. in the counterattack you would face your own structures?</li></ul>

This kind of option brings me an idea about an exit strategy: scorched earth. If you see you lost and there is no hope for retake, you could demolish ALL of your buildings, leaving your enemy to rebuild everything from scratch and cost them an enormous money to bring everything up to speed again. Just imagine, a planet of 100k inhabitants demanding food, energy, hospital and living space from the AI. An instant 250k credit drain? It is highly possible the planet will revolt.