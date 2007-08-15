package org.lnicholls.galleon.apps.music;

import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.server.MusicPlayerConfiguration;
import org.lnicholls.galleon.server.Server;
import org.lnicholls.galleon.util.FileSystemContainer.Item;
import org.lnicholls.galleon.widget.DefaultApplication;
import org.lnicholls.galleon.widget.DefaultPlayer;
import org.lnicholls.galleon.widget.DefaultScreen;
import org.lnicholls.galleon.widget.MusicPlayer;
import org.lnicholls.galleon.widget.ScreenSaver;
import org.lnicholls.galleon.widget.DefaultApplication.Player;
import org.lnicholls.galleon.widget.DefaultApplication.Tracker;
import org.lnicholls.galleon.winamp.WinampPlayer;


public class PlayerScreen extends DefaultScreen {

    // private WinampPlayer player;

    private DefaultPlayer player;

    private Tracker mTracker;

    private ScreenSaver mScreenSaver;

    public PlayerScreen(Music app, Tracker tracker) {
        super(app, true);

        getBelow().setResource(app.getPlayerBackground(), RSRC_HALIGN_LEFT | RSRC_IMAGE_VFIT);

        boolean sameTrack = false;
        DefaultApplication defaultApplication = (DefaultApplication) getApp();
        Audio currentAudio = defaultApplication.getCurrentAudio();
        Tracker currentTracker = defaultApplication.getTracker();
        if (currentTracker != null && currentAudio != null) {
            Item newItem = (Item) tracker.getList().get(tracker.getPos());
            if (currentAudio.getPath().equals(newItem.getValue().toString())) {
                mTracker = currentTracker;
                sameTrack = true;
            } else {
                mTracker = tracker;
                app.setTracker(mTracker);
            }
        } else {
            mTracker = tracker;
            app.setTracker(mTracker);
        }

        setTitle(" ");

        setFooter("Press INFO for lyrics, REPLAY to return to this screen");

        if (!sameTrack || app.getPlayer().getState() == Player.STOP)
            app.getPlayer().startTrack();
    }
    
    public Music getApp() {
        return (Music)super.getApp();
    }

    public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
        new Thread() {
            public void run() {
                mBusy.setVisible(true);
                mBusy.flush();

                synchronized (this) {
                    try {
                        setPainting(false);
                        MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer()
                                .getMusicPlayerConfiguration();
                        if (musicPlayerConfiguration.getPlayer().equals(MusicPlayerConfiguration.CLASSIC))
                            player = new MusicPlayer(PlayerScreen.this, BORDER_LEFT, SAFE_TITLE_H, BODY_WIDTH,
                                    BODY_HEIGHT, false, (DefaultApplication) getApp(), mTracker);
                        else
                            player = new WinampPlayer(PlayerScreen.this, 0, 0, PlayerScreen.this.getWidth(),
                                    PlayerScreen.this.getHeight(), false, (DefaultApplication) getApp(), mTracker);
                        player.updatePlayer();
                        player.setVisible(true);
                    } finally {
                        setPainting(true);
                    }
                }
                setFocusDefault(player);
                setFocus(player);
                mBusy.setVisible(false);

                MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer()
                        .getMusicPlayerConfiguration();
                if (musicPlayerConfiguration.isScreensaver()) {
                    mScreenSaver = new ScreenSaver(PlayerScreen.this);
                    mScreenSaver.start();
                }
                getBApp().flush();
            }

            public void interrupt() {
                synchronized (this) {
                    super.interrupt();
                }
            }
        }.start();

        return super.handleEnter(arg, isReturn);
    }

    public boolean handleExit() {
        try {
            setPainting(false);
            if (mScreenSaver != null && mScreenSaver.isAlive()) {
                mScreenSaver.interrupt();
                mScreenSaver = null;
            }
            if (player != null) {
                player.stopPlayer();
                player.setVisible(false);
                player.flush();
                player.remove();
                player = null;
            }
        } finally {
            setPainting(true);
        }
        return super.handleExit();
    }

    public boolean handleKeyPress(int code, long rawcode) {
        if (mScreenSaver != null)
            mScreenSaver.handleKeyPress(code, rawcode);
        switch (code) {
        case KEY_INFO:
        case KEY_NUM0:
            getBApp().play("select.snd");
            getBApp().flush();
            LyricsScreen lyricsScreen = new LyricsScreen((Music) getBApp(), mTracker);
            getBApp().push(lyricsScreen, TRANSITION_LEFT);
            getBApp().flush();
            return true;
        /*
         * case KEY_NUM0: MusicConfiguration musicConfiguration =
         * (MusicConfiguration) ((MusicFactory) getContext().getFactory())
         * .getAppContext().getConfiguration(); MusicPlayerConfiguration
         * musicPlayerConfiguration =
         * Server.getServer().getMusicPlayerConfiguration(); if
         * (musicPlayerConfiguration.isShowImages()) {
         * getBApp().play("select.snd"); getBApp().flush(); ImagesScreen
         * imagesScreen = new ImagesScreen((Music) getBApp(), mTracker);
         * getBApp().push(imagesScreen, TRANSITION_LEFT); getBApp().flush();
         * return true; } else return false;
         */
        }

        return super.handleKeyPress(code, rawcode);
    }
}