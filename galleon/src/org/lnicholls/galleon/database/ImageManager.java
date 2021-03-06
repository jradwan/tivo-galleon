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
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.classic.Session;
import org.hibernate.Transaction;
//import org.apache.log4j.Logger;
import org.lnicholls.galleon.util.Tools;
public class ImageManager {
//	private static Logger log = Logger.getLogger(ImageManager.class.getName());
	public static interface Callback {
		public void visit(Session session, Image image);
	}
	public static Image retrieveImage(Image image) throws HibernateException {
		return retrieveImage(new Integer(image.getId()));
	}
	public static Image retrieveImage(int id) throws HibernateException {
		return retrieveImage(new Integer(id));
	}
	public static Image retrieveImage(Integer id) throws HibernateException {
		Image result = null;
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			result = (Image) session.load(Image.class, id);
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
	public static Image createImage(Image image) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(trim(image));
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw he;
		} finally {
			HibernateUtil.closeSession();
		}
		return image;
	}
	public static void updateImage(Image image) throws HibernateException {
		if (image.getId()!=0)
		{
			Session session = HibernateUtil.openSession();
	
			Transaction tx = null;
	
			try {
	
				tx = session.beginTransaction();
	
				session.update(trim(image));
	
				tx.commit();
	
			} catch (HibernateException he) {
	
				if (tx != null)
	
					tx.rollback();
	
				throw he;
	
			} finally {
	
				HibernateUtil.closeSession();
	
			}
		}
	}
	public static void deleteImage(Image image) throws HibernateException {
		if (image.getId()!=0)
		{
			Session session = HibernateUtil.openSession();
	
			Transaction tx = null;
	
			try {
	
				tx = session.beginTransaction();
	
				session.delete(image);
	
				tx.commit();
	
			} catch (HibernateException he) {
	
				if (tx != null)
	
					tx.rollback();
	
				throw he;
	
			} finally {
	
				HibernateUtil.closeSession();
	
			}
		}
	}
	@SuppressWarnings("unchecked")
	public static List<Image> listAll() throws HibernateException {
		List<Image> list = new ArrayList<Image>();
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			list = session.createQuery(
					"from org.lnicholls.galleon.database.Image").list();
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
	public static List<Image> listBetween(int start, int end)
			throws HibernateException {
		List<Image> list = new ArrayList<Image>();
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session
					.createQuery("from org.lnicholls.galleon.database.Image");
			ScrollableResults items = query.scroll();
			int counter = start;
			if (items.first()) {
				items.scroll(start);
				while (items.next() && (counter < end)) {
					Image image = (Image) items.get(0);
					list.add(image);
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
			Query q = session
					.createQuery("from org.lnicholls.galleon.database.Image");
			ScrollableResults items = q.scroll();
			if (items.first()) {
				items.beforeFirst();
				while (items.next()) {
					Image image = (Image) items.get(0);
					callback.visit(session, image);
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
	@SuppressWarnings("unchecked")
	public static List<Image> findByPath(String path) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<Image> list = session
					.createQuery(
							"from org.lnicholls.galleon.database.Image as Image where Image.path=?")
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
	@SuppressWarnings("unchecked")
	public static long countImagesByOrigin(String origin)
			throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<Long> list = session
					.createQuery(
							"select count(image) from org.lnicholls.galleon.database.Image as image where image.origen=?")
					.setString(0, origin).list();
			tx.commit();
			return ((Long) list.iterator().next()).longValue();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw he;
		} finally {
			HibernateUtil.closeSession();
		}
	}
	@SuppressWarnings("unchecked")
	public static List<Image> findByExternalId(String id) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<Image> list = session
					.createQuery(
					"from org.lnicholls.galleon.database.Image as Image where Image.externalId=?")
					.setString(0, id)
					.list();
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
	private static Image trim(Image image)
	{
		if (image != null) {
			image.setComments(Tools.trim(image.getComments(), 2048));
			image.setMimeType(Tools.trim(image.getMimeType(), 50));
			image.setOrigen(Tools.trim(image.getOrigen(), 30));
			image.setPath(Tools.trim(image.getPath(), 1024));
			image.setTitle(Tools.trim(image.getTitle(), 255));
			image.setTone(Tools.trim(image.getTone(), 50));
		}
		return image;
	}
}
