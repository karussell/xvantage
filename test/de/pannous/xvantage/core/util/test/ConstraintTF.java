package de.pannous.xvantage.core.util.test;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class ConstraintTF {

    private float weight;
    private EventTF event;

    public ConstraintTF(float weight, EventTF event) {
        this.weight = weight;
        this.event = event;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public EventTF getEvent() {
        return event;
    }

    public void setEvent(EventTF event) {
        this.event = event;
    }
}
