package org.lnicholls.galleon.skin;

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

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

import com.tivo.hme.sdk.*;
import com.tivo.hme.util.*;
import com.tivo.hme.http.server.*;
import com.tivo.hme.http.share.*;

/**
 * A konfabulator widget interpreter 
 *
 * @author      Leon Nicholls
 */
public class Konfabulator extends Application
{
    public static WidgetLoader mWidgetLoader;
    View mContent;
    
    /**
     * Create the app.
     */
    public void init(Context context)
    {
        //root.setResource("myloop.mpg");
                           root.width - SAFE_ACTION_H,
                           root.height - SAFE_ACTION_V);
    }
        if (mWidget!=null)
            mWidget.close();
    
     * Handle events from the mp3 stream.
     */
    public boolean handleEvent(HmeEvent event)
    {
	switch (event.opcode) {

              //if (title.handleEvent(event))
          //
          // The stream will send resouce info events while it plays. The SDK
          // will automatically generate a synthetic RSRC_STATUS event whenever
          // the status of the stream changes.
          //
          // If the track finishes, start playing the next one.
          //
          
	  case StreamResource.EVT_RSRC_INFO: {
	      HmeEvent.ResourceInfo info = (HmeEvent.ResourceInfo)event;
	        String pos = (String)info.map.get("pos");
	        {
                    {
	                if (tokenizer.countTokens()==2)
	                {
	                    String total = tokenizer.nextToken();
	                    //positionControl.setPosition(value);
                        }
	            catch (Exception ex) {}                        
	        }
	        if (bitrate!=null)
	            try
	            {
	                String newValue = Integer.toString(value);
	                if (value<100)
	                    newValue = " " + newValue;
	            catch (Exception ex) {}
	        }    
	      return true;
	      HmeEvent.ResourceInfo info = (HmeEvent.ResourceInfo)event;
                  // the track finished - what next?
                  
	      }
	      return true;
	}
	return super.handleEvent(event);
    }
    /**
     * All events received by the app are sent through dispatchEvent. This is a
     * convenient place to listen for ALL key presses since they hit this method
     * regardless of which view has the focus.
     */
    protected void dispatchEvent(HmeEvent event)
    {
        //System.out.println(event);
          case EVT_KEY:
              HmeEvent.Key e = (HmeEvent.Key)event;
              if (e.action==KEY_PRESS)
                switch (e.code){
                  case KEY_PAUSE:
                    break;
                    break;                  
                    break;
                  case KEY_RIGHT:
                    break;                                                            
                    break;                    
                    break;                    
                    break;                                                                 
                } 
              else             
                    break;
                    break;                                  
                  case KEY_CHANNELUP:
                    break;                    
                    break;                    
                  case KEY_SLOW:
                    break;                                        
                  case KEY_ENTER:
                    break;                                             
                    break;
                  case KEY_RIGHT:
                    break;                                                            
            break;
        }
        super.dispatchEvent(event);
    }

    public static class KonfabulatorFactory extends Factory
    {
        WidgetLoader widgetLoader = null;

        /**
         * Create the factory - scan the folder.
         */
	protected void init(ArgumentList args)
	{
            try {
                args.checkForIllegalFlags();
                if (args.getRemainingCount() != 1) {
                    usage();
                }
            } catch (ArgumentList.BadArgumentException e) {
                usage();
            }

            String file = args.shift();
            if (!new File(file).exists()) {
                System.out.println("Widget not found: " + file);
                usage();
            }
	    
	}

        /**
         * Print usage and exit.
         */
        void usage()
        {
            System.err.println("Usage: Konfabulator widget");
            System.err.println("For example 'Konfabulator Satsuki.widget' ");
            System.exit(1);
        }
    }
