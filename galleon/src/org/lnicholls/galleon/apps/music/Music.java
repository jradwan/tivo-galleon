package org.lnicholls.galleon.apps.music;

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

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.app.AppContext;
import org.lnicholls.galleon.app.AppFactory;
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.AudioManager;
import org.lnicholls.galleon.media.MediaManager;
import org.lnicholls.galleon.media.Playlist;
import org.lnicholls.galleon.server.MusicPlayerConfiguration;
import org.lnicholls.galleon.server.Server;
import org.lnicholls.galleon.util.FileFilters;
import org.lnicholls.galleon.util.FileSystemContainer;
import org.lnicholls.galleon.util.Lyrics;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.util.Yahoo;
import org.lnicholls.galleon.util.FileSystemContainer.FileItem;
import org.lnicholls.galleon.util.FileSystemContainer.FolderItem;
import org.lnicholls.galleon.util.FileSystemContainer.Item;
import org.lnicholls.galleon.widget.DefaultApplication;
import org.lnicholls.galleon.widget.DefaultMenuScreen;
import org.lnicholls.galleon.widget.DefaultOptionList;
import org.lnicholls.galleon.widget.DefaultPlayer;
import org.lnicholls.galleon.widget.DefaultScreen;
import org.lnicholls.galleon.widget.MusicInfo;
import org.lnicholls.galleon.widget.MusicPlayer;
import org.lnicholls.galleon.widget.ScrollText;
import org.lnicholls.galleon.winamp.ClassicSkin;
import org.lnicholls.galleon.winamp.WinampPlayer;

import com.tivo.hme.bananas.BButton;
import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BList;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.util.ArgumentList;

public class Music extends DefaultApplication {

    private static Logger log = Logger.getLogger(Music.class.getName());

    public final static String TITLE = "Music";

    private Resource mMenuBackground;

    private Resource mInfoBackground;

    private Resource mPlayerBackground;

    private Resource mLyricsBackground;

    private Resource mImagesBackground;

    private Resource mFolderIcon;

    private Resource mCDIcon;

    private Resource mPlaylistIcon;

    private MusicScreen mMusicScreen;

    protected void init(Context context) {
        super.init(context);

        mMenuBackground = getSkinImage("menu", "background");
        mInfoBackground = getSkinImage("info", "background");
        mPlayerBackground = getSkinImage("player", "background");
        mLyricsBackground = getSkinImage("lyrics", "background");
        mImagesBackground = getSkinImage("images", "background");
        mFolderIcon = getSkinImage("menu", "folder");
        mCDIcon = getSkinImage("menu", "item");
        mPlaylistIcon = getSkinImage("menu", "playlist");

        mMusicScreen = new MusicScreen(this);

        MusicConfiguration musicConfiguration = (MusicConfiguration) ((MusicFactory) context.factory).getAppContext()
                .getConfiguration();

        if (musicConfiguration.getPaths().size() == 1) {
            try {
                NameValue nameValue = (NameValue) musicConfiguration.getPaths().get(0);
                File file = new File(nameValue.getValue());
                FileItem nameFile = new FileItem(nameValue.getName(), file);
                FileSystemContainer fileSystemContainer = new FileSystemContainer(file.getCanonicalPath());
                setCurrentTrackerContext(file.getCanonicalPath());
                Tracker tracker = new Tracker(fileSystemContainer.getItems(FileFilters.audioDirectoryFilter), 0);
                PathScreen pathScreen = new PathScreen(this, tracker, true);
                push(pathScreen, TRANSITION_LEFT);
            } catch (Exception ex) {
                Tools.logException(Music.class, ex);
            }
        } else
            push(new MusicMenuScreen(this), TRANSITION_NONE);
    }

    public class MusicMenuScreen extends DefaultMenuScreen {
        public MusicMenuScreen(Music app) {
            super(app, "Music");

            below.setResource(mMenuBackground);

            MusicConfiguration musicConfiguration = (MusicConfiguration) ((MusicFactory) context.factory)
                    .getAppContext().getConfiguration();

            for (Iterator i = musicConfiguration.getPaths().iterator(); i.hasNext(); /* Nothing */) {
                NameValue nameValue = (NameValue) i.next();
                mMenuList.add(new FolderItem(nameValue.getName(), new File(nameValue.getValue())));
            }
        }

