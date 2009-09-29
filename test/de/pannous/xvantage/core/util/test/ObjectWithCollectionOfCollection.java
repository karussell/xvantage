package de.pannous.xvantage.core.util.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class ObjectWithCollectionOfCollection {

    private ArrayList<List<SimpleObj>> field = new ArrayList();

    public ArrayList<List<SimpleObj>> getField() {
        return field;
    }

    public void setField(ArrayList<List<SimpleObj>> field) {
        this.field = field;
    }
}
