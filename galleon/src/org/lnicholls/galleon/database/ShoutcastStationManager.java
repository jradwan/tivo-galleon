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
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import org.apache.log4j.Logger;
import org.lnicholls.galleon.util.Tools;
public class ShoutcastStationManager {
	private static Logger log = Logger.getLogger(ShoutcastStationManager.class
			.getName());
	public static interface Callback {
		public void visit(Session session, ShoutcastStation ShoutcastStation);
	}
	public static ShoutcastStation retrieveShoutcastStation(
			ShoutcastStation ShoutcastStation) throws HibernateException {
		return retrieveShoutcastStation(ShoutcastStation.getId());
	}
	public static ShoutcastStation retrieveShoutcastStation(Integer id)
			throws HibernateException {
		ShoutcastStation result = null;
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			result = (ShoutcastStation) session
					.load(ShoutcastStation.class, id);
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
	public static ShoutcastStation createShoutcastStation(
			ShoutcastStation ShoutcastStation) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(trim(ShoutcastStation));
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw he;
		} finally {
			HibernateUtil.closeSession();
		}
		return ShoutcastStation;
	}
	public static void updateShoutcastStation(ShoutcastStation ShoutcastStation)
			throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(trim(ShoutcastStation));
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw he;
		} finally {
			HibernateUtil.closeSession();
		}
	}
	public static void deleteShoutcastStation(ShoutcastStation ShoutcastStation)
			throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(ShoutcastStation);
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
			list = session.createQuery(
					"from org.lnicholls.galleon.database.ShoutcastStation")
					.list();
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
	public static List findByGenre(String genre) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List list = session
					.createQuery(
							"from org.lnicholls.galleon.database.ShoutcastStation as ShoutcastStation where ShoutcastStation.genre=?")
					.setString(0, genre).list();
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
	public static List findByUrl(String url) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List list = session
					.createQuery(
							"from org.lnicholls.galleon.database.ShoutcastStation as ShoutcastStation where ShoutcastStation.url=?")
					.setString(0, url).list();
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
	private static ShoutcastStation trim(ShoutcastStation ShoutcastStation)
	{
		if (ShoutcastStation!=null)
		{
			ShoutcastStation.setGenre(Tools.trim(ShoutcastStation.getGenre(), 30));
	
			ShoutcastStation.setUrl(Tools.trim(ShoutcastStation.getUrl(), 1024));
		}
		return ShoutcastStation;
	}
}