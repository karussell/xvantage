  Welcome to the xvantage project!
====================================

Please give your feedback to peathal@yaaahoooo.de (some a and o too much ;-))
Or see this blog entry to comment:
http://karussell.wordpress.com/2009/09/29/xvantage-yet-another-xml-serializer/

Xvantage is yet another xml serializer and deserializer without much configuration
hassle. It is well suited if you have java objects and want to serialize them
into xml and read this xml back. Writing and reading is possible without
regenerating your xsd files every time you change your classes.
See more advantages in the "Advantage" section below.

Now just look at the code snippets and give xvantage a try!
+ Clone this repository and add your test case to improve the stability!

For an up to date example please look into the source at test/de.pannous.xvantage.core.XvantageTest.java


  Writing
===========

// Create a data pool with all your objects you want to serialize
DataPool pool = new DefaultDataPool();
Map<Long, SimpleObj> map = pool.getData(SimpleObj.class);
map.put(0L, new SimpleObj("test"));
StringWriter writer = new StringWriter();
xadv.mount("/path/", SimpleObj.class);
xadv.saveObjects(pool, writer);
// Now look into writer.toString() for the created xml, which should look like
// the xml above in the iStream declaration


  Reading
===========

// get xml from somewhere
StringReader iStream = new StringReader(
    "<path>" +
    "   <myobject><name>test</name></myobject>" +
    "</path>");
// mount to /path/ with an alternative name 'myobject' instead of the default which would be simpleObj
// this is the preferred way for mounting, because otherwise class refactoring results in different xml
xadv.mount("/path/myobject", SimpleObj.class);
DataPool pool = xadv.readObjects(iStream);
SimpleObj obj = pool.getData(SimpleObj.class).values().iterator().next();
assertEquals("test", obj.getName());


  Disadvantages
=================

there are a lot, because it is a young idea ;-)

 * not so powerful and configurable like JAXB and all the others,
   see this post for more information http://karussell.wordpress.com/2009/09/03/xml-serializers-for-java/
 * maybe not so fast
 * maybe not so stable like all the others
 * not thread save, you have to use multiple instances of Xvantage
 * no arg constructor (at least private), getter and setters are necessary


  Advantages
==============

There are X, because of the X in xvantage ;-)

 * easy xml (de-)serialization (is not really about binding ...)
 * small library <50KB without dependencies (Sept 2009 => even 35KB)
 * allowed cross references!
   even between documents (you could read/write from/to multiple files)
 * well tested
 * no checked exceptions
 * xml could be checked via xsd (but no must)
 * no license and free source code (public domain!)


  License
===========

no license and free source code aka as public domain, see http://en.wikipedia.org/wiki/Public_domain


  Xvantage compared to ...
============================

 * compared to xstream or XmlEncode, you have additional mount calls in xvantage,
   but the resulting xml could be checked via xsd!
   (to create your xsd from the resulting xml see this post http://karussell.wordpress.com/2009/09/14/xml-to-xsd-schema-xml2xsd/)
   And cross reference is easy with xvantage and difficult (if not impossible) with xstream
 * compared to JAXB, you don't *need* an xsd, but you can use one (if your class design is stable you should)
   At the moment you cannot change how the xml should look like (so xvantage is more an xml-serializer than a binder ...)
 * compared to apache digester, you can read and *write* and you don't have so much
   java configuration hassle (only one 'mount' call with xvantage)


  Use cases
=============

I will use it in my timefinder.de project to save/read objects to/from xml
... so xml as one additional datasource.


  Hints
=========

 * the default implementations of a list, collection and set
   could be replaced through its methods in Xadvantage
 * to avoid writing of null values set this through Xadvantage
 * to avoid writing of some properties -> Xadvantage.ignoreMethod
   (this will be replaced with the transient keyword at a later time)
