package com.runemate.bots.dev.ui.element.adapter;

import com.runemate.game.api.hybrid.local.Varbit;
import com.runemate.game.api.script.framework.listeners.events.VarbitEvent;

public class VarbitEventWrapper {

    private final VarbitEvent event;

    public VarbitEventWrapper(final VarbitEvent event) {
        this.event = event;
    }

    public int getOldValue() {
        return event.getOldValue();
    }

    public int getNewValue() {
        return event.getNewValue();
    }

    public int getChange() {
        return event.getChange();
    }

    public Varbit getVarbit() {
        return event.getVarbit();
    }

    @Override
    public String toString() {
        String result = event.toString();
        final VarbitID v = VarbitID.byId(event.getVarbit().getId());
        if (v != null) {
            result += " [" + v.name() + "]";
        }
        return result;
    }
}
