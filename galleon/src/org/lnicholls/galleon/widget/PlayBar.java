package org.lnicholls.galleon.widget;

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

import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import java.awt.Color;

/*
 * 
 * Based on code and graphics by Marty Lamb (http://www.martiansoftware.com/contact.html)
 */

public class PlayBar extends BView {

    private static final int PREFERRED_H = 71;

    private static final int PLAYBAR_H = 31;

    private static final int PLAYBAR_LEFT = 54;

    private static final int PLAYBAR_RIGHT = 57;

    private static final int PLAYBAR_Y_OFFSET = 20;

    private static final int TIMEWIDTH = 45;

    private static final int SHUTTLEPART_HEIGHT = 20;
    
    private static final int SHUTTLEPART_WIDTH = 75;

    private static final int CAP_WIDTH = PLAYBAR_LEFT + PLAYBAR_RIGHT;
    

    private static final int SHUTTLE_HEIGHT = PREFERRED_H;

    private BView mBar;

    private BView mBarLeft;

    private BView mBarMiddle;

    private BView mBarRight;
    
    private BView mBorder;

    private BView mProgress;

    private BView mShuttle;

    private BText mStart;

    private BText mEnd;

    private BView mShuttleTop;

    private BView mShuttleBottom;

    private BText mShuttleTime;

    private BView mShuttleIcon;

    private int mDuration;

    private int mProgressed;

    private int mPosition;

    public PlayBar(BView parent) {
        super(parent, 0, parent.getHeight() - PREFERRED_H - 10, parent.getWidth(), PREFERRED_H);

        mProgress = new TileView(this, PLAYBAR_LEFT-1, PLAYBAR_Y_OFFSET + 8, 
                this.getWidth() - CAP_WIDTH - 2, PLAYBAR_H - 16, 650, PLAYBAR_H - 16);
        mProgress.setVisible(false);
        mProgress.setResource(Color.GREEN);
        setProgress(0);

        mBar = new BView(this, 0, PLAYBAR_Y_OFFSET, this.getWidth(), PLAYBAR_H, false);

        mBarLeft = new BView(mBar, 0, 0, PLAYBAR_LEFT, PLAYBAR_H);
        mBarLeft.setResource(createImage("org/lnicholls/galleon/widget/playbar_left.png"));

        mBarMiddle = new TileView(mBar, PLAYBAR_LEFT, 0, this.getWidth() - CAP_WIDTH, 
                PLAYBAR_H, 450, PLAYBAR_H);
        mBarMiddle.setResource(createImage("org/lnicholls/galleon/widget/playbar.png"));

        mBarRight = new BView(mBar, this.getWidth()-PLAYBAR_RIGHT, 0, PLAYBAR_RIGHT, PLAYBAR_H);
        mBarRight.setResource(createImage("org/lnicholls/galleon/widget/playbar_right.png"));

        mStart = new BText(mBar, 0, 0, TIMEWIDTH, 23);
        mStart.setFont("default-15.font");
        mStart.setFlags(RSRC_HALIGN_CENTER | RSRC_VALIGN_BOTTOM);
        mStart.setValue(formatTime(0));

        mEnd = new BText(mBar, this.getWidth() - TIMEWIDTH, 0, TIMEWIDTH, 23);
        mEnd.setFont("default-15.font");
        mEnd.setFlags(RSRC_HALIGN_CENTER | RSRC_VALIGN_BOTTOM);
        mEnd.setValue(formatTime(0));

        int diff = (getWidth() - CAP_WIDTH) / 4;
        for (int i = 0; i < 3; ++i) {
            BView playbarTick = new BView(this, TIMEWIDTH + (i+1)*diff, PLAYBAR_Y_OFFSET + 10, 2, 3);
            playbarTick.setResource(java.awt.Color.WHITE);
        }
        mBar.setVisible(true);

        mShuttle = new BView(this, TIMEWIDTH - (SHUTTLEPART_WIDTH / 2), 0, SHUTTLEPART_WIDTH, SHUTTLE_HEIGHT);

        mShuttleTop = new BView(mShuttle, 0, 0, SHUTTLEPART_WIDTH, SHUTTLEPART_HEIGHT);
        mShuttleTop.setResource(createImage("org/lnicholls/galleon/widget/playbar_shuttle_top.png"));

        mShuttleTime = new BText(mShuttle, 0, 2, SHUTTLEPART_WIDTH, 18);
        mShuttleTime.setFont("default-15.font");
        mShuttleTime.setFlags(RSRC_HALIGN_CENTER | RSRC_VALIGN_CENTER);
        mShuttleTime.setValue(formatTime(0));

        mShuttleBottom = new BView(mShuttle, 0, SHUTTLE_HEIGHT - SHUTTLEPART_HEIGHT, SHUTTLEPART_WIDTH, SHUTTLEPART_HEIGHT);
        mShuttleBottom.setResource(createImage("org/lnicholls/galleon/widget/playbar_shuttle_bottom.png"));

        mShuttleIcon = new BView(mShuttleBottom, (SHUTTLEPART_WIDTH - 15) / 2, 2, 15, 15);
        mShuttleIcon.setResource(createImage("org/lnicholls/galleon/widget/pause.png"));

        BView playbarShuttleNeedle = new BView(mShuttle, SHUTTLEPART_WIDTH / 2, SHUTTLEPART_HEIGHT + 10, 2, PLAYBAR_H - 18);
        playbarShuttleNeedle.setResource(Color.WHITE);
    }

