package de.pannous.xvantage.core.util;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class BiMapTest {

    public BiMapTest() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testPut() {
        BiMap<Long, String> map = new BiMap<Long, String>();
        map.put(1L, "1");
        map.put(1L, "1old");
        assertEquals(1, map.size());

        map.put(2L, "1old");
        assertEquals(1, map.size());

        map.put(3L, "1new");
        assertEquals(2, map.size());
    }
}
