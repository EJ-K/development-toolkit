package com.runemate.bots.dev.ui.element.adapter;

import com.runemate.game.api.hybrid.local.*;
import com.runemate.game.api.script.framework.listeners.events.VarbitEvent;
import java.util.*;

public class VarbitEventWrapper {

    private final VarbitEvent event;

    public VarbitEventWrapper(final VarbitEvent event) {
        this.event = event;
    }

    @Override
    public String toString() {
        String result = event.toString();
        final VarbitID v = Arrays.stream(VarbitID.values()).filter(it -> it.getId() == event.getVarbit().getId()).findAny().orElse(null);
        if (v != null) {
            result += " [" + v.name() + "]";
        }
        return result;
    }
}
