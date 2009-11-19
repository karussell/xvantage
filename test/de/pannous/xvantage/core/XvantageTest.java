package de.pannous.xvantage.core;

import de.pannous.xvantage.core.parsing.Parsing;
import de.pannous.xvantage.core.util.Helper;
import de.pannous.xvantage.core.util.test.SimpleObj;
import de.pannous.xvantage.core.util.test.ComplexObject;
import de.pannous.xvantage.core.util.test.ConstraintTF;
import de.pannous.xvantage.core.util.test.EventTF;
import de.pannous.xvantage.core.util.test.Person;
import de.pannous.xvantage.core.util.test.Task;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static org.junit.Assert.*;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class XvantageTest extends XvantageTester {

    private static String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    private Xvantage xvantage = new Xvantage();

    public XvantageTest() {
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        xvantage = new Xvantage();
    }

    @Test
    public void testReadObjectsWithPathIncluded() {
        StringReader iStream = new StringReader(
                "<path>" +
                "   <fake><myobject>haha! fake! binding cannot parse me if incorrectly implemented</myobject></fake>" +
                "   <myobject><name>test</name></myobject>" +
                "</path>");
        xvantage.mount("/path/myobject", SimpleObj.class);
        DataPool pool = xvantage.readObjects(iStream);
        assertEquals(1, pool.getData(SimpleObj.class).size());
        SimpleObj obj = pool.getData(SimpleObj.class).values().iterator().next();
        assertNotNull(obj);
        assertEquals("test", obj.getName());
    }

    @Test
    public void testReadTwoObjects() {
        InputStream iStream = getClass().getResourceAsStream("readTwoObjects.xml");
        xvantage.mount("/path/", SimpleObj.class);
        DataPool pool = xvantage.readObjects(iStream);
        assertEquals(2, pool.getData(SimpleObj.class).size());
    }

    @Test
    public void testReadTwoObjectsEscaped() {
        InputStream iStream = getClass().getResourceAsStream("readTwoObjectsEscaped.xml");
        xvantage.mount("/path/", SimpleObj.class);
        DataPool pool = xvantage.readObjects(iStream);
        assertEquals(2, pool.getData(SimpleObj.class).size());
        assertEquals("bay<reuth", pool.getData(SimpleObj.class).get(1L).getName());
        assertEquals("Tester&1", pool.getData(SimpleObj.class).get(0L).getName());
    }

    @Test
    public void testReadMoreComplexObjects() {
        InputStream iStream = getClass().getResourceAsStream("readMoreComplexObjects.xml");
        xvantage.mount("/path/", ComplexObject.class);
        DataPool pool = xvantage.readObjects(iStream);
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
            xvantage.mount("/myobject", SimpleObj.class);
            assertTrue(true);
        } catch (Exception ex) {
            assertTrue(true);
        }
    }

    @Test
    public void testWrite() {
        Map<Long, SimpleObj> map = dataPool.getData(SimpleObj.class);
        map.put(0L, new SimpleObj("test"));
        StringWriter writer = new StringWriter();
        xvantage.mount("/path/myobject", SimpleObj.class);
        xvantage.saveObjects(writer, dataPool);
//        System.out.println(writer.toString());
        String expected = HEADER +
                "<path>\n" +
                "<myobject id=\"0\">\n<name>test</name>\n</myobject>\n" +
                "</path>\n";

        assertEquals(expected, writer.toString());
    }

    @Test
    public void testWriteDirectlyToRootIsCurrentlyNotSupported() {
        try {
            xvantage.mount("/myobject", SimpleObj.class);
            assertTrue(false);
        } catch (Exception ex) {
        }
    }

    @Test
    public void testWriteTwoObjects() {
        Map<Long, SimpleObj> map = dataPool.getData(SimpleObj.class);
        map.put(0L, new SimpleObj("test"));
        map.put(1L, new SimpleObj("test2"));

        StringWriter writer = new StringWriter();
        xvantage.mount("/p1/myobject", SimpleObj.class);
        xvantage.saveObjects(writer, dataPool);

        String result = writer.toString();

        assertTrue(result.contains(HEADER));
        assertTrue(result.contains("<myobject id=\"1\">\n<name>test2</name>\n</myobject>\n"));
        assertTrue(result.contains("<myobject id=\"0\">\n<name>test</name>\n</myobject>\n"));
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
        Map<Long, Person> pMap = dataPool.getData(Person.class);

        Person p1 = new Person("p1", 1L);
        Person p2 = new Person("p2", 2L);
        pMap.put(p1.getId(), p1);
        pMap.put(p2.getId(), p2);

        Map<Long, Task> tMap = dataPool.getData(Task.class);
        Task t1 = new Task("t1", 1L);
        tMap.put(t1.getId(), t1);

        // create many to many relation ship
        p1.getTasks().add(t1);
        t1.getPersons().add(p1);

        p2.getTasks().add(t1);
        t1.getPersons().add(p2);

        xvantage.mount("/root/", Person.class);
        xvantage.mount("/root/", Task.class);
        tmpResult1 = xvantage.saveObjects(new StringWriter(), dataPool).toString();

        assertTrue(tmpResult1.contains(HEADER));
        assertTrue(tmpResult1.contains("<person id=\"2\">"));
        assertTrue(tmpResult1.contains("<person id=\"1\">"));
        assertTrue(tmpResult1.contains("<tasks jc=\"ArrayList\" valueClass=\"task\">\n<value>1</value>\n</tasks>"));
        assertTrue(tmpResult1.contains("<persons jc=\"ArrayList\" valueClass=\"person\">\n<value>1</value>\n<value>2</value>\n</persons>"));
    }

    @Test
    public void testReadOutPutFromPreviousWrite() {
        testWriteTwoRelatedObjects();

        DataPool pool = xvantage.readObjects(new StringReader(tmpResult1));
        Person p1 = pool.getData(Person.class).get(1L);
        assertNotNull(p1);
        assertEquals("p1", p1.getName());
        assertNull(p1.getMainTask());
        assertEquals(2L, pool.getData(Person.class).get(2L).getId());

        assertEquals(1, p1.getTasks().size());

        assertEquals(1, pool.getData(Task.class).size());
        Task t1 = pool.getData(Task.class).values().iterator().next();
        assertEquals("t1", t1.getName());
        assertEquals(2, t1.getPersons().size());
        assertEquals(p1, t1.getPersons().get(0));

        assertEquals(t1, p1.getTasks().get(0));
    }

    @Test
    public void testWriteTwoRelatedObjectsWithOneMount() {
        Map<Long, Person> pMap = dataPool.getData(Person.class);

        Person p1 = new Person("p1", 1L);
        Person p2 = new Person("p2", 2L);
        pMap.put(p1.getId(), p1);
        pMap.put(p2.getId(), p2);

        Map<Long, Task> tMap = dataPool.getData(Task.class);
        Task t1 = new Task("t1", 1L);
        tMap.put(t1.getId(), t1);

        // create many to many relation ship
        p1.getTasks().add(t1);
        t1.getPersons().add(p1);

        p2.getTasks().add(t1);
        t1.getPersons().add(p2);

        xvantage.mount("/root/", Person.class);
        String str = xvantage.saveObjects(new StringWriter(), dataPool).toString();

        assertTrue(str.contains(HEADER));
        assertTrue(str.contains("<tasks jc=\"ArrayList\" valueClass=\"de.pannous.xvantage.core.util.test.Task\">\n<value>1</value>\n</tasks>"));
        assertFalse(str.contains("<persons jc=\"ArrayList\" valueClass=\"de.pannous.xvantage.core.util.test.Person\">\n<value>1</value>\n<value>2</value>\n</persons>"));
    }

    @Test
    public void testWriteHeavyReferenced() throws Exception {
        Task t1 = new Task("t1", 1L);
        Task t2 = new Task("t2", 2L);
        Task t3 = new Task("t3", 3L);
        Task t4 = new Task("t4", 4L);

        t1.getSubTasks().add(t3);
        t1.getSubTasks().add(t2);
        t1.setParentTask(t4);

        Map<Long, Task> tasks = dataPool.getData(Task.class);
        tasks.put(t1.getId(), t1);
        tasks.put(t2.getId(), t2);
        tasks.put(t3.getId(), t3);
        tasks.put(t4.getId(), t4);

        xvantage.mount("/t/", Task.class);

        tmpResult1 = xvantage.saveObjects(new StringWriter(), dataPool).toString();

        assertEquals(4, Helper.countPattern(tmpResult1, "<task id=\""));
        assertEquals(1, Helper.countPattern(tmpResult1, "<parentTask>4</parentTask>"));
        assertEquals(1, Helper.countPattern(tmpResult1, "<value>3</value>"));
        assertEquals(1, Helper.countPattern(tmpResult1, "<value>2</value>"));
    }

    @Test
    public void testReadHeavyReferenced() throws Exception {
        testWriteHeavyReferenced();
        DataPool pool = xvantage.readObjects(new StringReader(tmpResult1));

        Map<Long, Task> map = pool.getData(Task.class);
        Task t1 = map.get(1L);
        Task t2 = map.get(2L);
        Task t3 = map.get(3L);
        Task t4 = map.get(4L);

        assertEquals(t4, t1.getParentTask());
        assertEquals(2, t1.getSubTasks().size());
        assertTrue(t1.getSubTasks().contains(t2));
        assertTrue(t1.getSubTasks().contains(t3));
    }

    @Test
    public void testReadWithAlreadyExistingTask() throws Exception {
        Task t1 = new Task("t1", 1L);
        Map<Long, Task> tasks = dataPool.getData(Task.class);
        tasks.put(t1.getId(), t1);
        xvantage.mount("/t/", Task.class);
        DataPool pool = xvantage.readObjects(getClass().getResourceAsStream("readTasksWithExisting.xml"), dataPool);
        Map<Long, Task> map = pool.getData(Task.class);

        assertEquals(4, map.size());
        assertEquals(t1, map.get(1L));
    }

    @Test
    public void testReadWithAlreadyExistingTask_XmlDefinitionWillOverwriteExisting() throws Exception {
        testWriteHeavyReferenced();

        Task t1 = new Task("t1old", 1L);
        Map<Long, Task> tasks = dataPool.getData(Task.class);
        assertEquals("t1", tasks.get(1L).getName());
        tasks.put(t1.getId(), t1);
        assertEquals("t1old", tasks.get(1L).getName());
        assertEquals(4, tasks.size());

        // finally read ..
        DataPool pool = xvantage.readObjects(new StringReader(tmpResult1), dataPool);
        Map<Long, Task> map = pool.getData(Task.class);

        assertEquals("Should create new task if present in xml", 4, map.size());
        assertEquals("reference stays", t1, map.get(1L));
        assertEquals("but properties will be overwritten", "t1", map.get(1L).getName());
    }

    @Test
    public void testCustomParsing() throws Exception {
        InputStream iStream = getClass().getResourceAsStream("complexFromTimeFinder.xml");

        xvantage.putParsing(ConstraintTF.class, new Parsing() {

            public Object parse(Node node) {
                float w = 0f;
                EventTF e = null;
                NodeList list = node.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    Node subNode = list.item(i);
                    if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                        if ("weight".equals(subNode.getNodeName()))
                            w = (Float) parsing.parseObjectAsProperty(Float.class, subNode);

                        if ("event".equals(subNode.getNodeName()))
                            e = (EventTF) parsing.parseObjectAsProperty(EventTF.class, subNode);
                    }
                }

                return new ConstraintTF(w, e);
            }
        });
        xvantage.mount("/root/events/event", EventTF.class);
        DataPool tmpPool = xvantage.readObjects(iStream, dataPool);

        Map<Long, EventTF> events = tmpPool.getData(EventTF.class);

        EventTF ev1 = events.get(1L);
        EventTF ev2 = events.get(2L);
        assertEquals("test1", ev1.getName());
        assertEquals("test2", ev2.getName());
        assertEquals(1, ev1.getConstraints().size());
        assertEquals(1, ev2.getConstraints().size());

        Iterator<ConstraintTF> iter = ev1.getConstraints().iterator();
        ConstraintTF ec = iter.next();
        assertTrue("original and reference should be the same!", ev1 == ec.getEvent());
        assertEquals(1, ec.getWeight(), 0.003f);
        assertEquals("test1", ec.getEvent().getName());

        ec = ev2.getConstraints().iterator().next();
        assertTrue(ev2 == ec.getEvent());
        assertEquals(2, ec.getWeight(), 0.003);
        assertEquals("test2", ec.getEvent().getName());
    }

    @Test
    public void testMapWriting() throws Exception {
        xvantage.mount("/root/map", HashMap.class);
        HashMap dataMap = new HashMap();
        dataMap.put("x", 1);
        dataMap.put("y", -1);

        Map subMap = new HashMap();
        subMap.put(1, "1");
        subMap.put(2, "2");

        dataMap.put("z", subMap);
        dataPool.getData(HashMap.class).put(0L, dataMap);
        String str = xvantage.saveObjects(new StringWriter(), dataPool).toString();

        assertTrue(str.contains("<map id=\"0\" keyClass=\"String\" valueClass=\"map\">"));

        assertTrue(str.contains("<value keyClass=\"Integer\" valueClass=\"String\">"));

        assertTrue(str.contains("<entry>\n<key>y</key>\n<value jc=\"Integer\">-1</value>\n</entry>"));
        assertTrue(str.contains("<entry>\n<key>x</key>\n<value jc=\"Integer\">1</value>\n</entry>"));

        assertTrue(str.contains("<entry>\n<key>1</key>\n<value>1</value>\n</entry>"));
    }

    @Test
    public void testMapParsing() throws Exception {
        xvantage.mount("/root/map", HashMap.class);

        InputStream iStream = getClass().getResourceAsStream("readMapOfMap.xml");
        dataPool = xvantage.readObjects(iStream, dataPool);

        HashMap map = dataPool.getData(HashMap.class).get(0L);

        assertEquals(1, map.get("x"));
        assertEquals(-1, map.get("y"));
        assertEquals("1", ((Map) map.get("z")).get(1));
        assertEquals("2", ((Map) map.get("z")).get(2));
    }
}
