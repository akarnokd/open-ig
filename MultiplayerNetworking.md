

# Basic properties #

  * TCP based approach
  * Cryptography will be avoided for now.
  * Sessioning (to allow reconnection)
  * Text commands and responses, UTF-8
  * Strings enclosed by ""
    * Escape sequences: \" \\ \n \r
  * Complex objects enclosed by { }
  * Arrays enclosed by `[` `]`
  * Messages may span multiple lines
  * Message terminated by a matching final '}'
  * Message exchanges will be pull-based for now, i.e., the server will never send data without a prior request from clients.

## Message format ##

Remark: whitespaces may be used freely where it does not cause ambiguity

```
message :: object | array
```

```
object :: [OBJECT_TYPE] '{' [attributes] '}'
```

```
array :: [ARRAY_NAME] '[' [value [, value [,...]]] ']'
```

```
attributes :: attribute [, attribute [,...]]
```

```
attribute :: attribute_name=value
```

```
value :: "string" | 1234 | 1234.567 | false | true | array | null | object
```


# Request and response formats #

## Batch queries ##

Send multiple requests and commands with one message exchange.

### Request ###

```
BATCH [
  MESSAGE_TYPE_1 { ... },
  MESSAGE_TYPE_2 [ ... ]
]

```

### Response ###

```
BATCH_RESPONSE [
  RESPONSE_TO_MESSAGE_1 { ... },
  RESPONSE_TO_MESSAGE_2 [ ... ]
]
```

If any of the individual batch records cause an error, the whole response message will be that error.

## General errors ##

### response ###

```
ERROR { 
  code=1234, 
  message="" 
}
```

Codes
| **Code** | **Meaning** |
|:---------|:------------|
| 1 | Request format error |
| 2 | Unknown message type |
| 3 | Unknown error |
| 4 | Server not ready |
| 5 | Invalid user or passphrase |
| 6 | Client version error |
| 7 | Message type not allowed in the current state |
| 8 | Unable to relogin, match might have ended |
| 9 | Match settings rejected; requested settings not allowed |

TBD

## Keepalive or latency measurement ##

When: any time

### Request ###
```
PING { }
```

### Response ###
```
PONG { }
```

# Game preparation messages #

## Login ##

When: after establishing a TCP connection.

### Request ###
```
LOGIN { 
  user="", 
  passphrase="", 
  version="" 
}
```

Remarks:
  * `passphrase` is plaintext
  * `language` the client's requested language

### Responses ###

```
WELCOME { 
  session=""
}
```

Remarks:
  * `session` contains a string that can be used to re-login in case of lost connection. Values will be unique among clients and started gameplays.
  * `language` the requested language or if not available, the server's supplied language

or an `ERROR`

## Relogin ##

When: after establishing a TCP connection.

### Request ###
```
RELOGIN { session="" }
```

### Response ###

```
WELCOME_BACK { }
```

## Match settings ##

When: after login, before game start

### Request ###

```
GET_MATCH_SETTINGS { }
```

### Response ###

```
MATCH_SETTINGS {
  galaxy_template="", 
  races_template="", 
  technology_template="",
  random_surfaces=false,
  tech_level_start=0, 
  tech_level_max=5,
  money=200000, 
  planets=3, 
  population=5000, 
  colony_hub_built=true, 
  colony_ship_available=true, 
  colony_ships=1,
  orbital_factory_available=true, 
  orbital_factories=0,
  conquest_victory=true,
  occupation_victory=true,
  occupation_percent=66,
  occupation_time=30,
  economic_victory=true,
  economic_credits=10000000,
  technology_victory=true,
  social_victory=true,
  social_morale=95,
  social_planets=30,
  players=[
    PLAYER {
      id="",
      name="",
      control="AI_EASY|AI_MEDIUM|AI_HARD|TRADERS|PIRATES|USER",
      race="",
      icon="",
      group=1,
      user="",
      traits=["TRAIT1", "TRAIT2"],
      home_planet=""
      may_choose_race=true,
      may_choose_icon=true,
      may_choose_traits=true
      may_choose_group=true,
    }
  ]
}
```

Remarks:
  * The `PLAYER.name` contains the user-editable empire name.
  * `PLAYER.user` tells who is assigned to the slot, `null` means the slot may be chosen by the player, if it contains the user name, the roles are preset.

## Choosing player settings ##

When: after match settings, before starting a match.

### Request ###

```
PLAYER_SETTINGS {
  id="",
  name="",
  race="",
  icon="",
  group=1,
  traits=[ "" ]
}
```

### Response ###

```
PLAYER_SETTINGS_ACCEPTED { }
```

