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
import java.awt.image.*;
import java.util.*;
import java.util.zip.*;

public class WidgetLoader {
    final static Button mediaTrackerComp = new Button();

    public WidgetLoader(String filename) {
    }

    private void loadResource(String filename) {
        ZipInputStream wsz = null;
        try {
            wsz = new ZipInputStream(new FileInputStream(filename));
            ZipEntry resource = wsz.getNextEntry();
            while (resource != null)
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int success = wsz.read(data);
                while (success != -1) {
                    baos.write(data, 0, success);
                }
            
                String name = resource.getName().toLowerCase();
                if (!resource.isDirectory())
                    int pos = name.indexOf("/");
                    if (pos!=-1)
                    {
                        
                        pos = name.indexOf("/");
                        if (pos!=-1)
                        {
                            //System.out.println(name);
                            {
                            else
                            {
                                Image image = Toolkit.getDefaultToolkit().createImage(baos.toByteArray());
                                    MediaTracker mt = new MediaTracker(mediaTrackerComp);
                                    mt.addImage(image, 0);
                                    mt.waitForAll();
                                } catch(InterruptedException e) {
                                }
                                
                                mResources.put(name, image);
                            else
                            if (name.endsWith("kon") || name.endsWith("js"))
                            {
                            }
                            else                
                            if (resource.getName().toLowerCase().endsWith("ttf"))
                            {
                            }
                resource = wsz.getNextEntry();
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                if (wsz != null)
                    wsz.close();
            } catch (IOException ex) {
            }
        }
    }
        return mResources.get(name.toLowerCase());
 
   private String mCode;
}