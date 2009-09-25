package de.pannous.xvantage.core.util.test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class Person {

    private String name;
    private Task mainTask;
    private Set<Task> tasks = new HashSet<Task>();
    private long id;

    public Person() {
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

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }
}
