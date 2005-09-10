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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.ScrollableResults;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.server.Server;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.Tools;

public class VideocastManager {

    private static Logger log = Logger.getLogger(VideocastManager.class.getName());

    public static interface Callback {
        public void visit(Session session, Videocast Videocast);
    }

    public static Videocast retrieveVideocast(Videocast Videocast) throws HibernateException {
        return retrieveVideocast(Videocast.getId());
    }

    public static Videocast retrieveVideocast(Integer id) throws HibernateException {

        Videocast result = null;
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            result = (Videocast) session.load(Videocast.class, id);
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

    public static Videocast createVideocast(Videocast Videocast) throws HibernateException {

        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(Videocast);
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null)
                tx.rollback();
            throw he;
        } finally {
            HibernateUtil.closeSession();
        }
        return Videocast;
    }

    public static void updateVideocast(Videocast Videocast) throws HibernateException {

        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(Videocast);
            tx.commit();
        } catch (HibernateException he) {
        	log.debug(Videocast.getPath());
            if (tx != null)
                tx.rollback();
            throw he;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public static void deleteVideocast(Videocast Videocast) throws HibernateException {
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(Videocast);
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
            list = session.createQuery("from org.lnicholls.galleon.database.Videocast").list();
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
    
    public static List listAllSubscribed() throws HibernateException {
        List list = new ArrayList();
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            list = session.createQuery("from org.lnicholls.galleon.database.Videocast as videocast where videocast.status=?").setInteger(0,
                    Videocast.STATUS_SUBSCRIBED).list();
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

            Query query = session.createQuery("from org.lnicholls.galleon.database.Videocast");
            ScrollableResults items = query.scroll();
            int counter = start;
            if (items.first()) {
                items.scroll(start);
                while (items.next() && (counter < end)) {
                    Videocast Videocast = (Videocast) items.get(0);
                    list.add(Videocast);
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
            Query q = session.createQuery("from org.lnicholls.galleon.database.Videocast");
            ScrollableResults items = q.scroll();
            if (items.first()) {
                items.beforeFirst();
                while (items.next()) {
                    Videocast Videocast = (Videocast) items.get(0);
                    callback.visit(session, Videocast);
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

    public static List findByPath(String path) throws HibernateException {
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            List list = session.createQuery(
                    "from org.lnicholls.galleon.database.Videocast as Videocast where Videocast.path=?")
                    .setString(0, path).list();

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

    public static List findByOrigen(String origen) throws HibernateException {
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            List list = session.createQuery(
                    "from org.lnicholls.galleon.database.Videocast as Videocast where Videocast.origen=?").setString(0,
                    origen).list();

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

    public static List findByTitle(String title) throws HibernateException {
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            List list = session.createQuery(
                    "from org.lnicholls.galleon.database.Videocast as Videocast where Videocast.title=?").setString(0,
                    title).list();

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

    public static List findByExternalId(String id) throws HibernateException {
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            List list = session.createQuery(
                    "from org.lnicholls.galleon.database.Videocast as Videocast where Videocast.externalId=?").setString(
                    0, id).list();

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
    
    public static List listTitles() throws HibernateException {
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            List list = session.createQuery(
                    "select Videocast.title from org.lnicholls.galleon.database.Videocast as Videocast").list();

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
    
    public static List getVideocasts() throws HibernateException
    {
    	List names = new ArrayList();
    	try {
            List videocasts = listAllSubscribed();
            if (videocasts != null && videocasts.size() > 0) {
                for (Iterator i = videocasts.iterator(); i.hasNext(); /* Nothing */) {
                    Videocast videocast = (Videocast) i.next();
                    names.add(new NameValue(videocast.getTitle(),videocast.getPath()));
                }
            }
        } catch (Exception ex) {
            Tools.logException(VideocastManager.class, ex);
        }
    	return names;
    }    
    
    public static void setVideocasts(List list) throws HibernateException
    {
    	try {
            List videocasts = listAllSubscribed();
            if (videocasts != null && videocasts.size() > 0) {
                Iterator iterator = list.iterator();
                while (iterator.hasNext())
                {
                	NameValue nameValue = (NameValue)iterator.next();
                	boolean found = false;
                	for (Iterator i = videocasts.iterator(); i.hasNext(); /* Nothing */) {
                        Videocast videocast = (Videocast) i.next();
                        if (videocast.getPath().equals(nameValue.getValue()))
                        {
                        	videocast.setTitle(nameValue.getName());
                        	updateVideocast(videocast);
                        	found = true;
                        	break;
                        }
                    }
                    
                    if (!found)
                    {
    	            	Videocast videocast = new Videocast(nameValue.getName(), Videocast.STATUS_SUBSCRIBED, nameValue.getValue(), 0, new ArrayList());
    	                createVideocast(videocast);
                    }
                }
                
                // Remove videocasts no longer on list
                for (Iterator i = videocasts.iterator(); i.hasNext(); /* Nothing */) {
                    Videocast videocast = (Videocast) i.next();
                    
                    boolean found = false;
                    iterator = list.iterator();
                    while (iterator.hasNext())
                    {
                    	NameValue nameValue = (NameValue)iterator.next();
                    	if (videocast.getPath().equals(nameValue.getValue()))
                    	{
                    		found = true;
                    		break;
                    	}
                    }
                    if (!found)
                    {
                    	videocast.setStatus(Videocast.STATUS_DELETED);
                    	updateVideocast(videocast);
                    }
                }
            }
            else
            {
            	Iterator iterator = list.iterator();
                while (iterator.hasNext())
                {
                	NameValue nameValue = (NameValue)iterator.next();
  	            	Videocast videocast = new Videocast(nameValue.getName(), Videocast.STATUS_SUBSCRIBED, nameValue.getValue(), 0, new ArrayList());
   	                createVideocast(videocast);
                }            	
            }
        } catch (Exception ex) {
            Tools.logException(VideocastManager.class, ex);
        }
    }    
    
}