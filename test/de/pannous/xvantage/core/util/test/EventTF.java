package de.pannous.xvantage.core.util.test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class EventTF {

    private String name;
    private Collection<ConstraintTF> constraints = new ArrayList<ConstraintTF>();

    public EventTF(String name) {
        this.name = name;
    }

    public EventTF() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<ConstraintTF> getConstraints() {
        return constraints;
    }

    public void setConstraints(Collection<ConstraintTF> constraints) {
        this.constraints = constraints;
    }
}
