  Welcome to the xvantage project!
====================================

Please give your feedback to peathal@yaaahoooo.com (some a and o too much ;-))

Xvantage is yet another xml serializer and deserializer without much configuration
hassle. It is well suited if you have java objects and want to serialize them
into xml. This is possible without regenerating your xsd files on every class
change. See more advantages in the "Advantage" section below.

Now just look at the code snippets and give xvantage a try!
+ Clone this repository and add your test case to improve the stability!

  Reading
===============

StringReader iStream = new StringReader(
    "<path>" +
    "   <myobject><name>test</name></myobject>" +
    "</path>");

// mount to /path/ with an alternative name 'myobject' instead of the default (simpleObj)
xadv.mount("/path/myobject", SimpleObj.class);
// now get the objects from the DataPool
DataPool pool = xadv.readObjects(iStream);
SimpleObj obj = pool.getData(SimpleObj.class).values().iterator().next();
assertEquals("test", obj.getName());

  Writing
===============

// create a data pool with your objects
DataPool pool = new DefaultDataPool();
Map<Long, SimpleObj> map = pool.getData(SimpleObj.class);
map.put(0L, new SimpleObj("test"));
StringWriter writer = new StringWriter();
// mount to /path/
xadv.mount("/path/", SimpleObj.class);
xadv.saveObjects(pool, writer);
// now look into writer.toString() for the created xml, which should look like
// the xml above in the iStream declaration

  Disadvantages
=================

there are a lot, because it is a young idea ;-)

 * not so powerful like JAXB and all the others,
    see this post for more information http://karussell.wordpress.com/2009/09/03/xml-serializers-for-java/
 * maybe not so fast
 * maybe not so stable like all the others
 * is not thread save, you have to use multiple instances of the Xvantage class

  Advantages
==============

There are X, because of the X in xvantage ;-)

 * easy xml (de-)serialization (is not really about binding ...)
 * small library (<50KB) without dependencies
 * allowed cross references!
   even between documents (you could read/write from/to multiple files)
 * well tested
 * no checked exceptions
 * xml could be checked via xsd (but no must)

  Xvantage compared to ...
==============================

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

 * the default implementation of the DataPool (an interface) can be easily replaced:
   new Xvantage().setDefaultDataPool(MyDataPool.class);
   (factories are currently not supported)
 * A BiMap in DataPool was necessary because of getting an id while writing an
   object to string
 * the default implementations of a list, collection and set could be replaced
   through its methods in the Xadvantage class
