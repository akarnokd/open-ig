/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.U;
import hu.openig.core.Location;
import hu.openig.core.Pathfinding;
import hu.openig.model.BattleProjectile.Mode;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A selectable spacewar structure with hitpoints.
 * @author akarnokd, 2011.08.16.
 */
public class SpacewarStructure extends SpacewarObject implements WarUnit {
    /** The spacewar structure type. */
    public enum StructureType {
        /** A ship. */
        SHIP,
        /** A station. */
        STATION,
        /** A projector. */
        PROJECTOR,
        /** A shield. */
        SHIELD,
        /** Rocket mode. */
        ROCKET,
        /** Multi rocket mode. */
        MULTI_ROCKET,
        /** Bomb mode. */
        BOMB,
        /** Virus mode. */
        VIRUS_BOMB
    }
    /** The container fleet. */
    public Fleet fleet;
    /** The referenced inventory item. */
    public InventoryItem item;
    /** The container planet. */
    public Planet planet;
    /** The building reference. */
    public Building building;
    /** The information image. */
    public String infoImageName;
    /** Is the ship selected? */
    public boolean selected;
    /** The value of this structure (e.g., the build cost). */
    public int value;
    /** The shield hitpoints. */
    public double shield;
    /** The maximum shield hitpoints. */
    public double shieldMax;
    /** The ECM level. */
    public int ecmLevel;
    /** The destruction sound. */
    public SoundType destruction;
    /** The structure type. */
    public StructureType type;
    /** The available weapon ports. */
    public final List<SpacewarWeaponPort> ports = new ArrayList<>();
    /** The position with fractional precision in grid coordinates. */
    public double gridX;
    /** The position with fractional precision in grid coordinates. */
    public double gridY;
    /** The angle images of the spaceship. */
    public BufferedImage[] angles;
    /** The symmetrically trimmed width of the object image at 0 rotation angle. */
    public int trimmedWidth;
    /** The symmetrically trimmed height of the object image at 0 rotation angle. */
    public int trimmedHeight;
    /** The rotation speed: millisecond time per angle element. */
    public double rotationTime;
    /** The movement speed: Milliseconds per one pixel. */
    public int movementSpeed;
    /** The range of the shortest beam-weapon. */
    public int minimumRange;
    /** The range of the longest beam-weapon. */
    public int maximumRange;
    /** The number of batched fighters. Once hp reaches zero, this number is reduced, the batch will disappear when the count reaches zero. */
    public int count = 1;
    /** The loss counter. */
    public int loss;
    /** The attack target. */
    public SpacewarStructure attackUnit;
    /** The target of the attack-move if non-null. */
    public Location attackMove;
    /** The next move rotation. */
    public Location nextRotate;
    /** The next move location. */
    public Location nextMove;
    /** The current movement path to the target. */
    public final LinkedList<Location> path = new LinkedList<>();
    /** The object the pathfinding used by this structure. */
    public Pathfinding pathfinding;
    /** Attack anything in range. */
    public boolean guard;
    /** The structure intends to leave the battle space. */
    public boolean flee;
    /** The unit has movement plans. */
    public boolean hasPlannedMove = false;
    /** Kamikaze mode if greater than zero, indicates impact damage. */
    public double kamikaze;
    /** Kamikaze mode time to live. */
    public int ttl;
    /** The available hitpoints. */
    public double hp;
    /** The maximum hitpoints. */
    public int hpMax;
    /** The technology id of this ship. */
    public final String techId;
    /** The technology category. */
    public final ResearchSubCategory category;
    /** The battle efficiency settings of this projector. */
    public List<BattleEfficiencyModel> efficiencies;
    /**
     * Construct a structure from a technology.
     * @param rt the technology
     */
    public SpacewarStructure(ResearchType rt) {
        this.techId = rt.id;
        this.category = rt.category;
    }
    /**
     * Construct a structure from building type.
     * @param bt the building type
     */
    public SpacewarStructure(BuildingType bt) {
        this.techId = bt.id;
        ResearchSubCategory cat = ResearchSubCategory.BUILDINGS_MILITARY;
        if (bt.kind.equals("Guns")) {
            cat = ResearchSubCategory.BUILDINGS_GUNS;
        }
        this.category = cat;
    }
    /**
     * Construct a structure with the given technology parameters.
     * @param techId the technology id
     * @param category the category
     */
    public SpacewarStructure(String techId, ResearchSubCategory category) {
        this.techId = techId;
        this.category = category;
    }
    /** @return the type name describing this structure. */
    public String getType() {
        if (type == StructureType.SHIP || type == StructureType.STATION) {
            return item.type.name;
        }
        return building.type.name;
    }
    /** @return the damage in percent. */
    public int getDamage() {
        return (int)(100 * (hpMax - hp) / hpMax);
    }
    /** @return the firepower of the beam weapons on board. */
    public double getFirepower() {
        double sum = 0;
        for (SpacewarWeaponPort p : ports) {
            if (p.projectile.mode == Mode.BEAM) {
                sum += p.damage(owner) * p.count;
            }
        }
        return sum;
    }
    /** Compute the longest and shortest weapon ranges. */
    public void computeRanges() {
        minimumRange = Integer.MAX_VALUE;
        maximumRange = Integer.MIN_VALUE;
        for (SpacewarWeaponPort p : ports) {
            if (p.projectile.mode == Mode.BEAM) {
                minimumRange = Math.min(minimumRange, p.projectile.range);
                maximumRange = Math.max(maximumRange, p.projectile.range);
            }
        }
        if (minimumRange == Integer.MAX_VALUE) {
            minimumRange = 0;
        }
        if (maximumRange == Integer.MIN_VALUE) {
            maximumRange = 0;
        }
    }
    @Override
    public BufferedImage get() {
        // -0.5 .. +0.5
        double a = U.normalizedAngle(angle) / Math.PI / 2;
        if (a < 0) {
            a = 1 + a;

        }
        return angles[((int)Math.round(angles.length * a)) % angles.length];
    }
    @Override
    public Location location() {
        return Location.of((int)Math.round(gridX), (int)Math.round(gridY));
    }

