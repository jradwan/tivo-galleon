package org.lnicholls.galleon.util;

/*
 * Copyright (C) 2005  Leon Nicholls
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * See the file "COPYING" for more details.
 */

import java.beans.IntrospectionException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.lnicholls.galleon.server.Constants;
import org.lnicholls.galleon.server.ServerConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.lnicholls.galleon.app.*;
import org.lnicholls.galleon.togo.*;

/**
 * Utility class to read the conf/configure.xml file on startup
 */
public class Configurator implements Constants {
    private static Logger log = Logger.getLogger(Configurator.class.getName());

    private static int DEFAULT_PORT = 8081;

    private static String TAG_CONFIGURATION = "configuration";

    private static String TAG_SERVER = "server";

    private static String TAG_PLUGIN = "plugin";
    
    private static String TAG_TIVO = "tivo";    

    private static String ATTRIBUTE_VERSION = "version";

    private static String ATTRIBUTE_URL = "url";

    private static String ATTRIBUTE_PORT = "port";

    private static String ATTRIBUTE_TITLE = "title";

    private static String ATTRIBUTE_CLASS = "class";

    private static String ATTRIBUTE_RELOAD = "reload";

    private static String ATTRIBUTE_IP_ADDRESS = "ipaddress";

    private static String ATTRIBUTE_NET_MASK = "netmask";

    private static String ATTRIBUTE_SHUFFLE_ITEMS = "shuffleItems";

    private static String ATTRIBUTE_GENERATE_THUMBNAILS = "generateThumbnails";

    private static String ATTRIBUTE_USE_STREAMING_PROXY = "useStreamingProxy";

    private static String ATTRIBUTE_USE_TIVO_BEACON = "useTiVoBeacon";
    
    private static String ATTRIBUTE_RECORDINGS_PATH = "recordingsPath";

    private static String ATTRIBUTE_MEDIA_ACCESS_KEY = "mediaAccessKey";    

    public Configurator(ServerConfiguration serverConfiguration) {
        mServerConfiguration = serverConfiguration;
    }
    
    public void load(AppManager appManager) {
        File configureDir = new File(System.getProperty("conf"));
        load(appManager, configureDir);
    }

    public void load(AppManager appManager, File configureDir) {
        try {
            File file = new File(configureDir.getAbsolutePath() + "/configure.xml.rpmsave");
            if (file.exists()) {
                loadDocument(appManager, file);
                save(appManager);
                File oldFile = new File(configureDir.getAbsolutePath() + "/configure.xml.rpmsave.old");
                if (oldFile.exists())
                    oldFile.delete();
                file.renameTo(oldFile);
            } else {
                loadDocument(appManager, new File(configureDir.getAbsolutePath() + "/configure.xml"));
            }
        } catch (Exception ex) {
            Tools.logException(Configurator.class, ex);
        }
    }

    private void loadDocument(AppManager appManager, File file) {
        // Need to handle previous version of configuration file
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setValidating(true);
        //factory.setNamespaceAware(true);
        try {
            FileInputStream in = null;
            DocumentBuilder builder = factory.newDocumentBuilder();
            in = new FileInputStream(file);
            Document document = builder.parse(in);
            in.close();
            in = null;

            // <configuration>
            Node domNode = document.getFirstChild();
            if (log.isDebugEnabled())
                log.debug("document:" + domNode.getNodeName());

            if (domNode.getNodeName().equalsIgnoreCase(TAG_CONFIGURATION)) {
                NamedNodeMap namedNodeMap = domNode.getAttributes();
                if (namedNodeMap != null) {
                    // Check for required attributes
                    Node attribute = namedNodeMap.getNamedItem(ATTRIBUTE_VERSION);
                    if (log.isDebugEnabled())
                        log.debug(domNode.getNodeName() + ":" + attribute.getNodeName() + "="
                                + attribute.getNodeValue());
                    loadDocument(domNode, appManager);
                    if (!attribute.getNodeValue().equals(mServerConfiguration.getVersion()))
                        save(appManager);
                }
            }
        } catch (SAXParseException spe) {
            // Error generated by the parser
            log.error("Parsing error, line " + spe.getLineNumber() + ", uri " + spe.getSystemId());
            log.error("   " + spe.getMessage());
            Tools.logException(Configurator.class, spe);

            // Use the contained exception, if any
            Exception x = spe;
            if (spe.getException() != null)
                x = spe.getException();
            Tools.logException(Configurator.class, x);

        } catch (SAXException sxe) {
            // Error generated during parsing)
            Exception x = sxe;
            if (sxe.getException() != null)
                x = sxe.getException();
            Tools.logException(Configurator.class, x);
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            log.error("Cannot get context" + file.getAbsolutePath());
            Tools.logException(Configurator.class, pce);
        } catch (IOException ioe) {
            // I/O error
            log.error("Cannot get context" + file.getAbsolutePath());
            Tools.logException(Configurator.class, ioe);
        } finally {
        }
    }