        public boolean handleAction(BView view, Object action) {
            if (action.equals("push")) {
                load();

                new Thread() {
                    public void run() {
                        try {
                            FileItem nameFile = (FileItem) (mMenuList.get(mMenuList.getFocus()));
                            File file = (File) nameFile.getValue();
                            FileSystemContainer fileSystemContainer = new FileSystemContainer(file.getCanonicalPath());
                            ((DefaultApplication) getBApp()).setCurrentTrackerContext(file.getCanonicalPath());
                            Tracker tracker = new Tracker(fileSystemContainer
                                    .getItems(FileFilters.audioDirectoryFilter), 0);
                            PathScreen pathScreen = new PathScreen((Music) getBApp(), tracker);
                            getBApp().push(pathScreen, TRANSITION_LEFT);
                            getBApp().flush();
                        } catch (Exception ex) {
                            Tools.logException(Music.class, ex);
                        }
                    }
                }.start();
                return true;
            }
            return super.handleAction(view, action);
        }

        protected void createRow(BView parent, int index) {
            BView icon = new BView(parent, 9, 2, 32, 32);
            Item nameFile = (Item) mMenuList.get(index);
            if (nameFile.isFolder()) {
                icon.setResource(mFolderIcon);
            } else {
                icon.setResource(mCDIcon);
            }

            BText name = new BText(parent, 50, 4, parent.width - 40, parent.height - 4);
            name.setShadow(true);
            name.setFlags(RSRC_HALIGN_LEFT);
            name.setValue(Tools.trim(nameFile.getName(), 40));
        }

    }

    public class PathScreen extends DefaultMenuScreen {

        public PathScreen(Music app, Tracker tracker) {
            this(app, tracker, false);
        }

        public PathScreen(Music app, Tracker tracker, boolean first) {
            super(app, "Music");

            below.setResource(mMenuBackground);

            mTracker = tracker;
            mFirst = first;

            Iterator iterator = mTracker.getList().iterator();
            while (iterator.hasNext()) {
                Item nameFile = (Item) iterator.next();
                mMenuList.add(nameFile);
            }
        }
        
        public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
            mFocus = mTracker.getPos();
            return super.handleEnter(arg, isReturn);
        }

