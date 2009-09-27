package de.pannous.xvantage.core.util.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class Person {

    private String name;
    private Task mainTask;
    private List<Task> tasks = new ArrayList<Task>();
    private long id;

    public Person() {
    }

    public Person(String n, Long id) {
        setName(n);
        setId(id);
    }

    public Person(String n) {
        setName(n);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Task getMainTask() {
        return mainTask;
    }

    public void setMainTask(Task mainTask) {

        this.mainTask = mainTask;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return "name:" + getName() + " id:" + getId();
    }
}