    private void loadDocument(Node configurationNode, AppManager appManager) {
        try {
            // <server>, <plugin>
            for (int i = 0; i < configurationNode.getChildNodes().getLength(); i++) {
                Node node = configurationNode.getChildNodes().item(i);
                if (log.isDebugEnabled())
                    log.debug("node:" + node.getNodeName());

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (node.getNodeName().equals(TAG_SERVER)) {
                        if (log.isDebugEnabled())
                            log.debug("Found server");
                        NamedNodeMap namedNodeMap = node.getAttributes();
                        if (namedNodeMap != null) {
                            Node attribute = namedNodeMap.getNamedItem(ATTRIBUTE_RELOAD);
                            // Required attributes
                            if (attribute != null) {
                                if (log.isDebugEnabled())
                                    log.debug(node.getNodeName() + ":" + attribute.getNodeName() + "="
                                            + attribute.getNodeValue());
                                try {
                                    int reload = Integer.parseInt(attribute.getNodeValue());
                                    mServerConfiguration.setReload(reload);
                                } catch (NumberFormatException ex) {
                                    log.error("Invalid " + ATTRIBUTE_RELOAD + " for " + TAG_SERVER + ": "
                                            + attribute.getNodeValue());
                                }
                            }
                            attribute = namedNodeMap.getNamedItem(ATTRIBUTE_PORT);
                            if (attribute != null) {
                                if (log.isDebugEnabled())
                                    log.debug(node.getNodeName() + ":" + attribute.getNodeName() + "="
                                            + attribute.getNodeValue());
                                try {
                                    int port = Integer.parseInt(attribute.getNodeValue());
                                    mServerConfiguration.setConfiguredPort(port);
                                } catch (NumberFormatException ex) {
                                    log.error("Invalid " + ATTRIBUTE_PORT + " for " + TAG_SERVER + ": "
                                            + attribute.getNodeValue());
                                }
                            }

                            attribute = namedNodeMap.getNamedItem(ATTRIBUTE_TITLE);
                            if (attribute != null) {
                                if (log.isDebugEnabled())
                                    log.debug(node.getNodeName() + ":" + attribute.getNodeName() + "="
                                            + attribute.getNodeValue());
                                mServerConfiguration.setName(attribute.getNodeValue());
                            }

                            attribute = namedNodeMap.getNamedItem(ATTRIBUTE_IP_ADDRESS);
                            if (attribute != null) {
                                if (log.isDebugEnabled())
                                    log.debug(node.getNodeName() + ":" + attribute.getNodeName() + "="
                                            + attribute.getNodeValue());
                                Node attribute2 = namedNodeMap.getNamedItem(ATTRIBUTE_NET_MASK);
                                if (attribute2 != null) {
                                    mServerConfiguration.setIPAddress(attribute.getNodeValue());
                                    mServerConfiguration.setNetMask(attribute2.getNodeValue());
                                } else
                                    log.error("Missing attribute " + ATTRIBUTE_NET_MASK + " for " + TAG_SERVER);
                            }

                            attribute = namedNodeMap.getNamedItem(ATTRIBUTE_SHUFFLE_ITEMS);
                            if (attribute != null) {
                                if (log.isDebugEnabled())
                                    log.debug(node.getNodeName() + ":" + attribute.getNodeName() + "="
                                            + attribute.getNodeValue());
                                mServerConfiguration.setShuffleItems(Boolean.valueOf(attribute.getNodeValue())
                                        .booleanValue());
                            }

                            attribute = namedNodeMap.getNamedItem(ATTRIBUTE_GENERATE_THUMBNAILS);
                            if (attribute != null) {
                                if (log.isDebugEnabled())
                                    log.debug(node.getNodeName() + ":" + attribute.getNodeName() + "="
                                            + attribute.getNodeValue());
                                mServerConfiguration.setGenerateThumbnails(Boolean.valueOf(attribute.getNodeValue())
                                        .booleanValue());
                            }

                            attribute = namedNodeMap.getNamedItem(ATTRIBUTE_USE_STREAMING_PROXY);
                            if (attribute != null) {
                                if (log.isDebugEnabled())
                                    log.debug(node.getNodeName() + ":" + attribute.getNodeName() + "="
                                            + attribute.getNodeValue());
                                mServerConfiguration.setUseStreamingProxy(Boolean.valueOf(attribute.getNodeValue())
                                        .booleanValue());
                            }
                            
                            attribute = namedNodeMap.getNamedItem(ATTRIBUTE_RECORDINGS_PATH);
                            if (attribute != null) {
                                if (log.isDebugEnabled())
                                    log.debug(node.getNodeName() + ":" + attribute.getNodeName() + "="
                                            + attribute.getNodeValue());
                                mServerConfiguration.setRecordingsPath(Tools.unEscapeXMLChars(URLDecoder.decode(attribute.getNodeValue(), ENCODING)));
                            }

                            attribute = namedNodeMap.getNamedItem(ATTRIBUTE_MEDIA_ACCESS_KEY);
                            if (attribute != null) {
                                if (log.isDebugEnabled())
                                    log.debug(node.getNodeName() + ":" + attribute.getNodeName() + "="
                                            + attribute.getNodeValue().length());
                                mServerConfiguration.setMediaAccessKey(attribute.getNodeValue());
                            }
                        }
                    } else if (node.getNodeName().equals(TAG_PLUGIN)) {
                        if (log.isDebugEnabled())
                            log.debug("Found plugin");
                        NamedNodeMap namedNodeMap = node.getAttributes();
                        if (namedNodeMap != null) {
                            String title = null;
                            String className = null;
                            Node attribute = namedNodeMap.getNamedItem(ATTRIBUTE_TITLE);
                            // Check for required attributes
                            if (attribute != null) {
                                if (log.isDebugEnabled())
                                    log.debug(node.getNodeName() + ":" + attribute.getNodeName() + "="
                                            + attribute.getNodeValue());
                                title = attribute.getNodeValue();
                            } else
                                log.error("Missing required " + ATTRIBUTE_TITLE + " attribute for " + TAG_PLUGIN);

                            attribute = namedNodeMap.getNamedItem(ATTRIBUTE_CLASS);
                            if (attribute != null) {
                                if (log.isDebugEnabled())
                                    log.debug(node.getNodeName() + ":" + attribute.getNodeName() + "="
                                            + attribute.getNodeValue());
                                className = attribute.getNodeValue();
                            } else
                                log.error("Missing required " + ATTRIBUTE_CLASS + " attribute for " + TAG_PLUGIN);

                            /*
                            if (title != null && className != null) {
                                Plugin plugin = null;
                                Iterator pluginDescriptorIterator = pluginManager.getPluginDescriptors();
                                while (pluginDescriptorIterator.hasNext()) {
                                    PluginDescriptor pluginDescriptor = (PluginDescriptor) pluginDescriptorIterator
                                            .next();
                                    if (log.isDebugEnabled())
                                        log.debug("Compare " + pluginDescriptor.getClassName() + " with " + className);
                                    if (pluginDescriptor.getClassName().equals(className)) {
                                        Class pluginClass = pluginManager.loadPlugin(pluginDescriptor);
                                        if (pluginClass != null) {
                                            try {
                                                BeanReader beanReader = new BeanReader();
                                                beanReader.getXMLIntrospector().setAttributesForPrimitives(true);
                                                beanReader.registerBeanClass("plugin", pluginClass);

                                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                                OutputFormat of = new OutputFormat("XML", ENCODING, true);
                                                XMLSerializer serializer = new XMLSerializer(bos, of);
                                                serializer.asDOMSerializer();
                                                serializer.serialize((Element) node);

                                                log.debug("node=" + bos.toString());
                                                StringReader xmlReader = new StringReader(bos.toString());
                                                bos.close();

                                                plugin = (Plugin) beanReader.parse(xmlReader);

                                                pluginManager.addPlugin(plugin);

                                                if (log.isDebugEnabled())
                                                    log.debug("Plugin=" + plugin);
                                            } catch (IntrospectionException ex) {
                                                log.error("Could not load plugin " + title + " (" + className + ")");
                                            }
                                        } else
                                            log.error("Could not find plugin " + title + " (" + className + ")");
                                    }
                                }

                                if (plugin == null) {
                                    log.error("Could not find plugin " + title + " (" + className + ")");
                                }
                            }
                            */
                        }
                    } else if (node.getNodeName().equals(TAG_TIVO)) {
                        if (log.isDebugEnabled())
                            log.debug("Found TiVo");
                        try {
                            BeanReader beanReader = new BeanReader();
                            beanReader.getXMLIntrospector().setAttributesForPrimitives(true);
                            beanReader.registerBeanClass("tivo", TiVo.class);

                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            OutputFormat of = new OutputFormat("XML", ENCODING, true);
                            XMLSerializer serializer = new XMLSerializer(bos, of);
                            serializer.asDOMSerializer();
                            serializer.serialize((Element) node);

                            log.debug("node=" + bos.toString());
                            StringReader xmlReader = new StringReader(bos.toString());
                            bos.close();

                            TiVo tivo = (TiVo) beanReader.parse(xmlReader);

                            mServerConfiguration.addTiVo(tivo);

                            if (log.isDebugEnabled())
                                log.debug("TiVo=" + tivo);
                        } catch (IntrospectionException ex) {
                            log.error("Could not load tivo");
                        }
                    }
                }
            }
        /*
        } catch (SAXParseException spe) {
            // Error generated by the parser
            log.error("Parsing error, line " + spe.getLineNumber() + ", uri " + spe.getSystemId());
            log.error("   " + spe.getMessage());
            Tools.logException(Configurator.class, spe);

            // Use the contained exception, if any
            Exception x = spe;
            if (spe.getException() != null)
                x = spe.getException();
            Tools.logException(Configurator.class, x);

        } catch (SAXException sxe) {
            // Error generated during parsing)
            Exception x = sxe;
            if (sxe.getException() != null)
                x = sxe.getException();
            Tools.logException(Configurator.class, x);
        } catch (IOException ioe) {
            // I/O error
            Tools.logException(Configurator.class, ioe, "Cannot get context");
*/            
        } catch (Exception ioe) {
            // I/O error
            Tools.logException(Configurator.class, ioe, "Cannot get context");            
        } finally {
        }
    }

    private Object convertType(Object value, Class conversionType) throws Exception {
        Class classArgs[] = new Class[1];
        classArgs[0] = value.getClass();
        Object objectArgs[] = new Object[1];
        objectArgs[0] = value;

        Constructor constructor = conversionType.getConstructor(classArgs);
        return constructor.newInstance(objectArgs);
    }
    
    public void save(AppManager appManager) {
        File configureDir = new File(System.getProperty("conf"));
        save(appManager, configureDir);
    }

    public void save(AppManager appManager, File configureDir) {
        // Utility class to control the behavior of Commons Betwixt
        class PluginXMLIntrospector extends XMLIntrospector {
            public org.apache.commons.betwixt.Descriptor createXMLDescriptor(
                    org.apache.commons.betwixt.BeanProperty beanProperty) {
                // Dont save the settings of the plugin interface methods
                if (beanProperty.getPropertyName().equals("lastModified")
                        || beanProperty.getPropertyName().equals("sourceFormat")
                        || beanProperty.getPropertyName().equals("contentType")
                        || beanProperty.getPropertyName().equals("url")
                        || beanProperty.getPropertyName().equals("group")
                        || beanProperty.getPropertyName().equals("url")
                        || beanProperty.getPropertyName().equals("items"))
                    return null;

                // Hack to allow the super method to save the class value
                if (beanProperty.getPropertyName().equals("class")) {
                    return super.createXMLDescriptor(new org.apache.commons.betwixt.BeanProperty(beanProperty
                            .getPropertyName(), String.class,
                            new org.apache.commons.betwixt.expression.ClassNameExpression(), beanProperty
                                    .getPropertyUpdater()));
                }

                // Only save methods that have both get and set methods
                if (beanProperty.getPropertyExpression() != null && beanProperty.getPropertyUpdater() != null)
                    return super.createXMLDescriptor(beanProperty);
                else
                    return null;
            }
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(configureDir.getAbsoluteFile() + "/configure.xml");
            PrintWriter printWriter = new PrintWriter(fileOutputStream);
            StringBuffer buffer = new StringBuffer();
            synchronized (buffer) {
                buffer.append("<?xml version=\"1.0\" encoding=\"").append(ENCODING).append("\"?>\n");
                buffer.append("<").append(TAG_CONFIGURATION).append(" ").append(ATTRIBUTE_VERSION).append("=\"")
                        .append(mServerConfiguration.getVersion()).append("\">\n");
                // Server
                buffer.append("<").append(TAG_SERVER).append(" ").append(ATTRIBUTE_TITLE).append("=\"").append(
                        mServerConfiguration.getName()).append("\" ").append(ATTRIBUTE_RELOAD).append("=\"").append(
                        mServerConfiguration.getReload()).append("\" ").append(ATTRIBUTE_PORT).append("=\"").append(
                        mServerConfiguration.getConfiguredPort()).append("\"");
                if (mServerConfiguration.getIPAddress() != null && mServerConfiguration.getIPAddress().length() > 0)
                    buffer.append(" ").append(ATTRIBUTE_IP_ADDRESS).append("=\"").append(
                            mServerConfiguration.getIPAddress()).append("\" ").append(ATTRIBUTE_NET_MASK).append("=\"")
                            .append(mServerConfiguration.getNetMask()).append("\"");
                buffer.append(" ").append(ATTRIBUTE_SHUFFLE_ITEMS).append("=\"").append(
                        mServerConfiguration.getShuffleItems()).append("\"");
                buffer.append(" ").append(ATTRIBUTE_GENERATE_THUMBNAILS).append("=\"").append(
                        mServerConfiguration.getGenerateThumbnails()).append("\"");
                buffer.append(" ").append(ATTRIBUTE_USE_STREAMING_PROXY).append("=\"").append(
                        mServerConfiguration.getUseStreamingProxy()).append("\"");
                buffer.append(" ").append(ATTRIBUTE_RECORDINGS_PATH).append("=\"").append(
                        URLEncoder.encode(Tools.escapeXMLChars(mServerConfiguration.getRecordingsPath()), ENCODING)).append("\"");
                buffer.append(" ").append(ATTRIBUTE_MEDIA_ACCESS_KEY).append("=\"").append(
                        mServerConfiguration.getMediaAccessKey()).append("\"");
                buffer.append("/>\n");

                // Plugins
                /*
                Plugin plugin = null;
                Object[] args = new Object[0];
                Object value = null;
                Iterator pluginIterator = pluginManager.getPlugins();
                while (pluginIterator.hasNext()) {
                    try {
                        plugin = (Plugin) pluginIterator.next();
                        log.debug("Plugin: " + plugin);
                        StringWriter outputWriter = new StringWriter();

                        // Create a BeanWriter which writes to our prepared stream
                        BeanWriter beanWriter = new BeanWriter(outputWriter);

                        PluginXMLIntrospector pluginXMLIntrospector = new PluginXMLIntrospector();
                        pluginXMLIntrospector.setAttributesForPrimitives(true);
                        beanWriter.setXMLIntrospector(pluginXMLIntrospector);

                        beanWriter.enablePrettyPrint();

                        // Write example bean as base element 'person'
                        beanWriter.write("plugin", plugin);

                        // Write to System.out
                        // (We could have used the empty constructor for BeanWriter
                        // but this way is more instructive)
                        buffer.append(outputWriter.toString());
                    } catch (Exception ex) {
                        Tools.logException(Configurator.class, ex, "Could not save plugin: " + plugin.getClass().getName());
                    }
                }
                */
                // TiVos
                TiVo tivo = null;
                Iterator tivoIterator = mServerConfiguration.getTiVos().iterator();
                while (tivoIterator.hasNext()) {
                    try {
                        tivo = (TiVo) tivoIterator.next();
                        log.debug("TiVo: " + tivo);
                        StringWriter outputWriter = new StringWriter();

                        // Create a BeanWriter which writes to our prepared stream
                        BeanWriter beanWriter = new BeanWriter(outputWriter);

                        PluginXMLIntrospector pluginXMLIntrospector = new PluginXMLIntrospector();
                        pluginXMLIntrospector.setAttributesForPrimitives(true);
                        beanWriter.setXMLIntrospector(pluginXMLIntrospector);

                        beanWriter.enablePrettyPrint();

                        // Write example bean as base element 'person'
                        beanWriter.write("tivo", tivo);

                        // Write to System.out
                        // (We could have used the empty constructor for BeanWriter
                        // but this way is more instructive)
                        buffer.append(outputWriter.toString());
                    } catch (Exception ex) {
                        Tools.logException(Configurator.class, ex, "Could not save tivo: " + tivo.getName());
                    }
                }                
                

                buffer.append("</").append(TAG_CONFIGURATION).append(">\n");
            }
            printWriter.print(buffer.toString());
            printWriter.close();
        } catch (Exception ex) {
            Tools.logException(Configurator.class, ex);
        }
    }

    private ServerConfiguration mServerConfiguration;
}