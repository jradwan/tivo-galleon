package org.lnicholls.galleon.apps.shoutcast;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.lnicholls.galleon.app.AppConfiguration;
//import org.lnicholls.galleon.util.NameValue;
@SuppressWarnings("serial")
public class ShoutcastConfiguration implements AppConfiguration {
	public static int DEFAULT_LIMIT = 40;
	public String getName() {
		return mName;
	}
	public void setName(String value) {
		if (mName != null && !mName.equals(value))
			mModified = true;
		mName = value;
	}
	public void setGenres(List<String> value) {
		mLimitedGenres.clear();
		Iterator<String> iterator = value.iterator();
		while (iterator.hasNext()) {
			String genre = iterator.next();
			LimitedGenre limitedGenre = new LimitedGenre(genre, DEFAULT_LIMIT);
			mLimitedGenres.add(limitedGenre);
		}
	}
	public List<String> getGenres() {
		return null;
	}
	public void addGenre(String value) {
		LimitedGenre limitedGenre = new LimitedGenre(value, DEFAULT_LIMIT);
		mLimitedGenres.add(limitedGenre);
	}
	public List<LimitedGenre> getLimitedGenres() {
		return mLimitedGenres;
	}
	public void setLimitedGenres(List<LimitedGenre> value) {
		mLimitedGenres = value;
	}
	public void addLimitedGenre(LimitedGenre value) {
		mLimitedGenres.add(value);
	}
	public void setModified(boolean value) {
		mModified = value;
	}
	public boolean isModified() {
		return mModified;
	}
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	public boolean isShared() {
		return mShared;
	}
	public void setShared(boolean value) {
		mShared = value;
	}
	@SuppressWarnings("serial")
	public static class LimitedGenre implements Serializable {
		public LimitedGenre() {
		}
		public LimitedGenre(String genre, int limit) {
			setGenre(genre);
			setLimit(limit);
		}
		public String getGenre() {
			return mGenre;
		}
		public void setGenre(String value) {
			mGenre = value;
		}
		public int getLimit() {
			return mLimit;
		}
		public void setLimit(int value) {
			mLimit = value;
		}
		private String mGenre;
		private int mLimit;
	}
	private boolean mShared;
	private String mName;
	private boolean mModified;
	private List<LimitedGenre> mLimitedGenres = new ArrayList<LimitedGenre>();
}