        public boolean handleAction(BView view, Object action) {
            if (action.equals("push")) {
                load();
                final Item nameFile = (Item) (mMenuList.get(mMenuList.getFocus()));
                if (nameFile.isFolder()) {
                    new Thread() {
                        public void run() {
                            try {
                                mTracker.setPos(mMenuList.getFocus());
                                File file = (File) nameFile.getValue();
                                FileSystemContainer fileSystemContainer = new FileSystemContainer(file
                                        .getCanonicalPath());
                                ((DefaultApplication) getBApp()).setCurrentTrackerContext(file.getCanonicalPath());
                                Tracker tracker = new Tracker(fileSystemContainer
                                        .getItems(FileFilters.audioDirectoryFilter), 0);
                                PathScreen pathScreen = new PathScreen((Music) getBApp(), tracker);
                                getBApp().push(pathScreen, TRANSITION_LEFT);
                                getBApp().flush();
                            } catch (Exception ex) {
                                Tools.logException(Music.class, ex);
                            }
                        }
                    }.start();
                } else {
                    if (nameFile.isPlaylist()) {
                        try {
                            File file = (File) nameFile.getValue();
                            Playlist playlist = (Playlist) MediaManager.getMedia(file.getCanonicalPath());
                            Tracker tracker = new Tracker(playlist.getList(), 0);
                            PathScreen pathScreen = new PathScreen((Music) getBApp(), tracker);
                            getBApp().push(pathScreen, TRANSITION_LEFT);
                            getBApp().flush();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        new Thread() {
                            public void run() {
                                try {
                                    mTracker.setPos(mMenuList.getFocus());
                                    mMusicScreen.setTracker(mTracker);

                                    getBApp().push(mMusicScreen, TRANSITION_LEFT);
                                    getBApp().flush();
                                } catch (Exception ex) {
                                    Tools.logException(Music.class, ex);
                                }
                            }
                        }.start();
                    }
                }

                return true;
            } else if (action.equals("play")) {
                load();
                final Item nameFile = (Item) (mMenuList.get(mMenuList.getFocus()));
                if (nameFile.isFolder()) {
                    new Thread() {
                        public void run() {
                            try {
                                mTracker.setPos(mMenuList.getFocus());
                                File file = (File) nameFile.getValue();
                                FileSystemContainer fileSystemContainer = new FileSystemContainer(file
                                        .getCanonicalPath(), true);
                                ((DefaultApplication) getBApp()).setCurrentTrackerContext(file.getCanonicalPath());
                                Tracker tracker = new Tracker(fileSystemContainer
                                        .getItems(FileFilters.audioDirectoryFilter), 0);
                                
                                MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer().getMusicPlayerConfiguration();
                                tracker.setRandom(musicPlayerConfiguration.isRandomPlayFolders());
                                getBApp().push(new PlayerScreen((Music) getBApp(), tracker), TRANSITION_LEFT);
                                getBApp().flush();
                            } catch (Exception ex) {
                                Tools.logException(Music.class, ex);
                            }
                        }
                    }.start();
                } else {
                    new Thread() {
                        public void run() {
                            try {
                                mTracker.setPos(mMenuList.getFocus());
                                getBApp().push(new PlayerScreen((Music) getBApp(), mTracker), TRANSITION_LEFT);
                                getBApp().flush();
                            } catch (Exception ex) {
                                Tools.logException(Music.class, ex);
                            }
                        }
                    }.start();
                }
            }
            return super.handleAction(view, action);
        }

        protected void createRow(BView parent, int index) {
            BView icon = new BView(parent, 9, 2, 32, 32);
            Item nameFile = (Item) mMenuList.get(index);
            if (nameFile.isFolder()) {
                icon.setResource(mFolderIcon);
            } else {
                if (nameFile.isPlaylist())
                    icon.setResource(mPlaylistIcon);
                else
                    icon.setResource(mCDIcon);
            }

            BText name = new BText(parent, 50, 4, parent.width - 40, parent.height - 4);
            name.setShadow(true);
            name.setFlags(RSRC_HALIGN_LEFT);
            name.setValue(Tools.trim(nameFile.getName(), 40));
        }

        public boolean handleKeyPress(int code, long rawcode) {
            switch (code) {
            case KEY_LEFT:
                if (!mFirst) {
                    postEvent(new BEvent.Action(this, "pop"));
                    return true;
                }
            }
            return super.handleKeyPress(code, rawcode);
        }

        private Tracker mTracker;

        private boolean mFirst;
    }

    public class MusicScreen extends DefaultScreen {

        private BList list;

        public MusicScreen(Music app) {
            super(app, "Song", true);

            below.setResource(mInfoBackground);

            mMusicInfo = new MusicInfo(this.normal, BORDER_LEFT, TOP - 30, BODY_WIDTH, BODY_HEIGHT
                    - (TOP - 30 - SAFE_TITLE_V), true);

            list = new DefaultOptionList(this.normal, SAFE_TITLE_H + 10, (height - SAFE_TITLE_V) - 80, (int) Math
                    .round((width - (SAFE_TITLE_H * 2)) / 2.5), 90, 35);
            list.add("Play");
            list.add("Don't do anything");

            setFocusDefault(list);
        }

        public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
            below.setResource(mInfoBackground);
            updateView();

            return super.handleEnter(arg, isReturn);
        }

        private void updateView() {
            Audio audio = currentAudio();
            if (audio != null) {
                Item nameFile = (Item) mTracker.getList().get(mTracker.getPos());
                mMusicInfo.setAudio(audio, nameFile.getName());
            }
        }

        public boolean handleExit() {
            mMusicInfo.clearResource();
            return super.handleExit();
        }

        public boolean handleKeyPress(int code, long rawcode) {
            if (mMusicInfo.handleKeyPress(code, rawcode))
                return true;
            Audio audio = currentAudio();
            switch (code) {
            case KEY_SELECT:
            case KEY_RIGHT:
                if (list.getFocus() == 0) {
                    postEvent(new BEvent.Action(this, "play"));
                    return true;
                } else {
                    postEvent(new BEvent.Action(this, "pop"));
                    return true;
                }
            case KEY_LEFT:
                postEvent(new BEvent.Action(this, "pop"));
                return true;
            case KEY_CHANNELUP:
                getBApp().play("pageup.snd");
                getBApp().flush();
                getPrevPos();
                updateView();
                return true;
            case KEY_CHANNELDOWN:
                getBApp().play("pagedown.snd");
                getBApp().flush();
                getNextPos();
                updateView();
                return true;
            }
            return super.handleKeyPress(code, rawcode);
        }

