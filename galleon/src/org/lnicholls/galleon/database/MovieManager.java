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
public class MovieManager {
//	private static Logger log = Logger.getLogger(MovieManager.class.getName());
	public static interface Callback {
		public void visit(Session session, Movie movide);
	}
	public static Movie retrieveMovie(Movie movie) throws HibernateException {
		return retrieveMovie(new Integer(movie.getId()));
	}
	public static Movie retrieveMovie(int id) throws HibernateException {
		return retrieveMovie(new Integer(id));
	}
	public static Movie retrieveMovie(Integer id) throws HibernateException {
		Movie result = null;
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			result = (Movie) session.load(Movie.class, id);
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
	public static Movie createMovie(Movie movie) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(trim(movie));
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw he;
		} finally {
			HibernateUtil.closeSession();
		}
		return movie;
	}
	public static void updateMovie(Movie Movie) throws HibernateException {
		if (Movie.getId()!=0)
		{
			Session session = HibernateUtil.openSession();
	
			Transaction tx = null;
	
			try {
	
				tx = session.beginTransaction();
	
				session.update(trim(Movie));
	
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
	public static void deleteMovie(Movie Movie) throws HibernateException {
		if (Movie.getId()!=0)
		{
			Session session = HibernateUtil.openSession();
	
			Transaction tx = null;
	
			try {
	
				tx = session.beginTransaction();
	
				session.delete(Movie);
	
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
	public static List<Movie> listAll() throws HibernateException {
		List<Movie> list = new ArrayList<Movie>();
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			list = session.createQuery(
					"from org.lnicholls.galleon.database.Movie").list();
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
	public static List<Movie> listBetween(int start, int end)
			throws HibernateException {
		List<Movie> list = new ArrayList<Movie>();
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session
					.createQuery("from org.lnicholls.galleon.database.Movie");
			ScrollableResults items = query.scroll();
			int counter = start;
			if (items.first()) {
				items.scroll(start);
				while (items.next() && (counter < end)) {
					Movie Movie = (Movie) items.get(0);
					list.add(Movie);
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
					.createQuery("from org.lnicholls.galleon.database.Movie");
			ScrollableResults items = q.scroll();
			if (items.first()) {
				items.beforeFirst();
				while (items.next())
				{
					Movie Movie = (Movie) items.get(0);
					callback.visit(session, Movie);
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
	public static List<Movie> findByPath(String path) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<Movie> list = session
					.createQuery(
							"from org.lnicholls.galleon.database.Movie as Movie where Movie.path=?")
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
	public static List<Movie> findByTitle(String title) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<Movie> list = session
					.createQuery(
							"from org.lnicholls.galleon.database.Movie as Movie where Movie.title=?")
					.setString(0, title).list();
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
	public static List<Movie> findByIMDB(String imdb) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<Movie> list = session
					.createQuery(
							"from org.lnicholls.galleon.database.Movie as Movie where Movie.imdb=?")
					.setString(0, imdb).list();
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
	private static Movie trim(Movie movie)
	{
		if (movie!=null)
		{
			movie.setActors(Tools.trim(movie.getActors(), 4096));
	
			movie.setCredits(Tools.trim(movie.getCredits(), 4096));
	
			movie.setDirector(Tools.trim(movie.getDirector(), 255));
	
			movie.setExternalId(Tools.trim(movie.getExternalId(), 255));
	
			movie.setGenre(Tools.trim(movie.getGenre(), 255));
	
			movie.setIMDB(Tools.trim(movie.getIMDB(), 255));
	
			movie.setMimeType(Tools.trim(movie.getMimeType(), 50));
	
			movie.setOrigen(Tools.trim(movie.getOrigen(), 30));
	
			movie.setPath(Tools.trim(movie.getPath(), 1024));
	
			movie.setPlot(Tools.trim(movie.getPlot(), 4096));
	
			movie.setPlotOutline(Tools.trim(movie.getPlotOutline(), 4096));
	
			movie.setProducer(Tools.trim(movie.getProducer(), 255));
	
			movie.setRated(Tools.trim(movie.getRated(), 255));
	
			movie.setRatedReason(Tools.trim(movie.getRatedReason(), 255));
	
			movie.setTagline(Tools.trim(movie.getTagline(), 255));
	
			movie.setThumbUrl(Tools.trim(movie.getThumbUrl(), 1024));
	
			movie.setTitle(Tools.trim(movie.getTitle(), 255));
	
			movie.setUrl(Tools.trim(movie.getUrl(), 1024));
		}
		return movie;
	}
}
