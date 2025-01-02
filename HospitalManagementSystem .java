import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

class HospitalManagementSystem {
    private static final String url = "jdbc:mysql://localhost:3306/hospital";
    private static final String username = "root";
    private static final String password = "shahdadpur123??";
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner(System.in);
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            Patient patient = new Patient(connection, scanner);
            Doctor doctor = new Doctor(connection);

            while (true) {
                System.out.println("HOSPITAL MANAGEMENT SYSTEM");
                System.out.println("1. Add Patient");
                System.out.println("2. View Patients");
                System.out.println("3. View Doctors");
                System.out.println("4. Book Appointment");
                System.out.println("5. View Appointments");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");

              int choice = getValidIntInput(scanner);

                switch (choice) {
                    case 1:
                        patient.addPatient();
                        break;
                    case 2:
                        patient.viewPatients();
                        break;
                    case 3:
                        doctor.viewDoctors();
                        break;
                    case 4:
                        bookAppointment(patient, doctor, connection, scanner);
                        break;
                    case 5:
                        viewAppointments(connection);
                        break;
                    case 6:
                        System.out.println("Exiting the system. Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice! Please select a valid option.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void bookAppointment(Patient patient, Doctor doctor, Connection connection, Scanner scanner) {
        System.out.print("Enter Patient ID: ");
        int patientId = getValidIntInput(scanner);

        if (!patient.getPatientById(patientId)) {
            System.out.println("Patient ID not found! Please add the patient first.");
            return;
        }

        System.out.print("Enter Doctor ID: ");
       int doctorId = getValidIntInput(scanner);
        System.out.print("Enter Appointment Date (YYYY-MM-DD): ");
        String appointmentDateInput = scanner.next();
        System.out.print("Enter Appointment Time (HH:MM): ");
        String appointmentTimeInput = scanner.next();

        try {
            LocalDate appointmentDate = LocalDate.parse(appointmentDateInput);
            LocalTime appointmentTime = LocalTime.parse(appointmentTimeInput);

            if (!doctor.getDoctorsById(doctorId)) {
                System.out.println("Doctor ID not found! Please check the doctor ID.");
                return;
            }

            if (!checkDoctorAvailability(doctorId, appointmentDate.toString(), appointmentTime.toString(), connection)) {
                System.out.println("Doctor not available on this date and time!");
                return;
            }

            if (getDailyAppointmentCount(appointmentDate.toString(), connection) >= 10) {
                System.out.println("Daily appointment limit reached Only 10 appointments can be booked per day.");
                return;
            }

            String query = "INSERT INTO appointments(patient_id, doctor_id, appointment_date, appointment_time) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, patientId);
            preparedStatement.setInt(2, doctorId);
            preparedStatement.setString(3, appointmentDate.toString());
            preparedStatement.setString(4, appointmentTime.toString());
            if (preparedStatement.executeUpdate() > 0) {
                System.out.println("Appointment Booked!");
            } else {
                System.out.println("Failed to Book Appointment!");
            }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date/time format. Use YYYY-MM-DD for date and HH:MM for time.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkDoctorAvailability(int doctorId, String appointmentDate, String appointmentTime, Connection connection) {
        String query = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, doctorId);
            preparedStatement.setString(2, appointmentDate);
            preparedStatement.setString(3, appointmentTime);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() && resultSet.getInt(1) == 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getDailyAppointmentCount(String appointmentDate, Connection connection) {
        String query = "SELECT COUNT(*) FROM appointments WHERE appointment_date = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, appointmentDate);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() ? resultSet.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void viewAppointments(Connection connection) {
        String query = "SELECT appointments.id, patients.name AS patient_name, doctors.name AS doctor_name, appointments.appointment_date, appointments.appointment_time " +
                "FROM appointments " +
                "JOIN patients ON appointments.patient_id = patients.id " +
                "JOIN doctors ON appointments.doctor_id = doctors.id";
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            System.out.println("Appointments:");
            System.out.println("+----+----------------+----------------+--------------+-------------+");
            System.out.println("| ID | Patient        | Doctor         | Date         | Time        |");
            System.out.println("+----+----------------+----------------+--------------+-------------+");
            while (resultSet.next()) {
                System.out.printf("| %-2d | %-14s | %-14s | %-12s | %-11s |\n",
                        resultSet.getInt("id"),
                        resultSet.getString("patient_name"),
                        resultSet.getString("doctor_name"),
                        resultSet.getString("appointment_date"),
                        resultSet.getString("appointment_time"));
            }
            System.out.println("+----+----------------+----------------+--------------+-------------+");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static int getValidIntInput(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input! Please enter a number: ");
            scanner.next();
        }
        return scanner.nextInt();
    }
}
