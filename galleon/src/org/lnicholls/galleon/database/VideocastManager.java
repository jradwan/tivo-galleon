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
import java.util.Iterator;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.classic.Session;
import org.hibernate.Transaction;
import org.apache.log4j.Logger;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.Tools;
public class VideocastManager {
	private static Logger log = Logger.getLogger(VideocastManager.class
			.getName());
	public static interface Callback {
		public void visit(Session session, Videocast Videocast);
	}
	public static Videocast retrieveVideocast(Videocast Videocast)
			throws HibernateException {
		return retrieveVideocast(new Integer(Videocast.getId()));
	}
	public static Videocast retrieveVideocast(int id)
	throws HibernateException {
		return retrieveVideocast(new Integer(id));
	}
	public static Videocast retrieveVideocast(Integer id)
			throws HibernateException {
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
	public static Videocast createVideocast(Videocast Videocast)
			throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(trim(Videocast));
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
	public static void updateVideocast(Videocast Videocast)
			throws HibernateException {
		if (Videocast.getId()!=0)
		{
			Session session = HibernateUtil.openSession();
	
			Transaction tx = null;
	
			try {
	
				tx = session.beginTransaction();
	
				session.update(trim(Videocast));
	
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
	}
	public static void deleteVideocast(Videocast Videocast)
			throws HibernateException {
		if (Videocast.getId()!=0)
		{
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
	}
	@SuppressWarnings("unchecked")
	public static List<Videocast> listAll() throws HibernateException {
		List<Videocast> list = new ArrayList<Videocast>();
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			list = session.createQuery(
					"from org.lnicholls.galleon.database.Videocast").list();
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
	@SuppressWarnings("unchecked")
	public static List<Videocast> listAllSubscribed() throws HibernateException {
		List<Videocast> list = new ArrayList<Videocast>();
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			list = session
					.createQuery(
							"from org.lnicholls.galleon.database.Videocast as videocast where videocast.status=?")
					.setInteger(
					0, Videocast.STATUS_SUBSCRIBED).list();
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
	public static List<Videocast> listBetween(int start, int end)
			throws HibernateException {
		List<Videocast> list = new ArrayList<Videocast>();
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session
					.createQuery("from org.lnicholls.galleon.database.Videocast");
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
			Query q = session
					.createQuery("from org.lnicholls.galleon.database.Videocast");
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
	@SuppressWarnings("unchecked")
	public static List<Videocast> findByPath(String path) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<Videocast> list = session
					.createQuery(
							"from org.lnicholls.galleon.database.Videocast as Videocast where Videocast.path=?")
					.setString(0,
					path).list();
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
	public static List<Videocast> findByOrigen(String origen) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<Videocast> list = session
					.createQuery(
							"from org.lnicholls.galleon.database.Videocast as Videocast where Videocast.origen=?")
					.setString(0,
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
	@SuppressWarnings("unchecked")
	public static List<Videocast> findByTitle(String title) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<Videocast> list = session
					.createQuery(
							"from org.lnicholls.galleon.database.Videocast as Videocast where Videocast.title=?")
					.setString(0,
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
	@SuppressWarnings("unchecked")
	public static List<Videocast> findByExternalId(String id) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<Videocast> list = session
					.createQuery(
							"from org.lnicholls.galleon.database.Videocast as Videocast where Videocast.externalId=?")
					.setString(0, id).list();
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
	public static List<Videocast> listTitles() throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<Videocast> list = session
					.createQuery(
							"select Videocast.title from org.lnicholls.galleon.database.Videocast as Videocast")
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
	public static List<NameValue> getVideocasts() throws HibernateException {
		List<NameValue> names = new ArrayList<NameValue>();
		try {
			List<Videocast> videocasts = listAllSubscribed();
			if (videocasts != null && videocasts.size() > 0) {
				for (Iterator<Videocast> i = videocasts.iterator(); i.hasNext(); /* Nothing */) {
					Videocast videocast = i.next();
					names.add(new NameValue(videocast.getTitle(), videocast
							.getPath()));
				}
			}
		} catch (Exception ex) {
			Tools.logException(VideocastManager.class, ex);
		}
		return names;
	}
	public static void setVideocasts(List<NameValue> list) throws HibernateException {
		try {
			List<Videocast> videocasts = listAllSubscribed();
			if (videocasts != null && videocasts.size() > 0) {
				Iterator<NameValue> iterator = list.iterator();
				while (iterator.hasNext()) {
					NameValue nameValue = (NameValue) iterator.next();
					boolean found = false;
					for (Iterator<Videocast> i = videocasts.iterator(); i.hasNext(); /* Nothing */) {
						Videocast videocast = i.next();
						if (videocast.getPath().equals(nameValue.getValue())) {
							videocast.setTitle(nameValue.getName());
							updateVideocast(videocast);
							found = true;
							break;
						}
					}
					if (!found) {
						Videocast videocast = new Videocast(
								nameValue.getName(),
								Videocast.STATUS_SUBSCRIBED, nameValue
								.getValue(), 0, new ArrayList<VideocastTrack>());
						createVideocast(videocast);
					}
				}
				// Remove videocasts no longer on list
				for (Iterator<Videocast> i = videocasts.iterator(); i.hasNext(); /* Nothing */) {
					Videocast videocast = (Videocast) i.next();
					boolean found = false;
					iterator = list.iterator();
					while (iterator.hasNext()) {
						NameValue nameValue = (NameValue) iterator.next();
						if (videocast.getPath().equals(nameValue.getValue())) {
							found = true;
							break;
						}
					}
					if (!found) {
						videocast.setStatus(Videocast.STATUS_DELETED);
						updateVideocast(videocast);
					}
				}
			} else {
				Iterator<NameValue> iterator = list.iterator();
				while (iterator.hasNext()) {
					NameValue nameValue = (NameValue) iterator.next();
					Videocast videocast = new Videocast(nameValue.getName(),
							Videocast.STATUS_SUBSCRIBED, nameValue
							.getValue(), 0, new ArrayList<VideocastTrack>());
					createVideocast(videocast);
				}
			}
		} catch (Exception ex) {
			Tools.logException(VideocastManager.class, ex);
		}
	}
	@SuppressWarnings("unchecked")
	private static Videocast trim(Videocast videocast) {
		if (videocast != null) {
			videocast.setAuthor(Tools.trim(videocast.getAuthor(), 255));
			videocast.setCategory(Tools.trim(videocast.getCategory(), 255));
			videocast.setDescription(Tools.trim(videocast.getDescription(),
					4096));
			videocast.setExternalId(Tools.trim(videocast.getExternalId(), 255));
			videocast.setImage(Tools.trim(videocast.getImage(), 1024));
			videocast.setKeywords(Tools.trim(videocast.getKeywords(), 255));
			videocast.setLink(Tools.trim(videocast.getLink(), 1024));
			videocast.setOrigen(Tools.trim(videocast.getOrigen(), 30));
			videocast.setPath(Tools.trim(videocast.getPath(), 1024));
			videocast.setSubtitle(Tools.trim(videocast.getSubtitle(), 4096));
			videocast.setSummary(Tools.trim(videocast.getSummary(), 4096));
			videocast.setTitle(Tools.trim(videocast.getTitle(), 255));
			List<VideocastTrack> list = (List<VideocastTrack>)videocast.getTracks();
			Iterator<VideocastTrack> iterator = list.iterator();
			while (iterator.hasNext()) {
				VideocastTrack track = iterator.next();
				track.setAuthor(Tools.trim(track.getAuthor(), 255));
				track.setCategory(Tools.trim(track.getCategory(), 255));
				track.setDescription(Tools.trim(track.getDescription(), 4096));
				track.setGuid(Tools.trim(track.getGuid(), 255));
				track.setKeywords(Tools.trim(track.getGuid(), 255));
				track.setLink(Tools.trim(track.getLink(), 1024));
				track.setMimeType(Tools.trim(track.getMimeType(), 50));
				track.setSubtitle(Tools.trim(track.getSubtitle(), 255));
				track.setSummary(Tools.trim(track.getSummary(), 4096));
				track.setTitle(Tools.trim(track.getTitle(), 255));
				track.setUrl(Tools.trim(track.getUrl(), 1024));
			}
		}
		return videocast;
	}
}
