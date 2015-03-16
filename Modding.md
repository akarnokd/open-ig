

# Introduction #

Adding more customizable skirmish and campaign editor are planned features.
Still, you can MOD your game, but currently it is complicated:

# Starting steps #
  * create a directory with the name "data"
  * extract the generic/skirmish/human from  open-ig-upgrade-NNNN-NN-NNA2.zip into the data directory.
  * You should now have the following structure:
```
data/skirmish/human/definition.xml
data/skirmish/human/planets.xml
data/skirmish/human/players.xml
data/skirmish/human/scripting.xml
data/skirmish/human/tech.xml
```

  * rename skirmish/human to something else (e.g., human2)
```
data/skirmish/human2/definition.xml
data/skirmish/human2/planets.xml
data/skirmish/human2/players.xml
data/skirmish/human2/scripting.xml
data/skirmish/human2/tech.xml
```

# Setup other races #

  * open players.xml and create a copy of the "FreeNations" player.
  * Change the values to another race.
  * If you need examples, look for the generic/campaign/main2/players.xml file for valid settings
```
<player id='Garthog' race='garthog' name='players.garthog'
	icon='starmap/fleets/garthog_fleet'
	color='FFFC2828'
	picture='database/garthog'
	money='25000'
	initial-stance='40'
	colonization-limit='-1'
/>
```

# Setup starting planets #

  * open planets.xml and look for 3 planets near by (run a normal game CTRL+V on starmap then look for the names in a cluster). Add

```
owner='Garthog' population='5000' race='garthog' 
```

to the planet node

# Change definition labels #

  * open definition.xml and change texts/title and texts/description something you would like to read on the single player screen.

# Finish #

  * Save and restart the program. You should now see your game.