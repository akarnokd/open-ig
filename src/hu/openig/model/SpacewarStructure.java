/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.model.BattleProjectile.Mode;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A selectable spacewar structure with hitpoints.
 * @author akarnokd, 2011.08.16.
 */
public class SpacewarStructure extends SpacewarObject {
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
    /** The angle images of the spaceship. */
    public BufferedImage[] angles;
    /** The beam angle in an X-Y screen directed coordinate system. */
    public double angle;
    /** The rotation speed: millisecond time per angle element. */
    public double rotationTime;
    /** The movement speed: Milliseconds per one pixel. */
    public int movementSpeed;
    /** The range of the shortest beam-weapon. */
    public int minimumRange;
    /** The number of batched fighters. Once hp reaches zero, this number is reduced, the batch will disappear when the count reaches zero. */
    public int count = 1;
    /** The loss counter. */
    public int loss;
    /** The movement target. */
    public Point2D.Double moveTo;
    /** The attack target. */
    public SpacewarStructure attack;
    /** Attack anything in range. */
    public boolean guard;
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
    /** Compute the smallest weapon range. */
    public void computeMinimumRange() {
        minimumRange = Integer.MAX_VALUE;
        for (SpacewarWeaponPort p : ports) {
            if (p.projectile.mode == Mode.BEAM) {
                minimumRange = Math.min(minimumRange, p.projectile.range);
            }
        }
        if (minimumRange == Integer.MAX_VALUE) {
            minimumRange = 0;
        }
    }
    @Override
    public BufferedImage get() {
        // -0.5 .. +0.5
        double a = normalizedAngle() / Math.PI / 2;
        if (a < 0) {
            a = 1 + a;

        }
        return angles[((int)Math.round(angles.length * a)) % angles.length];
    }
    /**
     * @return Creates a new deep copy of this record.
     */
    public SpacewarStructure copy() {
        SpacewarStructure r = new SpacewarStructure(techId, category);
        r.x = x;
        r.y = y;
        r.owner = owner;
        r.angle = angle;
        r.angles = angles;
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
     * @return the normalized angle between -PI and +PI.
     */
    public double normalizedAngle() {
        return Math.atan2(Math.sin(angle), Math.cos(angle));
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
    /** @return is the structure destroyed? */
    public boolean isDestroyed() {
        return count <= 0;
    }
    @Override
    public String toString() {
        return String.format("Type = %s, Count = %s, Owner = %s, HP = %s, Shield = %s, Parent = %s"
                , type, count, owner.id, hp, shield, (fleet != null ? fleet.name() : (planet != null ? planet.id : "")));
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
}
