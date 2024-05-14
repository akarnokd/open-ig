/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.core.Location;
import hu.openig.model.WarUnit;
import hu.openig.utils.U;


/**
 * Abstract base class for handling movement of war units.
 */
public abstract class WarMovementHandler {

    /** The size of a cell in pixels. */
    final int cellSize;
    /** The simulation delay on normal speed. */
    int simulationDelay;


    WarMovementHandler(int cellSize, int simulationDelay) {
        this.cellSize = cellSize;
        this.simulationDelay = simulationDelay;
    }
    /** Remove a unit handled by the movement handler object.
     * @param unit the unit to remove
     * */
    public abstract void removeUnit(WarUnit unit);
    /** Set new movement goal for a unit.
     * @param unit the unit to move
     * @param loc the target location
     * */
    public abstract void setMovementGoal(WarUnit unit, Location loc);
    /** Move a handled unit by one step.
     * @param unit the unit to move
     * */
    public abstract void moveUnit(WarUnit unit);
    /** Clear the movement goal of a unit handled by this object.
     * @param unit the unit to move
     * */
    public abstract void clearUnitGoal(WarUnit unit);

    /** Execute path plannings for that handled units. */
    public abstract void doPathPlannings();

    /**
     * Rotate the unit towards the given target angle by a step.
     * @param unit the war unit
     * @param targetX x coordinate of the target location
     * @param targetY y coordinate of the target location
     * @return rotation done?
     */
    public boolean rotateStep(WarUnit unit, double targetX, double targetY) {
        RotationAngles ra = computeRotation(unit, targetX, targetY);

        double anglePerStep = 2 * Math.PI * unit.getRotationTime() / unit.getAngleCount() / simulationDelay;
        if (Math.abs(ra.diff) < anglePerStep) {
            unit.setAngle(ra.targetAngle);
            return true;
        }
        unit.increaseAngle(Math.signum(ra.diff) * anglePerStep);
        return false;
    }
    /**
     * Checks if the given target location requires the unit to rotate before move.
     * @param unit the unit
     * @param target the target location
     * @return true if rotation is needed
     */
    public static boolean needsRotation(WarUnit unit, Location target) {
        return needsRotation(unit, target, 0);
    }

    /**
     * Checks if the given target location requires the unit to rotate before move.
     * @param unit the unit
     * @param target the target location
     * @param tolerance the tolerated angle difference of the rotation in radians
     * @return true if rotation is needed
     */
    public static boolean needsRotation(WarUnit unit, Location target, double tolerance) {
        if (target == null) {
            return false;
        }
        RotationAngles ra = computeRotation(unit, target.x, target.y);
        return Math.abs(ra.diff) > tolerance;
    }

    /**
     * Computes the rotation angles.
     * @param unit the unit
     * @param targetX the target X coordinate
     * @param targetY the target Y coordinate
     * @return the angles
     */
    static RotationAngles computeRotation(WarUnit unit, double targetX, double targetY) {
        RotationAngles result = new RotationAngles();

        if (targetY - unit.exactLocation().y == 0 && targetX - unit.exactLocation().x == 0) {
            result.targetAngle = U.normalizedAngle(unit.getAngle());
            result.currentAngle = result.targetAngle;
            result.diff = 0;
        } else {
            result.targetAngle = Math.atan2(targetY - unit.exactLocation().y, targetX - unit.exactLocation().x);

            result.currentAngle = U.normalizedAngle(unit.getAngle());

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

    /** The composite record to return the rotation angles. */
    static class RotationAngles {
        /** The unit's current angle. */
        double currentAngle;
        /** The target angle. */
        double targetAngle;
        /** The difference to turn. */
        double diff;
    }
}
