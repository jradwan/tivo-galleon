@echo off
REM
REM Run the Galleon GUI
REM
rem set JAVA_HOME=c:\Program Files\Java\jre1.5.0
set oldclasspath=%classpath%
set classpath=..\build
set classpath=%classpath%;..\conf
set classpath=%classpath%;..\build\lib\galleon.jar
set classpath=%classpath%;..\lib\galleon.jar
set classpath=%classpath%;..\lib\cglib-full.jar
set classpath=%classpath%;..\lib\commons-beanutils.jar
set classpath=%classpath%;..\lib\commons-betwixt.jar
set classpath=%classpath%;..\lib\commons-collections.jar
set classpath=%classpath%;..\lib\commons-digester.jar
set classpath=%classpath%;..\lib\commons-lang.jar
set classpath=%classpath%;..\lib\commons-logging.jar
set classpath=%classpath%;..\lib\commons-net.jar
set classpath=%classpath%;..\lib\commons-pool.jar
set classpath=%classpath%;..\lib\commons-dbcp.jar
set classpath=%classpath%;..\lib\derby.jar
set classpath=%classpath%;..\lib\derbynet.jar
set classpath=%classpath%;..\lib\derbytools.jar
set classpath=%classpath%;..\lib\dom4j.jar
set classpath=%classpath%;..\lib\ehcache.jar
set classpath=%classpath%;..\lib\hibernate-tools.jar
set classpath=%classpath%;..\lib\hibernate.jar
set classpath=%classpath%;..\lib\hme.jar
set classpath=%classpath%;..\lib\jdai.jar
set classpath=%classpath%;..\lib\jdbc2_0-stdext.jar
set classpath=%classpath%;..\lib\jdom.jar
set classpath=%classpath%;..\lib\jl.jar
set classpath=%classpath%;..\lib\js.jar
set classpath=%classpath%;..\lib\jshortcut.dll
set classpath=%classpath%;..\lib\jshortcut.jar
set classpath=%classpath%;..\lib\jta.jar
set classpath=%classpath%;..\lib\log4j.jar
set classpath=%classpath%;..\lib\mp3spi.jar
set classpath=%classpath%;..\lib\ocutil-1.1.4.jar
set classpath=%classpath%;..\lib\odmg.jar
set classpath=%classpath%;..\lib\pja.jar
set classpath=%classpath%;..\lib\silib.jar
set classpath=%classpath%;..\lib\simulator.jar
set classpath=%classpath%;..\lib\tritonus_share.jar
set classpath=%classpath%;..\lib\velocity.jar
set classpath=%classpath%;..\lib\wrapper.jar
set classpath=%classpath%;..\lib\xbean.jar
set classpath=%classpath%;..\lib\xercesImpl.jar
set classpath=%classpath%;..\lib\xml-apis.jar
set classpath=%classpath%;..\lib\jampal.jar
set classpath=%classpath%;..\lib\concurrent.jar
set classpath=%classpath%;..\lib\mp3dings.jar
set classpath=%classpath%;..\lib\bananas.jar
set classpath=%classpath%;..\lib\commons-httpclient.jar
set classpath=%classpath%;..\lib\forms.jar
set classpath=%classpath%;..\lib\looks.jar
java -Xms32m -Xmx32m org.lnicholls.galleon.gui.Galleon
set classpath=%oldclasspath%