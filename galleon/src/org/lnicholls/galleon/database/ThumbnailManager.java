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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.lnicholls.galleon.util.Tools;

//import com.sun.image.codec.jpeg.ImageFormatException;
//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGImageDecoder;
import javax.imageio.*;

public class ThumbnailManager {
//	private static Logger log = Logger.getLogger(ThumbnailManager.class
//			.getName());
	public static interface Callback {
		public void visit(Session session, Thumbnail thumbnail);
	}
	public static Thumbnail retrieveThumbnail(Thumbnail thumbnail)
			throws HibernateException {
		return retrieveThumbnail(new Integer(thumbnail.getId()));
	}
	public static Thumbnail retrieveThumbnail(int id)
	throws HibernateException {
		return retrieveThumbnail(new Integer(id));
	}
	public static Thumbnail retrieveThumbnail(Integer id)
			throws HibernateException {
		Thumbnail result = null;
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			result = (Thumbnail) session.load(Thumbnail.class, id);
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
	public static Thumbnail createThumbnail(Thumbnail thumbnail)
			throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(trim(thumbnail));
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw he;
		} finally {
			HibernateUtil.closeSession();
		}
		return thumbnail;
	}
	public static void updateThumbnail(Thumbnail thumbnail)
			throws HibernateException {
		if (thumbnail.getId()!=0)
		{
			Session session = HibernateUtil.openSession();
	
			Transaction tx = null;
	
			try {
	
				tx = session.beginTransaction();
	
				session.update(trim(thumbnail));
	
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
	public static void deleteThumbnail(Thumbnail thumbnail)
			throws HibernateException {
		if (thumbnail.getId()!=0)
		{
			Session session = HibernateUtil.openSession();
	
			Transaction tx = null;
	
			try {
	
				tx = session.beginTransaction();
	
				session.delete(thumbnail);
	
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
	public static List<Thumbnail> listAll() throws HibernateException {
		List<Thumbnail> list = new ArrayList<Thumbnail>();
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			list = session.createQuery(
					"from org.lnicholls.galleon.database.Thumbnail").list();
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
	public static List<Thumbnail> listBetween(int start, int end)
			throws HibernateException {
		List<Thumbnail> list = new ArrayList<Thumbnail>();
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session
					.createQuery("from org.lnicholls.galleon.database.Thumbnail");
			ScrollableResults items = query.scroll();
			int counter = start;
			if (items.first()) {
				items.scroll(start);
				while (items.next() && (counter < end)) {
					Thumbnail thumbnail = (Thumbnail) items.get(0);
					list.add(thumbnail);
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
					.createQuery("from org.lnicholls.galleon.database.Thumbnail");
			ScrollableResults items = q.scroll();
			if (items.first()) {
				items.beforeFirst();
				while (items.next()) {
					Thumbnail thumbnail = (Thumbnail) items.get(0);
					callback.visit(session, thumbnail);
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
	public static List<Thumbnail> findByKey(String key) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<Thumbnail> list = session
					.createQuery(
							"from org.lnicholls.galleon.database.Thumbnail as thumbnail where thumbnail.keywords=?")
					.setString(
					0, key).list();
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
	public static BufferedImage findImageByKey(String key)
			throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<Thumbnail> list = session
					.createQuery(
							"from org.lnicholls.galleon.database.Thumbnail as thumbnail where thumbnail.keywords=?")
					.setString(
					0, key).list();
			BufferedImage image = null;
			if (list.size() > 0) {
				Thumbnail thumbnail = list.get(0);
				Blob blob = thumbnail.getImage();
				image = createImage(blob);
			}
			tx.commit();
			return image;
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw he;
		} finally {
			HibernateUtil.closeSession();
		}
	}
	public static BufferedImage findImageById(int id)
			throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Thumbnail thumbnail = (Thumbnail) session.load(Thumbnail.class, new Integer(id));
			BufferedImage image = null;
			Blob blob = thumbnail.getImage();
			image = createImage(blob);
			tx.commit();
			return image;
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw he;
		} finally {
			HibernateUtil.closeSession();
		}
	}
	private static BufferedImage createImage(Blob blob) {
		BufferedImage image = null;
		try {
			InputStream is = blob.getBinaryStream();
			//JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(is);
			//image = decoder.decodeAsBufferedImage();
			image = ImageIO.read(is);
		//} catch (ImageFormatException ex) {
		} catch (IIOException ex) {
			Tools.logException(ThumbnailManager.class, ex);
		} catch (SQLException ex) {
			Tools.logException(ThumbnailManager.class, ex);
		} catch (IOException ex) {
			Tools.logException(ThumbnailManager.class, ex);
		}
		return image;
	}
	private static Thumbnail trim(Thumbnail thumbnail) {
		if (thumbnail!=null)
		{
			thumbnail.setKeywords(Tools.trim(thumbnail.getKeywords(), 1024));
	
			thumbnail.setMimeType(Tools.trim(thumbnail.getMimeType(), 50));
	
			thumbnail.setTitle(Tools.trim(thumbnail.getTitle(), 255));
		}
		return thumbnail;
	}
}
