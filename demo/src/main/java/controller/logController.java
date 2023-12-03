package controller;

import alerts.Alerts;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;
/* được rùi nha 3:38AM*/
public class logController implements Initializable {
    public static int ID_user, ID_Admin;
    private final Alerts alerts = new Alerts();

    @FXML
    private Button sign_in, createAcc, sign_up;

    @FXML
    private TextField name, email, accSignUp, passSignUp, checkPass;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private Button quit;

    @FXML
    private TextField account_input, password_input;

    @FXML
    private Label successAlertSignIn, failAlert, successAlertSignUp, validPass;

    @FXML
    private Label invalidAcc, invalidEmail, checkAgain;

    @FXML
    private AnchorPane signUp_control;

    private static final String url = "jdbc:mysql://localhost:3306/lingoland";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sign_in.setVisible(false);
        account_input.textProperty().addListener((observable, oldValue, newValue) -> handleInput());
        password_input.textProperty().addListener((observable, oldValue, newValue) -> handleInput());
        failAlert.setVisible(false);
        successAlertSignIn.setVisible(false);
        signUp_control.setVisible(false);
        setupSignUpButtonVisibility();
        createAcc.setOnAction(event -> showSignUpControl());
        quit.setOnMouseClicked(e -> System.exit(0));
    }

    private void handleInput() {
        String acc = account_input.getText().trim();
        String pass = password_input.getText().trim();

        sign_in.setVisible(!acc.isEmpty() && !pass.isEmpty());
    }

    private void showSignUpControl() {
        signUp_control.setVisible(true);
        sign_up.setVisible(false);
        successAlertSignUp.setVisible(false);
        invalidAcc.setVisible(false);
        invalidEmail.setVisible(false);
    }

    @FXML
    private void handleClickSignInBtn() {
        String acc = account_input.getText().trim();
        String pass = password_input.getText().trim();
        checkAccountSignIn(acc, pass);
    }

    public enum Gender {
        Nữ,
        Nam,
        Khác
    }

    @FXML
    private Gender parseGender(String genderStr) {
        if (genderStr.equalsIgnoreCase("nam")) {
            return Gender.Nam;
        } else if (genderStr.equalsIgnoreCase("nữ")) {
            return Gender.Nữ;
        } else {
            return Gender.Khác;
        }
    }

    @FXML
    private void handleClickSignUpBtn() {
        String nameValue = name.getText();
        String emailValue = email.getText();
        String usernameValue = accSignUp.getText();
        String passwordValue = passSignUp.getText();
        LocalDate dateOfBirthValue = datePicker.getValue();
        String genderValue = genderComboBox.getValue();
        Gender gender = parseGender(genderValue);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(url, "root", null);

            String query = "INSERT INTO users (name, date_of_birth, gender, email, username, password) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, nameValue);
            preparedStatement.setDate(2, java.sql.Date.valueOf(dateOfBirthValue));
            preparedStatement.setString(3, gender.name());
            preparedStatement.setString(4, emailValue);
            preparedStatement.setString(5, usernameValue);
            preparedStatement.setString(6, passwordValue);

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                // Thêm dữ liệu thành công
                System.out.println("Dữ liệu đã được thêm vào bảng users!");
                sign_up.setVisible(false);
            }

            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Xử lý ngoại lệ khi thêm dữ liệu bị lỗi
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        successAlertSignUp.setVisible(true);
    }


    private void checkAccountSignIn(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection connection = DriverManager.getConnection(url, "root", null);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                successAlertSignIn.setVisible(true);
                ID_user = getUserID();
                ID_Admin = getAdminID();
                System.out.println(ID_user + " " + ID_Admin);
                String path = "/view/user.fxml";
                if (ID_Admin != -1)
                    path = "/view/admin.fxml";
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
                    Parent root = loader.load();
                    Scene scene = new Scene(root);
                    Stage stage = (Stage) signUp_control.getScene().getWindow();
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                failAlert.setVisible(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupSignUpButtonVisibility() {
        successAlertSignUp.setVisible(false);
        name.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsNotEmpty());
        email.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsNotEmpty());
        accSignUp.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsNotEmpty());
        passSignUp.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsNotEmpty());
        checkPass.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsNotEmpty());
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> checkFieldsNotEmpty());
        genderComboBox.valueProperty().addListener((observable, oldValue, newValue) -> checkFieldsNotEmpty());
    }

    private boolean checkInvalidPass() {
        if (checkPass.getText().trim().equals(passSignUp.getText().trim())) {
            validPass.setVisible(true);
            validPass.setText("OK");
            return true;
        }
        validPass.setVisible(true);
        validPass.setText("Mật khẩu không khớp");
        return false;
    }
    private void checkFieldsNotEmpty() {
        boolean a = checkInvalidMail();
        boolean m = checkInvalidAcc();
        boolean p = checkInvalidPass();
        if (    !m || !a || !p ||
                name.getText().isEmpty() || email.getText().isEmpty() ||
                accSignUp.getText().isEmpty() || passSignUp.getText().isEmpty() ||
                checkPass.getText().isEmpty() || datePicker.getValue() == null ||
                genderComboBox.getValue() == null || genderComboBox.getValue().isEmpty()) {
            sign_up.setVisible(false);
            checkAgain.setVisible(true);
            checkAgain.setVisible(true);
            return;
        }
        checkAgain.setVisible(false);
        sign_up.setVisible(true);
    }

    @FXML
    private boolean checkInvalidAcc() {

        try (Connection connection = DriverManager.getConnection(url, "root", null)) {
            String username = accSignUp.getText().trim();
            if (username.isEmpty()) {
                invalidAcc.setText("Tên đăng nhập không được bỏ trống");
                invalidAcc.setVisible(true);
                return false;
            }

            String query = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    invalidAcc.setText("Tên đăng nhập đã tồn tại");
                    invalidAcc.setVisible(true);
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        invalidAcc.setText("Tên đăng nhập hợp lệ");
        invalidAcc.setVisible(true);
        return true;
    }

    @FXML
    private boolean checkInvalidMail() {
        try (Connection connection = DriverManager.getConnection(url, "root", "")) {
            String email_ = email.getText().trim();
            if (email_.isEmpty()) {
                invalidEmail.setText("Email không được rỗng");
                invalidEmail.setVisible(true);
                return false;
            }

            String query = "SELECT * FROM users WHERE email = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, email_);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    invalidEmail.setText("Email đã được sử dụng");
                    invalidEmail.setVisible(true);
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        invalidEmail.setText("Email hợp lệ");
        invalidEmail.setVisible(true);
        return true;
    }

    @FXML
    private int getUserID() {
        String query = "SELECT ID_user FROM Users WHERE username = ?";
        try (Connection connection = DriverManager.getConnection(url, "root", null)) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, account_input.getText().trim());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("ID_user");
            } else {
                System.out.println("Không tìm thấy người dùng có username: " + account_input.getText().trim());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @FXML
    private int getAdminID() {
        String query = "SELECT ID_Admin FROM Admin WHERE ID_user = ?";
        // String url = "jdbc:mysql://localhost:3306/ten_co_so_du_lieu"; // Thay thế bằng URL kết nối của bạn

        try (Connection connection = DriverManager.getConnection(url, "root", null)) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, ID_user);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("ID_Admin");
            } else {
                System.out.println("Không tìm thấy người dùng có ID_user: " + ID_user + " trong bảng Admin.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Trả về -1 nếu không tìm thấy ID_user trong bảng Admin
    }

    public static int getID_user() {
        return ID_user;
    }

    public static int getID_Admin() {
        return ID_Admin;
    }

}