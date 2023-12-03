package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;


public class admin_data_controller implements Initializable {

    private static final String url_ = "jdbc:mysql://localhost:3306/lingoland";
    private static final String user = "root";
    private static final String password = null;

    @FXML
    private Button add, addTopic;

    @FXML
    private TableView<ObservableList<String>> tableData, tableTopic;

    @FXML
    private TextField nameFile, nameTopic, nameData;

    @FXML
    private TextArea description;

    @FXML
    private Label info, infoTopic;

    public static int ID_Data = 0, ID_topic = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDataFromDatabase("data", tableData);
        loadDataFromDatabase("topic", tableTopic);

        nameFile.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                insertNameData(nameFile.getText());
            }
        });
        nameTopic.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                insertDataTopic(nameTopic.getText().trim(), nameData.getText().trim());
            }
        });

        nameData.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                insertDataTopic(nameTopic.getText().trim(), nameData.getText().trim());
            }
        });
    }

    // hàm để kiểm tra xem data đã được sử dụng hay chưa
    // nếu chưa thì button add xuất hieện để ta có thể thêm nó vào bảng data
    private void insertNameData(String nameFile) {
        nameFile = nameFile.trim();
        if (nameFile.isEmpty()) {
            add.setVisible(false);
            info.setText("Data không hợp lệ.");
            return;
        }
        try {
            Connection conn = DriverManager.getConnection(url_, user, password);
            String sql = "SELECT ID_Data FROM Data WHERE NameFile = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nameFile);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ID_Data = rs.getInt("ID_Data");
                System.out.println("ID_Data found: " + ID_Data);
                info.setText("Data này đã được sử dụng.");
                add.setVisible(false);
            } else {
                System.out.println(nameFile + " not found in the Data table.");
                info.setText("Data hợp lệ, bạn có thể thêm.");
                add.setVisible(true);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void insertDataTopic(String nameTopic, String nameFile) {
        addTopic.setVisible(false);
        boolean checkData = false;
        boolean checkTopic = false;

        if (nameFile.isEmpty() || nameTopic.isEmpty()) {
            infoTopic.setText("Không được để trống dữ liệu.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(url_, user, password)) {
            String checkDataQuery = "SELECT ID_Data FROM Data WHERE NameFile = ?";
            try (PreparedStatement dataStmt = conn.prepareStatement(checkDataQuery)) {
                dataStmt.setString(1, nameFile);
                try (ResultSet dataRs = dataStmt.executeQuery()) {
                    if (dataRs.next()) {
                        ID_Data = dataRs.getInt("ID_Data");
                        infoTopic.setText("Data hợp lệ.");
                        checkData = true;
                    } else {
                        infoTopic.setText("Data chưa được cung cấp.");
                    }
                }
            }
            String checkTopicQuery = "SELECT ID_Topic FROM topic WHERE topic = ?";
            try (PreparedStatement topicStmt = conn.prepareStatement(checkTopicQuery)) {
                topicStmt.setString(1, nameTopic);
                try (ResultSet topicRs = topicStmt.executeQuery()) {
                    if (topicRs.next()) {
//                        ID_topic = topicRs.getInt("ID_Topic");
                        infoTopic.setText("Topic đã tồn tại.");
                    } else {
                        //infoTopic.setText("Topic hợp lệ.");
                        checkTopic = true;
                    }
                }
            }

            addTopic.setVisible(checkTopic && checkData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // hàm để khi ấn nút add (data) thì sẽ thêm data vào bảng và lấy ID_data
    // rồi sẽ nhập dữ liệu từ vào dictionary vơ ID_data đó
    @FXML
    private void handleClickAddBtn() {
        logController a = new logController();
        int ID_Admin = a.getID_Admin();
        System.out.println("Admin " + ID_Admin + "dang hoat dong.");
        String nameData = nameFile.getText();

        String sql = "INSERT INTO Data(NameFile, Description, ID_Admin) VALUES(?, ?, ?)";

        try {
            Connection conn = DriverManager.getConnection(url_, user, password);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nameFile.getText().trim());
            pstmt.setString(2, description.getText().trim());
            pstmt.setInt(3, ID_Admin);
            pstmt.executeUpdate();
            System.out.println("Dữ liệu đã được thêm vào bảng data thành công!");
            tableData.getColumns().clear();
            loadDataFromDatabase("data", tableData);
            // nhập từ
            System.out.println(nameData);
            insertFromFile(nameData);
            insertHistoryUpdateVoca(ID_Admin, "Thêm dữ liệu " + nameFile.getText());

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    // khi ấn thêm topic
    @FXML
    private void handleClickAddTopicBtn() {
        logController a = new logController();
        int ID_Admin = a.getID_Admin();
        System.out.println("Admin " + ID_Admin + "dang hoat dong.");

        String sql = "INSERT INTO Topic(topic, Description, ID_Data, ID_Admin) VALUES(?, ?, ?, ?)";

        try {
            Connection conn = DriverManager.getConnection(url_, user, password);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nameTopic.getText().trim());
            pstmt.setString(2, description.getText().trim());
            pstmt.setInt(3, ID_Data);
            pstmt.setInt(4, ID_Admin);
            pstmt.executeUpdate();
            System.out.println("Dữ liệu đã được thêm vào bảng data thành công!");
            infoTopic.setText("Thêm thành công!");
            tableTopic.getColumns().clear();
            loadDataFromDatabase("topic", tableTopic);
            insertHistoryUpdateVoca(ID_Admin, "Thêm chủ đề " + nameTopic.getText());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // hàm để nhập từ, nghĩa vào bảng dict mỗi khi thêm tệp vào bảng data thành công
    public void insertFromFile(String nameFile) {
        System.out.println("yayayaya line 209");
        try {
            Connection connection = DriverManager.getConnection(url_, user, null);
            String sql = "INSERT INTO dictionary (target, meaning, ID_data) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            String filePath = "src/main/resources/voca_txt/" + nameFile;
            String sqlID = "SELECT ID_data FROM data WHERE nameFile in ('" + nameFile + "')";
            PreparedStatement statement = connection.prepareStatement(sqlID);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                ID_Data = rs.getInt("ID_data");
            }
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            String target = null;
            String explain = "";

            boolean first = true;
            while ((line = bufferedReader.readLine()) != null) {

                // |target
                // explain
                if (line.startsWith("|")) {
                    //System.out.println(line + target);
                    if (target != null) {

                        if (!explain.isEmpty()) {
                            System.out.println(target + " " + explain + " " + ID_Data);
                            preparedStatement.setString(1, target);
                            preparedStatement.setString(2, explain);
                            preparedStatement.setInt(3, ID_Data);

                            preparedStatement.executeUpdate();

                            explain = "";
                        }
                    }
                    target = line.substring(1).trim();
                } else {
                    explain += line + "\n";
                }
            }
            if (target != null && !explain.isEmpty()) {
                preparedStatement.setString(1, target);
                preparedStatement.setString(2, explain);
                preparedStatement.setInt(3, ID_Data);

                preparedStatement.executeUpdate();

            }
            System.out.println("Succes reading file" + filePath);
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("Error reading file");
        }
            preparedStatement.executeUpdate();
          //  System.out.println("Dữ liệu đã được chèn thành công vào bảng Dictionary.");

            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

        private void loadDataFromDatabase(String tableName, TableView<ObservableList<String>> tableView) {
        try {
            Connection connection = DriverManager.getConnection(url_, user, password);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
            int numColumns = resultSet.getMetaData().getColumnCount();
            for (int i = 0; i < numColumns; i++) {
                final int j = i;
                TableColumn<ObservableList<String>, String> column = new TableColumn<>(resultSet.getMetaData().getColumnName(i + 1));
                column.setCellValueFactory(param -> {
                    ObservableList<String> row = param.getValue();
                    return javafx.beans.binding.Bindings.createStringBinding(() -> row.get(j));
                });
                tableView.getColumns().add(column);
            }

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            while (resultSet.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= numColumns; i++) {
                    row.add(resultSet.getString(i));
                }
                data.add(row);
            }

            tableView.setItems(data);

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertHistoryUpdateVoca(int ID_Admin, String work) {
        try (Connection conn = DriverManager.getConnection(url_, user, password)) {
            String sql = "INSERT INTO historyUpdateVoca (ID_Admin, work) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, ID_Admin);
                stmt.setString(2, work);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
