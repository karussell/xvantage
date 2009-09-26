package de.pannous.xvantage.core;

import de.pannous.xvantage.core.util.test.SimpleObj;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class BindingTreeTest extends XvantageTester {

    public BindingTreeTest() {
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testMount() {        
        assertEquals(0, bindingTree.getMaxLevel());
        try {
            // mounting directly into root is currently unsupported
            bindingTree.mount(newBinding("/root1", SimpleObj.class));
            assertTrue(false);
        } catch (Exception ex) {
        }

        bindingTree.mount(newBinding("/root1/path/el1", Float.class));
        assertEquals(3, bindingTree.getMaxLevel());
        assertEquals("root1", bindingTree.getRoot().getName());
        assertEquals(1, bindingTree.getRoot().getChilds().size());
        assertEquals("path", bindingTree.getRoot().getChildsMap().get("path").getName());

        bindingTree.mount(newBinding("/root1/path2/el2", Double.class));
        assertEquals(3, bindingTree.getMaxLevel());
        assertEquals(2, bindingTree.getRoot().getChilds().size());
        assertEquals("path2", bindingTree.getRoot().getChildsMap().get("path2").getName());

        bindingTree.mount(newBinding("/root1/path2/el2/test", Integer.class));
        assertEquals(4, bindingTree.getMaxLevel());
        assertEquals(2, bindingTree.getRoot().getChilds().size());
        assertEquals(1, bindingTree.getRoot().getChildsMap().get("path2").getChilds().size());
        assertEquals("el2", bindingTree.getRoot().getChildsMap().get("path2").getChildsMap().get("el2").getName());

        try {
            bindingTree.mount(newBinding("/root2", Long.class));
            assertTrue(false);
        } catch (Exception ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
        }
    }
}
