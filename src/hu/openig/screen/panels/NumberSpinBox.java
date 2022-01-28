/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.screen.CommonResources;
import hu.openig.ui.UIComponent;

/**
 * A number spin box.
 * @author akarnokd, 2012.08.20.
 */
public class NumberSpinBox extends SpinBox {
    /** The current value. */
    public int value;
    /** The minimum value. */
    protected int min;
    /** The maximum value. */
    protected int max;
    /** The small step. */
    protected int step;
    /** The large step. */
    protected int largeStep;
    /**
     * Constructor. Initializes the parameters.
     * @param commons the common resources object
     * @param min minimum value
     * @param max maximum value
     * @param step small step
     * @param largeStep large step
     */
    public NumberSpinBox(CommonResources commons, int min, int max, int step, int largeStep) {
        super(commons);
        this.min = min;
        this.max = max;
        this.step = step;
        this.largeStep = largeStep;
        this.value = min;
    }
    @Override
    public void onNext(boolean shift) {
        value = Math.min(max, value + (shift ? largeStep : step));
        update();
    }
    @Override
    public void onPrev(boolean shift) {
        value = Math.max(min, value - (shift ? largeStep : step));
        update();
    }
    /** Update controls. */
    @Override
    public void update() {
        spin.prev.enabled(enabled && value > min);
        spin.next.enabled(enabled && value < max);
    }
    @Override
    public String onValue() {
        return String.format("%,d", value);
    }
    /**
     * Set the size to the maximum of the campaign name.
     */
    public void setMaxSize() {
        int val = value;
        value = min;
        int w = commons.text().getTextWidth(14, onValue());
        value = max;
        w = Math.max(w, commons.text().getTextWidth(14, onValue()));
        width = w + spin.prev.width + spin.next.width + 20;
        spin.width = width;
        height = spin.prev.height;
        value = val;
    }
    @Override
    public UIComponent enabled(boolean state) {
        spin.enabled(state);
        if (state) {
            update();
        }
        return super.enabled(state);
    }
}