package com.core.match.drops.gui;

import com.core.match.drops.gui.msgs.GUIPrice;

/**
 * Created by jgreco on 3/16/16.
 */
public class GUIPrice32 extends GUIPrice {
    public GUIPrice32(int id, String sec, boolean side, int pos) {
        super(id, sec, side, pos);
    }

    @Override
    public String getType() {
        return "price32";
    }
}
