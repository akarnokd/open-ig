/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.EnumSet;
import java.util.Set;

import javax.swing.SwingUtilities;

/**
 * The modernized Mouse event class with enums instead
 * of complex ints.
 * @author akarnokd, 2011.02.25.
 */
public class UIMouse {
    /** The mouse event location X. */
    public int x;
    /** The mouse event location Y. */
    public int y;
    /** The number of mouse events, consecutive clicks or wheel rotations depending on event type. */
    public int z;
    /** The event type. */
    public Type type;
    /** The affected buttons. */
    public final Set<Button> buttons = EnumSet.noneOf(Button.class);
    /** The affected modifiers. */
    public final Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
    /** The mouse button enumeration. */
    public enum Button {
        /** The left button, i.e., the primary button. */
        LEFT,
        /** The middle button, i.e., the scroll wheel button. */
        MIDDLE,
        /** The right button, i.e., the secondary button. */
        RIGHT,
    }
    /** The key modifier enumeration. */
    public enum Modifier {
        /** Shift pressed. */
        SHIFT,
        /** Alt pressed. */
        ALT,
        /** Ctrl pressed. */
        CTRL
    }
    /**
     * The mouse event type.
     * @author akarnokd, 2011.02.25.
     */
    public enum Type {
        /** Mouse enters some component. */
        ENTER,
        /** The mouse leaves the component. */
        LEAVE,
        /** A button is pressed. */
        DOWN,
        /** A button is released. */
        UP,
        /** The mouse is moved. */
        MOVE,
        /** The mouse is dragged. */
        DRAG,
        /** A single click. */
        CLICK,
        /** A double click. */
        DOUBLE_CLICK,
        /** A wheel event. */
        WHEEL
    }
    /** Empty constructor. */
    public UIMouse() {

    }
    /**
     * Create an instance with the given coordinates.
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public UIMouse(int x, int y) {
        this.x = x;
        this.y = y;
    }
    /**
     * Create an UIMouse from a Mouse Event.
     * @param e the mouse event
     * @return the UI mouse
     */
    public static UIMouse from(MouseEvent e) {
        UIMouse m = new UIMouse();
        m.x = e.getX();
        m.y = e.getY();
        if (SwingUtilities.isLeftMouseButton(e)) {
            m.buttons.add(Button.LEFT);
        }
        if (SwingUtilities.isMiddleMouseButton(e)) {
            m.buttons.add(Button.MIDDLE);
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            m.buttons.add(Button.RIGHT);
        }
        if (e.isShiftDown()) {
            m.modifiers.add(Modifier.SHIFT);
        }
        if (e.isControlDown()) {
            m.modifiers.add(Modifier.CTRL);
        }
        if (e.isAltDown()) {
            m.modifiers.add(Modifier.ALT);
        }
        switch (e.getID()) {
        case MouseEvent.MOUSE_CLICKED:
            m.type = e.getClickCount() == 1 ? Type.CLICK : Type.DOUBLE_CLICK;
            m.z = e.getClickCount();
            break;
        case MouseEvent.MOUSE_PRESSED:
            m.type = Type.DOWN;
            break;
        case MouseEvent.MOUSE_RELEASED:
            m.type = Type.UP;
            break;
        case MouseEvent.MOUSE_MOVED:
            m.type = Type.MOVE;
            break;
        case MouseEvent.MOUSE_DRAGGED:
            m.type = Type.DRAG;
            break;
        case MouseEvent.MOUSE_ENTERED:
            m.type = Type.ENTER;
            break;
        case MouseEvent.MOUSE_EXITED:
            m.type = Type.LEAVE;
            break;
        case MouseEvent.MOUSE_WHEEL:
            m.type = Type.WHEEL;
            m.z = ((MouseWheelEvent)e).getUnitsToScroll();
            break;
        default:
        }

        return m;
    }
    /**
     * Is this mouse event within the specified absolute coordinates.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param width the width of the region
     * @param height the height of the region
     * @return true if within the region
     */
    public boolean within(int x, int y, int width, int height) {
        return (x <= this.x && this.x < x + width)
        && (y <= this.y && this.y < y + height);
    }
    /**
     * Create a mouse event copy from this event but use the given new event type.
     * @param newType the new event type
     * @return the new mouse event
     */
    public UIMouse copy(Type newType) {
        UIMouse m = new UIMouse();
        m.x = this.x;
        m.y = this.y;
        m.z = this.z;
        m.buttons.addAll(this.buttons);
        m.modifiers.addAll(this.modifiers);
        m.type = newType;
        return m;
    }
    /**
     * Create a mouse event copy from this event but use the given new event type
     * and the given translation.
     * @param newType the new event type
     * @param dx the delta X
     * @param dy the delta Y
     * @return the new mouse event
     */
    public UIMouse copy(Type newType, int dx, int dy) {
        UIMouse m = new UIMouse();
        m.x = this.x + dx;
        m.y = this.y + dy;
        m.z = this.z;
        m.buttons.addAll(this.buttons);
        m.modifiers.addAll(this.modifiers);
        m.type = newType;
        return m;
    }
    /**
     * Create a translated copy of this mouse event.
     * @param dx the delta X
     * @param dy the delta Y
     * @return the new mouse event
     */
    public UIMouse translate(int dx, int dy) {
        UIMouse m = new UIMouse();
        m.x = this.x + dx;
        m.y = this.y + dy;
        m.z = this.z;
        m.buttons.addAll(this.buttons);
        m.modifiers.addAll(this.modifiers);
        m.type = type;
        return m;
    }
    /**
     * Create a mouse movement event as if the mouse just
     * moved to its current position.
     * @param c the base swing component to relativize the mouse location
     * @return the mouse event
     */
    public static UIMouse createCurrent(Component c) {
        UIMouse m = new UIMouse();
        m.type = UIMouse.Type.MOVE;
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo != null) {
            Point pm = pointerInfo.getLocation();
            Point pc = new Point(0, 0);
            try {
                pc = c.getLocationOnScreen();
            } catch (IllegalStateException ex) {
                // ignored
            }
            m.x = pm.x - pc.x;
            m.y = pm.y - pc.y;
        }
        return m;
    }
    /**
     * Convenience method to check if the given button is contained by this event.
     * @param button the button
     * @return the event contains this button
     */
    public boolean has(Button button) {
        return buttons.contains(button);
    }
    /**
     * Convenience method to test if this event has the given type.
     * @param type the type to test
     * @return true if the type equal
     */
    public boolean has(Type type) {
        return this.type == type;
    }
    /**
     * Convenience method to test if the given modifier is contained by this event.
     * @param modifier the modifier
     * @return true if the modifier is present in this event
     */
    public boolean has(Modifier modifier) {
        return modifiers.contains(modifier);
    }
}