    @Override
    public void setLocation(double x, double y) {
        this.gridX = x;
        this.gridY = y;
    }

    @Override
    public Point2D.Double exactLocation() {
        return new Point2D.Double(gridX, gridY);
    }

    @Override
    public void setAngle(double angle) {
        this.angle = angle;
    }

    @Override
    public double getAngle() {
        return this.angle;
    }

    @Override
    public void increaseAngle(double angle) {
        this.angle += angle;
    }

    @Override
    public int getAngleCount() {
        return angles.length;
    }

    @Override
    public int getRotationTime() {
        return (int)rotationTime;
    }

    @Override
    public int getMovementSpeed() {
        return movementSpeed;
    }

    @Override
    public WarUnit getAttackTarget() {
        return attackUnit;
    }
    @Override
    public Location attackMoveLocation() {
        return attackMove;
    }
    @Override
    public Location getNextMove() {
        return nextMove;
    }

    @Override
    public void setNextMove(Location nextMove) {
        this.nextMove = nextMove;
    }

    @Override
    public void clearNextMove() {
        this.nextMove = null;
    }

    @Override
    public Location getNextRotate() {
        return nextRotate;
    }

    @Override
    public void setNextRotate(Location nextRotate) {
        this.nextRotate = nextRotate;
    }

    @Override
    public void clearNextRotate() {
        this.nextRotate = null;
    }
    /** @return true if the unit is moving. */
    @Override
    public boolean isMoving() {
        return nextMove != null || hasPlannedMove;
    }
    /** @return true if the unit is in between cells. */
    @Override
    public boolean inMotion()  {
        return (gridX % 1 != 0) || (gridY % 1 != 0);
    }

    @Override
    public LinkedList<Location> getPath() {
        return path;
    }

