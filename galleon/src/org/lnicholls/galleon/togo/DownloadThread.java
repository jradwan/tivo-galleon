package org.lnicholls.galleon.togo;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import org.lnicholls.galleon.server.*;
import org.lnicholls.galleon.util.*;

public class DownloadThread extends Thread implements Constants {
    private static Logger log = Logger.getLogger(DownloadThread.class.getName());

    public DownloadThread(Server server) throws IOException {
        super("DownloadThread");
        mServer = server;
        setPriority(Thread.MIN_PRIORITY);

        mToGo = new ToGo(server.getServerConfiguration());
    }

    public void run() {
        while (true) {
            try {
                boolean retry = false;
                Show next = mToGo.pickNextShowForDownloading();

                if (next != null) {
                    if (log.isDebugEnabled())
                        log.debug("Picked: " + next);

                    ToGoList togoList = new ToGoList();
                    ArrayList downloaded = togoList.load();
                    boolean found = false;
                    Iterator iterator = downloaded.iterator();
                    while (iterator.hasNext()) {
                        Show show = (Show) iterator.next();
                        if (show.equals(next)) {
                            show.setStatus(Show.STATUS_DOWNLOADING);
                            break;
                        }
                    }
                    togoList.save(downloaded);

                    CancelThread cancelThread = new CancelThread(next);
                    cancelThread.start();
                    boolean success = mToGo.Download(next, cancelThread);

                    if (success) {
                        if (cancelThread.isAlive()) {
                            cancelThread.interrupt();
                        }

                        if (!cancelThread.cancel()) {
                            downloaded = togoList.load();
                            iterator = downloaded.iterator();
                            while (iterator.hasNext()) {
                                Show show = (Show) iterator.next();
                                if (show.equals(next)) {
                                    show.setStatus(Show.STATUS_DOWNLOADED);
                                    show.setPath(next.getPath());
                                    break;
                                }
                            }
                            togoList.save(downloaded);
                        }
                    }
                } else
                    sleep(1000 * 30);
            } catch (InterruptedException ex) {
            } // handle silently for waking up
            catch (Exception ex2) {
                Tools.logException(ToGoThread.class, ex2);
            }
        }
    }

    class CancelThread extends Thread implements CancelDownload {
        public CancelThread(Show show) throws IOException {
            super("CancelThread");
            setPriority(Thread.MIN_PRIORITY);
            mShow = show;
            mCancel = false;
        }

        public void run() {
            while (!mCancel) {
                try {
                    ToGoList togoList = new ToGoList();
                    ArrayList downloaded = togoList.load();
                    boolean found = false;
                    Iterator iterator = downloaded.iterator();
                    while (iterator.hasNext()) {
                        Show show = (Show) iterator.next();
                        if (show.equals(mShow) && show.getStatus() == Show.STATUS_USER_CANCELLED) {
                            log.info("Download cancelled by user: " + show.getTitle());
                            mCancel = true;
                            break;
                        }
                    }
                    sleep(1000 * 60 * 1);
                } catch (InterruptedException ex) {
                    return;
                } // handle silently for waking up
                catch (Exception ex2) {
                    Tools.logException(CancelThread.class, ex2);
                }
            }
        }

        public boolean cancel() {
            return mCancel;
        }

        private Show mShow;

        private boolean mCancel;
    }

    private Server mServer;

    private ToGo mToGo;

    public void setServerConfiguration(ServerConfiguration value) {
        mToGo.setServerConfiguration(value);
    }
}
