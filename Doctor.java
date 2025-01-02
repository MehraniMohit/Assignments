import java.sql.*;

public class Doctor {
    private Connection connection;

    public Doctor(Connection connection) {
        this.connection = connection;
    }

    public void viewDoctors() {
        String query = "SELECT * FROM doctors";
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            System.out.println("Doctors:");
            System.out.println("+----+-----------------+----------------+");
            System.out.println("| ID | Name            | Specialization |");
            System.out.println("+----+-----------------+----------------+");
            while (resultSet.next()) {
                System.out.printf("| %-2d | %-15s | %-14s |\n", resultSet.getInt("id"),
                        resultSet.getString("name"), resultSet.getString("specialization"));
            }
            System.out.println("+----+-----------------+----------------+");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean getDoctorsById(int doctorId) {
        String query = "SELECT * FROM doctors WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, doctorId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
