package service;

import model.Group;
import model.Student;
import service.interfaces.IStudentService;

import java.util.ArrayList;
import java.util.List;

public class StudentService implements IStudentService {
    private List<Group> groups = new ArrayList<>();

    @Override
    public void addGroup(String groupName) {
        // Tikrina, ar grupė jau egzistuoja
        if (getGroup(groupName) == null) {
            groups.add(new Group(groupName));
        }
    }

    @Override
    public Group getGroup(String groupName) {
        return groups.stream()
                .filter(g -> g.getName().equals(groupName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addStudentToGroup(String groupName, Student student) {
        Group group = getGroup(groupName);
        if (group == null) {
            group = new Group(groupName);
            groups.add(group);
        }
        group.addStudent(student);
    }

    @Override
    public List<Group> getAllGroups() {
        return groups;
    }
}