/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Func1;
import hu.openig.core.Func2;
import hu.openig.core.Location;
import hu.openig.core.Pathfinding;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * The ground battle environment, tracking
 * units, guns, projectiles, mines and explosions.
 * @author akarnokd, 2013.06.01.
 */
public class GroundwarManager implements GroundwarWorld {
    /** The target planet. */
    public final Planet planet;
    /** The ground objects. */
    public final PlanetGround ground;
    /** The world object. */
    public final World world;
    /** The simulation delay on normal speed. */
    public static final int SIMULATION_DELAY = 100;
    /** The time in sumulations steps during the paralize effect is in progress. */
    public static final int PARALIZED_TTL = 15 * 1000 / SIMULATION_DELAY;
    /** How many steps to yield before replanning. */
    public static final int YIELD_TTL = 10 * 1000 / SIMULATION_DELAY;
    /** List of requests about path planning. */
    public final Set<PathPlanning> pathsToPlan = new HashSet<>();
    /** Plan only the given amount of paths per tick. */
    public static final int PATHS_PER_TICK = 10;
    /** Disable AI unit management. */
    public boolean noAI;
    /** Allow controlling non-player units? */
    public boolean controlAll;
    /** The set where the vehicles may be placed for the current player. */
    public final Set<Location> battlePlacements = new HashSet<>();
    /** The list of remaining units to place for the current player. */
    public final LinkedList<GroundwarUnit> unitsToPlace = new LinkedList<>();
    /** Indicator that the allocator needs to run after the simulation step. */
    public boolean runAllocator;
    /** The set of sounds to play after the simulation step was run. */
    public final EnumSet<SoundType> playSounds = EnumSet.noneOf(SoundType.class);
    /** The battle objects of those who are involved. */
    public final Map<Integer, BattleInfo> battles = new HashMap<>();
    /**

     * The default cell-passable test which ignores moving units
     * and other units currently doing pathfinding calculation.
     */
    final Func1<Location, Boolean> defaultPassable = new Func1<Location, Boolean>() {
        @Override
        public Boolean invoke(Location value) {
            return isPassable(value.x, value.y);
        }
    };
    /**
     * The defalt estimator for distance away from the target.
     */
    final Func2<Location, Location, Integer> defaultEstimator = new Func2<Location, Location, Integer>() {
        @Override
        public Integer invoke(Location t, Location u) {
            return (Math.abs(t.x - u.x) + Math.abs(t.y - u.y)) * 1000;
        }
    };
    /** Routine that tells the distance between two neighboring locations. */
    final Func2<Location, Location, Integer> defaultDistance = new Func2<Location, Location, Integer>() {
        @Override
        public Integer invoke(Location t, Location u) {
            if (t.x == u.x || u.y == t.y) {
                return 1000;
            }
            return 1414;
        }
    };
    /**
     * Computes the distance between any cells.
     */
    final Func2<Location, Location, Integer> defaultTrueDistance = new Func2<Location, Location, Integer>() {
        @Override
        public Integer invoke(Location t, Location u) {
            return (int)(1000 * Math.hypot(t.x - u.x, t.y - u.y));
        }
    };

    /**
     * Constructor with the war id.
     * @param planet the target planet
     */
    public GroundwarManager(Planet planet) {
        this.planet = planet;
        this.ground = planet.ground;
        this.world = planet.world;
    }
    /**
     * The task to plan a route to the given destination asynchronously.

     * @author akarnokd, 2011.12.25.
     */
    public class PathPlanning implements Callable<PathPlanning> {
        /** The initial location. */
        final Location current;
        /** The goal location. */
        final Location goal;
        /** The unit. */
        final GroundwarUnit unit;
        /** The computed path. */
        final List<Location> path = new ArrayList<>();
        /** The player to ignore. */
        final Player ignore;
        /**
         * Constructor. Initializes the fields.
         * @param initial the initial location
         * @param goal the goal location
         * @param unit the unit
         * @param ignore the player units to ignore
         */
        public PathPlanning(
                Location initial,

                Location goal,

                GroundwarUnit unit,
                Player ignore) {
            this.current = initial;
            this.goal = goal;
            this.unit = unit;
            this.ignore = ignore;
        }