        public void getNextPos() {
            if (mTracker != null) {
                int pos = mTracker.getNextPos();
                Item nameFile = (Item) mTracker.getList().get(pos);
                while (nameFile.isFolder() || nameFile.isPlaylist()) {
                    pos = mTracker.getNextPos();
                    nameFile = (Item) mTracker.getList().get(pos);
                }
            }
        }

        public void getPrevPos() {
            if (mTracker != null) {
                int pos = mTracker.getPrevPos();
                Item nameFile = (Item) mTracker.getList().get(pos);
                while (nameFile.isFolder() || nameFile.isPlaylist()) {
                    pos = mTracker.getPrevPos();
                    nameFile = (Item) mTracker.getList().get(pos);
                }
            }
        }

        public boolean handleAction(BView view, Object action) {
            if (action.equals("play")) {

                getBApp().play("select.snd");
                getBApp().flush();

                new Thread() {
                    public void run() {
                        getBApp().push(new PlayerScreen((Music) getBApp(), mTracker), TRANSITION_LEFT);
                        getBApp().flush();
                    }
                }.start();
                return true;
            }

            return super.handleAction(view, action);
        }

        public void setTracker(Tracker value) {
            mTracker = value;
        }

        private Audio currentAudio() {
            if (mTracker != null) {
                try {
                    Item nameFile = (Item) mTracker.getList().get(mTracker.getPos());
                    if (nameFile != null) {
                        if (nameFile.isFile())
                            return getAudio(((File) nameFile.getValue()).getCanonicalPath());
                        else
                            return getAudio((String) nameFile.getValue());
                    }
                } catch (Exception ex) {
                    Tools.logException(Music.class, ex);
                }
            }
            return null;
        }

        private MusicInfo mMusicInfo;

