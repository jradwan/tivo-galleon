package org.lnicholls.galleon.apps.weather;

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
import java.awt.Font;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.app.AppContext;
import org.lnicholls.galleon.app.AppFactory;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.widget.DefaultScreen;
import org.lnicholls.galleon.widget.ScrollText;

import com.tivo.hme.bananas.BApplication;
import com.tivo.hme.bananas.BEvent;
import com.tivo.hme.bananas.BHighlight;
import com.tivo.hme.bananas.BHighlights;
import com.tivo.hme.bananas.BList;
import com.tivo.hme.bananas.BScreen;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.http.server.HttpRequest;
import com.tivo.hme.sdk.IHmeProtocol;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.util.ArgumentList;

public class Weather extends BApplication {

    private static Logger log = Logger.getLogger(Weather.class.getName());

    public final static String TITLE = "Weather";

    private int mCurrent = 0;

    private static WeatherScreen[] screens = new WeatherScreen[5];

    private Resource mBackground;

    private Resource mAlertIcon;

    private Resource mIcon;

    protected void init(Context context) {
        super.init(context);

        mBackground = getResource("background.jpg");

        mAlertIcon = getResource("alerticon.png");

        mIcon = getResource("icon.png");

        //push(new WeatherMenuScreen(this), TRANSITION_NONE);

        WeatherData weatherData = ((WeatherFactory) context.factory).getWeatherData();

        screens[0] = new CurrentConditionsScreen(this, weatherData);
        screens[1] = new ForecastScreen(this, weatherData);
        screens[2] = new LocalRadarScreen(this, weatherData);
        screens[3] = new NationalRadarScreen(this, weatherData);
        screens[4] = new AlertsScreen(this, weatherData);

        push(screens[0], TRANSITION_NONE);
    }
    
    public class WeatherScreen extends DefaultScreen {
        public WeatherScreen(Weather app) {
            super(app);

            this.setFocusable(true);

            BHighlights h = getHighlights();
            h.setWhisperingArrow(H_LEFT, A_LEFT + SAFE_TITLE_H, A_BOTTOM - SAFE_TITLE_V, "pop");
            h.setWhisperingArrow(H_RIGHT, A_RIGHT - SAFE_TITLE_H, A_BOTTOM - SAFE_TITLE_V, "right");

            setFocusDefault(this);
        }

        public boolean getHighlightIsVisible(int visible) {
            return visible == H_VIS_TRUE;
        }

        public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
            BHighlights h = getHighlights();
            BHighlight right = h.get(H_RIGHT);
            BHighlight left = h.get(H_LEFT);
            if (right != null && left != null) {
                if (mCurrent == 4)
                    right.setVisible(H_VIS_FALSE);
                else
                    right.setVisible(H_VIS_TRUE);
                if (mCurrent == 0)
                    left.setVisible(H_VIS_FALSE);
                else
                    left.setVisible(H_VIS_TRUE);
            }
            return super.handleEnter(arg, isReturn);
        }

        public boolean handleAction(BView view, Object action) {
            if (action.equals("right")) {
                if (mCurrent == 4) {
                    getBApp().play("thumbsdown.snd");
                    return true;
                }
                mCurrent = mCurrent + 1;
                push(screens[mCurrent], TRANSITION_LEFT);
                return true;
            } else if (action.equals("pop")) {
                if (mCurrent == 0) {
                    getBApp().play("pageup.snd");
                    getBApp().flush();
                    getBApp().setActive(false);
                }
                --mCurrent;
                pop();
                return true;
            }
            return super.handleAction(view, action);
        }
        