        @Override
        public PathPlanning call() {
            //path.addAll(getPathfinding(ignore).searchApproximate(current, goal));
            return this;
        }
        /**
         * Apply the computation result.
         */
        public void apply() {
            unit.path.addAll(path);
            unit.inMotionPlanning = false;
            unit.yieldTTL = 0;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj.getClass() == getClass()) {
                return unit.equals(((PathPlanning)obj).unit);
            }
            return false;
        }
        @Override
        public int hashCode() {
            return unit.hashCode();
        }
    }
    /**
     * Returns a preset pathfinding object with the optional
     * ignore player.
     * @param ignore the player to ignore
     * @return the pathfinding ibject
     */
    Pathfinding getPathfinding(final Player ignore) {
        Pathfinding pathfinding = new Pathfinding();
        if (ignore == null) {
            pathfinding.isPassable = defaultPassable;
        } else {
            pathfinding.isPassable = new Func1<Location, Boolean>() {
                @Override
                public Boolean invoke(Location value) {
                    return isPassable(value.x, value.y, ignore);
                }
            };
        }
        pathfinding.estimation = defaultEstimator;
        pathfinding.distance = defaultDistance;
        pathfinding.trueDistance = defaultTrueDistance;
        return pathfinding;
    }
    @Override
    public boolean isPassable(int x, int y) {
        if (planet.surface.placement.canPlaceBuilding(x, y)) {
            Set<GroundwarUnit> gunits = ground.unitsForPathfinding.get(Location.of(x, y));
            if (gunits != null) {
                boolean ip = true;
                for (GroundwarUnit u : gunits) {
                    ip &= (!u.path.isEmpty()

                                && u.yieldTTL * 2 < YIELD_TTL) || u.inMotionPlanning;
                }
                return ip;
            }
            return true;
        }
        return false;
    }
    /**
     * Check if the given cell is passable and ignore the units
     * of the given player.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param ignore the player units to ignore
     * @return true if the place is passable
     */
    public boolean isPassable(int x, int y, Player ignore) {
        if (planet.surface.placement.canPlaceBuilding(x, y)) {
            Set<GroundwarUnit> gunits = ground.unitsForPathfinding.get(Location.of(x, y));
            if (gunits != null) {
                boolean ip = true;
                for (GroundwarUnit u : gunits) {
                    ip &= u.owner == ignore
                            || (!u.path.isEmpty() && u.yieldTTL * 2 < YIELD_TTL)

                            || u.inMotionPlanning;
                }
                return ip;
            }
            return true;
        }
        return false;
    }
    /**
     * Returns the owner of the currently selected units.
     * @return the owner
     */
    public Player selectionOwner() {
        for (GroundwarUnit u : ground.selectedUnits) {
            return u.owner;
        }
        return null;
    }
    /**
     * Test if there are units deployed in this war by
     * the given player.
     * @param player the target player
     * @return true if units deployed
     */
    public boolean hasUnits(Player player) {
        for (GroundwarUnit u : ground.units) {
            if (u.owner == player) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void stop(GroundwarGun g) {
        g.attack = null;
    }
    @Override
    public void stop(GroundwarUnit u) {
        u.path.clear();
        if (u.nextMove != null) {
            u.path.add(u.nextMove);
        }
        u.attackBuilding = null;
        u.attackUnit = null;
        u.attackMove = null;
        u.advanceOnBuilding = null;
        u.advanceOnUnit = null;
        ground.minelayers.remove(u);
    }
    @Override
    public List<GroundwarUnit> units() {
        return ground.units;
    }
    @Override
    public boolean hasMine(int x, int y) {
        return ground.mines.containsKey(Location.of(x, y));
    }
    @Override
    public void special(GroundwarUnit u) {
        if (u.model.type == GroundwarUnitType.MINELAYER) {
            ground.minelayers.add(u);
        } else
        if (u.model.type == GroundwarUnitType.KAMIKAZE) {
            double x = u.x;
            double y = u.y;
            double m = world.battle.getDoubleProperty(u.model.id, u.owner.id, "self-destruct-multiplier");
            damageArea(x, y, (u.damage() * m), u.model.area, u.owner);
            u.hp = 0; // destroy self
            createExplosion(u, ExplosionType.GROUND_YELLOW);
        }
    }
    @Override
    public void attack(GroundwarUnit u, Building target) {
        if (world.battle.directAttackUnits.contains(u.model.type)

                && u.owner != planet().owner && u.attackBuilding != target) {
            stop(u);
            u.attackBuilding = target;
            u.attackUnit = null;
        }
    }
    @Override
    public List<GroundwarGun> guns() {
        return ground.guns;
    }
    @Override
    public void move(GroundwarUnit u, int x, int y) {
        stop(u);
        u.guard = true;
        Location lu = u.location();
        Location lm = Location.of(x, y);
        u.inMotionPlanning = true;
        pathsToPlan.add(new PathPlanning(lu, lm, u, null));
    }
    /**
     * Perform an attack move towards the specified location
     * and ignore enemy ground units in the path.
     * @param u the unit to use for attack move
     * @param x the target X coordinate
     * @param y the target Y coordinate
     */
    public void attackMove(GroundwarUnit u, int x, int y) {
        stop(u);
        u.guard = true;
        Location lu = u.location();
        Location lm = Location.of(x, y);
        u.attackMove = lm;
        u.inMotionPlanning = true;
        pathsToPlan.add(new PathPlanning(lu, lm, u, enemy(u.owner)));
    }
    /**
     * Returns the enemy player to the given player.
     * @param p the player
     * @return the enemy player
     */
    protected Player enemy(Player p) {
        for (GroundwarUnit u : ground.units) {
            if (u.owner != p) {
                return u.owner;
            }
        }
        return null;
    }
    @Override
    public Set<Location> placementOptions(Player player) {
        return getDeploymentLocations(player == planet().owner, true);
    }
    @Override
    public void attack(GroundwarGun g, GroundwarUnit target) {
        if (g.owner != target.owner) {
            g.attack = target;
        }
    }
    @Override
    public void attack(GroundwarUnit u, GroundwarUnit target) {
        if (world.battle.directAttackUnits.contains(u.model.type)

                && u.owner != target.owner
                && u.attackUnit != target) {
            stop(u);
            u.attackBuilding = null;
            u.attackUnit = target;
        }
    }
    @Override
    public BattleInfo battle() {
        return null; // FIXME

    }
    /**

     * Generate the set of deployment locations.
     * @param atBuildings should the placement locations around buildings?
     * @param skipEdge skip the most outer location
     * @return the set of locations

     */
    Set<Location> getDeploymentLocations(boolean atBuildings, boolean skipEdge) {
        Set<Location> result = new HashSet<>();
        if (atBuildings) {
            for (Building b : planet().surface.buildings.iterable()) {
                result.addAll(placeAround(b));
            }
        } else {
            for (int i = skipEdge ? 1 : 0; i < 3; i++) {
                result.addAll(placeEdge(i));
            }
        }
        return result;
    }
    /**
     * Place around building.
     * @param b the building
     * @return the set of available placement locations
     */
    Set<Location> placeAround(Building b) {
        Set<Location> result = new HashSet<>();
        for (int x = b.location.x - 3; x < b.location.x + b.tileset.normal.width + 3; x++) {
            for (int y = b.location.y + 3; y > b.location.y - b.tileset.normal.height - 3; y--) {
                if (planet.surface.placement.canPlaceBuilding(x, y)) {
                    result.add(Location.of(x, y));
                }

            }
        }
        return result;
    }
    /**
     * Place deployment indicators.
     * @param distance the distance from edge
     * @return the set of available placement locations
     */
    Set<Location> placeEdge(int distance) {
        Set<Location> result = new HashSet<>();
        int w = planet().surface.width;
        int h = planet().surface.height;
        int n = 0;
        int x = 0;
        int y = -distance;
        while (n < w) {
            if (planet.surface.placement.canPlaceBuilding(x, y)) {
                result.add(Location.of(x, y));
            }

            x++;
            y--;
            n++;
        }
        n = 0;
        x = 0;
        y = -distance;
        while (n < h) {
            if (planet.surface.placement.canPlaceBuilding(x, y)) {
                result.add(Location.of(x, y));
            }

            x--;
            y--;
            n++;
        }
        n = 0;
        x = -h + distance + 1;
        y = -h + 1;
        while (n < w) {
            if (planet.surface.placement.canPlaceBuilding(x, y)) {
                result.add(Location.of(x, y));
            }

            x++;
            y--;
            n++;
        }
        n = 0;
        x = w - distance - 1;
        y = -w + 1;
        while (n < h) {
            if (planet.surface.placement.canPlaceBuilding(x, y)) {
                result.add(Location.of(x, y));
            }

            x--;
            y--;
            n++;
        }
        return result;
    }
    /**
     * Damage units and structures within the specified cell area.
     * @param cx the cell x
     * @param cy the cell y
     * @param damage the damage to apply
     * @param area the effect area
     * @param owner the units and structures *NOT* to damage
     */
    void damageArea(double cx, double cy, double damage, int area, Player owner) {
        for (GroundwarUnit u : ground.units) {
            if (u.owner != owner) {
                if (U.cellInRange(cx, cy, u.x, u.y, area)) {
                    if (!u.isDestroyed()) {
                        u.damage((int)(damage * (area - Math.hypot(cx - u.x, cy - u.y)) / area));
                        if (u.isDestroyed()) {
                            createExplosion(u, ExplosionType.GROUND_RED);

                            u.owner.statistics.vehiclesLost.value++;
                            u.owner.statistics.vehiclesLostCost.value += world.researches.get(u.model.id).productionCost;

                            owner.statistics.vehiclesDestroyed.value++;
                            owner.statistics.vehiclesDestroyedCost.value += world.researches.get(u.model.id).productionCost;

                        }
                    }
                }
            }
        }
        if (planet().owner != owner) {
            for (Building b : planet.surface.buildings.list()) {
                Location u = centerCellOf(b);
                if (U.cellInRange(cx, cy, u.x, u.y, area)) {
                    damageBuilding(b, (int)(damage * (area - Math.hypot(cx - u.x, cy - u.y)) / area));
                }
            }
        }
    }
    /**
     * Create an explosion animation at the given center location.
     * @param target the target of the explosion
     * @param type the type of the explosion animation
     */
    void createExplosion(GroundwarUnit target, ExplosionType type) {
        GroundwarExplosion exp = new GroundwarExplosion(world.battle.groundExplosions.get(type));
        exp.x = target.x;
        exp.y = target.y;
        exp.target = target;
        ground.explosions.add(exp);
    }
    /**
     * Create an explosion animation at the given center location.
     * @param x the explosion center in surface coordinates
     * @param y the explosion center in surface coordinates
     * @param type the type of the explosion animation
     */
    void createExplosion(double x, double y, ExplosionType type) {
        GroundwarExplosion exp = new GroundwarExplosion(world.battle.groundExplosions.get(type));
        exp.x = x;
        exp.y = y;
        ground.explosions.add(exp);
    }
    /**
     * Notify AIs before the next simulation step.
     */
    protected void notifyAIs() {
        Set<Player> players = getAllPlayers();
        for (Player p : players) {
            if (p.ai != null) {
                p.ai.groundBattle(this);
            }
        }
    }
    /**
     * Returns a set of players who have units on the current planet.
     * @return the set of players
     */
    private Set<Player> getAllPlayers() {
        Set<Player> players = new HashSet<>();
        players.add(planet.owner);
        for (GroundwarUnit u : ground.units) {
            players.add(u.owner);
        }
        return players;
    }
    /** The ground war simulation. */
    public void simulation() {

        runAllocator = false;
        playSounds.clear();

        notifyAIs();

        // execute path plannings
        doPathPlannings();

        // destruction animations
        for (GroundwarExplosion exp : new ArrayList<>(ground.explosions)) {
            updateExplosion(exp);
        }
        for (GroundwarRocket rocket : new ArrayList<>(ground.rockets)) {
            updateRocket(rocket);
        }
        Iterator<GroundwarGun> itg = ground.guns.iterator();
        while (itg.hasNext()) {
            GroundwarGun g = itg.next();
            if (g.building == null || g.building.hitpoints <= 0) {
                ground.deselect(g);
                itg.remove();
            } else {
                updateGun(g);
            }
        }

        for (GroundwarUnit u : ground.units) {
            updateUnit(u);
        }

        Player winner = checkWinner();
        if (winner != null) {
            for (GroundwarUnit u : ground.units) {
                stop(u);
            }
            if (ground.explosions.size() == 0 && ground.rockets.size() == 0) {
                concludeBattle(winner);
            }
        }
    }

    /**
     * Execute path plannings asynchronously.
     */
    void doPathPlannings() {
        if (pathsToPlan.size() > 0) {

            // map all units to locations
//            long t0 = System.nanoTime();

            List<Future<PathPlanning>> inProgress = new LinkedList<>();
            Iterator<PathPlanning> it = pathsToPlan.iterator();
            int i = PATHS_PER_TICK;
            while (i-- > 0 && it.hasNext()) {
                PathPlanning ppi = it.next();
                it.remove();
                inProgress.add(world.env.schedule(ppi));
            }
            for (Future<PathPlanning> f : inProgress) {
                try {
                    f.get().apply();
                } catch (ExecutionException | InterruptedException ex) {
                    Exceptions.add(ex);
                }
            }
//            for (PathPlanning pp : pathsToPlan) {
//                pp.apply();
//            }
//            pathsToPlan.clear();

//            t0 = System.nanoTime() - t0;
//            System.out.printf("Planning %.6f%n", t0 / 1000000000d);
        }
    }
    /**
     * Update the graphical state of an explosion.
     * @param exp the explosion
     */
    void updateExplosion(GroundwarExplosion exp) {
        if (exp.next()) {
            if (exp.half()) {
                if (exp.target != null) {
                    ground.remove(exp.target);
                    // FIXME battle
//                    if (battle != null) {
//                        battle.groundLosses.add(exp.target);
//                    }
                }
            }
        } else {
            ground.explosions.remove(exp);
        }
    }
    /**
     * Conclude the battle.
     * @param winner the winner
     */
    void concludeBattle(Player winner) {
        // FIXME battle reference handling
//        final BattleInfo bi = battle;
//

//        bi.groundwarWinner = winner;
//

//        for (GroundwarUnit u : bi.groundLosses) {
//            u.item.count--;
//            if (u.owner == planet().owner) {
//                bi.defenderGroundLosses++;
//                if (u.item.count <= 0) {
//                    planet().inventory.remove(u.item);
//                }
//            } else {
//                bi.attackerGroundLosses++;
//                if (u.item.count <= 0) {
//                    bi.attacker.inventory.remove(u.item);
//                }
//            }
//        }
//

//        Player np = nonPlayer();
//
//        if (bi.attacker.owner == winner) {
//            planet().takeover(winner);
//
//            BattleSimulator.applyPlanetConquered(planet(), BattleSimulator.PLANET_CONQUER_LOSS);
//
//            // remove unfinished buildings
//            for (Building b : planet().surface.buildings.list()) {
//                if (!b.isComplete()) {
//                    destroyBuilding(b);
//                }
//            }
//            planet().rebuildRoads();
//

//        } else {
//            BattleSimulator.applyPlanetDefended(planet(), BattleSimulator.PLANET_DEFENSE_LOSS);
//        }
//

//        planet().rebuildRoads();
//
//        player().ai.groundBattleDone(this);
//        np.ai.groundBattleDone(this);
//

//        world().scripting.onGroundwarFinish(this);
//

//        battle = null;
//

//        BattlefinishScreen bfs = (BattlefinishScreen)displaySecondary(Screens.BATTLE_FINISH);
//        bfs.displayBattleSummary(bi);
    }
    /** @return Check if one of the fighting parties has run out of units/structures. */
    Player checkWinner() {
        // FIXME winner check!
//        if (battle == null) {
//            return null;
//        }
//        int attackerCount = 0;
//        int defenderCount = 0;
//        for (GroundwarGun g : guns) {
//            if (g.building.enabled && g.building.assignedEnergy != 0) {
//                defenderCount++;
//            }
//        }
//        for (GroundwarUnit u : units) {
//            if (u.owner == planet().owner) {
//                if (!winIngoreUnits.contains(u.model.type)) {
//                    defenderCount++;
//                }
//            } else {
//                if (!winIngoreUnits.contains(u.model.type)) {
//                    attackerCount++;
//                }
//            }
//        }
//        // if attacker looses all of its units, the winner is always the defender
//        if (attackerCount == 0) {
//            return planet().owner;
//        } else
//        if (defenderCount == 0) {
//            return battle.attacker.owner;
//        }
        return null;
    }
    /**
     * Is the given target within the min-max range of the unit.
     * @param u the unit
     * @param target the target unit
     * @return true if within the min-max range
     */
    boolean unitWithinRange(GroundwarUnit u, GroundwarUnit target) {
        return u.inRange(target, u.model.maxRange)
                && !u.inRange(target, u.model.minRange);
    }
    /**
     * Is the given target within the min-max range of the unit.
     * @param u the unit
     * @param target the target unit
     * @return true if within the min-max range
     */
    boolean unitWithinRange(GroundwarUnit u, Building target) {
        return u.inRange(target, u.model.maxRange)
                && !u.inRange(target, u.model.minRange);
    }
    /**
     * Apply groundwar damage to the given building.
     * @param b the target building
     * @param damage the damage amout
     */
    void damageBuilding(Building b, double damage) {
        int hpBefore = b.hitpoints;
        int maxHp = world.getHitpoints(b.type, planet().owner, false);
        b.hitpoints = (int)Math.max(0, b.hitpoints - 1L * damage * b.type.hitpoints / maxHp);

        List<GroundwarGun> guns = ground.guns;
        // if damage passes the half mark
        if ("Defensive".equals(b.type.kind)) {
            if (hpBefore * 2 >= b.type.hitpoints && b.hitpoints * 2 < b.type.hitpoints) {
                int count = world.battle.getTurrets(b.type.id, planet.race).size() / 2;
                int i = guns.size() - 1;
                while (i >= 0 && count > 0) {
                    // remove half of the guns
                    GroundwarGun g = guns.get(i);
                    if (g.building == b) {
                        count--;
                        ground.removeGun(i);
                    }
                    i--;
                }
            }
        }
        // if building got destroyed
        if (hpBefore > 0 && b.hitpoints <= 0) {
            for (int i = guns.size() - 1; i >= 0; i--) {
                GroundwarGun g = ground.guns.get(i);
                if (g.building == b) {
                    ground.removeGun(i);
                }
            }
            // FIXME battle
//            if (battle != null && "Defensive".equals(b.type.kind)) {
//                battle.defenderFortificationLosses++;
//            }
            playSounds.add(SoundType.EXPLOSION_LONG);
            destroyBuilding(b);
        }
        if (!"Defensive".equals(b.type.kind)) {
            runAllocator = true;
        }
    }
    /**
     * Destroy the given building and apply statistics.
     * @param b the target building
     */
    void destroyBuilding(Building b) {
        planet.surface.removeBuilding(b);
        b.hitpoints = 0;

        planet().owner.statistics.buildingsLost.value++;
        planet().owner.statistics.buildingsLostCost.value += b.type.cost * (1 + b.upgradeLevel);

        // FIXME battle
//        if (battle != null) {
//            battle.attacker.owner.statistics.buildingsDestroyed.value++;
//            battle.attacker.owner.statistics.buildingsDestroyedCost.value += b.type.cost * (1 + b.upgradeLevel);
//        }
        runAllocator = true;
    }
    /**
     * Update the properties of the target unit.
     * @param u the unit to update
     */
    void updateUnit(GroundwarUnit u) {
        if (u.paralizedTTL > 0) {
            u.paralizedTTL--;
            if (u.paralizedTTL == 0) {
                u.paralized = null;
            }
        }
        if (u.isDestroyed()) {
            return;
        }
        if (u.model.selfRepairTime > 0) {
            if (u.hp < u.model.hp) {
                u.hp = Math.min(u.model.hp, u.hp + 1.0 * u.model.hp / u.model.selfRepairTime);
            }
        }
        if (ground.minelayers.contains(u) && u.path.size() == 0) {
            Location loc = u.location();
            if (!ground.mines.containsKey(loc)) {
                u.fireAnimPhase++;
                if (u.fireAnimPhase >= u.maxPhase()) {
                    Mine m = new Mine();
                    m.damage = u.damage();
                    m.owner = u.owner;
                    ground.mines.put(loc, m);
                    ground.minelayers.remove(u);
                    u.fireAnimPhase = 0;
                }
            } else {
                ground.minelayers.remove(u);
            }
        } else
        if (u.fireAnimPhase > 0) {
            u.fireAnimPhase++;
            if (u.fireAnimPhase >= u.maxPhase()) {
                if (u.attackUnit != null) {
                    attackUnitEndPhase(u);
                } else
                if (u.attackBuilding != null) {
                    attackBuildingEndPhase(u);
                }
                u.fireAnimPhase = 0;

                if (u.hasValidTarget()

                        && world.config.aiGroundAttackGetCloser
                        && !u.guard
                        && u.attackMove == null

                        && world.battle.getCloserUnits.contains(u.model.type)) {
                    moveOneCellCloser(u);
                }
            }
        } else

        if (u.paralizedTTL == 0) {
            Location am = u.attackMove;
            if (u.attackUnit != null && !u.attackUnit.isDestroyed()) {
                approachTargetUnit(u);
            } else

            if (u.attackBuilding != null && !u.attackBuilding.isDestroyed()) {
                approachTargetBuilding(u);
            } else {
                if (u.attackBuilding != null && u.attackBuilding.isDestroyed()) {
                    if (am != null) {
                        move(u, am.x, am.y);
                        u.attackMove = am;
                    } else {
                        stop(u);
                    }
                } else
                if (u.attackUnit != null && u.attackUnit.isDestroyed()) {
                    if (am != null) {
                        move(u, am.x, am.y);
                        u.attackMove = am;
                    } else {
                        stop(u);
                    }
                }
                if ((am != null || u.path.isEmpty()) && world.battle.directAttackUnits.contains(u.model.type)) {
                    // find a new target in range
                      List<GroundwarUnit> targets = unitsInRange(u);
                    if (targets.size() > 0) {
                        attack(u, ModelUtils.random(targets));
                        u.attackMove = am;
                    } else {
                        List<Building> targets2 = buildingsInRange(u);
                        if (targets2.size() > 0) {
                            attack(u, ModelUtils.random(targets2));
                            u.attackMove = am;
                        }
                    }
                    if (u.attackUnit == null && u.attackBuilding == null

                            && am != null && u.path.isEmpty()) {
                        if (!am.equals(u.location())) {
                            attackMove(u, am.x, am.y);
                        } else {
                            stop(u);
                        }
                    }
                }
            }
            if (!u.path.isEmpty()) {
                moveUnit(u);
                if (u.nextMove == null) {
                    Location loc = u.location();
                    Mine m = ground.mines.get(loc);
                    if (m != null && m.owner != u.owner) {
                        playSounds.add(SoundType.EXPLOSION_MEDIUM);
                        createExplosion(u.x, u.y, ExplosionType.GROUND_RED);
                        damageArea(u.x, u.y, m.damage, 1, m.owner);
                        ground.mines.remove(loc);
                    }
                }
            } else {
                if (u.advanceOnBuilding != null) {
                    u.attackBuilding = u.advanceOnBuilding;
                    u.advanceOnBuilding = null;
                }
                if (u.advanceOnUnit != null) {
                    u.attackUnit = u.advanceOnUnit;
                    u.advanceOnUnit = null;
                }
            }
        }
    }
    /**
     * Try to move the unit one cell closer to its target.
     * @param u the unit
     */
    void moveOneCellCloser(GroundwarUnit u) {
        Location source = u.location();
        double tx = 0d;
        double ty = 0d;
        if (u.attackBuilding != null) {
            tx = u.attackBuilding.location.x + u.attackBuilding.width() / 2d;
            ty = u.attackBuilding.location.y - u.attackBuilding.height() / 2d;
        } else
        if (u.attackUnit != null) {
            Location target = u.attackUnit.location();
            tx = target.x;
            ty = target.y;
        }
        double currentDistance = Math.hypot(tx - source.x, ty - source.y);
        if (currentDistance <= 1.42) {
            return;
        }
        List<Location> neighbors = getPathfinding(null).neighbors.invoke(source);
        if (!neighbors.isEmpty()) {
            Location cell = null;
            double cellDistance = currentDistance;
            for (Location loc : neighbors) {
                double newDistance = Math.hypot(tx - loc.x, ty - loc.y);
                if (newDistance < cellDistance) {
                    cellDistance = newDistance;
                    cell = loc;
                }
            }
            if (cell != null) {
                if (u.attackUnit != null) {
                    u.advanceOnUnit = u.attackUnit;
                    u.attackUnit = null;
                }
                if (u.attackBuilding != null) {
                    u.advanceOnBuilding = u.attackBuilding;
                    u.attackBuilding = null;
                }
                u.inMotionPlanning = true;
                pathsToPlan.add(new PathPlanning(source, cell, u, null));
            }
        }
    }
    /**
     * Approach the target building.
     * @param u the unit who is attacking
     */
    void approachTargetBuilding(GroundwarUnit u) {
        if (unitWithinRange(u, u.attackBuilding)) {
            if (u.nextMove != null) {
                u.path.clear();
                u.path.add(u.nextMove);
            } else
            if (rotateStep(u, centerCellOf(u.attackBuilding))) {
                if (u.cooldown <= 0) {
                    u.fireAnimPhase++;
                    if (u.model.fireSound != null) {
                        playSounds.add(u.model.fireSound);
                    }

                    if (u.model.type == GroundwarUnitType.ROCKET_SLED) {
                        Location loc = centerCellOf(u.attackBuilding);
                        createRocket(u, loc.x, loc.y);
                    }
                    u.cooldown = u.model.delay;
                } else {
                    u.cooldown -= SIMULATION_DELAY;
                }
            }
        } else {
            if (u.path.isEmpty()) {
                Location ul = u.location();
                if (!u.inRange(u.attackBuilding, u.model.maxRange)) {
                    // plot path to the building
                    u.inMotionPlanning = true;
                    pathsToPlan.add(new PathPlanning(ul,

                            buildingNearby(u.attackBuilding, ul), u, null));
                } else {
                    // plot path outside the minimum range
                    Location c = buildingNearby(u.attackBuilding, ul);
                    double angle = ModelUtils.random() * 2 * Math.PI;
                    Location c1 = Location.of(
                            (int)(c.x + (u.model.minRange + 1.4142) * Math.cos(angle)),
                            (int)(c.y + (u.model.minRange + 1.4142) * Math.sin(angle))
                    );

                    u.inMotionPlanning = true;
                    pathsToPlan.add(new PathPlanning(u.location(), c1, u, null));
                }
            }
        }
    }
    /**
     * Returns a reachable location of the building in relation to the source.
     * @param b the building
     * @param initial the source location
     * @return the nearby reachable location
     */
    Location buildingNearby(Building b, final Location initial) {
        final Location destination = centerCellOf(b);

        final Func2<Location, Location, Integer> trueDistance = defaultTrueDistance;

        Comparator<Location> nearestComparator = new Comparator<Location>() {
            @Override
            public int compare(Location o1, Location o2) {
                int d1 = trueDistance.invoke(destination, o1);
                int d2 = trueDistance.invoke(destination, o2);
                int c = Integer.compare(d1, d2);
                if (c == 0) {
                    d1 = trueDistance.invoke(initial, o1);
                    d2 = trueDistance.invoke(initial, o2);
                    c = Integer.compare(d1, d2);
                }
                return c;
            }
        };
        Location result = null;
        for (int x = b.location.x - 1; x < b.location.x + b.width(); x++) {
            Location loc = Location.of(x, b.location.y + 1);
            if (result == null || nearestComparator.compare(result, loc) > 0) {
                result = loc;
            }
            loc = Location.of(x, b.location.y - b.height());
            if (result == null || nearestComparator.compare(result, loc) > 0) {
                result = loc;
            }
        }
        for (int y = b.location.y; y > b.location.y - b.height(); y--) {
            Location loc = Location.of(b.location.x - 1, y);
            if (result == null || nearestComparator.compare(result, loc) > 0) {
                result = loc;
            }
            loc = Location.of(b.location.x + b.width(), y);
            if (result == null || nearestComparator.compare(result, loc) > 0) {
                result = loc;
            }
        }
        return result;
    }
    /**
     * Approach the target unit.
     * @param u the unit who is attacking
     */
    void approachTargetUnit(GroundwarUnit u) {
        // if within range
        if (unitWithinRange(u, u.attackUnit)) {
            if (u.nextMove != null) {
                u.path.clear();
                u.path.add(u.nextMove);
            } else
            if (rotateStep(u, u.attackUnit.location())) {
                if (u.cooldown <= 0) {
                    u.fireAnimPhase++;
                    if (u.model.fireSound != null) {
                        playSounds.add(u.model.fireSound);
                    }
                    if (u.model.type == GroundwarUnitType.PARALIZER) {
                        if (u.attackUnit.paralized == null) {
                            u.attackUnit.paralized = u;
                            u.attackUnit.paralizedTTL = PARALIZED_TTL; // FIXME paralize time duration
                            // deparalize target of a paralizer
                            if (u.attackUnit.model.type == GroundwarUnitType.PARALIZER) {
                                for (GroundwarUnit u2 : ground.units) {
                                    if (u2.paralized == u.attackUnit) {
                                        u2.paralized = null;
                                        u2.paralizedTTL = 0;
                                    }
                                }
                            }
                        } else {
                            // if target already paralized, look for another
                            u.attackUnit = null;
                        }
                    }
                    if (u.model.type == GroundwarUnitType.ROCKET_SLED) {
                        createRocket(u, u.attackUnit.x, u.attackUnit.y);
                    }
                } else {
                    u.cooldown -= SIMULATION_DELAY;
                }
            }
        } else {
            if (u.path.isEmpty()) {
                u.inMotionPlanning = true;
                if (u.attackMove == null) {
                    // plot path
                    pathsToPlan.add(new PathPlanning(u.location(), u.attackUnit.location(), u, null));
                } else {
                    pathsToPlan.add(new PathPlanning(u.location(), u.attackMove, u, enemy(u.owner)));
                }
            } else {
                if (u.attackMove == null) {
                    Location ep = u.path.get(u.path.size() - 1);
                    // if the target unit moved since last
                    double dx = ep.x - u.attackUnit.x;
                    double dy = ep.y - u.attackUnit.y;
                    if (Math.hypot(dx, dy) > 1 && !u.attackUnit.path.isEmpty()) {
                        u.path.clear();
                        u.inMotionPlanning = true;
                        pathsToPlan.add(new PathPlanning(u.location(), u.attackUnit.location(), u, null));
                    }
                }
            }
        }
    }

    /**
     * Attack the target building.
     * @param u the unit who is attacking
     */
    void attackBuildingEndPhase(GroundwarUnit u) {
        u.cooldown = u.model.delay;
        // for rocket sleds, damage is inflicted by the rocket impact
        if (u.model.type == GroundwarUnitType.KAMIKAZE

        && u.hp * 10 < u.model.hp) {
            special(u);
        } else
        if (u.model.type != GroundwarUnitType.ROCKET_SLED) {
            damageBuilding(u.attackBuilding, u.damage());
        }

        if (u.attackBuilding.isDestroyed()) {
            // TODO demolish animation?
            u.attackBuilding = null;
        }
    }

    /**
     * Attack the target unit.
     * @param u the unit who is attacking
     */
    void attackUnitEndPhase(GroundwarUnit u) {
        u.cooldown = u.model.delay;
        if (u.model.type == GroundwarUnitType.ROCKET_SLED) {
            return;
        }
        if (u.model.type == GroundwarUnitType.ARTILLERY) {
            damageArea(u.attackUnit.x, u.attackUnit.y, u.damage(), u.model.area, u.owner);
        } else
        if (u.model.type == GroundwarUnitType.KAMIKAZE

            && u.hp * 10 < u.model.hp) {
            special(u);
        } else
        if (unitWithinRange(u, u.attackUnit)) {
            if (!u.attackUnit.isDestroyed()) {
                u.attackUnit.damage(u.damage());
                if (u.attackUnit.isDestroyed()) {
                    playSounds.add(u.attackUnit.model.destroy);
                    createExplosion(u.attackUnit, ExplosionType.GROUND_RED);
                    // if the unit destroyed was a paralizer, deparalize everyone
                    if (u.attackUnit.model.type == GroundwarUnitType.PARALIZER) {
                        for (GroundwarUnit u2 : ground.units) {
                            if (u2.paralized == u.attackUnit) {
                                u2.paralized = null;
                                u2.paralizedTTL = 0;
                            }
                        }
                    }

                    u.attackUnit.owner.statistics.vehiclesLost.value++;
                    u.attackUnit.owner.statistics.vehiclesLostCost.value += world.research(u.attackUnit.model.id).productionCost;

                    u.owner.statistics.vehiclesDestroyed.value++;
                    u.owner.statistics.vehiclesDestroyedCost.value += world.research(u.attackUnit.model.id).productionCost;

                    u.attackUnit = null;
                }
            } else {
                u.attackUnit = null;
            }
        }
    }
    /**
     * Update the properties of the given gun.
     * @param g the target gun
     */
    void updateGun(GroundwarGun g) {
        if (!g.building.enabled) {
            return;
        }
        // underpowered guns will not fire
        double powerRate = 1d * g.building.assignedEnergy / g.building.getEnergy();
        double indexRate = (g.index + 0.5) / g.count;
        if (indexRate >= powerRate) {
            g.fireAnimPhase = 0;
            return;
        }

        if (g.fireAnimPhase > 0) {
            g.fireAnimPhase++;
            if (g.fireAnimPhase >= g.maxPhase()) {
                if (g.attack != null && !g.attack.isDestroyed()

                        && g.inRange(g.attack)) {
                    if (!g.attack.isDestroyed()) {
                        g.attack.damage(g.damage());
                        if (g.attack.isDestroyed()) {
                            playSounds.add(g.attack.model.destroy);
                            createExplosion(g.attack, ExplosionType.GROUND_RED);

                            g.attack.owner.statistics.vehiclesLost.value++;
                            g.attack.owner.statistics.vehiclesLostCost.value += world.research(g.attack.model.id).productionCost;

                            g.owner.statistics.vehiclesDestroyed.value++;
                            g.owner.statistics.vehiclesDestroyedCost.value += world.research(g.attack.model.id).productionCost;

                            g.attack = null;
                        }
                    } else {
                        g.attack = null;
                    }
                }
                g.cooldown = g.model.delay;
                g.fireAnimPhase = 0;
            }
        } else {
            if (g.attack != null && !g.attack.isDestroyed()

                    && g.inRange(g.attack)) {
                if (rotateStep(g, g.attack.center())) {
                    if (g.cooldown <= 0) {
                        g.fireAnimPhase++;
                        playSounds.add(g.model.fire);
                    } else {
                        g.cooldown -= SIMULATION_DELAY;
                    }
                }
            } else {
                g.attack = null;
                // find a new target
                List<GroundwarUnit> targets = unitsInRange(g);
                if (targets.size() > 0) {
                    g.attack = ModelUtils.random(targets);
                }
            }
        }
    }
    /**
     * Returns the center cell op the given building.
     * @param b the building
     * @return the location of the center cell
     */
    Location centerCellOf(Building b) {
        return Location.of(b.location.x + b.tileset.normal.width / 2,

                b.location.y - b.tileset.normal.height / 2);
    }
    /**
     * Rotate the structure towards the given target angle by a step.
     * @param gun the gun in question
     * @param target the target point
     * @return rotation done?
     */
    boolean rotateStep(GroundwarGun gun, Point target) {
        Point pg = gun.center();
        double targetAngle = Math.atan2(target.y - pg.y, target.x - pg.x);

        double currentAngle = gun.normalizedAngle();

        double diff = targetAngle - currentAngle;
        if (diff < -Math.PI) {
            diff = 2 * Math.PI - diff;
        } else
        if (diff > Math.PI) {
            diff -= 2 * Math.PI;

        }
        double anglePerStep = 2 * Math.PI * gun.model.rotationTime / gun.model.angles() / SIMULATION_DELAY;
        if (Math.abs(diff) < anglePerStep) {
            gun.angle = targetAngle;
            return true;
        }
        gun.angle += Math.signum(diff) * anglePerStep;
        return false;
    }
    /**
     * Find the units within the range of the gun.
     * @param g the gun
     * @return the units in range
     */
    List<GroundwarUnit> unitsInRange(GroundwarUnit g) {
        List<GroundwarUnit> result = new ArrayList<>();
        for (GroundwarUnit u : ground.units) {
            if (u.owner != g.owner && !u.isDestroyed()

                    && g.inRange(u, g.model.maxRange)
                    && !g.inRange(u, g.model.minRange)) {
                result.add(u);
            }
        }
        return result;
    }
    /**
     * Find the units within the range of the gun.
     * @param g the gun
     * @return the units in range
     */
    List<Building> buildingsInRange(GroundwarUnit g) {
        List<Building> result = new ArrayList<>();
        for (Building u : planet.surface.buildings.iterable()) {
            if (planet().owner != g.owner && !u.isDestroyed()

                    && g.inRange(u, g.model.maxRange)
                    && !g.inRange(u, g.model.minRange) && u.type.kind.equals("Defensive")) {
                result.add(u);
            }
        }
        return result;
    }
    /**
     * Find the units within the range of the gun.
     * @param g the gun
     * @return the units in range
     */
    List<GroundwarUnit> unitsInRange(GroundwarGun g) {
        List<GroundwarUnit> result = new ArrayList<>();
        for (GroundwarUnit u : ground.units) {
            if (u.owner != g.owner && !u.isDestroyed()

                    && g.inRange(u)) {
                result.add(u);
            }
        }
        return result;
    }
    /** The composite record to return the rotation angles. */
    static class RotationAngles {
        /** The unit's current angle. */
        double currentAngle;
        /** The target angle. */
        double targetAngle;
        /** The difference to turn. */
        double diff;
    }
    /**
     * Computes the rotation angles.
     * @param u the unit
     * @param target the target location
     * @return the angles
     */
    RotationAngles computeRotation(GroundwarUnit u, Location target) {
        RotationAngles result = new RotationAngles();

        Point pg = u.center();
        Point tg = planet.surface.center(target);
        if (tg.y - pg.y == 0 && tg.x - pg.x == 0) {
            result.targetAngle = u.normalizedAngle();
            result.currentAngle = result.targetAngle;
            result.diff = 0;
        } else {
            result.targetAngle = Math.atan2(tg.y - pg.y, tg.x - pg.x);

            result.currentAngle = u.normalizedAngle();

            result.diff = result.targetAngle - result.currentAngle;
            if (result.diff < -Math.PI) {
                result.diff += 2 * Math.PI;
            } else
            if (result.diff > Math.PI) {
                result.diff -= 2 * Math.PI;

            }
        }

        return result;
    }
    /**
     * Rotate the structure towards the given target angle by a step.
     * @param u the gun in question
     * @param target the target point
     * @return rotation done?
     */
    boolean rotateStep(GroundwarUnit u, Location target) {
        RotationAngles ra = computeRotation(u, target);
        double anglePerStep = 2 * Math.PI * u.model.rotationTime / u.angleCount() / SIMULATION_DELAY;
        if (Math.abs(ra.diff) < anglePerStep) {
            u.angle = ra.targetAngle;
            return true;
        }
        u.angle += Math.signum(ra.diff) * anglePerStep;
        return false;
    }
    /**
     * Checks if the given target location requires the unit to rotate before move.
     * @param u the unit
     * @param target the target location
     * @return true if rotation is needed
     */
    boolean needsRotation(GroundwarUnit u, Location target) {
        RotationAngles ra = computeRotation(u, target);
        double anglePerStep = 2 * Math.PI * u.model.rotationTime / u.angleCount() / SIMULATION_DELAY;
        return Math.abs(ra.diff) >= anglePerStep;
    }
    /**
     * Plan a new route to the current destination.
     * @param u the unit.
     */
    void repath(final GroundwarUnit u) {
        if (u.path.size() > 0) {
            final Location current = u.location();
            final Location goal = u.path.get(u.path.size() - 1);
            u.path.clear();
            u.nextMove = null;
            u.nextRotate = null;
            u.inMotionPlanning = true;
            pathsToPlan.add(new PathPlanning(current, goal, u, u.attackMove != null ? enemy(u.owner) : null));
        }
    }
    /**
     * Move an unit by a given amount into the next path location.
     * @param u The unit to move one step.

     */
    void moveUnit(GroundwarUnit u) {
        if (u.isDestroyed()) {
            return;
        }
        if (u.yieldTTL > 0) {
            u.yieldTTL--;
            if (u.yieldTTL == 0) {
                // trigger replanning
                repath(u);
                return;
            }
        }
        if (u.nextMove == null) {
            u.nextMove = u.path.get(0);
            u.nextRotate = u.nextMove;

            // is the next move location still passable?
            if (!isPassable(u.nextMove.x, u.nextMove.y)) {
                // trigger replanning
                repath(u);
                return;
            }

        }

        if (u.nextRotate != null && rotateStep(u, u.nextRotate)) {
            u.nextRotate = null;
        }
        if (u.nextRotate == null) {
            moveUnitStep(u, SIMULATION_DELAY);
        }
    }

    /**
     * Move the ground unit one step.
     * @param u the unit
     * @param time the available time
     */
    void moveUnitStep(GroundwarUnit u, double time) {
        double dv = 1.0 * time / u.model.movementSpeed / 28;
        // detect collision
        for (GroundwarUnit gu : ground.units) {
            if (gu != u) {
                int minx = (int)Math.floor(gu.x);
                int miny = (int)Math.floor(gu.y);
                int maxx = (int)Math.ceil(gu.x);
                int maxy = (int)Math.ceil(gu.y);
                // check if our next position collided with the movement path of someone else
                if (minx <= u.nextMove.x && u.nextMove.x <= maxx

                        && miny <= u.nextMove.y && u.nextMove.y <= maxy) {
                    // yield
                    dv = 0;
                    if (u.yieldTTL <= 0) {
                        u.yieldTTL = ModelUtils.randomInt(YIELD_TTL) + YIELD_TTL / 2;
                    }
                    break;
                }
            }
        }
        if (dv > 0) {
            u.yieldTTL = 0;
            double distanceToTarget = (u.nextMove.x - u.x) * (u.nextMove.x - u.x)
                    + (u.nextMove.y - u.y) * (u.nextMove.y - u.y);
            if (distanceToTarget < dv * dv) {
                ground.updateUnitLocation(u, u.nextMove.x, u.nextMove.y, false);

                u.nextMove = null;
                u.path.remove(0);

                double remaining = Math.sqrt(dv * dv - distanceToTarget);
                if (!u.path.isEmpty()) {
                    Location nextCell = u.path.get(0);
                    if (!needsRotation(u, nextCell)) {
                        double time2 = remaining * u.model.movementSpeed * 28;
                        u.nextMove = nextCell;
                        moveUnitStep(u, time2);
                    }
                }

            } else {
                double angle = Math.atan2(u.nextMove.y - u.y, u.nextMove.x - u.x);
                ground.updateUnitLocation(u, dv * Math.cos(angle), dv * Math.sin(angle), true);
            }
        }
    }
    /**
     * Stop the currently selected objects (guns and vehicles).
     * @param owner the owner
     * @return true if there was anything to stop
     */
    public boolean stopSelectedObjects(Player owner) {
        boolean stopped = false;
        for (GroundwarUnit u : ground.selectedUnits) {
            if (controlAll || u.owner == owner) {
                stopped = true;
                stop(u);
                u.guard = true;
            }
        }
        for (GroundwarGun g : ground.selectedGuns) {
            if (controlAll || g.owner == owner) {
                stopped = true;
                g.attack = null;
            }
        }
        return stopped;
    }
    /**
     * Instruct the selected units of the owner to attack
     * move to the specified location.
     * @param lm the target location
     * @param owner the owner
     * @return true command was accepted
     */
    public boolean attackMoveSelectedUnits(Location lm, Player owner) {
        boolean attacked = false;
        for (GroundwarUnit u : ground.selectedUnits) {
            if (controlAll || u.owner == owner) {
                if (world.battle.directAttackUnits.contains(u.model.type)) {
                    move(u, lm.x, lm.y);
                    u.attackMove = lm;
                    u.guard = true;
                    attacked = true;
                }
            }
        }
        return attacked;
    }
    /**
     * Attack the building or unit at the specified location.
     * @param gu the potential target unit
     * @param b building at this location
     * @param owner the owner
     * @return true if command accepted
     */
    public boolean attackSelectedUnits(GroundwarUnit gu, Building b, Player owner) {
        boolean attacked = false;
        for (GroundwarUnit u : ground.selectedUnits) {
            if (controlAll || u.owner == owner) {
                if (world.battle.directAttackUnits.contains(u.model.type)) {
                    if (b != null && planet().owner != u.owner

                            && u.model.type != GroundwarUnitType.PARALIZER) {
                        attack(u, b);
                        u.guard = false;
                        attacked = true;
                    } else {
                        if (gu != null) {
                            attack(u, gu);
                            attacked = true;
                            u.guard = false;
                        }
                    }
                }
            }
        }
        for (GroundwarGun g : ground.selectedGuns) {
            if (controlAll || g.owner == owner) {
                if (gu != null) {
                    attack(g, gu);
                    attacked = true;
                }
            }
        }
        return attacked;
    }
    /**
     * Update rocket properties.
     * @param rocket the rocket
     */
    void updateRocket(GroundwarRocket rocket) {
        double dv = 1.0 * SIMULATION_DELAY / rocket.movementSpeed / 28;
        double distanceToTarget = Math.hypot(rocket.targetX - rocket.x, rocket.targetY - rocket.y);
        if (distanceToTarget < dv) {
            // target reached, check for enemy rocket jammers
            boolean jammed = isRocketJammed(rocket, 0.5);
            if (!jammed) {
                // if no jammers, affect area
                damageArea(rocket.targetX, rocket.targetY, rocket.damage, rocket.area, rocket.owner);
            }
            createExplosion(rocket.x, rocket.y, ExplosionType.GROUND_ROCKET_2);
            ground.rockets.remove(rocket);
        } else {
            double angle = Math.atan2(rocket.targetY - rocket.y, rocket.targetX - rocket.x);
            rocket.x += dv * Math.cos(angle);
            rocket.y += dv * Math.sin(angle);
            rocket.fireAnimPhase++;

            if (isRocketJammed(rocket, 0.5)) {
                createExplosion(rocket.x, rocket.y, ExplosionType.GROUND_ROCKET_2);
                ground.rockets.remove(rocket);
            }
        }

    }
    /**

     * Check if any enemy uint is within jamming range?
     * @param rocket the rocket
     * @param penetrationRatio how far may the rocket travel into the range before it is reported as jammed?
     * @return is jammed
     */
    boolean isRocketJammed(GroundwarRocket rocket, double penetrationRatio) {
        for (GroundwarUnit u : ground.units) {
            if (u.owner != rocket.owner

                    && u.model.type == GroundwarUnitType.ROCKET_JAMMER && u.paralizedTTL == 0) {
                double distance = Math.hypot(u.x - rocket.x, u.y - rocket.y);
                if (distance < u.model.maxRange * penetrationRatio) {
                    if (u.fireAnimPhase == 0) {
                        u.fireAnimPhase++;
                    }
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Create a rocket.
     * @param sender the sender object
     * @param x the target spot
     * @param y the target spot
     */
    void createRocket(GroundwarUnit sender, double x, double y) {
        GroundwarRocket rocket = new GroundwarRocket(world.battle.groundRocket);
        rocket.x = sender.x;
        rocket.y = sender.y;
        rocket.owner = sender.owner;
        rocket.targetX = x;
        rocket.targetY = y;
        rocket.movementSpeed = 25; // FIXME rocket movement speed

        rocket.damage = sender.damage();
        rocket.area = sender.model.area;

        Point pg = sender.center();
        Point tg = planet.surface.center(x, y);
        rocket.angle = Math.atan2(tg.y - pg.y, tg.x - pg.x);

        ground.rockets.add(rocket);
    }
    /**
     * Record to store the center and radius of the available location set.
     * @author akarnokd, 2011.10.01.
     */
    static class CenterAndRadius {
        /** The center X coordinate. */
        int icx;
        /** The center Y coordinate. */
        int icy;
        /** The maximum radius. */
        int rmax;
    }
    /**
     * Compute the placement circle's center and radius.
     * @param locations the set of locations
     * @return the center and radius
     */
    CenterAndRadius computePlacementCircle(Iterable<Location> locations) {
        // locate geometric center
        double cx = 0;
        double cy = 0;
        boolean first = true;
        int minX = 0;
        int maxX = 0;
        int minY = 0;
        int maxY = 0;
        int count = 0;
        for (Location loc : locations) {
            cx += loc.x;
            cy += loc.y;
            if (first) {
                minX = loc.x;
                maxX = loc.x;
                minY = loc.y;
                maxY = loc.y;
                first = false;
            } else {
                minX = Math.min(minX, loc.x);
                minY = Math.min(minY, loc.y);
                maxX = Math.max(maxX, loc.x);
                maxY = Math.max(maxY, loc.y);
            }
            count++;
        }
        cx /= count;
        cy /= count;
        CenterAndRadius result = new CenterAndRadius();
        result.icx = (int)cx;
        result.icy = (int)cy;
        // expand radially and place vehicles
        result.rmax = Math.max(Math.max(Math.abs(result.icx - minX), Math.abs(result.icx - maxX)),

                Math.max(Math.abs(result.icy - minY), Math.abs(result.icy - maxY))) + 1;
        return result;
    }
    /**
     * Place the non-player units near the geometric center of the deployment location.
     * @param atBuildings place at buildings
     * @param gus the list of units
     */
    public void placeGroundUnits(boolean atBuildings, LinkedList<GroundwarUnit> gus) {
        Set<Location> locations = getDeploymentLocations(atBuildings, true);
        CenterAndRadius car = computePlacementCircle(locations);
        if (atBuildings) {
            placeAroundInCircle(gus, locations, car.icx, car.icy, car.rmax);
        } else {
            Location furthest = ModelUtils.random(locations);
            placeAroundInCircle(gus, locations, furthest.x, furthest.y, car.rmax);
        }
    }

    /**
     * Place units in circular pattern around the center location.
     * @param gus the list of units
     * @param locations the set of locations
     * @param icx the center point
     * @param icy the center point
     * @param rmax the maximum attempt radius
     */
    public void placeAroundInCircle(LinkedList<GroundwarUnit> gus,
            Set<Location> locations, int icx, int icy, int rmax) {
        if (!gus.isEmpty() && !locations.isEmpty()) {
            outer:
            for (int r = 0; r < rmax; r++) {
                for (int x = icx - r; x <= icx + r; x++) {
                    if (tryPlaceNonPlayerUnit(x, icy + r, locations, gus)) {
                        break outer;
                    }
                    if (tryPlaceNonPlayerUnit(x, icy - r, locations, gus)) {
                        break outer;
                    }
                }
                for (int y = icy + r; y >= icy - r; y--) {
                    if (tryPlaceNonPlayerUnit(icx - r, y, locations, gus)) {
                        break outer;
                    }
                    if (tryPlaceNonPlayerUnit(icx + r, y, locations, gus)) {
                        break outer;
                    }
                }
            }
        }
    }
    /**
     * Try placing an unit at the specified location and if successful,
     * remove the location and unit from the corresponding collection.
     * @param x the location X
     * @param y the location Y
     * @param locations the set of locations
     * @param units the list of units
     * @return if no more units or locations available
     */
    public boolean tryPlaceNonPlayerUnit(int x, int y,

            Set<Location> locations,

            LinkedList<GroundwarUnit> units) {
        Location loc = Location.of(x, y);
        if (locations.contains(loc)) {
            locations.remove(loc);
            GroundwarUnit u = units.removeFirst();
            u.x = x;
            u.y = y;
            ground.add(u);
        }
        return units.isEmpty() || locations.isEmpty();
    }
    /**
     * Create the non-player units.
     * @param atBuildings place them at buildings
     * @param alternative use the alternative image?
     * @param iis the inventory
     * @param gus the unit holder
     */
    public void createGroundUnits(
            boolean atBuildings,

            boolean alternative,

            Iterable<InventoryItem> iis,
            LinkedList<GroundwarUnit> gus) {
        for (InventoryItem ii : iis) {
            if (ii.type.category == ResearchSubCategory.WEAPONS_TANKS
                    || ii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {

                BattleGroundVehicle bge = world.battle.groundEntities.get(ii.type.id);

                for (int i = 0; i < ii.count; i++) {
                    GroundwarUnit u = new GroundwarUnit(alternative

                            ? bge.normal : bge.alternative);

                    u.owner = ii.owner;
                    u.item = ii;
                    u.model = bge;
                    u.hp = u.model.hp;

                    gus.add(u);
                }
            }
        }
    }
    /**
     * Move the selected units of the player to the given location.
     * @param lm the target location
     * @param owner the owner
     * @return true if command accepted
     */
    public boolean moveSelectedUnits(Location lm, Player owner) {
        boolean moved = false;
        for (GroundwarUnit u : ground.selectedUnits) {
            if (controlAll || u.owner == owner) {
                moved = true;
                move(u, lm.x, lm.y);
            }
        }
        return moved;
    }
    @Override
    public Planet planet() {
        return planet;
    }
}
