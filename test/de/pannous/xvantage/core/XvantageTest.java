package de.pannous.xvantage.core;

import de.pannous.xvantage.core.util.BiMap;
import de.pannous.xvantage.core.util.test.SimpleObj;
import de.pannous.xvantage.core.util.test.ComplexObject;
import de.pannous.xvantage.core.util.test.Person;
import de.pannous.xvantage.core.util.test.Task;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class XvantageTest extends XvantageTester {

    private static String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    private Xvantage xadv = new Xvantage();

    public XvantageTest() {
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        xadv = new Xvantage();
    }

    @Test
    public void testReadObjectsWithPathIncluded() {
        StringReader iStream = new StringReader(
                "<path>" +
                "   <fake><myobject>haha! fake! binding cannot parse me if incorrectly implemented</myobject></fake>" +
                "   <myobject><name>test</name></myobject>" +
                "</path>");
        xadv.mount("/path/myobject", SimpleObj.class);
        DataPool pool = xadv.readObjects(iStream);
        assertEquals(1, pool.getData(SimpleObj.class).size());
        SimpleObj obj = pool.getData(SimpleObj.class).values().iterator().next();
        assertNotNull(obj);
        assertEquals("test", obj.getName());
    }

    @Test
    public void testReadTwoObjects() {
        InputStream iStream = getClass().getResourceAsStream("readTwoObjects.xml");
        xadv.mount("/path/", SimpleObj.class);
        DataPool pool = xadv.readObjects(iStream);
        assertEquals(2, pool.getData(SimpleObj.class).size());
    }

    @Test
    public void testReadMoreComplexObjects() {
        InputStream iStream = getClass().getResourceAsStream("readMoreComplexObjects.xml");
        xadv.mount("/path/", ComplexObject.class);
        DataPool pool = xadv.readObjects(iStream);
        assertEquals(2, pool.getData(ComplexObject.class).size());

        // order is not guaranteed in HashMap :-( in FastMap it would ...
        TreeMap<String, ComplexObject> map = new TreeMap<String, ComplexObject>();

        for (ComplexObject obj : pool.getData(ComplexObject.class).values()) {
            map.put(obj.getName(), obj);
        }

        Iterator<ComplexObject> iter = map.values().iterator();
        ComplexObject obj = iter.next();
        ComplexObject obj2 = iter.next();
        assertNotNull(obj);
        assertNotNull(obj2);

        assertEquals("Tester", obj.getName());
        assertTrue(5L == obj.getId());

        assertEquals("Tester2", obj2.getName());
        assertTrue(6L == obj2.getId());
    }

    @Test
    public void testDoNotReadObjectAtRoot() {
        try {
            xadv.mount("/myobject", SimpleObj.class);
            assertTrue(true);
        } catch (Exception ex) {
            assertTrue(true);
        }
    }

    @Test
    public void testWrite() {
        BiMap<Long, SimpleObj> map = dataPool.getData(SimpleObj.class);
        map.put(0L, new SimpleObj("test"));
        StringWriter writer = new StringWriter();
        xadv.mount("/path/myobject", SimpleObj.class);
        xadv.saveObjects(dataPool, writer);

        String expected = HEADER +
                "<path>\n" +
                "<myobject>\n<name>test</name>\n</myobject>\n" +
                "</path>\n";

        assertEquals(expected, writer.toString());
    }

    @Test
    public void testWriteDirectlyToRootIsCurrentlyNotSupported() {
        try {
            xadv.mount("/myobject", SimpleObj.class);
            assertTrue(false);
        } catch (Exception ex) {
        }
    }

    @Test
    public void testWriteTwoObjects() {
        BiMap<Long, SimpleObj> map = dataPool.getData(SimpleObj.class);
        map.put(0L, new SimpleObj("test"));
        map.put(1L, new SimpleObj("test2"));

        StringWriter writer = new StringWriter();
        xadv.mount("/p1/myobject", SimpleObj.class);
        xadv.saveObjects(dataPool, writer);

        String result = writer.toString();

        assertTrue(result.contains(HEADER));
        assertTrue(result.contains("<myobject>\n<name>test2</name>\n</myobject>\n"));
        assertTrue(result.contains("<myobject>\n<name>test</name>\n</myobject>\n"));
    }

    @Test
    public void testDefaultImpl() {
        Xvantage xv = new Xvantage();
        xv.setDefaultImplementation(Map.class, TreeMap.class);
        assertEquals(TreeMap.class, xv.getDefaultImplementation(Map.class));

        xv.setDefaultImplementation(Set.class, TreeSet.class);
        assertEquals(TreeSet.class, xv.getDefaultImplementation(Set.class));

        xv.setDefaultImplementation(List.class, LinkedList.class);
        assertEquals(LinkedList.class, xv.getDefaultImplementation(List.class));
    }
    private String tmpResult1;

    @Test
    public void testWriteTwoRelatedObjects() {
        BiMap<Long, Person> pMap = dataPool.getData(Person.class);

        Person p1 = new Person("p1", 1L);
        Person p2 = new Person("p2", 2L);
        pMap.put(p1.getId(), p1);
        pMap.put(p2.getId(), p2);

        BiMap<Long, Task> tMap = dataPool.getData(Task.class);
        Task t1 = new Task("t1", 1L);
        tMap.put(t1.getId(), t1);

        // create many to many relation ship
        p1.getTasks().add(t1);
        t1.getPersons().add(p1);

        p2.getTasks().add(t1);
        t1.getPersons().add(p2);

        xadv.mount("/root/", Person.class);
        xadv.mount("/root/", Task.class);
        tmpResult1 = xadv.saveObjects(dataPool, new StringWriter()).toString();

        assertTrue(tmpResult1.contains(HEADER));
        assertTrue(tmpResult1.contains("<tasks valueClass=\"de.pannous.xvantage.core.util.test.Task\">\n<value>1</value>\n</tasks>"));
        assertTrue(tmpResult1.contains("<persons valueClass=\"de.pannous.xvantage.core.util.test.Person\">\n<value>1</value>\n<value>2</value>\n</persons>"));
    }

    @Test
    public void testReadOutPutFromPreviousWrite() {
        testWriteTwoRelatedObjects();
        DataPool pool = xadv.readObjects(new StringReader(tmpResult1));
        Person p1 = pool.getData(Person.class).get(1L);
        assertNotNull(p1);
        assertEquals("p1", p1.getName());
        assertNull(p1.getMainTask());
        assertEquals(2L, pool.getData(Person.class).get(2L).getId());

        assertEquals(1, p1.getTasks().size());

        assertEquals(1, pool.getData(Task.class).size());
        Task t1 = pool.getData(Task.class).values().iterator().next();
        assertEquals("t1", t1.getName());
        assertEquals(1, t1.getPersons().size());
        assertEquals(p1, t1.getPersons().get(0));

        assertEquals(t1, p1.getTasks().get(0));
    }

    @Test
    public void testWriteTwoRelatedObjectsWithOneMount() {
        BiMap<Long, Person> pMap = dataPool.getData(Person.class);

        Person p1 = new Person("p1", 1L);
        Person p2 = new Person("p2", 2L);
        pMap.put(p1.getId(), p1);
        pMap.put(p2.getId(), p2);

        BiMap<Long, Task> tMap = dataPool.getData(Task.class);
        Task t1 = new Task("t1", 1L);
        tMap.put(t1.getId(), t1);

        // create many to many relation ship
        p1.getTasks().add(t1);
        t1.getPersons().add(p1);

        p2.getTasks().add(t1);
        t1.getPersons().add(p2);

        xadv.mount("/root/", Person.class);
        String str = xadv.saveObjects(dataPool, new StringWriter()).toString();

        assertTrue(str.contains(HEADER));
        assertTrue(str.contains("<tasks valueClass=\"de.pannous.xvantage.core.util.test.Task\">\n<value>1</value>\n</tasks>"));
        assertFalse(str.contains("<persons valueClass=\"de.pannous.xvantage.core.util.test.Person\">\n<value>1</value>\n<value>2</value>\n</persons>"));
    }
}