        public boolean handleKeyPress(int code, long rawcode) {
            switch (code) {
            case KEY_LEFT:
                postEvent(new BEvent.Action(this, "pop"));
                return true;
            }
            return super.handleKeyPress(code, rawcode);
        }
    }

    public class WeatherMenuScreen extends WeatherScreen {
        private TGList list;

        public WeatherMenuScreen(Weather app) {
            super(app);
            setTitle("Weather");

            list = new TGList(this.normal, SAFE_TITLE_H + 10, (height - SAFE_TITLE_V) - 290, width
                    - ((SAFE_TITLE_H * 2) + 32), 280, 35);
            BHighlights h = list.getHighlights();
            h.setPageHint(H_PAGEUP, A_RIGHT + 13, A_TOP - 25);
            h.setPageHint(H_PAGEDOWN, A_RIGHT + 13, A_BOTTOM + 30);

            WeatherData weatherData = ((WeatherFactory) context.factory).getWeatherData();

            list.add(new CurrentConditionsScreen(app, weatherData));
            list.add(new ForecastScreen(app, weatherData));
            list.add(new LocalRadarScreen(app, weatherData));
            list.add(new NationalRadarScreen(app, weatherData));
            if (weatherData.hasAlerts())
                list.add(new AlertsScreen(app, weatherData));

            setFocusDefault(list);
        }

        public boolean handleAction(BView view, Object action) {
            if (action.equals("push")) {
                BScreen screen = (BScreen) (list.get(list.getFocus()));
                getBApp().push(screen, TRANSITION_LEFT);
                return true;
            }
            return super.handleAction(view, action);
        }

        public String toString() {
            return "Weather";
        }

        public class TGList extends BList {
            protected SimpleDateFormat mDateFormat;

            protected GregorianCalendar mCalendar;

            public TGList(BView parent, int x, int y, int width, int height, int rowHeight) {
                super(parent, x, y, width, height, rowHeight);

                mDateFormat = new SimpleDateFormat();
                mDateFormat.applyPattern("EEE M/dd");
                mCalendar = new GregorianCalendar();

                setBarAndArrows(BAR_HANG, BAR_DEFAULT, null, "push");
            }

            protected void createRow(BView parent, int index) {
                BView icon = new BView(parent, 10, 3, 30, 30);
                if (index == 4)
                    icon.setResource(mAlertIcon);
                else
                    icon.setResource(mIcon);

                BText name = new BText(parent, 50, 4, parent.width - 40, parent.height - 4);
                name.setShadow(true);
                name.setFlags(RSRC_HALIGN_LEFT);
                name.setValue(get(index).toString());
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
    }

    public class CurrentConditionsScreen extends WeatherScreen {
        private BList list;

        private final int top = SAFE_TITLE_V + 100;

        private final int border_left = SAFE_TITLE_H + 256;

        private final int text_width = width - border_left - (SAFE_TITLE_H);

        public CurrentConditionsScreen(Weather app, WeatherData data) {
            super(app);

            mWeatherData = data;

            setTitle("Current Conditions");

            int start = top;

            icon = new BView(normal, SAFE_TITLE_H, SAFE_TITLE_V + 30, 256, 256);
            icon.setResource("NA.png");

            Resource font = createFont("Dekadens.ttf", Font.BOLD, 60);
            temperatureText = new BText(normal, border_left, SAFE_TITLE_V + 70, text_width - 70, 70);
            temperatureText.setFlags(RSRC_HALIGN_RIGHT | RSRC_VALIGN_TOP);
            temperatureText.setFont(font);
            temperatureText.setColor(new Color(127, 235, 192));
            temperatureText.setShadow(Color.black, 3);

            conditionsText = new BText(normal, SAFE_TITLE_H, SAFE_TITLE_V + 280, 256, 80);
            conditionsText.setFlags(IHmeProtocol.RSRC_HALIGN_CENTER | RSRC_TEXT_WRAP | RSRC_VALIGN_TOP);
            conditionsText.setFont("default-24-bold.font");
            conditionsText.setColor(new Color(127, 235, 192));
            conditionsText.setShadow(true);
            conditionsText.setValue("Snowing");

            start += 70;

            BText labelText = new BText(normal, border_left, start, text_width, 30);
            labelText.setFlags(IHmeProtocol.RSRC_HALIGN_LEFT);
            labelText.setFont("default-18-bold.font");
            labelText.setShadow(true);
            labelText.setValue("UV Index:");

            uvIndexText = new BText(normal, border_left, start, text_width, 30);
            uvIndexText.setFlags(IHmeProtocol.RSRC_HALIGN_RIGHT);
            uvIndexText.setFont("default-18-bold.font");
            uvIndexText.setShadow(true);

            start += 25;

            labelText = new BText(normal, border_left, start, text_width, 30);
            labelText.setFlags(IHmeProtocol.RSRC_HALIGN_LEFT);
            labelText.setFont("default-18-bold.font");
            labelText.setShadow(true);
            labelText.setValue("Wind:");

            windText = new BText(normal, border_left, start, text_width, 30);
            windText.setFlags(IHmeProtocol.RSRC_HALIGN_RIGHT);
            windText.setFont("default-18-bold.font");
            windText.setShadow(true);

            start += 25;

            labelText = new BText(normal, border_left, start, text_width, 30);
            labelText.setFlags(IHmeProtocol.RSRC_HALIGN_LEFT);
            labelText.setFont("default-18-bold.font");
            labelText.setShadow(true);
            labelText.setValue("Humidity:");

            humidityText = new BText(normal, border_left, start, text_width, 30);
            humidityText.setFlags(IHmeProtocol.RSRC_HALIGN_RIGHT);
            humidityText.setFont("default-18-bold.font");
            humidityText.setShadow(true);

            start += 25;

            labelText = new BText(normal, border_left, start, text_width, 30);
            labelText.setFlags(IHmeProtocol.RSRC_HALIGN_LEFT);
            labelText.setFont("default-18-bold.font");
            labelText.setShadow(true);
            labelText.setValue("Pressure:");

            pressureText = new BText(normal, border_left, start, text_width, 30);
            pressureText.setFlags(IHmeProtocol.RSRC_HALIGN_RIGHT);
            pressureText.setFont("default-18-bold.font");
            pressureText.setShadow(true);

            start += 25;

            labelText = new BText(normal, border_left, start, text_width, 30);
            labelText.setFlags(IHmeProtocol.RSRC_HALIGN_LEFT);
            labelText.setFont("default-18-bold.font");
            labelText.setShadow(true);
            labelText.setValue("Dew Point:");

            dewPointText = new BText(normal, border_left, start, text_width, 30);
            dewPointText.setFlags(IHmeProtocol.RSRC_HALIGN_RIGHT);
            dewPointText.setFont("default-18-bold.font");
            dewPointText.setShadow(true);

            start += 25;

            labelText = new BText(normal, border_left, start, text_width, 30);
            labelText.setFlags(IHmeProtocol.RSRC_HALIGN_LEFT);
            labelText.setFont("default-18-bold.font");
            labelText.setShadow(true);
            labelText.setValue("Visibility:");

            visibilityText = new BText(normal, border_left, start, text_width, 30);
            visibilityText.setFlags(IHmeProtocol.RSRC_HALIGN_RIGHT);
            visibilityText.setFont("default-18-bold.font");
            visibilityText.setShadow(true);

            setFooter("weather.com");
            
            /*
             * list = new OptionList(this.normal, SAFE_TITLE_H + 10, (height - SAFE_TITLE_V) - 50, (int) Math
             * .round((width - (SAFE_TITLE_H * 2)) / 2.5), 90, 35); list.add("Return to menu");
             * 
             * setFocusDefault(list);
             */

            updateText();
        }

        private void updateText() {
            temperatureText.setValue(mWeatherData.getCurrentConditions().getTemperature());
            conditionsText.setValue(mWeatherData.getCurrentConditions().getConditions());
            icon.setResource(pad(mWeatherData.getCurrentConditions().getIcon()) + ".png");
            uvIndexText.setValue(mWeatherData.getCurrentConditions().getUltraVioletIndex() + " "
                    + mWeatherData.getCurrentConditions().getUltraVioletDescription());
            windText.setValue("From " + mWeatherData.getCurrentConditions().getWindDescription() + " at "
                    + mWeatherData.getCurrentConditions().getWindSpeed() + " " + mWeatherData.getSpeedUnit());
            humidityText.setValue(mWeatherData.getCurrentConditions().getHumidity() + "%");
            pressureText.setValue(mWeatherData.getCurrentConditions().getBarometricPressure() + " "
                    + mWeatherData.getPressureUnit() + ".");
            dewPointText.setValue(mWeatherData.getCurrentConditions().getDewPoint() + "\u00BA"
                    + mWeatherData.getTemperatureUnit());
            visibilityText.setValue(mWeatherData.getCurrentConditions().getVisibility() + " "
                    + mWeatherData.getDistanceUnit() + ".");
        }

        public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
            try {
                updateText();
            } catch (Exception ex) {
                log.error("Could not update weather text", ex);
            }
            return super.handleEnter(arg, isReturn);
        }

        public String toString() {
            return "Current Conditions";
        }

        private BView icon;

        private BText temperatureText;

        private BText conditionsText;

        private BText uvIndexText;

        private BText windText;

        private BText humidityText;

        private BText pressureText;

        private BText dewPointText;

        private BText visibilityText;

        WeatherData mWeatherData;
    }

    public class ForecastScreen extends WeatherScreen {
        private BList list;

        private final int top = SAFE_TITLE_V + 80;

        private final int border_left = SAFE_TITLE_H;

        private final int text_width = width - border_left - (SAFE_TITLE_H);

        public ForecastScreen(Weather app, WeatherData data) {
            super(app);

            mWeatherData = data;

            setTitle("Forecast");

            int gap = 6;

            int dayWidth = (text_width - 4 * gap) / 5;

            for (int i = 0; i < 5; i++) {
                int start = top;

                int x = (dayWidth + gap / 2) * i;

                dayText[i] = new BText(normal, border_left + x, start, dayWidth, 20);
                dayText[i].setFlags(RSRC_HALIGN_CENTER | RSRC_VALIGN_TOP);
                dayText[i].setFont("default-18-bold.font");
                dayText[i].setShadow(true);

                start = start + 20;

                icon[i] = new BView(normal, border_left + x, start, dayWidth, dayWidth);
                icon[i].setResource("NA.png");

                start = start + dayWidth;

                hiText[i] = new BText(normal, border_left + x, start, dayWidth, 30);
                hiText[i].setFlags(RSRC_HALIGN_CENTER | RSRC_VALIGN_TOP);
                hiText[i].setFont("default-24-bold.font");
                hiText[i].setShadow(true);

                start = start + 30;

                loText[i] = new BText(normal, border_left + x, start, dayWidth, 20);
                loText[i].setFlags(RSRC_HALIGN_CENTER | RSRC_VALIGN_TOP);
                loText[i].setFont("default-18-bold.font");
                loText[i].setShadow(true);

                start = start + 20;

                descriptionText[i] = new BText(normal, border_left + x, start, dayWidth, 60);
                descriptionText[i].setFlags(RSRC_HALIGN_CENTER | RSRC_VALIGN_TOP | RSRC_TEXT_WRAP);
                descriptionText[i].setFont("default-18-bold.font");
                descriptionText[i].setColor(new Color(127, 235, 192));
                descriptionText[i].setShadow(true);
            }

            setFooter("weather.gov");

            /*
             * list = new OptionList(this.normal, SAFE_TITLE_H + 10, (height - SAFE_TITLE_V) - 50, (int) Math
             * .round((width - (SAFE_TITLE_H * 2)) / 2.5), 90, 35); list.add("Return to menu");
             * 
             * setFocusDefault(list);
             */

            updateText();
        }

        private void updateText() {

            WeatherData.Forecasts forecasts = mWeatherData.getForecasts();

            int counter = 0;
            Iterator iterator = forecasts.getForecast();
            while (iterator.hasNext()) {
                WeatherData.Forecast forecast = (WeatherData.Forecast) iterator.next();
                WeatherData.Part dayPart = forecast.getDayForecast();
                WeatherData.Part nightPart = forecast.getNightForecast();
                java.awt.Image image = Tools.getResourceAsImage(getClass(), pad(dayPart.getIcon()) + ".png")
                        .getScaledInstance(text_width / 5, text_width / 5, java.awt.Image.SCALE_SMOOTH);
                image = Tools.getImage(image);
                icon[counter].setResource(image);

                dayText[counter].setValue(forecast.getDescription());
                String value = forecast.getHigh();
                if (value.equals("N/A"))
                    value = mWeatherData.getCurrentConditions().getTemperature();
                hiText[counter].setValue(value);
                loText[counter].setValue(forecast.getLow());
                value = dayPart.getDescription();
                if (value.equals("N/A"))
                    value = nightPart.getDescription();
                descriptionText[counter].setValue(value);

                counter = counter + 1;
            }
        }

        private String pad(String value) {
            if (value.length() == 0)
                return "00";
            if (value.length() == 1)
                return "0" + value;
            return value;
        }

        public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
            try {
                updateText();
            } catch (Exception ex) {
                log.error("Could not update weather text", ex);
            }
            return super.handleEnter(arg, isReturn);
        }

        public String toString() {
            return "Forecast";
        }

        private BView[] icon = new BView[5];

        private BText[] dayText = new BText[5];

        private BText[] hiText = new BText[5];

        private BText[] loText = new BText[5];

        private BText[] descriptionText = new BText[5];

        private WeatherData mWeatherData;
    }

    public class LocalRadarScreen extends WeatherScreen {
        private BList list;

        public LocalRadarScreen(Weather app, WeatherData data) {
            super(app);

            mWeatherData = data;

            setTitle(" ");

            image = new BView(below, SAFE_TITLE_H, SAFE_TITLE_V, width - (SAFE_TITLE_H * 2), height
                    - (SAFE_TITLE_V * 2));

            /*
             * list = new OptionList(this.normal, SAFE_TITLE_H + 10, (height - SAFE_TITLE_V) - 50, (int) Math
             * .round((width - (SAFE_TITLE_H * 2)) / 2.5), 90, 35); list.add("Return to menu");
             * 
             * setFocusDefault(list);
             */

            updateImage();
        }

        private void updateImage() {
            WeatherData.Forecasts forecasts = mWeatherData.getForecasts();

            try {
                if (mWeatherData.getLocalRadar() != null) {
                    java.awt.Image cached = Tools.retrieveCachedImage(new URL(mWeatherData.getLocalRadar()));
                    if (cached != null) {
                        //cached = cached.getScaledInstance(image.width, image.height, java.awt.Image.SCALE_SMOOTH);
                        //cached = Tools.getImage(cached);
                        image.setResource(cached);
                        return;
                    }
                }
            } catch (MalformedURLException ex) {
                log.error("Could not update weather local radar", ex);
            }

            below.setResource(mBackground);
            image.setResource("NA.png");
        }

        public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
            try {
                updateImage();
            } catch (Exception ex) {
                log.error("Could not update weather text", ex);
            }
            return super.handleEnter(arg, isReturn);
        }

        /*
         * public boolean handleKeyPress(int code, long rawcode) { switch (code) { case KEY_SELECT: case KEY_RIGHT:
         * postEvent(new BEvent.Action(this, "pop")); return true; case KEY_LEFT: // TODO Why never gets this code?
         * postEvent(new BEvent.Action(this, "pop")); return true; } return super.handleKeyPress(code, rawcode); }
         */

        public String toString() {
            return "Local Radar";
        }

        private BView image;

        WeatherData mWeatherData;
    }

    public class NationalRadarScreen extends WeatherScreen {
        private BList list;

        public NationalRadarScreen(Weather app, WeatherData data) {
            super(app);

            mWeatherData = data;

            setTitle(" ");

            image = new BView(below, SAFE_TITLE_H, SAFE_TITLE_V, width - (SAFE_TITLE_H * 2), height
                    - (SAFE_TITLE_V * 2));

            /*
             * list = new OptionList(this.normal, SAFE_TITLE_H + 10, (height - SAFE_TITLE_V) - 50, (int) Math
             * .round((width - (SAFE_TITLE_H * 2)) / 2.5), 90, 35); list.add("Return to menu");
             * 
             * setFocusDefault(list);
             */

            updateImage();
        }

        private void updateImage() {
            WeatherData.Forecasts forecasts = mWeatherData.getForecasts();

            try {
                if (mWeatherData.getNationalRadar() != null) {
                    java.awt.Image cached = Tools.retrieveCachedImage(new URL(mWeatherData.getNationalRadar()));
                    if (cached != null) {
                        image.setResource(cached);
                        return;
                    }
                }
            } catch (MalformedURLException ex) {
                log.error("Could not update weather local radar", ex);
            }
            below.setResource(mBackground);
            image.setResource("NA.png");
        }

        public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
            try {
                updateImage();
            } catch (Exception ex) {
                log.error("Could not update weather text", ex);
            }
            return super.handleEnter(arg, isReturn);
        }

        /*
         * public boolean handleKeyPress(int code, long rawcode) { switch (code) { case KEY_SELECT: case KEY_RIGHT:
         * postEvent(new BEvent.Action(this, "pop")); return true; case KEY_LEFT: // TODO Why never gets this code?
         * postEvent(new BEvent.Action(this, "pop")); return true; } return super.handleKeyPress(code, rawcode); }
         */

        public String toString() {
            return "National Radar";
        }

        private BView image;

        WeatherData mWeatherData;
    }

    public class AlertsScreen extends WeatherScreen {
        private BList list;

        private final int top = SAFE_TITLE_V + 100;

        private final int border_left = SAFE_TITLE_H;

        private final int text_width = width - border_left - (SAFE_TITLE_H);

        public AlertsScreen(Weather app, WeatherData data) {
            super(app);

            mWeatherData = data;

            mDateFormat = new SimpleDateFormat();
            mDateFormat.applyPattern("EEE M/d hh:mm a");

            setTitle("Alerts");

            int start = top;

            eventText = new BText(normal, border_left, start, text_width, 30);
            eventText.setFlags(RSRC_HALIGN_CENTER | RSRC_VALIGN_TOP | RSRC_TEXT_WRAP);
            eventText.setFont("default-24-bold.font");
            eventText.setColor(new Color(150, 100, 100));
            eventText.setShadow(Color.black, 3);

            start += 30;

            datesText = new BText(normal, border_left, start, text_width, 20);
            datesText.setFlags(RSRC_HALIGN_CENTER | RSRC_VALIGN_TOP | RSRC_TEXT_WRAP);
            datesText.setFont("default-18-bold.font");
            eventText.setColor(new Color(150, 100, 100));
            datesText.setShadow(true);

            start += 25;

            scrollText = new ScrollText(normal, SAFE_TITLE_H, start, text_width - 10, height - 2 * SAFE_TITLE_V - 193,
                    "");

            setFocusDefault(scrollText);
            
            setFooter("weather.gov");

            /*
             * list = new OptionList(this.normal, SAFE_TITLE_H + 10, (height - SAFE_TITLE_V) - 50, (int) Math
             * .round((width - (SAFE_TITLE_H * 2)) / 2.5), 90, 35); list.add("Return to menu");
             * 
             * setFocusDefault(list);
             */

            updateText();
        }

        private void updateText() {
            Iterator iterator = mWeatherData.getAlerts();
            if (iterator.hasNext()) {
                WeatherData.Alert alert = (WeatherData.Alert) iterator.next();

                eventText.setValue(alert.getEvent() != null ? alert.getEvent() : alert.getHeadline());
                if (alert.getEffective() != null)
                    datesText.setValue(mDateFormat.format(alert.getEffective()) + " to "
                            + mDateFormat.format(alert.getExpires()));
                scrollText.setText(alert.getDescription());
            }
        }

        public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
            try {
                updateText();
            } catch (Exception ex) {
                log.error("Could not update alerts text", ex);
            }
            return super.handleEnter(arg, isReturn);
        }

        /*
         * public boolean handleKeyPress(int code, long rawcode) { switch (code) { case KEY_SELECT: case KEY_RIGHT:
         * postEvent(new BEvent.Action(this, "pop")); return true; case KEY_LEFT: // TODO Why never gets this code?
         * postEvent(new BEvent.Action(this, "pop")); return true; } return super.handleKeyPress(code, rawcode); }
         */

        public String toString() {
            return "Alerts";
        }

        private BText eventText;

        private BText datesText;

        private ScrollText scrollText;

        private SimpleDateFormat mDateFormat;

        WeatherData mWeatherData;
    }

    public class OptionList extends BList {
        public OptionList(BView parent, int x, int y, int width, int height, int rowHeight) {
            super(parent, x, y, width, height, rowHeight);

            setBarAndArrows(BAR_HANG, BAR_DEFAULT, null, "push");
        }

        protected void createRow(BView parent, int index) {
            BText text = new BText(parent, 10, 4, parent.width - 40, parent.height - 4);
            text.setShadow(true);
            text.setFlags(RSRC_HALIGN_LEFT);
            text.setValue(get(index).toString());
        }

        public boolean handleKeyPress(int code, long rawcode) {
            switch (code) {
            case KEY_SELECT:
                postEvent(new BEvent.Action(this, "pop"));
                return true;
            }
            return super.handleKeyPress(code, rawcode);
        }
    }

    public static class WeatherFactory extends AppFactory {
        WeatherData weatherData = null;

        public WeatherFactory(AppContext appContext) {
            super(appContext);
        }

        protected void init(ArgumentList args) {
            super.init(args);
            WeatherConfiguration weatherConfiguration = (WeatherConfiguration) getAppContext().getConfiguration();
            weatherData = new WeatherData(weatherConfiguration.getId(), weatherConfiguration.getCity(),
                    weatherConfiguration.getState(), weatherConfiguration.getZip(), 512, 384); // TODO get real
            // dimensions
        }

        public void handleHTTP(HttpRequest http, String uri) throws IOException {
            if (uri.equals("icon.png")) {
                if (weatherData.hasAlerts()) {
                    super.handleHTTP(http, "alerticon.png");
                    return;
                }
            }
            super.handleHTTP(http, uri);
        }

        public WeatherData getWeatherData() {
            return weatherData;
        }
    }

    private static String pad(String value) {
        if (value.length() == 0)
            return "00";
        if (value.length() == 1)
            return "0" + value;
        return value;
    }
}