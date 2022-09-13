package com.runemate.bots.dev.ui.element.adapter;

import com.runemate.game.api.hybrid.local.*;
import com.runemate.game.api.script.framework.listeners.events.*;
import java.util.*;

public class VarpEventWrapper {

    private final VarpEvent event;

    public VarpEventWrapper(final VarpEvent event) {
        this.event = event;
    }

    @Override
    public String toString() {
        String result = event.toString();
        final VarpID v = Arrays.stream(VarpID.values()).filter(it -> it.getId() == event.getVarp().getIndex()).findAny().orElse(null);
        if (v != null) {
            result += " [" + v.name() + "]";
        }
        return result;
    }
}
