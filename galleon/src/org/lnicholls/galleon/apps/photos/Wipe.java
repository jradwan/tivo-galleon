package org.lnicholls.galleon.apps.photos;

/*
 * Copyright (C) 2005 Leon Nicholls
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * 
 * See the file "COPYING" for more details.
 */

import java.awt.Image;

import com.tivo.hme.sdk.Resource;
import com.tivo.hme.sdk.View;

public class Wipe extends Effect {
    public static final int LEFT = 1;

    public static final int RIGHT = 2;

    public static final int TOP = 3;

    public static final int BOTTOM = 4;

    public static final int NW = 5;

    public static final int NE = 6;

    public static final int SE = 7;

    public static final int SW = 8;

    public Wipe(int direction) {
        this(direction, false, false);
    }

    public Wipe(int direction, boolean outTransparent, boolean inTransparent) {
        this(0, 0, outTransparent, inTransparent);
        mDirection = direction;
    }

    public Wipe(float x, float y) {
        this(x, y, false, false);
    }

    public Wipe(float x, float y, boolean outTransparent, boolean inTransparent) {
        mX = x;
        mY = y;
        mInTransparent = inTransparent;
        mOutTransparent = outTransparent;
    }

    public void apply(View view, Image image) {
        Resource anim = view.getResource("*" + getDelay());

        view.getApp().root.setPainting(false);
        View view2 = new View(view.parent, view.x, view.y, view.width, view.height);
        view2.setResource(view.resource);
        view.setResource(view.createImage(image));
        view.getApp().root.setPainting(true);
        image.flush(); 
        image = null;

        if (mInTransparent) {
            view.setTransparency(1);
            view.setTransparency(0, anim);
        }
        if (mDirection != -1) {
            int[] direction = getDirection(view);
            view2.setLocation(direction[0], direction[1], anim);
        } else
            view2.setLocation(Math.round(mX*view.parent.width), Math.round(mY*view.parent.height), anim);
        if (mOutTransparent)
            view2.setTransparency(1, anim);

        wait(view2, anim);
    }

    private int[] getDirection(View view) {
        int[] values = new int[2];
        switch (mDirection) {
        case LEFT:
            values[0] = -view.parent.width;
            values[1] = view.y;
            break;
        case RIGHT:
            values[0] = view.parent.width;
            values[1] = view.y;;
            break;
        case TOP:
            values[0] = view.x;
            values[1] = -view.parent.height;
            break;
        case BOTTOM:
            values[0] = view.x;
            values[1] = view.parent.height;
            break;
        case NW:
            values[0] = -view.parent.width;
            values[1] = -view.parent.height;
            break;
        case NE:
            values[0] = view.parent.width;
            values[1] = -view.parent.height;
            break;
        case SE:
            values[0] = view.parent.width;
            values[1] = view.parent.height;
            break;
        case SW:
            values[0] = -view.parent.width;
            values[1] = view.parent.height;
            break;
        default:
            values[0] = view.parent.width;
            values[1] = view.y;
            break;
        }
        return values;
    }
    
    private int mDirection = -1;

    private float mX;

    private float mY;
    
    private boolean mOutTransparent;

    private boolean mInTransparent;
}