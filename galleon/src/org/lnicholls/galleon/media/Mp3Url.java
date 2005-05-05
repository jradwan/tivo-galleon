package org.lnicholls.galleon.media;

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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javazoom.spi.mpeg.sampled.file.tag.IcyInputStream;
import javazoom.spi.mpeg.sampled.file.tag.MP3Tag;
import javazoom.spi.mpeg.sampled.file.tag.TagParseEvent;
import javazoom.spi.mpeg.sampled.file.tag.TagParseListener;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.params.*;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.lnicholls.galleon.apps.weather.StateData;
import org.lnicholls.galleon.database.Audio;
import org.lnicholls.galleon.database.AudioManager;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.widget.DefaultApplication;

import EDU.oswego.cs.dl.util.concurrent.Callable;
import EDU.oswego.cs.dl.util.concurrent.TimedCallable;

public final class Mp3Url {
    private static final Logger log = Logger.getLogger(Mp3Url.class.getName());

    public static final Audio getAudio(String url) {
        Audio audio = new Audio();
        Mp3File.defaultProperties(audio);
        audio.setPath(url);
        audio.setDuration(-1);
        return audio;
    }

    public static InputStream getStream(String uri) throws IOException {
        return getStream(uri, null);
    }

    public static InputStream getStream(String uri, DefaultApplication application) throws IOException {
        if (uri.toLowerCase().endsWith(".http.mp3")) {
            log.debug("getStream: " + uri + ", " + application);

            try {
                class TimedThread implements Callable {
                    private DefaultApplication mApplication = null;

                    private String mPath = null;

                    public TimedThread(String path, DefaultApplication application) {
                        mPath = path;
                        mApplication = application;
                    }

                    public synchronized java.lang.Object call() throws java.lang.Exception {
                        //URL url = new URL("http://64.236.34.4:80/stream/1065");
                        URL url = new URL(mPath);
                        URLConnection conn = url.openConnection();
                        conn.setRequestProperty("Icy-Metadata", "1");
                        conn.setRequestProperty("http.useragent", "WinampMPEG/5.0");
                        IcyInputStream input = new IcyInputStream(new URLStream(conn.getInputStream(), conn
                                .getContentLength()));
                        final IcyListener icyListener = new IcyListener(mApplication);
                        input.addTagParseListener(icyListener);
                        
                        return input;
                        
                        /*
                        try
                        {
                            System.out.println(mPath);
                            HttpClient httpclient = new HttpClient();
                            httpclient.setStrictMode(false);
                            httpclient.setConnectionTimeout(1000);
                            HttpConnectionManager httpConnectionManager = httpclient.getHttpConnectionManager();
                            HttpConnectionManagerParams httpConnectionManagerParams = httpConnectionManager.getParams();
                            httpConnectionManagerParams.setConnectionTimeout(1000);
                            httpclient.getParams().setParameter("http.socket.timeout", new Integer(1000));
                            httpclient.getParams().setParameter("http.useragent", "WinampMPEG/5.0");
                            httpclient.getParams().setParameter("Icy-Metadata", "1");
    
                            GetMethod get = new GetMethod(mPath);
                            //NameValuePair state = new NameValuePair("state", StateData.getFipFromSymbol(mState));
                            //NameValuePair place = new NameValuePair("place", mCity + "," + mState + "," + mZip);
                            //get.setQueryString(new NameValuePair[] { state, place });
                            get.setFollowRedirects(true);
    
                            System.out.println("before result:");
                            int iGetResultCode = httpclient.executeMethod(get);
                            System.out.println(iGetResultCode);
                            
                            IcyInputStream input = new IcyInputStream(new URLStream(get.getResponseBodyAsStream(), 0));
                            final IcyListener icyListener = new IcyListener(mApplication);
                            input.addTagParseListener(icyListener);
                            
                            return input;
                        }
                        catch (Exception ex)
                        {
                            Tools.logException(Mp3Url.class, ex, mPath);
                            throw ex;
                        }
                        */
                    }
                }

                String id = Tools.extractName(Tools.extractName(uri));
                Audio audio = AudioManager.retrieveAudio(Integer.valueOf(id));

                TimedThread timedThread = new TimedThread(audio.getPath(), application);
                TimedCallable timedCallable = new TimedCallable(timedThread, 1000 * 10);
                InputStream mp3Stream = (InputStream) timedCallable.call();

                if (mp3Stream != null)
                    return mp3Stream;
            } catch (Exception ex) {
                Tools.logException(Mp3Url.class, ex, uri);
            }
        }
        return Mp3Url.class.getResourceAsStream("/couldnotconnect.mp3");
    }

    private static final class URLStream extends FilterInputStream {
        URLStream(InputStream in, long contentLength) {
            super(in);
            mContentLength = contentLength;
        }

        public int available() {
            return (int) mContentLength;
        }

        public int read() throws IOException {
            mContentLength -= 1;
            return in.read();
        }

        public int read(byte b[], int off, int length) throws IOException {
            // Ugly hack to detect tight loop because TiVo didnt close the stream properly
            if (System.currentTimeMillis() - mTime == 0) {
                if (mCounter++ == 10)
                    close();
            } else
                mCounter = 0;
            mTime = System.currentTimeMillis();
            int n = super.read(b, off, length);
            if (n > 0) {
                mContentLength -= n;
            }
            return n;
        }

        public long skip(long n) throws IOException {
            n = super.skip(n);
            if (n > 0) {
                mContentLength -= n;
            }
            return n;
        }

        public void close() throws IOException {
            super.close();
        }

        private long mContentLength;

        private long mTime = System.currentTimeMillis();

        private int mCounter;
    }

    public static class IcyListener implements TagParseListener {
        public IcyListener(DefaultApplication application) {
            mApplication = application;
        }

        public void tagParsed(TagParseEvent tpe) {
            mLastTag = tpe.getTag();
            String name = mLastTag.getName();
            log.debug("tagParsed=" + name + "=" + mLastTag.getValue());
            if ((name != null) && (name.equalsIgnoreCase("streamtitle"))) {
                mLastTitle = (String) mLastTag.getValue();
            } else if ((name != null) && (name.equalsIgnoreCase("streamurl"))) {
                mLastUrl = (String) mLastTag.getValue();
            }

            mApplication.getPlayer().setTitle(mLastTitle);
        }

        public MP3Tag getLastTag() {
            return mLastTag;
        }

        public void setLastTag(MP3Tag tag) {
            mLastTag = tag;
        }

        public String getTitle() {
            return mLastTitle;
        }

        public String getUrl() {
            return mLastUrl;
        }

        public void setTitle(String string) {
            mLastTitle = string;
        }

        public void setUrl(String string) {
            mLastUrl = string;
        }

        private DefaultApplication mApplication;

        private MP3Tag mLastTag = null;

        private String mLastTitle = null;

        private String mLastUrl = null;
    }
}