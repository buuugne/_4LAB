package service.interfaces;

import model.Group;
import model.Student;

import java.util.List;

public interface IStudentService {
    /**
     * Prideda naują grupę
     * @param groupName Grupės pavadinimas
     */
    void addGroup(String groupName);

    /**
     * Gauna grupę pagal pavadinimą
     * @param groupName Grupės pavadinimas
     * @return Grupė arba null, jei tokia neegzistuoja
     */
    Group getGroup(String groupName);

    /**
     * Prideda studentą į nurodytą grupę
     * @param groupName Grupės pavadinimas
     * @param student Studentas
     */
    void addStudentToGroup(String groupName, Student student);

    /**
     * Grąžina visas grupes
     * @return Grupių sąrašas
     */
    List<Group> getAllGroups();
}