        private Tracker mTracker;
    }

    public class PlayerScreen extends DefaultScreen {

        public PlayerScreen(Music app, Tracker tracker) {
            super(app, true);

            below.setResource(mPlayerBackground);

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

            MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer().getMusicPlayerConfiguration();
            if (musicPlayerConfiguration.isShowImages())
                setFooter("Press INFO for lyrics, 0 for images");
            else
                setFooter("Press INFO for lyrics");

            if (!sameTrack || getPlayer().getState() == Player.STOP)
                getPlayer().startTrack();
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
                                player = new MusicPlayer(PlayerScreen.this, BORDER_LEFT, SAFE_TITLE_V, BODY_WIDTH,
                                        BODY_HEIGHT - 20, false, (DefaultApplication) getApp(), mTracker);
                            else
                                player = new WinampPlayer(PlayerScreen.this, 0, 0, PlayerScreen.this.width,
                                        PlayerScreen.this.height, false, (DefaultApplication) getApp(), mTracker);
                            player.updatePlayer();
                            player.setVisible(true);
                        } finally {
                            setPainting(true);
                        }
                    }
                    setFocusDefault(player);
                    setFocus(player);
                    mBusy.setVisible(false);

                    mScreenSaver = new ScreenSaver(PlayerScreen.this);
                    mScreenSaver.start();
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
                player.stopPlayer();

                if (mScreenSaver != null && mScreenSaver.isAlive()) {
                    mScreenSaver.interrupt();
                    mScreenSaver = null;
                }
                if (player != null) {
                    player.setVisible(false);
                    player.remove();
                    player = null;
                }
            } finally {
                setPainting(true);
            }
            return super.handleExit();
        }

        public boolean handleKeyPress(int code, long rawcode) {
            if (code!=KEY_VOLUMEDOWN && code!=KEY_VOLUMEUP)
            {
                if (transparency != 0.0f)
                    setTransparency(0.0f);
            }
            switch (code) {
            case KEY_INFO:
                getBApp().play("select.snd");
                getBApp().flush();
                LyricsScreen lyricsScreen = new LyricsScreen((Music) getBApp(), mTracker);
                getBApp().push(lyricsScreen, TRANSITION_LEFT);
                getBApp().flush();
                return true;
            case KEY_NUM0:
                MusicConfiguration musicConfiguration = (MusicConfiguration) ((MusicFactory) context.factory)
                        .getAppContext().getConfiguration();
                MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer().getMusicPlayerConfiguration();
                if (musicPlayerConfiguration.isShowImages()) {
                    getBApp().play("select.snd");
                    getBApp().flush();
                    ImagesScreen imagesScreen = new ImagesScreen((Music) getBApp(), mTracker);
                    getBApp().push(imagesScreen, TRANSITION_LEFT);
                    getBApp().flush();
                    return true;
                } else
                    return false;
            }
            return super.handleKeyPress(code, rawcode);
        }

        //private WinampPlayer player;

        private DefaultPlayer player;

        private Tracker mTracker;

        private ScreenSaver mScreenSaver;
    }

    private static Audio getAudio(String path) {
        Audio audio = null;
        try {
            List list = AudioManager.findByPath(path);
            if (list != null && list.size() > 0) {
                audio = (Audio) list.get(0);
            }
        } catch (Exception ex) {
            Tools.logException(Music.class, ex);
        }

        if (audio == null) {
            try {
                audio = (Audio) MediaManager.getMedia(path);
                AudioManager.createAudio(audio);
            } catch (Exception ex) {
                Tools.logException(Music.class, ex);
            }
        }
        return audio;
    }

    public class LyricsScreen extends DefaultScreen {
        private BList list;

        public LyricsScreen(Music app, Tracker tracker) {
            super(app, "Lyrics", false);

            below.setResource(mLyricsBackground);

            mTracker = tracker;

            scrollText = new ScrollText(normal, SAFE_TITLE_H, TOP, BODY_WIDTH - 10, height - SAFE_TITLE_V - TOP - 70,
                    "");
            scrollText.setVisible(false);

            //setFocusDefault(scrollText);

            //setFooter("lyrc.com.ar");
            setFooter("lyrictracker.com");

            mBusy.setVisible(true);

            /*
            list = new DefaultOptionList(this.normal, SAFE_TITLE_H + 10, (height - SAFE_TITLE_V) - 60, (int) Math
                    .round((width - (SAFE_TITLE_H * 2)) / 2), 90, 35);
            //list.setBarAndArrows(BAR_HANG, BAR_DEFAULT, H_LEFT, null);
            list.add("Back to player");
            setFocusDefault(list);
            */
            
            BButton button = new BButton(normal, SAFE_TITLE_H + 10, (height-SAFE_TITLE_V)-55, (int) Math
                    .round((width - (SAFE_TITLE_H * 2)) / 2), 35);
            button.setResource(createText("default-24.font", Color.white, "Return to player"));
            button.setBarAndArrows(BAR_HANG, BAR_DEFAULT, "pop", null, null, null, true);
            setFocus(button);
        }

        public void updateLyrics() {
            try {
                setPainting(false);
                if (mLyricsThread != null && mLyricsThread.isAlive())
                    mLyricsThread.interrupt();
            } finally {
                setPainting(true);
            }
            Item nameFile = (Item) mTracker.getList().get(mTracker.getPos());
            Audio audio = null;
            try {
                List list = null;
                if (nameFile.isFile())
                    list = AudioManager.findByPath(((File) nameFile.getValue()).getCanonicalPath());
                else
                    list = AudioManager.findByPath((String) nameFile.getValue());
                if (list != null && list.size() > 0) {
                    audio = (Audio) list.get(0);
                }
            } catch (Exception ex) {
                Tools.logException(Music.class, ex);
            }
            if (audio.getLyrics() != null && audio.getLyrics().length() > 0) {
                try {
                    setPainting(false);
                    mBusy.setVisible(false);
                    getBApp().flush();
                    scrollText.setVisible(true);
                    scrollText.setText(audio.getLyrics());
                    getBApp().flush();
                } finally {
                    setPainting(true);
                }
            } else {
                final Audio lyricsAudio = audio;

                mLyricsThread = new Thread() {
                    public void run() {
                        try {
                            String lyrics = Lyrics.getLyrics(lyricsAudio.getTitle(), lyricsAudio.getArtist());
                            if (lyrics == null || lyrics.trim().length() == 0) {
                                lyrics = "Lyrics not found";
                            } else {
                                synchronized (this) {
                                    try {
                                        lyricsAudio.setLyrics(lyrics);
                                        AudioManager.updateAudio(lyricsAudio);
                                    } catch (Exception ex) {
                                        Tools.logException(Music.class, ex, "Could not update lyrics");
                                    }
                                }
                            }
                            synchronized (this) {
                                try {
                                    setPainting(false);
                                    mBusy.setVisible(false);
                                    getBApp().flush();
                                    scrollText.setVisible(true);
                                    scrollText.setText(lyrics);
                                    getBApp().flush();
                                } finally {
                                    setPainting(true);
                                }
                            }
                        } catch (Exception ex) {
                            Tools.logException(Music.class, ex, "Could retrieve lyrics");
                        }
                    }

                    public void interrupt() {
                        synchronized (this) {
                            super.interrupt();
                        }
                    }
                };
                mLyricsThread.start();
            }
        }

        public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
            updateLyrics();

            return super.handleEnter(arg, isReturn);
        }

        public boolean handleExit() {
            try {
                setPainting(false);
                if (mLyricsThread != null && mLyricsThread.isAlive()) {
                    mLyricsThread.interrupt();
                    mLyricsThread = null;
                }
            } finally {
                setPainting(true);
            }
            return super.handleExit();
        }

        public boolean handleKeyPress(int code, long rawcode) {
            switch (code) {
            case KEY_SELECT:
                postEvent(new BEvent.Action(this, "pop"));
                return true;
            case KEY_UP:
            case KEY_DOWN:
            case KEY_CHANNELUP:
            case KEY_CHANNELDOWN:
                scrollText.handleKeyPress(code, rawcode);
                return true;
            }
            return super.handleKeyPress(code, rawcode);
        }

        private ScrollText scrollText;

        private Thread mLyricsThread;

        private Tracker mTracker;
    }

    public class ImagesScreen extends DefaultScreen {
        private BList list;

        public ImagesScreen(Music app, Tracker tracker) {
            super(app, "Images", true);

            below.setResource(mImagesBackground);

            mTracker = tracker;

            mImageView = new BView(this.normal, BORDER_LEFT, TOP, BODY_WIDTH, height - SAFE_TITLE_V - TOP - 75);
            mImageView.setVisible(false);

            mPosText = new BText(normal, BORDER_LEFT, height - SAFE_TITLE_V - 60, BODY_WIDTH, 30);
            mPosText.setFlags(RSRC_HALIGN_RIGHT | RSRC_VALIGN_TOP);
            mPosText.setFont("default-18-bold.font");
            mPosText.setColor(Color.CYAN);
            mPosText.setShadow(true);

            mUrlText = new BText(normal, SAFE_TITLE_H, height - SAFE_TITLE_V - 78, BODY_WIDTH, 15);
            mUrlText.setFlags(RSRC_HALIGN_CENTER | RSRC_VALIGN_BOTTOM);
            mUrlText.setFont("default-12-bold.font");
            mUrlText.setColor(Color.WHITE);
            mUrlText.setShadow(true);

            setFooter("search.yahoo.com");

            mBusy.setVisible(true);

            /*
            list = new DefaultOptionList(this.normal, SAFE_TITLE_H + 10, (height - SAFE_TITLE_V) - 60, (int) Math
                    .round((width - (SAFE_TITLE_H * 2)) / 2), 90, 35);
            //list.setBarAndArrows(BAR_HANG, BAR_DEFAULT, H_LEFT, null);
            list.add("Back to player");
            setFocusDefault(list);
            */
            
            BButton button = new BButton(normal, SAFE_TITLE_H + 10, (height-SAFE_TITLE_V)-55, (int) Math
                    .round((width - (SAFE_TITLE_H * 2)) / 2), 35);
            button.setResource(createText("default-24.font", Color.white, "Return to player"));
            button.setBarAndArrows(BAR_HANG, BAR_DEFAULT, "pop", null, null, null, true);
            setFocus(button);
        }

        public void updateImage() {
            Item nameFile = (Item) mTracker.getList().get(mTracker.getPos());
            Audio audio = null;
            try {
                List list = null;
                if (nameFile.isFile())
                    list = AudioManager.findByPath(((File) nameFile.getValue()).getCanonicalPath());
                else
                    list = AudioManager.findByPath((String) nameFile.getValue());
                if (list != null && list.size() > 0) {
                    audio = (Audio) list.get(0);
                }
            } catch (Exception ex) {
                Tools.logException(Music.class, ex);
            }
            final Audio lyricsAudio = audio;

            mImageThread = new Thread() {
                public void run() {
                    try {
                        synchronized (this) {
                            mBusy.setVisible(true);
                            getBApp().flush();
                        }

                        if (mResults == null || mResults.size() == 0) {
                            mResults = Yahoo.getImages("\"" + lyricsAudio.getArtist() + "\" music");
                            mPos = 0;
                        }
                        if (mResults.size() == 0) {
                            synchronized (this) {
                                setPainting(false);
                                try {
                                    mBusy.setVisible(false);
                                    getBApp().flush();
                                } finally {
                                    setPainting(true);
                                }
                            }
                            return;
                        }

                        NameValue nameValue = (NameValue) mResults.get(mPos);
                        Image image = Tools.getImage(new URL(nameValue.getValue()), -1, -1);

                        if (image != null) {
                            synchronized (this) {
                                setPainting(false);
                                try {
                                    if (mImageView.resource != null)
                                        mImageView.resource.remove();
                                    mUrlText.setValue(nameValue.getName());
                                    mImageView.setVisible(true);
                                    mImageView.setTransparency(1f);
                                    mImageView.setResource(createImage(image), RSRC_IMAGE_BESTFIT);
                                    mImageView.setTransparency(0f, getResource("*500"));
                                    image.flush();
                                    image = null;
                                } finally {
                                    setPainting(true);
                                }
                            }
                        } else {
                            mResults.remove(mPos);
                        }

                    } catch (Exception ex) {
                        Tools.logException(Music.class, ex, "Could not retrieve image");
                        mResults.remove(mPos);
                    } finally {
                        synchronized (this) {
                            setPainting(false);
                            try {
                                if (mResults != null && mResults.size() > 0)
                                    mPosText.setValue(String.valueOf(mPos + 1) + " of "
                                            + String.valueOf(mResults.size()));
                                else
                                    mPosText.setValue("No images found");
                                mBusy.setVisible(false);
                            } finally {
                                setPainting(true);
                            }
                            getBApp().flush();
                        }
                    }
                }

                public void interrupt() {
                    synchronized (this) {
                        super.interrupt();
                    }
                }
            };
            mImageThread.start();
        }

        public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
            updateImage();

            return super.handleEnter(arg, isReturn);
        }

        public boolean handleExit() {
            setPainting(false);
            try {
                if (mImageThread != null && mImageThread.isAlive()) {
                    mImageThread.interrupt();
                    mImageThread = null;
                    mResults.clear();
                    mResults = null;
                }
            } finally {
                setPainting(true);
            }
            return super.handleExit();
        }

        public boolean handleKeyPress(int code, long rawcode) {
            switch (code) {
            case KEY_SELECT:
                postEvent(new BEvent.Action(this, "pop"));
                return true;
            case KEY_UP:
            case KEY_DOWN:
            case KEY_CHANNELUP:
                if (mResults != null && mResults.size() > 0) {
                    getBApp().play("pageup.snd");
                    getBApp().flush();
                    mPos = mPos - 1;
                    if (mPos == -1)
                        mPos = mResults.size() - 1;
                }
                updateImage();
                return true;
            case KEY_CHANNELDOWN:
                if (mResults != null && mResults.size() > 0) {
                    getBApp().play("pagedown.snd");
                    getBApp().flush();
                    mPos = mPos + 1;
                    if (mPos == mResults.size())
                        mPos = 0;
                }
                updateImage();
                return true;
            }
            return super.handleKeyPress(code, rawcode);
        }

        private BView mImageView;

        private Thread mImageThread;

        private Tracker mTracker;

        private List mResults;

        private int mPos;

        private BText mPosText;

        private BText mUrlText;
    }

    private class ScreenSaver extends Thread {
        public ScreenSaver(PlayerScreen playerScreen) {
            mPlayerScreen = playerScreen;
        }

        public void run() {
            while (true) {
                try {
                    sleep(1000 * 5 * 60);
                    synchronized (this) {
                        mPlayerScreen.setTransparency(0.9f, getResource("*60000"));
                    }
                } catch (InterruptedException ex) {
                    return;
                } catch (Exception ex2) {
                    Tools.logException(Music.class, ex2);
                    break;
                }
            }
        }

        public void interrupt() {
            synchronized (this) {
                super.interrupt();
            }
        }

        private PlayerScreen mPlayerScreen;
    }

    public static class MusicFactory extends AppFactory {

        public MusicFactory(AppContext appContext) {
            super(appContext);
        }

        protected void init(ArgumentList args) {
            super.init(args);
            MusicConfiguration musicConfiguration = (MusicConfiguration) getAppContext().getConfiguration();

            MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer().getMusicPlayerConfiguration();
        }
    }
}