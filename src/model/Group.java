package model;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String name;
    private List<Student> students = new ArrayList<>();

    public Group(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public List<Student> getStudents() { return students; }

    public void addStudent(Student s) {
        students.add(s);
        s.setGroup(this);
    }

    public void removeStudent(Student s) {
        students.remove(s);
        s.setGroup(null);
    }
}