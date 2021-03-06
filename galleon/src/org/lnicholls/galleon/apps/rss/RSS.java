package org.lnicholls.galleon.apps.rss;
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
import java.awt.Color;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.lnicholls.galleon.app.AppContext;
import org.lnicholls.galleon.app.AppFactory;
import org.lnicholls.galleon.data.Users;
import org.lnicholls.galleon.database.PersistentValue;
import org.lnicholls.galleon.database.PersistentValueManager;
import org.lnicholls.galleon.server.DataConfiguration;
import org.lnicholls.galleon.server.Server;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.ReloadCallback;
import org.lnicholls.galleon.util.ReloadTask;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.widget.DefaultApplication;
import org.lnicholls.galleon.widget.DefaultMenuScreen;
import org.lnicholls.galleon.widget.DefaultOptionsScreen;
import org.lnicholls.galleon.widget.DefaultScreen;
import org.lnicholls.galleon.widget.OptionsButton;
import org.lnicholls.galleon.widget.ScrollText;
import com.tivo.hme.bananas.BButton;
import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BList;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.interfaces.IContext;
import com.tivo.hme.sdk.Resource;
import de.nava.informa.core.ChannelBuilderIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.FeedParser;
public class RSS extends DefaultApplication {
	private static Logger log = Logger.getLogger(RSS.class.getName());
	public final static String TITLE = "RSS";
	private Resource mMenuBackground;
	private Resource mInfoBackground;
	private Resource mViewerBackground;
	private Resource mFolderIcon;
	private Resource mItemIcon;
	public void init(IContext context) throws Exception {
		super.init(context);
	}
	public void initService() {
        super.initService();
		mMenuBackground = getSkinImage("menu", "background");
		mInfoBackground = getSkinImage("info", "background");
		mViewerBackground = getSkinImage("viewer", "background");
		mFolderIcon = getSkinImage("menu", "folder");
		mItemIcon = getSkinImage("menu", "item");
		RSSConfiguration rssConfiguration = (RSSConfiguration) ((RSSFactory) getFactory())
				.getAppContext().getConfiguration();
		Tracker tracker = new Tracker(rssConfiguration.getSharedFeeds(), 0);
		if (Server.getServer().getDataConfiguration().isConfigured())
			push(new RssMenuScreen(this), TRANSITION_NONE);
		else
			push(new FavoritesMenuScreen(this, tracker, true), TRANSITION_NONE);
		initialize();
	}
	public class RssMenuScreen extends DefaultMenuScreen {
		public RssMenuScreen(RSS app) {
			super(app, "RSS");
			// setFooter("Press ENTER for options");
			getBelow().setResource(mMenuBackground);
			mMenuList.add("My Favorites");
			mMenuList.add("Find by Tag");
		}
		public boolean handleAction(BView view, Object action) {
			if (action.equals("push")) {
				if (mMenuList.size() > 0) {
					load();
					final RSSConfiguration rssConfiguration = (RSSConfiguration) ((RSSFactory) getFactory())
							.getAppContext().getConfiguration();
					new Thread() {
						public void run() {
							try {
								if (mMenuList.getFocus() == 0)
								{
									Tracker tracker = new Tracker(
											rssConfiguration.getSharedFeeds(),
											0);
									getBApp().push(
											new FavoritesMenuScreen(
													(RSS) getBApp(), tracker),
											TRANSITION_NONE);
								}
								else
									getBApp()
											.push(
													new TagsMenuScreen(
															(RSS) getBApp()),
													TRANSITION_NONE);
								getBApp().flush();
							} catch (Exception ex) {
								Tools.logException(RSS.class, ex);
							}
						}
					}.start();
					return true;
				}
			}
			return super.handleAction(view, action);
		}
		protected void createRow(BView parent, int index) {
			try
			{
				BView icon = new BView(parent, 9, 2, 32, 32);
				icon.setResource(mFolderIcon);
				String title = (String) mMenuList.get(index);
				BText name = new BText(parent, 50, 4, parent.getWidth() - 40,
						parent.getHeight() - 4);
				name.setShadow(true);
				name.setFlags(RSRC_HALIGN_LEFT);
				name.setValue(Tools.trim(Tools.clean(title), 40));
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_SELECT:
				postEvent(new BEvent.Action(this, "push"));
				return true;
			}
			return super.handleKeyPress(code, rawcode);
		}
	}
	public class OptionsScreen extends DefaultOptionsScreen {
		public OptionsScreen(DefaultApplication app) {
			super(app);
			getBelow().setResource(mInfoBackground);
			RSSConfiguration rssConfiguration = (RSSConfiguration) ((RSSFactory) getFactory())
					.getAppContext().getConfiguration();
			int start = TOP;
			int width = 280;
			int increment = 37;
			int height = 25;
			BText text = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH,
					30);
			text.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP
					| RSRC_VALIGN_CENTER);
			text.setFont("default-24-bold.font");
			text.setShadow(true);
			text.setValue("Sort");
			NameValue[] nameValues = new NameValue[] {
					new NameValue("Yes", "true"), new NameValue("No", "false") };
			mSortedButton = new OptionsButton(getNormal(), BORDER_LEFT
					+ BODY_WIDTH - width, start, width, height,
			true, nameValues, String.valueOf(rssConfiguration.isSorted()));
			setFocusDefault(mSortedButton);
		}
		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			getBelow().setResource(mInfoBackground);
			return super.handleEnter(arg, isReturn);
		}
		public boolean handleExit() {
			try {
				DefaultApplication application = (DefaultApplication) getApp();
				if (!application.isDemoMode())
				{
					RSSConfiguration rssConfiguration = (RSSConfiguration) ((RSSFactory) getFactory())
							.getAppContext().getConfiguration();
					rssConfiguration.setSorted(Boolean.valueOf(
							mSortedButton.getValue()).booleanValue());
					Server.getServer().updateApp(
							((RSSFactory) getFactory()).getAppContext());
				}
			} catch (Exception ex) {
				Tools
						.logException(RSS.class, ex,
								"Could not configure rss app");
			}
			return super.handleExit();
		}
		private OptionsButton mSortedButton;
	}
	public class FavoritesMenuScreen extends DefaultMenuScreen {
		public FavoritesMenuScreen(RSS app, Tracker tracker) {
			this(app, tracker, false);
		}
		public FavoritesMenuScreen(RSS app, Tracker tracker, boolean first) {
			super(app, "RSS");
			mTracker = tracker;
			mFirst = first;
			setFooter("Press ENTER for options");
			getBelow().setResource(mMenuBackground);
			RSSConfiguration rssConfiguration = (RSSConfiguration) ((RSSFactory) getFactory())
					.getAppContext().getConfiguration();
			/*
			 * 
			 * RSSConfiguration rssConfiguration = (RSSConfiguration)
			 * ((RSSFactory) getFactory())
			 * 
			 * .getAppContext().getConfiguration();
			 * 
			 * List feeds = rssConfiguration.getSharedFeeds();
			 * 
			 * RSSConfiguration.SharedFeed[] feedArray =
			 * (RSSConfiguration.SharedFeed[]) feeds.toArray(new
			 * RSSConfiguration.SharedFeed[0]);
			 * 
			 * Arrays.sort(feedArray, new Comparator() {
			 * 
			 * public int compare(Object o1, Object o2) {
			 * 
			 * RSSConfiguration.SharedFeed nameValue1 =
			 * (RSSConfiguration.SharedFeed) o1;
			 * 
			 * RSSConfiguration.SharedFeed nameValue2 =
			 * (RSSConfiguration.SharedFeed) o2;
			 * 
			 * 
			 * 
			 * return -nameValue1.getName().compareTo(nameValue2.getName());
			 *  }
			 * 
			 * });
			 * 
			 * 
			 * 
			 * for (int i = 0; i < feedArray.length; i++) {
			 * 
			 * RSSConfiguration.SharedFeed nameValue =
			 * (RSSConfiguration.SharedFeed) feedArray[i];
			 * 
			 * List stories = (List) ((RSSFactory)
			 * getFactory()).mChannels.get(nameValue.getValue());
			 * 
			 * if (stories != null)
			 * 
			 * mMenuList.add(nameValue);
			 *  }
			 * 
			 */
			createMenu();
		}
		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			if (isReturn)
			{
				createMenu();
			}
			return super.handleEnter(arg, isReturn);
		}
		private void createMenu()
		{
			RSSConfiguration rssConfiguration = (RSSConfiguration) ((RSSFactory) getFactory())
					.getAppContext().getConfiguration();
			mMenuList.clear();
			ArrayList list = new ArrayList();
			Iterator iterator = mTracker.getList().iterator();
			while (iterator.hasNext()) {
				RSSConfiguration.SharedFeed nameValue = (RSSConfiguration.SharedFeed) iterator
						.next();
				List stories = (List) ((RSSFactory) getFactory()).mChannels
						.get(nameValue.getValue());
				if (stories != null)
					list.add(nameValue);
			}
			RSSConfiguration.SharedFeed feeds[] = new RSSConfiguration.SharedFeed[0];
			feeds = (RSSConfiguration.SharedFeed[]) list.toArray(feeds);
			if (rssConfiguration.isSorted())
			{
				Arrays.sort(feeds, new Comparator() {
					public int compare(Object o1, Object o2) {
						RSSConfiguration.SharedFeed feed1 = (RSSConfiguration.SharedFeed) o1;
						RSSConfiguration.SharedFeed feed2 = (RSSConfiguration.SharedFeed) o2;
						return feed1.getName().compareTo(feed2.getName());
					}
				});
			}
			for (int i = 0; i < feeds.length; i++) {
				mMenuList.add(feeds[i]);
			}
		}
		public boolean handleAction(BView view, Object action) {
			if (action.equals("push")) {
				if (mMenuList.size() > 0) {
					load();
					RSSConfiguration.SharedFeed nameValue = (RSSConfiguration.SharedFeed) mMenuList
							.get(mMenuList.getFocus());
					List stories = (List) ((RSSFactory) getFactory()).mChannels
							.get(nameValue.getValue());
					RSSFeedMenuScreen rssFeedMenuScreen = new RSSFeedMenuScreen(
							(RSS) getBApp(), nameValue, stories);
					getBApp().push(rssFeedMenuScreen, TRANSITION_LEFT);
					getBApp().flush();
					return true;
				}
			}
			return super.handleAction(view, action);
		}
		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_PLAY:
			case KEY_SELECT:
				postEvent(new BEvent.Action(this, "play"));
				return true;
			case KEY_LEFT:
				if (!mFirst) {
					postEvent(new BEvent.Action(this, "pop"));
					return true;
				}
				break;
			case KEY_ENTER:
				getBApp().push(new OptionsScreen((RSS) getBApp()),
						TRANSITION_LEFT);
			case KEY_NUM1:
			case KEY_THUMBSUP:
				try
				{
					RSSConfiguration.SharedFeed value = (RSSConfiguration.SharedFeed) mMenuList
							.get(mMenuList.getFocus());
					RSSConfiguration rssConfiguration = (RSSConfiguration) ((RSSFactory) getFactory())
							.getAppContext().getConfiguration();
					List list = rssConfiguration.getSharedFeeds();
					boolean duplicate = false;
					Iterator iterator = list.iterator();
					while (iterator.hasNext())
					{
						RSSConfiguration.SharedFeed sharedFeed = (RSSConfiguration.SharedFeed) iterator
								.next();
						if (sharedFeed.getValue().equals(value.getValue()))
						{
							duplicate = true;
							break;
						}
					}
					if (!duplicate)
					{
						getApp().play("thumbsup.snd");
						getApp().flush();
						mMenuList.flash();
						list.add(value);
						Server.getServer().updateApp(
								((RSSFactory) getFactory()).getAppContext());
					}
				}
				catch (Exception ex) {
					Tools.logException(RSS.class, ex);
				}
				break;
			}
			return super.handleKeyPress(code, rawcode);
		}
		protected void createRow(BView parent, int index) {
			BView icon = new BView(parent, 10, 3, 30, 30);
			icon.setResource(mFolderIcon);
			RSSConfiguration.SharedFeed nameValue = (RSSConfiguration.SharedFeed) mMenuList
					.get(index);
			BText name = new BText(parent, 50, 4, parent.getWidth() - 40,
					parent.getHeight() - 4);
			name.setShadow(true);
			name.setFlags(RSRC_HALIGN_LEFT);
			name.setValue(Tools.trim(nameValue.getName(), 40));
		}
		private Tracker mTracker;
		private boolean mFirst;
	}
	public class RSSFeedMenuScreen extends DefaultMenuScreen {
		public RSSFeedMenuScreen(RSS app,
				RSSConfiguration.SharedFeed nameValue, List list) {
			super(app, null);
			mList = list;
			getBelow().setResource(mMenuBackground);
			Image image = image = Tools.retrieveCachedImage(nameValue
					.getValue());
			if (image != null) {
				mImage = new BView(getBelow(), SAFE_TITLE_H
						+ (this.BODY_WIDTH - image.getWidth(null)) / 2,
				SAFE_TITLE_V, image.getWidth(null), image.getHeight(null));
				mImage.setResource(createImage(image));
			} else
				setTitle(nameValue.getName());
			for (int i = 0; i < list.size(); i++) {
				ItemIF item = (ItemIF) list.get(i);
				mMenuList.add(item);
			}
		}
		public boolean handleAction(BView view, Object action) {
			if (action.equals("push")) {
				if (mMenuList.size() > 0) {
					load();
					ItemIF item = (ItemIF) mMenuList.get(mMenuList.getFocus());
					Tracker tracker = new Tracker(mList, mMenuList.getFocus());
					RSSScreen rssScreen = new RSSScreen((RSS) getBApp(),
							tracker);
					getBApp().push(rssScreen, TRANSITION_LEFT);
					getBApp().flush();
					return true;
				}
			}
			return super.handleAction(view, action);
		}
		protected void createRow(BView parent, int index) {
			BView icon = new BView(parent, 10, 3, 30, 30);
			icon.setResource(mItemIcon);
			ItemIF item = (ItemIF) mMenuList.get(index);
			BText name = new BText(parent, 50, 4, parent.getWidth() - 40,
					parent.getHeight() - 4);
			name.setShadow(true);
			name.setFlags(RSRC_HALIGN_LEFT);
			name.setValue(Tools.trim(Tools.cleanHTML(item.getTitle()), 40));
		}
		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_LEFT:
				postEvent(new BEvent.Action(this, "pop"));
				return true;
			}
			return super.handleKeyPress(code, rawcode);
		}
		private BView mImage;
		private List mList;
	}
	public class RSSScreen extends DefaultScreen {
		public RSSScreen(RSS app, Tracker tracker) {
			super(app, true);
			mTracker = tracker;
			getBelow().setResource(mInfoBackground);
			int start = TOP;
			mScrollText = new ScrollText(getNormal(), BORDER_LEFT, BORDER_TOP,
					BODY_WIDTH - 25, getHeight() - 2
					* SAFE_TITLE_V - 175, "");
			/*
			 * 
			 * mList = new DefaultOptionList(this.getNormal(), SAFE_TITLE_H,
			 * (getHeight() - SAFE_TITLE_V) - 40, (width -
			 * 
			 * (SAFE_TITLE_H * 2)) / 2, 90, 35); mList.add("Back to menu");
			 * setFocusDefault(mList);
			 * 
			 */
			BButton button = new BButton(getNormal(), SAFE_TITLE_H + 10,
					(getHeight() - SAFE_TITLE_V) - 40, (int) Math
					.round((getWidth() - (SAFE_TITLE_H * 2)) / 2.5), 35);
			button.setResource(createText("default-24.font", Color.white,
					"Return to menu"));
			button.setBarAndArrows(BAR_HANG, BAR_DEFAULT, "pop", null, null,
					null, true);
			setFocus(button);
		}
		public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
			getBelow().setResource(mInfoBackground);
			updateView();
			return super.handleEnter(arg, isReturn);
		}
		private void updateView() {
			ItemIF item = (ItemIF) mTracker.getList().get(mTracker.getPos());
			setSmallTitle(Tools.cleanHTML(item.getTitle()));
			mScrollText.setText(Tools.cleanHTML(item.getDescription()));
		}
		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_SELECT:
				postEvent(new BEvent.Action(this, "pop"));
				return true;
			case KEY_CHANNELUP:
				getBApp().play("pageup.snd");
				getBApp().flush();
				getPrevPos();
				updateView();
				return true;
			case KEY_CHANNELDOWN:
				getBApp().play("pagedown.snd");
				getBApp().flush();
				getNextPos();
				updateView();
				return true;
			case KEY_UP:
			case KEY_DOWN:
				return mScrollText.handleKeyPress(code, rawcode);
			}
			return super.handleKeyPress(code, rawcode);
		}
		public void getNextPos() {
			if (mTracker != null) {
				int pos = mTracker.getNextPos();
			}
		}
		public void getPrevPos() {
			if (mTracker != null) {
				int pos = mTracker.getPrevPos();
			}
		}
		private BList mList;
		private ScrollText mScrollText;
		private Tracker mTracker;
	}
	public class TagsMenuScreen extends DefaultMenuScreen {
		public TagsMenuScreen(RSS app) {
			super(app, "RSS Tags");
			getBelow().setResource(mMenuBackground);
			DataConfiguration dataConfiguration = Server.getServer()
					.getDataConfiguration();
			try
			{
				String result = Users.retrieveRssTags(dataConfiguration);
				if (result != null && result.length() > 0)
				{
					SAXReader saxReader = new SAXReader();
					log.debug("Tags: " + result);
					StringReader stringReader = new StringReader(result);
					Document document = saxReader.read(stringReader);
					// Document document = saxReader.read(new
					// File("d:/galleon/location.xml"));
					Element root = document.getRootElement();
					Element tags = root.element("tags");
					if (tags != null)
					{
						for (Iterator i = tags.elementIterator("tag"); i
								.hasNext();) {
							Element element = (Element) i.next();
							mMenuList.add(Tools.getAttribute(element, "name"));
						}
					}
				}
			} catch (Exception ex) {
				Tools.logException(RSS.class, ex);
			}
		}
		public boolean handleAction(BView view, Object action) {
			if (action.equals("push")) {
				if (mMenuList.size() > 0) {
					load();
					final RSSConfiguration rssConfiguration = (RSSConfiguration) ((RSSFactory) getFactory())
							.getAppContext().getConfiguration();
					new Thread() {
						public void run() {
							try {
								String tag = (String) mMenuList.get(mMenuList
										.getFocus());
								DataConfiguration dataConfiguration = Server
										.getServer().getDataConfiguration();
								ArrayList list = new ArrayList();
								String result = Users.retrieveRssFromTag(
										dataConfiguration, tag);
								if (result != null && result.length() > 0)
								{
									SAXReader saxReader = new SAXReader();
									log.debug("Tags: " + result);
									StringReader stringReader = new StringReader(
											result);
									Document document = saxReader
											.read(stringReader);
									// Document document = saxReader.read(new
									// File("d:/galleon/location.xml"));
									Element root = document.getRootElement();
									Element rss = root.element("rss");
									if (rss != null)
									{
										for (Iterator i = rss
												.elementIterator("feed"); i
												.hasNext();) {
											Element element = (Element) i
													.next();
											RSSConfiguration.SharedFeed sharedFeed = new RSSConfiguration.SharedFeed(
													Tools.getAttribute(element,
															"name"),
													Tools.getAttribute(element,
															"url"),
													Tools.getAttribute(element,
															"description"),
													tag,
													RSSConfiguration.SharedFeed.PRIVATE);
											list.add(sharedFeed);
										}
									}
								}
								Tracker tracker = new Tracker(list, 0);
								getBApp().push(
										new FavoritesMenuScreen(
												(RSS) getBApp(), tracker),
										TRANSITION_NONE);
								getBApp().flush();
							} catch (Exception ex) {
								Tools.logException(RSS.class, ex);
							}
						}
					}.start();
					return true;
				}
			}
			return super.handleAction(view, action);
		}
		protected void createRow(BView parent, int index) {
			try
			{
				BView icon = new BView(parent, 9, 2, 32, 32);
				icon.setResource(mFolderIcon);
				String title = (String) mMenuList.get(index);
				BText name = new BText(parent, 50, 4, parent.getWidth() - 40,
						parent.getHeight() - 4);
				name.setShadow(true);
				name.setFlags(RSRC_HALIGN_LEFT);
				name.setValue(Tools.trim(Tools.clean(title), 40));
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_SELECT:
				postEvent(new BEvent.Action(this, "push"));
				return true;
			case KEY_LEFT:
				postEvent(new BEvent.Action(this, "pop"));
				return true;
			}
			return super.handleKeyPress(code, rawcode);
		}
	}
	public class TagMenuScreen extends DefaultMenuScreen {
		public TagMenuScreen(RSS app, String tag) {
			super(app, "RSS Tag");
			setFooter("Press ENTER for options");
			getBelow().setResource(mMenuBackground);
			DataConfiguration dataConfiguration = Server.getServer()
					.getDataConfiguration();
			try
			{
				String result = Users
						.retrieveRssFromTag(dataConfiguration, tag);
				if (result != null && result.length() > 0)
				{
					SAXReader saxReader = new SAXReader();
					log.debug("Tags: " + result);
					StringReader stringReader = new StringReader(result);
					Document document = saxReader.read(stringReader);
					// Document document = saxReader.read(new
					// File("d:/galleon/location.xml"));
					Element root = document.getRootElement();
					Element internet = root.element("internet");
					if (internet != null)
					{
						for (Iterator i = internet.elementIterator("url"); i
								.hasNext();) {
							Element element = (Element) i.next();
							mMenuList.add(Tools.getAttribute(element, "name"));
						}
					}
				}
			} catch (Exception ex) {
				Tools.logException(RSS.class, ex);
			}
		}
		public boolean handleAction(BView view, Object action) {
			if (action.equals("push")) {
				if (mMenuList.size() > 0) {
					load();
					final RSSConfiguration rssConfiguration = (RSSConfiguration) ((RSSFactory) getFactory())
							.getAppContext().getConfiguration();
					new Thread() {
						public void run() {
							try {
								/*
								 * 
								 * if (mMenuList.getFocus()==0)
								 *  {
								 * 
								 * Tracker tracker = new
								 * Tracker(internetConfiguration.getSharedUrls(),
								 * 0);
								 * 
								 * getBApp().push(new
								 * FavoritesMenuScreen((Internet)getBApp(),
								 * tracker), TRANSITION_NONE);
								 *  }
								 * 
								 * else
								 * 
								 * getBApp().push(new FavoritesMenuScreen(this,
								 * tracker), TRANSITION_NONE);
								 * 
								 * getBApp().flush();
								 * 
								 */
							} catch (Exception ex) {
								Tools.logException(RSS.class, ex);
							}
						}
					}.start();
					return true;
				}
			}
			return super.handleAction(view, action);
		}
		protected void createRow(BView parent, int index) {
			try
			{
				BView icon = new BView(parent, 9, 2, 32, 32);
				icon.setResource(mFolderIcon);
				String title = (String) mMenuList.get(index);
				BText name = new BText(parent, 50, 4, parent.getWidth() - 40,
						parent.getHeight() - 4);
				name.setShadow(true);
				name.setFlags(RSRC_HALIGN_LEFT);
				name.setValue(Tools.trim(Tools.clean(title), 40));
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		public boolean handleKeyPress(int code, long rawcode) {
			switch (code) {
			case KEY_SELECT:
				postEvent(new BEvent.Action(this, "push"));
				return true;
			case KEY_LEFT:
				postEvent(new BEvent.Action(this, "pop"));
				return true;
			}
			return super.handleKeyPress(code, rawcode);
		}
	}
	public static class RSSFactory extends AppFactory {
		public void updateAppContext(AppContext appContext) {
			super.updateAppContext(appContext);
			updateChannels();
			updateData();
		}
		private void updateChannels() {
			final RSSConfiguration rssConfiguration = (RSSConfiguration) getAppContext()
					.getConfiguration();
			new Thread() {
				public void run() {
					Iterator iterator = rssConfiguration.getSharedFeeds()
							.iterator();
					while (iterator.hasNext()) {
						RSSConfiguration.SharedFeed nameValue = (RSSConfiguration.SharedFeed) iterator
								.next();
						List stories = (List) mChannels.get(nameValue
								.getValue());
						if (stories == null) {
							stories = new ArrayList();
							mChannels.put(nameValue.getValue(), stories);
						}
						try {
							PersistentValue persistentValue = PersistentValueManager
							.loadPersistentValue(RSSFactory.this.getClass()
									.getName()
									+ "."
									+ nameValue.getValue() + "." + "content");
							String content = persistentValue == null ? null
									: persistentValue.getValue();
							if (PersistentValueManager.isAged(persistentValue)) {
								String page = Tools.getPage(new URL(nameValue
										.getValue()));
								if (page != null && page.length() > 0)
									content = page;
							}
							if (content != null) {
								ChannelBuilderIF builder = new ChannelBuilder();
								ChannelIF channel = FeedParser.parse(builder,
										new ByteArrayInputStream((content
										.getBytes("UTF-8"))));
								if (channel.getItems().size() > 0) {
									stories.clear();
									int count = 0;
									Iterator chs = channel.getItems()
											.iterator();
									while (chs.hasNext()) {
										ItemIF item = (ItemIF) chs.next();
										stories.add(item);
									}
									if (channel.getImage() != null
											&& Tools
													.retrieveCachedImage(nameValue
															.getValue()) == null) {
										Tools.cacheImage(channel.getImage()
												.getLocation(), nameValue
												.getValue());
									}
								}
								if (PersistentValueManager
										.isAged(persistentValue)) {
									int ttl = channel.getTtl();
									if (ttl < 10)
										ttl = 60;
									else
										ttl = 60 * 6;
									PersistentValueManager.savePersistentValue(
											RSSFactory.this.getClass()
													.getName()
													+ "."
													+ nameValue.getValue()
													+ "." + "content", content,
											ttl * 60);
								}
							}
						} catch (Exception ex) {
							Tools.logException(RSS.class, ex,
									"Could not reload " + nameValue.getValue());
						}
					}
				}
			}.start();
		}
		public void initialize() {
			final RSSConfiguration rssConfiguration = (RSSConfiguration) getAppContext()
					.getConfiguration();
			Server.getServer().scheduleShortTerm(
					new ReloadTask(new ReloadCallback() {
						public void reload() {
							try {
								updateChannels();
							} catch (Exception ex) {
								log.error("Could not download channels", ex);
							}
						}
					}), 5);
			Server.getServer().scheduleData(
					new ReloadTask(new ReloadCallback() {
						public void reload() {
							try {
								updateData();
							} catch (Exception ex) {
								log.error("Could not update rss dats", ex);
							}
						}
					}), 60 * 24);
		}
		private void updateData() {
			RSSConfiguration rssConfiguration = (RSSConfiguration) getAppContext()
					.getConfiguration();
			try {
				StringBuffer buffer = new StringBuffer();
				synchronized (buffer)
				{
					buffer
							.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
					buffer.append("<data>\n");
					buffer.append("<rss>\n");
					Iterator iterator = rssConfiguration.getSharedFeeds()
							.iterator();
					while (iterator.hasNext())
					{
						RSSConfiguration.SharedFeed nameValue = (RSSConfiguration.SharedFeed) iterator
								.next();
						if (nameValue.getPrivacy().equals(
								RSSConfiguration.SharedFeed.PUBLIC))
						{
							buffer.append("<feed");
							buffer.append(" name=\""
									+ Tools.escapeXMLChars(nameValue.getName())
									+ "\"");
							buffer.append(" url=\""
									+ Tools
											.escapeXMLChars(nameValue
													.getValue()) + "\"");
							buffer.append(" description=\""
									+ Tools.escapeXMLChars(nameValue
											.getDescription()) + "\"");
							buffer.append(" tags=\""
									+ Tools.escapeXMLChars(nameValue.getTags())
									+ "\"");
							if (nameValue.getPrivacy().equals(
									RSSConfiguration.SharedFeed.PRIVATE))
								buffer.append(" privacy=\"0\"");
							else
							if (nameValue.getPrivacy().equals(
									RSSConfiguration.SharedFeed.PUBLIC))
								buffer.append(" privacy=\"1\"");
							else
							if (nameValue.getPrivacy().equals(
									RSSConfiguration.SharedFeed.FRIENDS))
								buffer.append(" privacy=\"2\"");
							else
								buffer.append(" privacy=\"0\"");
							buffer.append(" />\n");
						}
					}
					buffer.append("</rss>\n");
					buffer.append("</data>\n");
				}
				Users.updateRss(Server.getServer().getDataConfiguration(),
						buffer.toString());
			} catch (Exception ex) {
				log.error("Could not update rss data", ex);
			}
		}
		private static Hashtable mChannels = new Hashtable();
	}
}