or `ERROR`

## Signal the intent to join the match ##

When: After player settings accepted

### Request ###

```
JOIN { }
```

Remark:
  * Clients should wait for the response messages.

### Response ###

Instructs the clients to start loading assets based on the match settings previously sent/chosen and with the given custom planet configuration,

```
LOAD { 
  players=[
    PLAYER {
      id="",
      name="",
      control="",
      race="",
      icon="",
      group=1,
      user=""
    }
  ],
  planet_settings=[
    PLANET {
      id="",
      x=100,
      y=100,
      size=30,
      surface_type="cratered",
      surface_variant=1,
      owned=true,
      colony_hub=true,
      orbital_factory=true
    }
  ],
  fleet_settings=[
    FLEET { ... }
  ],
  inventory=[
    INVENTORY { ... }
  ],
  researches={
    available=[ "type 1" ]
  }
}
```

or

```
MATCH_CANCELLED { }
```

Remark:
  * All fields may be optional in many combinations.
  * See the definition of the `FLEET` and `INVENTORY` object later on.

## Synchronize start of the game ##

When: after client finished the load

### Request ###

```
READY { }
```

### Response ###

```
BEGIN { }
```

# In-game non-battle status requests #


## Query empire status ##

### Requests ###

```
QUERY_EMPIRE_STATUSES { }
```

```
QUERY_EMPIRE_STATUS {
  id=""
}
```

### Response ###

```
EMPIRE_STATUSES {
  date="yyyy-MM-dd HH:mm:ss",
  empires=[
    EMPIRE {
      id="",
      money=100000
    }
  ]
}
```

## Query fleet statuses ##

### Request ###

```
QUERY_FLEETS { }
```

```
QUERY_FLEET {
  id=1
}
```


### Response ###

```
FLEETS [
    FLEET {
      fleet_id=1,
      name="",
      owner="",
      x=0.1,
      y=0.1,
      task="",
      target_fleet=null,
      target_planet=null,
      attacking=false,
      path=[
        POINT {
          x=0.1,
          y=0.1
        }
      ],
      inventory=[
        INVENTORY {
          id=0,
          type="",
          hp=100,
          shield=100,
          count=1,
          tag=null,
          equipment=[
            EQUIPMENT {
              id="",
              type="",
              count=1,
              hp=1
            }
          ]
        }
      ]
    }
]
```

Remarks:
  * Other player's fleet won't have inventory details, up to 1 path entry, no task and no target fields if it is not referring to the current player's objects.

## Query own inventory ##

### Request ###

```
QUERY_INVENTORY { }
```

### Response ###

```
INVENTORIES [
    INVENTORY {
      type="",
      count=1
    }
]
```

## Query production status ##

### Request ###

```
QUERY_PRODUCTIONS
```

### Response ###

```
PRODUCTIONS [
    PRODUCTION {
      type="",
      count=1,
      progress=20,
      priority=50
    }
]
```

## Query research status ##

### Request ###

```
QUERY_RESEARCHES
```

### Response ###

```
RESEARCHES {
  available=[
    "tech id", "tech id"
  ],
  running=[
    RESEARCH {
      type="",
      status="RUNNING|MONEY|LAB|STOPPED",
      assigned=1400,
      total=1400,
      current=true
    }
  ]
}
```

## Query spy statuses ##

TBD

## Query trade statuses ##

TBD

## Query planet statuses ##

### Request ###

```
QUERY_PLANETS { }
```

```
QUERY_PLANET {
  planet_id=""
```

### Response ###

```
PLANETS [
    PLANET {
      id="",
      owner="",
      race="",
      morale=50,
      last_morale=50,
      auto_build="NONE|CIVIL|...",
      tax="NONE|VERY_LOW|...",
      tax_income=50,
      trade_income=50,
      construction=50,
      inventory=[
        INVENTORY {
          id=2
          owner="",
          type="",
          count=1,
          hp=100,
          shield=100,
          equipment=[
            EQUIPMENT {
              id="",
              type="",
              count=1,
              hp=100
            }
          ]
        }
      ]
      buildings=[
        BUILDING {
          id=511,
          x=-50,
          y=-20,
          type="",
          race="",
          enabled=true,
          repair=false,
          construction=5000,
          hp=5000,
          level=0
        }
      ]
    }
]
```

Remarks:
  * If the inventory contains enemy satellites or the whole planet is owned by an enemy, only partial information is contained in the records.

# In-game non-battle commands #

## Fleet commands ##

### Request ###

```
MOVE_FLEET {
  fleet_id=1,
  path=[
    POINT {
      x = 0.1,
      y = 0.1
    }
  ]  
}
```

