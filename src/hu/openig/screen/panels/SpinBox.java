/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.core.Action0;
import hu.openig.core.Func1;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.screen.CommonResources;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UISpinner;
import hu.openig.ui.UIMouse.Modifier;

/**
 * A spin box with left, right and value display.
 * @author akarnokd, 2012.08.20.
 */
public abstract class SpinBox extends UIContainer {
    /** The spin control. */
    public final UISpinner spin;
    /** Shift pressed while clicking on left. */
    protected boolean shiftLeft;
    /** Shift pressed while clicking on right. */
    protected boolean shiftRight;
    /** The common resources. */
    protected final CommonResources commons;
    /**
     * Constructor. Initializes the fields.
     * @param commons the common resources
     */
    public SpinBox(CommonResources commons) {
        this.commons = commons;
        final UIImageButton aprev = new UIImageButton(commons.common().moveLeft) {
            @Override
            public boolean mouse(UIMouse e) {
                shiftLeft = e.has(Modifier.SHIFT);
                return super.mouse(e);
            }
        };
        aprev.setDisabledPattern(commons.common().disabledPattern);
        aprev.setHoldDelay(150);
        final UIImageButton anext = new UIImageButton(commons.common().moveRight) {
            @Override
            public boolean mouse(UIMouse e) {
                shiftRight = e.has(Modifier.SHIFT);
                return super.mouse(e);
            }
        };
        anext.setDisabledPattern(commons.common().disabledPattern);
        anext.setHoldDelay(150);

        aprev.onClick = new Action0() {
            @Override
            public void invoke() {
                SpinBox.this.commons.playSound(SoundTarget.BUTTON, SoundType.CLICK_LOW_1, null);
                onPrev(shiftLeft);
                askRepaint();
            }
        };
        anext.onClick = new Action0() {
            @Override
            public void invoke() {
                SpinBox.this.commons.playSound(SoundTarget.BUTTON, SoundType.CLICK_LOW_1, null);
                onNext(shiftRight);
                askRepaint();
            }
        };

        spin = new UISpinner(14, aprev, anext, commons.text());
        spin.getValue = new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return onValue();
            }
        };
        this.add(spin);
    }
    /**
     * The action to perform when clicked on previous.
     * @param shift the SHIFT key was pressed
     */
    public abstract void onPrev(boolean shift);
    /**
     * The action to perform when clicked on next.
     * @param shift the SHIFT key was pressed
     */
    public abstract void onNext(boolean shift);
    /**
     * @return the value to display
     */
    public abstract String onValue();
    /** Update state after changes. */
    public void update() {

    }
    @Override
    public UIComponent enabled(boolean state) {
        spin.enabled(state);
        return super.enabled(state);
    }
}