    public void stop() {
        mShuttleIcon.setResource(createImage("org/lnicholls/galleon/widget/stop.png"));
    }

    public void pause() {
        mShuttleIcon.setResource(createImage("org/lnicholls/galleon/widget/pause.png"));
    }

    public void play() {
        mShuttleIcon.setResource(createImage("org/lnicholls/galleon/widget/play.png"));
    }
    
    public void rewind() {
        mShuttleIcon.setResource(createImage("org/lnicholls/galleon/widget/rewind.png"));
    }

    public void setPosition(int seconds) {
        if (seconds == mPosition)
            return;

        int centerOffset = 0;
        int maxWidth = this.getWidth() - CAP_WIDTH - 2;

        mPosition = Math.min(Math.max(seconds, 0), mDuration);

        if (mDuration > 0) {
            centerOffset = (mPosition * maxWidth) / mDuration;
            mProgress.setVisible(true);
        }
        mShuttleTime.setValue(formatTime(mPosition));
        mShuttle.setLocation(PLAYBAR_LEFT + centerOffset - SHUTTLEPART_WIDTH / 2, mShuttle.getY());
        mShuttle.flush();
        
        mProgress.setSize(centerOffset+1, mProgress.getHeight());
        mProgress.flush();
    }

    public void setProgress(int seconds) {
        if (seconds == mProgressed)
            return;

        int newWidth = 0;
        int maxWidth = this.getWidth() - CAP_WIDTH - 2;

        mProgressed = Math.min(Math.max(seconds, 0), mDuration);

        if (mDuration > 0) {
            newWidth = (mProgressed * maxWidth) / mDuration;
            mProgress.setVisible(true);
        }
        mProgress.setSize(newWidth, mProgress.getHeight());
        mProgress.flush();
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
        mEnd.setValue(formatTime(duration));
        setPosition(mPosition);
    }

    private String formatTime(int seconds) {
        int secondD = 0, second = 0, minuteD = 0, minute = 0;
        int minutes = (int) Math.floor(seconds / 60);
        int hours = (int) Math.floor(minutes / 60);
        minutes = minutes - hours * 60;
        seconds = seconds - minutes * 60 - hours * 3600;
        if (seconds < 10) {
            secondD = 0;
            second = seconds;
        } else {
            secondD = ((int) seconds / 10);
            second = ((int) (seconds - (((int) seconds / 10)) * 10));
        }
        if (minutes < 10) {
            minuteD = 0;
            minute = minutes;
        } else {
            minuteD = ((int) minutes / 10);
            minute = ((int) (minutes - (((int) minutes / 10)) * 10));
        }

        if (minuteD != 0)
            return minuteD + "" + minute + ":" + secondD + "" + second;
        else
            return minute + ":" + secondD + "" + second;
    }
}
