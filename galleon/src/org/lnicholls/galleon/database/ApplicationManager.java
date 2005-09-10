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
import org.lnicholls.galleon.app.AppContext;

public class ApplicationManager {

	private static Logger log = Logger.getLogger(ApplicationManager.class.getName());

	public static interface Callback {
		public void visit(Session session, Application Application);
	}

	public static Application retrieveApplication(Application Application) throws HibernateException {
		return retrieveApplication(Application.getId());
	}

	public static Application retrieveApplication(Integer id) throws HibernateException {

		Application result = null;
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			result = (Application) session.load(Application.class, id);
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

	public static Application createApplication(Application Application) throws HibernateException {

		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(Application);
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw he;
		} finally {
			HibernateUtil.closeSession();
		}
		return Application;
	}

	public static void updateApplication(Application Application) throws HibernateException {

		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(Application);
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw he;
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public static void deleteApplication(Application Application) throws HibernateException {

		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(Application);
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
			list = session.createQuery("from org.lnicholls.galleon.database.Application").list();
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

	public static List findByClazz(String clazz) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			List list = session.createQuery(
					"from org.lnicholls.galleon.database.Application as Application where Application.clazz=?")
					.setString(0, clazz).list();

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
	
	public static void trackApplication(AppContext appContext) throws HibernateException {
		List list = findByClazz(appContext.getDescriptor().getClassName());
		if (list==null || list.size()==0)
		{
			Application application = new Application(appContext.getDescriptor().getClassName(), appContext.getDescriptor().getTitle(), appContext.getDescriptor().getVersion(), 1);
			application = createApplication(application);
		}
		else
		{
			Application application = (Application)list.get(0);
			application.setTotal(application.getTotal()+1);
		}
	}
}