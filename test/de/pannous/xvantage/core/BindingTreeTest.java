package de.pannous.xvantage.core;

import de.pannous.xvantage.core.BindingTree;
import de.pannous.xvantage.core.Binding;
import de.pannous.xvantage.core.util.test.SimpleObj;
import de.pannous.xvantage.core.util.test.TesterMain;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class BindingTreeTest extends TesterMain {

    public BindingTreeTest() {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testMount() {
        BindingTree tree = new BindingTree();
        assertEquals(0, tree.getMaxLevel());
        try {
            // mounting directly into root is currently unsupported
            tree.mount(new Binding(objectParser, "/root1", SimpleObj.class));
            assertTrue(false);
        } catch (Exception ex) {
        }

        tree.mount(new Binding(objectParser, "/root1/path/el1", Float.class));
        assertEquals(3, tree.getMaxLevel());
        assertEquals("root1", tree.getRoot().getName());
        assertEquals(1, tree.getRoot().getChilds().size());
        assertEquals("path", tree.getRoot().getChildsMap().get("path").getName());

        tree.mount(new Binding(objectParser, "/root1/path2/el2", Double.class));
        assertEquals(3, tree.getMaxLevel());
        assertEquals(2, tree.getRoot().getChilds().size());
        assertEquals("path2", tree.getRoot().getChildsMap().get("path2").getName());

        tree.mount(new Binding(objectParser, "/root1/path2/el2/test", Integer.class));
        assertEquals(4, tree.getMaxLevel());
        assertEquals(2, tree.getRoot().getChilds().size());
        assertEquals(1, tree.getRoot().getChildsMap().get("path2").getChilds().size());
        assertEquals("el2", tree.getRoot().getChildsMap().get("path2").getChildsMap().get("el2").getName());

        try {
            tree.mount(new Binding(objectParser, "/root2", Long.class));
            assertTrue(false);
        } catch (Exception ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
        }
    }
}