```
ATTACK_FLEET {
  fleet_id=1,
  target=2
}
```

```
ATTACK_PLANET {
  fleet_id=1,
  target="",
}
```

```
MOVE_FLEET_PLANET {
  fleet_id=1,
  target="",
}
```

```
FOLLOW_FLEET {
  fleet_id=1,
  target="",
}
```

```
COLONIZE_FLEET {
  fleet_id=1
}
```

### Response ###

```
OK { }
```

or

```
ERROR_FLEET_MISSING { }
```

or

```
ERROR_UNKNOWN_TARGET { }
```

or

```
ERROR_CANT_ATTACK { }
```

or

```
ERROR_NO_COLONY_SHIPS { }
```

or

```
ERROR_PLANET_ALREADY_COLONIZED { }
```

## Fleet equipment commands ##

### Requests ###

Create a new fleet at the specified location.
```
NEW_FLEET_AT_PLANET { 
  planet_id=""
}
```

Create a new fleet at the specified other fleet.
```
NEW_FLEET_AT_FLEET {
  fleet_id=10
}
```

```
RENAME_FLEET {
  fleet_id=10,
  name=""
}
```

```
SELL_FLEET_ITEM {
  fleet_id=10,
  item_id=521
}
```

```
DEPLOY_FLEET_ITEM {
  fleet_id=10,
  type=""
}
```

```
UNDEPLOY_FLEET_ITEM {
  fleet_id=10,
  item_id=521
}
```

Add or replace equipment at the specified slot
```
ADD_FLEET_EQUIPMENT {
  fleet_id=10,
  item_id=522,
  slot_id="radar",
  type="Radar1",
  count=1
}
```

```
REMOVE_FLEET_EQUIPMENT {
  fleet_id=10,
  item_id=522,
  slot_id="radar",
  count=1
}
```

```
FLEET_AUTO_UPGRADE { 
  fleet_id=10
}
```

### Responses ###

```
NEW_FLEET_OK {
  fleet_id=10
}
```

```
ERROR_NO_MILITARY_SPACEPORT { }
```

```
ERROR_UNKNOWN_FLEET { }
```

```
ERROR_UNKNOWN_FLEET_ITEM { }
```

```
ERROR_UNKNOWN_FLEET_EQUIPMENT { }
```

```
ERROR_UNKNOWN_TECHNOLOGY { }
```

```
ERROR_INVALID_FLEET_EQUIPMENT { }
```

```
DEPLOY_FLEET_ITEM_OK {
  fleet_id=412
}
```

## Planet colonization commands ##

### Requests ###

Mark the planet for automatic colonization.
```
COLONIZE { 
  planet_id=""
}
```

```
CANCEL_COLONIZE { 
  planet_id=""
}
```

### Responses ###

```
OK { }
```

or

```
ERROR_PLANET_ALREADY_COLONIZED { }
```

or

```
ERROR_TECHNOLOGY_MISSING { }
```

## Planet management commands ##

### Requests ###

```
BUILD {
  planet_id="",
  building_type="",
  building_race="",
  x=-20,
  y=-50
}
```

```
ENABLE {
  building_id=10
}
```

```
DISABLE {
  building_id=10
}
```

```
REPAIR_ON {
  building_id=10
}
```

```
REPAIR_OFF {
  building_id=10
}
```

```
DEMOLISH {
  building_id=10
```

```
LEVEL {
  building_id=10,
  level=4
}
```
### Responses ###

General acknowledgement.
```
OK { }
```

Command accepted, return the building's unique id.
```
BUILD_OK { 
  building_id=5
}
```

or

```
ERROR_PLANET_LOST { }
```

or

```
ERROR_NO_MONEY { }
```

or, building is at maximum level
```
ERROR_MAX_LEVEL { }
```

or, the destination position is blocked

```
ERROR_CANT_PLACE { }
```

```
ERROR_UNKNOWN_BUILDING { }
```

Level down not supported.
```
ERROR_LEVEL_DOWN { }
```

## Planet equipment commands ##

### Requests ###

```
DEPLOY_PLANET_ITEM {
  planet_id="",
  type="SpaceStation1",
  count=1
}
```

```
UNDEPLOY_PLANET_ITEM {
  planet_id="",
  item_id=100
  count=1
}
```

Add or replace equipment at the specified slot
```
ADD_PLANET_EQUIPMENT {
  planet_id="",
  item_id=522,
  slot_id="radar",
  type="Radar1",
  count=1
}
```

```
REMOVE_PLANET_EQUIPMENT {
  planet_id="",
  item_id=522,
  slot_id="radar",
  count=1
}
```

