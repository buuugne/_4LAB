package gui;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Student;
import model.Group;
import service.StudentService;

import java.io.*;
import java.time.LocalDate;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class Main extends Application {
    private StudentService studentService = new StudentService();
    private ObservableList<Student> studentObservableList = FXCollections.observableArrayList();
    private TableView<Student> studentTable = new TableView<>();
    private ComboBox<String> groupComboBox = new ComboBox<>();
    private ComboBox<String> filterGroupComboBox = new ComboBox<>();
    private DatePicker attendanceDatePicker = new DatePicker(LocalDate.now());
    private DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7));
    private DatePicker toDatePicker = new DatePicker(LocalDate.now());
    private CheckBox showOnlyMarkedCheckbox = new CheckBox("Rodyti tik pažymėtus");
    private TextArea reportArea = new TextArea();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Studentų Registracijos Sistema");

        TableColumn<Student, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));

        TableColumn<Student, String> nameCol = new TableColumn<>("Vardas");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<Student, String> groupCol = new TableColumn<>("Grupė");
        groupCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getGroup() != null ? data.getValue().getGroup().getName() : ""));

        TableColumn<Student, String> attendanceCol = new TableColumn<>("Lankomumas");
        attendanceCol.setCellValueFactory(data -> {
            Boolean attended = data.getValue().getAttendance(attendanceDatePicker.getValue());
            return new SimpleStringProperty(attended == null ? "-" : (attended ? "✔" : "✘"));
        });

        studentTable.getColumns().addAll(idCol, nameCol, groupCol, attendanceCol);
        studentTable.setItems(studentObservableList);

        TextField idField = new TextField();
        idField.setPromptText("ID");
        TextField nameField = new TextField();
        nameField.setPromptText("Vardas");

        groupComboBox.setPromptText("Pasirink grupę");
        refreshGroupComboBoxes();

        Button addButton = new Button("Pridėti");
        addButton.setOnAction(e -> {
            String id = idField.getText();
            String name = nameField.getText();
            String groupName = groupComboBox.getValue();
            if (!id.isEmpty() && !name.isEmpty() && groupName != null) {
                Student student = new Student(id, name);
                studentService.addStudentToGroup(groupName, student);
                idField.clear();
                nameField.clear();
                groupComboBox.setValue(null);
                refreshStudentTable();
            }
        });

        TextField newGroupField = new TextField();
        newGroupField.setPromptText("Nauja grupė");
        Button addGroupButton = new Button("Sukurti grupę");
        addGroupButton.setOnAction(e -> {
            String groupName = newGroupField.getText();
            if (!groupName.isEmpty()) {
                studentService.addGroup(groupName);
                newGroupField.clear();
                refreshGroupComboBoxes();
            }
        });

        Button markPresent = new Button("✔ Pažymėti kaip dalyvavusį");
        markPresent.setOnAction(e -> markAttendance(true));

        Button markAbsent = new Button("✘ Pažymėti kaip nedalyvavusį");
        markAbsent.setOnAction(e -> markAttendance(false));

        filterGroupComboBox.setPromptText("Filtruoti pagal grupę");
        filterGroupComboBox.setOnAction(e -> refreshStudentTable());

        showOnlyMarkedCheckbox.setOnAction(e -> refreshStudentTable());
        attendanceDatePicker.setOnAction(e -> refreshStudentTable());

        Button generateReport = new Button("Generuoti ataskaitą");
        generateReport.setOnAction(e -> generateReport());

        Button exportPDF = new Button("📄 Eksportuoti į PDF");
        exportPDF.setOnAction(e -> exportToPDF(primaryStage));

        Button exportCSV = new Button("📥 Eksportuoti į CSV");
        exportCSV.setOnAction(e -> exportToCSV(primaryStage));

        Button importCSV = new Button("📤 Importuoti iš CSV");
        importCSV.setOnAction(e -> importFromCSV(primaryStage));

        Button exportExcel = new Button("📄 Eksportuoti į Excel");
        exportExcel.setOnAction(e -> exportToExcel(primaryStage));

        Button importExcel = new Button("📤 Importuoti iš Excel");
        importExcel.setOnAction(e -> importFromExcel(primaryStage));


        HBox inputBox = new HBox(10, idField, nameField, groupComboBox, addButton);
        HBox groupBox = new HBox(10, newGroupField, addGroupButton);
        HBox attendanceBox = new HBox(10, attendanceDatePicker, markPresent, markAbsent);
        HBox filterBox = new HBox(10, new Label("Filtras:"), filterGroupComboBox, showOnlyMarkedCheckbox);
        HBox reportBox = new HBox(10, new Label("Nuo:"), fromDatePicker, new Label("Iki:"), toDatePicker, generateReport, exportPDF, exportCSV, importCSV, exportExcel, importExcel);

        VBox topBox = new VBox(10, inputBox, groupBox, attendanceBox, filterBox, reportBox);

        reportArea.setEditable(false);
        reportArea.setPrefRowCount(10);

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(studentTable);
        root.setBottom(reportArea);

        Scene scene = new Scene(root, 1100, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
        refreshStudentTable();
    }

    private void refreshGroupComboBoxes() {
        ObservableList<String> groups = FXCollections.observableArrayList();
        for (Group g : studentService.getAllGroups()) {
            groups.add(g.getName());
        }
        groupComboBox.setItems(groups);
        filterGroupComboBox.setItems(FXCollections.observableArrayList(groups));
    }

    private void refreshStudentTable() {
        studentObservableList.clear();
        String filter = filterGroupComboBox.getValue();
        LocalDate date = attendanceDatePicker.getValue();

        for (Group g : studentService.getAllGroups()) {
            if (filter == null || filter.equals(g.getName())) {
                for (Student s : g.getStudents()) {
                    Boolean attended = s.getAttendance(date);
                    if (!showOnlyMarkedCheckbox.isSelected() || attended != null) {
                        studentObservableList.add(s);
                    }
                }
            }
        }
    }

    private void markAttendance(boolean present) {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.markAttendance(attendanceDatePicker.getValue(), present);
            refreshStudentTable();
        }
    }

    private void generateReport() {
        StringBuilder report = new StringBuilder();
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        String filter = filterGroupComboBox.getValue();

        for (Group g : studentService.getAllGroups()) {
            if (filter == null || filter.equals(g.getName())) {
                report.append("\nGrupė: ").append(g.getName()).append("\n");
                for (Student s : g.getStudents()) {
                    report.append("• ").append(s.getName()).append(" (ID: ").append(s.getId()).append(")\n");
                    for (Map.Entry<LocalDate, Boolean> entry : s.getAllAttendance().entrySet()) {
                        LocalDate date = entry.getKey();
                        if (!date.isBefore(from) && !date.isAfter(to)) {
                            String status = entry.getValue() ? "✔ Dalyvavo" : "✘ Nedalyvavo";
                            report.append("   - ").append(date).append(": ").append(status).append("\n");
                        }
                    }
                }
            }
        }
        reportArea.setText(report.toString());
    }

    private void exportToPDF(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Eksportuoti PDF");
        fileChooser.setInitialFileName("ataskaita.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                document.add(new Paragraph(reportArea.getText()));
                document.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void exportToCSV(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Eksportuoti CSV");
        fileChooser.setInitialFileName("ataskaita.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("Grupė,Studento ID,Vardas,Data,Lankomumas");
                LocalDate from = fromDatePicker.getValue();
                LocalDate to = toDatePicker.getValue();
                String filter = filterGroupComboBox.getValue();

                for (Group g : studentService.getAllGroups()) {
                    if (filter == null || filter.equals(g.getName())) {
                        for (Student s : g.getStudents()) {
                            for (Map.Entry<LocalDate, Boolean> entry : s.getAllAttendance().entrySet()) {
                                LocalDate date = entry.getKey();
                                if (!date.isBefore(from) && !date.isAfter(to)) {
                                    String status = entry.getValue() ? "Dalyvavo" : "Nedalyvavo";
                                    pw.printf("%s,%s,%s,%s,%s\n", g.getName(), s.getId(), s.getName(), date, status);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void importFromCSV(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importuoti CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = br.readLine(); // skip header
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 5) {
                        String groupName = parts[0];
                        String id = parts[1];
                        String name = parts[2];
                        LocalDate date = LocalDate.parse(parts[3]);
                        boolean present = parts[4].equalsIgnoreCase("Dalyvavo");

                        Group group = studentService.getGroup(groupName);
                        if (group == null) {
                            group = new Group(groupName);
                            studentService.getAllGroups().add(group);
                        }

                        Student student = group.getStudents().stream()
                                .filter(s -> s.getId().equals(id))
                                .findFirst()
                                .orElse(null);

                        if (student == null) {
                            student = new Student(id, name);
                            group.addStudent(student);
                        }

                        student.markAttendance(date, present);
                    }
                }
                refreshGroupComboBoxes();
                refreshStudentTable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void exportToExcel(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Eksportuoti į Excel");
        fileChooser.setInitialFileName("ataskaita.xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Lankomumas");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Grupė");
                header.createCell(1).setCellValue("ID");
                header.createCell(2).setCellValue("Vardas");
                header.createCell(3).setCellValue("Data");
                header.createCell(4).setCellValue("Lankomumas");

                int rowIdx = 1;
                LocalDate from = fromDatePicker.getValue();
                LocalDate to = toDatePicker.getValue();
                String filter = filterGroupComboBox.getValue();

                for (Group g : studentService.getAllGroups()) {
                    if (filter == null || filter.equals(g.getName())) {
                        for (Student s : g.getStudents()) {
                            for (Map.Entry<LocalDate, Boolean> entry : s.getAllAttendance().entrySet()) {
                                LocalDate date = entry.getKey();
                                if (!date.isBefore(from) && !date.isAfter(to)) {
                                    Row row = sheet.createRow(rowIdx++);
                                    row.createCell(0).setCellValue(g.getName());
                                    row.createCell(1).setCellValue(s.getId());
                                    row.createCell(2).setCellValue(s.getName());
                                    row.createCell(3).setCellValue(date.toString());
                                    row.createCell(4).setCellValue(entry.getValue() ? "Dalyvavo" : "Nedalyvavo");
                                }
                            }
                        }
                    }
                }

                try (FileOutputStream out = new FileOutputStream(file)) {
                    workbook.write(out);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void importFromExcel(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importuoti iš Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook(new FileInputStream(file))) {
                Sheet sheet = workbook.getSheetAt(0);
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String groupName = row.getCell(0).getStringCellValue();
                    String id = row.getCell(1).getStringCellValue();
                    String name = row.getCell(2).getStringCellValue();
                    LocalDate date = LocalDate.parse(row.getCell(3).getStringCellValue());
                    boolean present = row.getCell(4).getStringCellValue().equalsIgnoreCase("Dalyvavo");

                    Group group = studentService.getGroup(groupName);
                    if (group == null) {
                        group = new Group(groupName);
                        studentService.getAllGroups().add(group);
                    }

                    Student student = group.getStudents().stream()
                            .filter(s -> s.getId().equals(id))
                            .findFirst()
                            .orElse(null);

                    if (student == null) {
                        student = new Student(id, name);
                        group.addStudent(student);
                    }

                    student.markAttendance(date, present);
                }
                refreshGroupComboBoxes();
                refreshStudentTable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}