package org.lnicholls.galleon.media;

/*
 *
 * Copyright (C) 2004 Leon Nicholls
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

import java.io.File;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

import org.lnicholls.galleon.util.*;
import org.lnicholls.galleon.database.*;
import org.lnicholls.galleon.media.*;
import org.lnicholls.galleon.media.MediaRefreshThread.PathInfo;

import org.apache.log4j.Logger;

import EDU.oswego.cs.dl.util.concurrent.Callable;
import EDU.oswego.cs.dl.util.concurrent.TimedCallable;

public final class MediaManager {
    private static final Logger log = Logger.getLogger(MediaManager.class.getName());
    
    static
    {
        ImageIO.setUseCache(false);
    }
    
    public static final Media getMedia(String filename)
    {
        return getMedia(null, filename);
    }

    public static final Media getMedia(Object object, String filename)
    {
        if (filename.toLowerCase().endsWith(".m3u"))
        {
            //return new M3uPlaylistProxy(filename);
        }    
        else if (filename.toLowerCase().startsWith("http"))
        {
            //return new Mp3UrlProxy(filename);
        }    
        else if (filename.toLowerCase().endsWith(".pls"))
        {
            //return new PlsPlaylistProxy(filename);
        }     
        else if (filename.toLowerCase().endsWith(".mp3"))
        {
            if (object!=null)
                return Mp3File.getAudio((Audio)object, filename);
            else
                return Mp3File.getAudio(filename);
        }    
        else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg"))
        {
            return JpgFile.getImage(filename);
        }    
        else if (filename.toLowerCase().endsWith(".png") || filename.toLowerCase().endsWith(".bmp")
                || filename.toLowerCase().endsWith(".gif") || filename.toLowerCase().endsWith(".pnm")
                || filename.toLowerCase().endsWith(".tiff") || filename.toLowerCase().endsWith(".tif")
                || filename.toLowerCase().endsWith(".wbmp") || filename.toLowerCase().endsWith(".fpx")
                || filename.toLowerCase().endsWith(".pgm"))
        {               
            //return new ImageProxy(filename);
        }    
        else
            return null;    
            
        return null;    
    }
    
    public static final void refresh()
    {
        if (mMediaRefreshThread!=null && !mMediaRefreshThread.isAlive())
        {
            mMediaRefreshThread.interrupt();
            mMediaRefreshThread = null;
        }

        try
        {
            mMediaRefreshThread = new MediaRefreshThread();
            for (int i=0;i<mPaths.size();i++)
                mMediaRefreshThread.addPath((PathInfo)mPaths.get(i));
            //new MediaRefreshThread.PathInfo("d:/download/mp3",FileFilters.audioDirectoryFilter)
            mMediaRefreshThread.start();
        }
        catch (Exception ex)
        {
            Tools.logException(MediaManager.class, ex);
        }
    }
    
    public static final void addPath(PathInfo pathInfo)
    {
        mPaths.add(pathInfo);
        if (mMediaRefreshThread!=null)
            refresh();
    }
    
    public static final void removePath(PathInfo pathInfo)
    {
        mPaths.remove(pathInfo);
        if (mMediaRefreshThread!=null)
            refresh();
    }    
    
    /**
     * Utility class used to do tasks at regular intervals. Used for adding tasks to system timer queue.
     */
    public static final class RefreshTask extends TimerTask {
        public void run() {
            if (log.isDebugEnabled())
                log.debug("RefreshTask run:");
            try {
                refresh();
            } catch (Exception ex) {
                Tools.logException(RefreshTask.class, ex);
            } catch (OutOfMemoryError ex) {
                if (log.isDebugEnabled())
                    Tools.logMemory();
                System.gc();
                if (log.isDebugEnabled())
                    Tools.logMemory();
            } catch (Throwable ex) {
                if (log.isDebugEnabled()) {
                    StringWriter writer = new StringWriter();
                    ex.printStackTrace(new PrintWriter(writer));
                    log.debug(writer.toString());
                }
            }
        }
    }

    private static MediaRefreshThread mMediaRefreshThread;
    private static ArrayList mPaths = new ArrayList();
}