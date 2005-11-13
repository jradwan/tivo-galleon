package org.lnicholls.galleon.database;

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

import java.util.ArrayList;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.ScrollableResults;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.log4j.Logger;

public class PlaylistsTracksManager {

    private static Logger log = Logger.getLogger(PlaylistsTracksManager.class.getName());

    public static interface Callback {
        public void visit(Session session, PlaylistsTracks PlaylistsTracks);
    }

    public static PlaylistsTracks retrievePlaylistsTracks(PlaylistsTracks PlaylistsTracks) throws HibernateException {
        return retrievePlaylistsTracks(PlaylistsTracks.getId());
    }

    public static PlaylistsTracks retrievePlaylistsTracks(Integer id) throws HibernateException {

        PlaylistsTracks result = null;
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            result = (PlaylistsTracks) session.load(PlaylistsTracks.class, id);
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null)
                tx.rollback();
            throw he;
        } finally {
            HibernateUtil.closeSession();
        }
        return result;
    }

    public static PlaylistsTracks createPlaylistsTracks(PlaylistsTracks PlaylistsTracks) throws HibernateException {

        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(PlaylistsTracks);
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null)
                tx.rollback();
            throw he;
        } finally {
            HibernateUtil.closeSession();
        }
        return PlaylistsTracks;
    }

    public static void updatePlaylistsTracks(PlaylistsTracks PlaylistsTracks) throws HibernateException {

        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(PlaylistsTracks);
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null)
                tx.rollback();
            throw he;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public static void deletePlaylistsTracks(PlaylistsTracks PlaylistsTracks) throws HibernateException {
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(PlaylistsTracks);
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null)
                tx.rollback();
            throw he;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public static List listAll() throws HibernateException {
        List list = new ArrayList();
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            list = session.createQuery("from org.lnicholls.galleon.database.PlaylistsTracks").list();
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null)
                tx.rollback();
            throw he;
        } finally {
            HibernateUtil.closeSession();
        }
        return list;
    }

    public static List listBetween(int start, int end) throws HibernateException {
        List list = new ArrayList();
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Query query = session.createQuery("from org.lnicholls.galleon.database.PlaylistsTracks");
            ScrollableResults items = query.scroll();
            int counter = start;
            if (items.first()) {
                items.scroll(start);
                while (items.next() && (counter < end)) {
                    PlaylistsTracks PlaylistsTracks = (PlaylistsTracks) items.get(0);
                    list.add(PlaylistsTracks);
                    counter++;
                }
            }

            tx.commit();
        } catch (HibernateException he) {
            if (tx != null)
                tx.rollback();
            throw he;
        } finally {
            HibernateUtil.closeSession();
        }
        return list;
    }

    public static void scroll(Callback callback) throws HibernateException {
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Query q = session.createQuery("from org.lnicholls.galleon.database.PlaylistsTracks");
            ScrollableResults items = q.scroll();
            if (items.first()) {
                items.beforeFirst();
                while (items.next()) {
                    PlaylistsTracks PlaylistsTracks = (PlaylistsTracks) items.get(0);
                    callback.visit(session, PlaylistsTracks);
                }
                ;
            }
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null)
                tx.rollback();
            throw he;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public static List findByPlaylists(Integer id) throws HibernateException {
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            List list = session.createQuery(
                    "from org.lnicholls.galleon.database.PlaylistsTracks as PlaylistsTracks where PlaylistsTracks.playlists=?")
                    .setInteger(0, id.intValue()).list();

            tx.commit();

            return list;
        } catch (HibernateException he) {
            if (tx != null)
                tx.rollback();
            throw he;
        } finally {
            HibernateUtil.closeSession();
        }
    }
}