    @Override
    public Player owner() {
        return owner;
    }
    @Override
    public void setPathingMethod(Pathfinding pathfinding) {
        this.pathfinding = pathfinding;
    }
    @Override
    public Pathfinding getPathingMethod() {
        return pathfinding;
    }
    /**
     * Merges the new path.
     * @param newPath the new path to follow
     */
    @Override
    public void setPath(List<Location> newPath) {
        path.clear();
        path.addAll(newPath);
        hasPlannedMove = true;
    }
    /**
     * @return Creates a new deep copy of this record.
     */
    public SpacewarStructure copy() {
        SpacewarStructure r = new SpacewarStructure(techId, category);
        r.x = x;
        r.y = y;
        r.gridX = gridX;
        r.gridY = gridY;
        r.owner = owner;
        r.angle = angle;
        r.angles = angles;
        r.trimmedWidth = trimmedWidth;
        r.trimmedHeight = trimmedHeight;
        r.count = count;
        r.destruction = destruction;
        r.ecmLevel = ecmLevel;
        r.hp = hp;
        r.hpMax = hpMax;
        r.value = value;
        r.infoImageName = infoImageName;
        r.item = item;
        r.movementSpeed = movementSpeed;
        for (SpacewarWeaponPort w : ports) {
            r.ports.add(w.copy());
        }
        r.rotationTime = rotationTime;
        r.selected = selected;
        r.shield = shield;
        r.shieldMax = shieldMax;
        r.minimumRange = minimumRange;
        r.maximumRange = maximumRange;
        r.building = building;
        r.type = type;
        r.planet = planet;
        r.fleet = fleet;

        return r;
    }
    /**
     * Returns a list of those BEAM weapon ports, which have the target structure in range.
     * @param target the target structure
     * @return the list of weapon ports
     */
    public List<SpacewarWeaponPort> inRange(SpacewarObject target) {
        List<SpacewarWeaponPort> result = new ArrayList<>();
        double d = Math.hypot(x - target.x, y - target.y);
        for (SpacewarWeaponPort p : ports) {
            if (p.projectile.range >= d && p.projectile.mode == Mode.BEAM) {
                result.add(p);
            }
        }

        return result;
    }
    /**
     * Apply damage to this structure, considering the shield level and
     * counts.
     * @param points the damage points to apply
     * @return is at least an unit destroyed
     */
    public boolean damage(double points) {
        double hp0 = hp;

        if (shield > 0) {
            if (shield > points) {
                shield -= points;
                points = 0;
            } else {
                points -= shield;
                shield = 0;
            }
        }
        hp -= points;

        boolean result = hp <= 0;
        while (count > 0 && hp <= 0) {
            hp += hpMax;
            count--;
            loss++;
        }
        if (count == 0) {
            hp = 0;
        }

        double ratio = 1.0 * (hp0 - hp) / hpMax;

        // subsystem damage
        if (item != null && ratio > 0) {
            List<InventorySlot> iss = new ArrayList<>();
            for (InventorySlot is0 : item.slots.values()) {
                if (!is0.slot.fixed && is0.type != null && is0.count > 0) {
                    iss.add(is0);
                }
            }
            if (!iss.isEmpty()) {
                InventorySlot is = ModelUtils.random(iss);
                int ihp = is.hpMax(item.owner);
                is.hp = Math.max(is.hp - ihp * ratio, 0);

                is.count = Math.min(is.count, (int)Math.round(is.slot.max * is.hp / ihp));
                for (SpacewarWeaponPort wp : ports) {
                    if (wp.is == is) {
                        wp.count = is.count;
                    }
                }
            }
        }

        return result;
    }

    @Override
    public boolean hasPlannedMove() {
        return hasPlannedMove;
    }

    @Override
    public void setHasPlannedMove(boolean hasPlannedMove) {
        this.hasPlannedMove = hasPlannedMove;
    }

    /** @return is the structure destroyed? */
    public boolean isDestroyed() {
        return count <= 0;
    }
    @Override
    public String toString() {
        return String.format("Type = %s, Count = %s, Owner = %s, HP = %s, Shield = %s, Parent = %s"
                , item.type.category, count, owner.id, hp, shield, (fleet != null ? fleet.name() : (planet != null ? planet.id : "")));
    }
    /**
     * @return checks if this structure has direct fire capability with a beam weapon.
     */
    public boolean canDirectFire() {
        if (type == StructureType.SHIP || type == StructureType.PROJECTOR || type == StructureType.STATION) {
            for (SpacewarWeaponPort wp : ports) {
                if (wp.projectile.mode == Mode.BEAM && wp.count > 0) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Returns an efficiency settings for the target.
     * @param target the target structure
     * @return the model or null if no such model found
     */
    public BattleEfficiencyModel getEfficiency(SpacewarStructure target) {
        if (efficiencies != null) {
            for (BattleEfficiencyModel m : efficiencies) {
                if (m.matches(target)) {
                    return m;
                }
            }
        }
        return null;
    }
    /**
     * Returns whether the space structure is a representation of a rocket projectile.
     * @return true if the space structure is a rocket
     */
    public boolean isRocket() {
        return type == StructureType.BOMB
                || type == StructureType.VIRUS_BOMB
                || type == StructureType.ROCKET
                || type == StructureType.MULTI_ROCKET;
    }
}
