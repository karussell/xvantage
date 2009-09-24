Welcome to the xvantage project!
Any feedback to peathal@yaaahoooo.com (some a and o too much ;-))

Give it a try and add your test case :-) !

  Reading
===============

StringReader iStream = new StringReader(
    "<path>" +
    "   <myobject><name>test</name></myobject>" +
    "</path>");

// mount to /path/ with an alternative name 'myobject' instead of the default (simpleObj)
xadv.mount("/path/myobject", SimpleObj.class);
// now get the objects from the interface DataPool
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
// now look into writer.toString for created the xml

  Disadvantages
=================

a lot, because it is a young idea ;-)
 * not so powerful like JAXB and all the others,
    see this post for more information http://karussell.wordpress.com/2009/09/03/xml-serializers-for-java/
 * not so fast
 * not so stable like all the others

  Advantages
==============

 * easy xml binding
 * small library without dependencies
 * no checked exceptions
 * you could read from multiple files
 * you can write to multiple files
 * well tested
 * compared to xstream, you have additional mount calls, but the resulting xml could be checked via xsd!
    (to create your xsd from the resulting xml see this post http://karussell.wordpress.com/2009/09/14/xml-to-xsd-schema-xml2xsd/)
 * compared to JAXB, you don't *need* an xsd
 * compared to apache digester, you can read and *write* and you don't need to be so specific (only one 'mount' call with xvantage)

  Use cases
=============

I will use it in my timefinder.de project to save/read objects to/from xml
... so xml as one additional datasource.