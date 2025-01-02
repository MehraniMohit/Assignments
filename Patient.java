import java.sql.*;
import java.util.Scanner;

public class Patient {
    private Connection connection;
    private Scanner scanner;

    public Patient(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public void addPatient() {
        System.out.print("Enter Patient Name: ");
        String name = scanner.next();
        System.out.print("Enter Patient Age: ");
        int age=scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter Gender");
        String gender=scanner.nextLine();
        String query = "INSERT INTO patients(name, age,gender) VALUES (?, ?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, age);
            preparedStatement.setString(3,gender);
            if (preparedStatement.executeUpdate() > 0) {
                System.out.println("Patient added successfully!");
            } else {
                System.out.println("Failed to add patient.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void viewPatients() {
        String query = "SELECT * FROM patients";
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            System.out.println("Patients:");
            System.out.println("+----+-----------+-----+------------+");
            System.out.println("| ID | Name      | Age | Gender     |");
            System.out.println("+----+-----------+-----+------------+");
            while (resultSet.next()) {
                System.out.printf("| %-2d | %-9s | %-3d | %-10s |\n", resultSet.getInt("id"),
                        resultSet.getString("name"), resultSet.getInt("age"),
                        resultSet.getString("gender"));
            }
            System.out.println("+----+-----------+-----+------------+");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean getPatientById(int patientId) {
        String query = "SELECT * FROM patients WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, patientId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
