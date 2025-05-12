package model;

import model.abstractions.AbstractPerson;
import model.abstractions.Attendable;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Student extends AbstractPerson implements Attendable {
    private Group group;
    private Map<LocalDate, Boolean> attendance = new HashMap<>();

    public Student(String id, String name) {
        super(id, name);
    }

    @Override
    public String getRole() {
        return "Student";
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    @Override
    public void markAttendance(LocalDate date, boolean present) {
        attendance.put(date, present);
    }

    @Override
    public Boolean getAttendance(LocalDate date) {
        return attendance.get(date);
    }

    @Override
    public Map<LocalDate, Boolean> getAllAttendance() {
        return attendance;
    }
}