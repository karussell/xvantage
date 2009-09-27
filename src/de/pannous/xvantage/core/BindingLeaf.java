package de.pannous.xvantage.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A leaf in the DOM tree can have xml children and mounted bindings.
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class BindingLeaf {

    private String name;
    private BindingLeaf parent;
    private Map<String, BindingLeaf> childs;
    private Map<String, Binding> mountedObjects;

    public BindingLeaf(String name) {
        this.name = name;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public void setParent(BindingLeaf parent) {
        this.parent = parent;
        parent.getChildsMap().put(getName(), this);
    }

    /**
     * @return the parent of this leaf, null if no such exists
     */
    public BindingLeaf getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public Collection<BindingLeaf> getChilds() {
        return getChildsMap().values();
    }

    public Map<String, BindingLeaf> getChildsMap() {
        if (childs == null)
            childs = new HashMap<String, BindingLeaf>();

        return childs;
    }

    public void mount(Binding bind) {
        getMountedBindingsMap().put(bind.getElementName(), bind);
    }

    public Collection<Binding> getMountedBindings() {
        return getMountedBindingsMap().values();
    }

    public Map<String, Binding> getMountedBindingsMap() {
        if (mountedObjects == null)
            mountedObjects = new HashMap<String, Binding>();

        return mountedObjects;
    }

    @Override
    public String toString() {
        return getName() + " childs:" + getChilds().size() + " mounted:" + getMountedBindings().size();
    }
}
