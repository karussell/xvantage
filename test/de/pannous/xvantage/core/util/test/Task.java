package de.pannous.xvantage.core.util.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class Task {

    private String name;
    private Task parentTask;
    private List<Task> subTasks = new ArrayList<Task>();
    private List<Person> persons = new ArrayList<Person>();
    private long id;

    public Task() {
    }

    public Task(String n) {
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

    public Task getParentTask() {
        return parentTask;
    }

    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
    }

    public List<Task> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<Task> subTasks) {
        this.subTasks = subTasks;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }   
}