```
PLANET_AUTO_UPGRADE { 
  planet_id=""
}
```

### Responses ###

```
OK { }
```

```
DEPLOY_PLANET_ITEM_OK {
  item_id=100
}
```

```
ERROR_PLANET_ITEM_LIMIT { }
```

## Production commands ##

### Requests ###

```
START_PRODUCTION {
  type="",
  count=10
}
```

```
STOP_PRODUCTION {
  type=""
}
```

```
ADD_PRODUCTION_UNITS {
  type="",
  count=1
}
```

```
REMOVE_PRODUCTION_UNITS {
  type="",
  count=1
}
```

```
SET_PRODUCTION_PRIORITY {
  type="",
  priority=50
}
```

### Responses ###

```
ERROR_UNKNOWN_TECHNOLOGY { }
```

## Research commands ##

### Requests ###
```
START_RESEARCH {
  type="",
  assigned=1000
}
```

```
STOP_RESEARCH {
  type=""
```

```
SET_RESEARCH_MONEY {
  type="",
  assigned=100
}
```

### Responses ###

```
OK { }
```

```
ERROR_PREREQUISITES_MISSING { }
```

```
ERROR_ALREADY_RESEARCHED { }
```

## Spying commands ##

TBD

## Trade commands ##

TBD

# In-game space battle commands #

```
STOP_SPACE_UNIT {
  id=1
}
```

```
MOVE_SPACE_UNIT {
  id=1,
  x=10,
  y=20
}
```

```
ATTACK_SPACE_UNIT {
  id=1,
  target=200
}
```

```
SPACE_KAMIKAZE {
  id=1
}
```

```
SPACE_FIRE_ROCKET {
  id=1,
  target=200
}
```

```
SPACE_GUARD {
  id=1
}
```

```
SPACE_RETREAT { 
  battle_id=1
}
```

```
SPACE_STOP_RETREAT { 
  battle_id=1
}
```

Set default fleet formation.
```
FLEET_FORMATION {
  id=1,
  formation=6
}
```

# In-game space battle status requests #

## Ongoing space battles ##

### Request ###
```
QUERY_BATTLES { }
```

```
QUERY_BATTLE {
  id=1
}
```

### Response ###

```
BATTLES [
  BATTLE {
    id=1,
    planet="planet id",
    players=[ "player1", "player2" ],
    fleets=[
      FLEET {
        id=1,
        target_fleet=null,
        target_planet="planet"
      }
    ],
    in_space_battle=true,
    in_space_retreat=true,
    in_ground_battle=false,
    in_ground_retreat=false,
    statistics={ ... }
  }
]
```

## Space battle information ##

### Request ###
```
QUERY_SPACE_BATTLE_UNITS {
  id=1
}
```

### Response ###
```
SPACE_BATTLE_UNITS [
  UNIT {
    id=1,
    owner="",
    type="Cruiser1",
    count=1,
    hp=100,
    shield=100,
    x=50,
    y=50,
    angle=0.4,
    target=null,
    guard=true,
    move_x=null,
    move_y=null,
    kamikaze=true,
    equipment=[
      EQUIPMENT {
        id="",
        type="",
        count=1,
        hp=1,
        cooldown=5
    ]
  }  
]
```


# In-game ground battle commands #

## Unit commands ##

### Request ###

Stop vehicle or gun.
```
STOP_GROUND_UNIT {
  id=1
}
```

```
MOVE_GROUND_UNIT {
  id=1,
  x=-5,
  y=-5
}
```

```
ATTACK_GROUND_UNIT {
  id=1,
  target=5
}
```

```
ATTACK_BUILDING {
  id=1,
  target=10
}
```

```
DEPLOY_MINE {
  id=1
}
```

```
GROUND_RETREAT {
  battle_id=1
}
```

```
STOP_GROUND_RETREAT {
  battle_id=1
}
```

# In-game ground battle status requests #

## Basic status ##

### Requests ###

```
QUERY_GROUND_BATTLE_UNITS {
}
```


### Responses ###

```
GROUND_BATTLE_UNITS [
  UNIT {
    id=1,
    type="type",
    owner="owner",
    hp=100,
    phase=0,
    cooldown=5,
    x=-1.1,
    y=-1.1,
    angle=0.5,
    attack_unit=null,
    attack_building=null,
    attack_move_x=null,
    attack_move_y=null,
    move_x=null,
    move_y=null,
    path=[
      POINT {
        x=-5,
        y=-5,
      }
    ]
  },
  GUN {
    id=5
    type="type",
    building=11,
    phase=0,
    cooldown=5,
    angle=0.5,
    attack_unit=null
  }
]
```