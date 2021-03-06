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
import java.util.Date;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.classic.Session;
import org.hibernate.Transaction;
import org.apache.log4j.Logger;
import org.lnicholls.galleon.util.Tools;
public class PersistentValueManager {
	private static Logger log = Logger.getLogger(PersistentValueManager.class
			.getName());
	public static interface Callback {
		public void visit(Session session, PersistentValue persistentValue);
	}
	public static PersistentValue retrievePersistentValue(
			PersistentValue persistentValue) throws HibernateException {
		return retrievePersistentValue(new Integer(persistentValue.getId()));
	}
	public static PersistentValue retrievePersistentValue(int id) throws HibernateException {
		return retrievePersistentValue(new Integer(id));
	}
	public static PersistentValue retrievePersistentValue(Integer id)
			throws HibernateException {
		PersistentValue result = null;
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			result = (PersistentValue) session.load(PersistentValue.class, id);
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
	public static PersistentValue createPersistentValue(
			PersistentValue persistentValue) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(trim(persistentValue));
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw he;
		} finally {
			HibernateUtil.closeSession();
		}
		return persistentValue;
	}
	public static void updatePersistentValue(PersistentValue persistentValue)
			throws HibernateException {
		if (persistentValue.getId()!=0)
		{
			Session session = HibernateUtil.openSession();
	
			Transaction tx = null;
	
			try {
	
				tx = session.beginTransaction();
	
				session.update(trim(persistentValue));
	
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
	public static void deletePersistentValue(PersistentValue persistentValue)
			throws HibernateException {
		if (persistentValue.getId()!=0)
		{
			Session session = HibernateUtil.openSession();
	
			Transaction tx = null;
	
			try {
	
				tx = session.beginTransaction();
	
				session.delete(persistentValue);
	
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
	public static List<PersistentValue> listAll() throws HibernateException {
		List<PersistentValue> list = new ArrayList<PersistentValue>();
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			list = session.createQuery(
					"from org.lnicholls.galleon.database.PersistentValue")
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
	public static List<PersistentValue> listBetween(int start, int end)
			throws HibernateException {
		List<PersistentValue> list = new ArrayList<PersistentValue>();
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session
					.createQuery("from org.lnicholls.galleon.database.PersistentValue");
			ScrollableResults items = query.scroll();
			int counter = start;
			if (items.first()) {
				items.scroll(start);
				while (items.next() && (counter < end)) {
					PersistentValue persistentValue = (PersistentValue) items
							.get(0);
					list.add(persistentValue);
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
					.createQuery("from org.lnicholls.galleon.database.PersistentValue");
			ScrollableResults items = q.scroll();
			if (items.first()) {
				items.beforeFirst();
				while (items.next()) {
					PersistentValue persistentValue = (PersistentValue) items
							.get(0);
					callback.visit(session, persistentValue);
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
	public static String findValueByName(String name) throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<PersistentValue> list = session
					.createQuery(
							"from org.lnicholls.galleon.database.PersistentValue as PersistentValue where PersistentValue.name=?")
					.setString(0, name).list();
			tx.commit();
			if (list.size() > 0) {
				PersistentValue persistentValue = (PersistentValue) list.get(0);
				return persistentValue.getValue();
			}
			return null;
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw he;
		} finally {
			HibernateUtil.closeSession();
		}
	}
	@SuppressWarnings("unchecked")
	public static PersistentValue findByName(String name)
			throws HibernateException {
		Session session = HibernateUtil.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<PersistentValue> list = session
					.createQuery(
							"from org.lnicholls.galleon.database.PersistentValue as PersistentValue where PersistentValue.name=?")
					.setString(0, name).list();
			tx.commit();
			if (list.size() > 0) {
				return (PersistentValue) list.get(0);
			}
			return null;
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw he;
		} finally {
			HibernateUtil.closeSession();
		}
	}
	public static void savePersistentValue(String name, String value) {
		savePersistentValue(name, value, new Date(), 0);
	}
	public static void savePersistentValue(String name, String value, int ttl) {
		savePersistentValue(name, value, new Date(), ttl);
	}
	public static void savePersistentValue(String name, String value,
			Date date, int ttl) {
		try {
			PersistentValue existingPersistentValue = PersistentValueManager
					.findByName(name);
			if (existingPersistentValue != null) {
				PersistentValue persistentValueCount = PersistentValueManager
						.findByName(name + ".count");
				if (persistentValueCount != null) {
					try {
						PersistentValueManager
								.deletePersistentValue(existingPersistentValue);
						int counter = Integer.parseInt(persistentValueCount
								.getValue());
						for (int i = 2; i <= counter; i++) {
							existingPersistentValue = PersistentValueManager
									.findByName(name + "." + i);
							if (existingPersistentValue != null)
								PersistentValueManager
										.deletePersistentValue(existingPersistentValue);
						}
						PersistentValueManager
								.deletePersistentValue(persistentValueCount);
					} catch (Exception ex) {
						Tools.logException(Tools.class, ex, name);
					}
				} else {
					try {
						PersistentValueManager
								.deletePersistentValue(existingPersistentValue);
					} catch (Exception ex) {
						Tools.logException(Tools.class, ex, name);
					}
				}
			}
			if (value.length() <= 32672) {
				PersistentValue persistentValue = new PersistentValue(name,
						value, date, ttl);
				PersistentValueManager.createPersistentValue(persistentValue);
			} else {
				int counter = 0;
				int start = 0;
				int end = start + 32672;
				String sub = value.substring(start, end);
				while (sub.length() > 0) {
					String postfix = "";
					if (++counter > 1)
						postfix = "." + counter;
					existingPersistentValue = new PersistentValue(name
							+ postfix, sub, date, ttl);
					PersistentValueManager
							.createPersistentValue(existingPersistentValue);
					start = end;
					end = start + 32672;
					if (end > value.length())
						end = value.length();
					sub = value.substring(start, end);
				}
				PersistentValue persistentValue = new PersistentValue(name
						+ ".count", String.valueOf(counter), date, ttl);
				PersistentValueManager.createPersistentValue(persistentValue);
			}
		} catch (HibernateException ex) {
			log.error("PersistentValue save failed", ex);
		}
	}
	public static PersistentValue loadPersistentValue(String name) {
		try {
			PersistentValue persistentValueCount = PersistentValueManager
					.findByName(name + ".count");
			if (persistentValueCount != null) {
				try {
					StringBuffer buffer = new StringBuffer();
					PersistentValue persistentValue = PersistentValueManager
							.findByName(name);
					Date when = new Date();
					int ttl = 0;
					if (persistentValue != null) {
						buffer.append(persistentValue.getValue());
						int counter = Integer.parseInt(persistentValueCount
								.getValue());
						for (int i = 2; i <= counter; i++) {
							persistentValue = PersistentValueManager
									.findByName(name + "." + i);
							if (persistentValue != null)
								buffer.append(persistentValue.getValue());
						}
						when = persistentValue.getDateModified();
						ttl = persistentValue.getTimeToLive();
					}
					return new PersistentValue(name + ".count", buffer
							.toString(), when, ttl);
				} catch (Exception ex) {
					Tools.logException(Tools.class, ex, name);
				}
			} else
				return PersistentValueManager.findByName(name);
		} catch (HibernateException ex) {
			log.error("PersistentValue load failed", ex);
		} catch (Exception ex) {
			Tools.logException(PersistentValueManager.class, ex, name);
		}
		return null;
	}
	public static boolean isAged(PersistentValue persistentValue) {
		if (persistentValue != null
				&& persistentValue.getDateModified() != null
				&& persistentValue.getTimeToLive() != 0) {
			Date ttl = new Date(persistentValue.getDateModified().getTime()
			+ persistentValue.getTimeToLive() * 1000);
			return new Date().after(ttl);
		}
		return true;
	}
	private static PersistentValue trim(PersistentValue persistentValue) {
		if (persistentValue!=null)
		{
			persistentValue.setName(Tools.trim(persistentValue.getName(), 256));
	
			persistentValue.setValue(Tools.trim(persistentValue.getValue(), 32672));
		}
		return persistentValue;
	}
}
