package controller;
import alerts.Alerts;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import java.net.URL;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import javafx.scene.layout.Pane;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class admin_voca_controller implements Initializable {
    private static final String url_ = "jdbc:mysql://localhost:3306/lingoland";
    private static final String user = "root";
    private static final String password = null;
    private ObservableList<String> listOb;

    @FXML
    private Button save;

    Alerts alerts = new Alerts();

    @FXML
    private TextArea explain;

    @FXML
    private TextField searchBar, target, quantityOfResult, targetImage;

    @FXML
    private ListView<String> listResults, listTopic;

    private List<Integer> listIndex, listDataID, listTopicID;

    private int indexTarget, ID_Admin;

    private String oldExplain;

    @FXML
    private Pane anchor_image;

    @FXML
    private Label successSaveImage;

    @FXML
    private Button saveImage;

    @FXML
    private TextField imageFile;

    @FXML
    private ImageView image_demo = new ImageView();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logController a = new logController();
        ID_Admin = a.getID_Admin();
        target.setEditable(false);
        explain.setEditable(false);
        save.setVisible(false);
        indexTarget = -1;
        showListTopic();
        searchBar.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String keyWord = searchBar.getText();
                if (!keyWord.isEmpty()) {
                    showListResult(keyWord);
                }
            }
        });
        anchor_image.setVisible(false);

    }

    public ObservableList<String> searchTargetsContainingString(String findString) {
        listOb = FXCollections.observableArrayList(); // Khởi tạo ObservableList

        List<Integer> wordIDs = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection(url_, user, password);
            String sql = "SELECT ID_word, Target FROM dictionary WHERE Target LIKE ? ORDER BY Target ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, findString + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int wordID = rs.getInt("ID_word");
                String target = rs.getString("Target");
                listOb.add(target);
                wordIDs.add(wordID);
            }
            listIndex = new ArrayList<>(wordIDs);
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Xử lý exception nếu có
        }

        return listOb;
    }

    public ObservableList<String> searchTargetTopic(int data) {
        listOb = FXCollections.observableArrayList(); // Khởi tạo ObservableList
        List<Integer> wordIDs = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection(url_, user, password);
            String sql = "SELECT ID_word, Target FROM dictionary WHERE ID_data = ? ORDER BY Target ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, data);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int wordID = rs.getInt("ID_word");
                String target = rs.getString("Target");
                listOb.add(target);
                wordIDs.add(wordID);
            }
            listIndex = new ArrayList<>(wordIDs);
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Xử lý exception nếu có
        }

        return listOb;
    }

    public ObservableList<String> searchTopic() {
        listOb = FXCollections.observableArrayList(); // Khởi tạo ObservableList
        listTopicID = new ArrayList<>();
        List<Integer> dataID= new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection(url_, user, password);
            String sql = "SELECT ID_data, ID_topic, topic FROM topic ORDER BY topic ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int dataID_ = rs.getInt("ID_data");
                int topicID_ = rs.getInt("ID_topic");
                String topic = rs.getString("topic");
                listOb.add(topic);
                listTopicID.add(topicID_);
                dataID.add(dataID_);
            }
            listDataID = new ArrayList<>(dataID);
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Xử lý exception nếu có
        }

        return listOb;
    }

    @FXML
    public void showListTopic() {
        ObservableList<String> listOb = FXCollections.observableArrayList(); // Khởi tạo ObservableList

        listOb = searchTopic();
        listTopic.setItems(listOb);

    }
    // hiện danh sách kết quả
    @FXML
    public void showListResult(String keyWord) {
        ObservableList<String> listOb = FXCollections.observableArrayList(); // Khởi tạo ObservableList

        listOb = searchTargetsContainingString(searchBar.getText().trim());
        int n = listOb.size();
        if (n == 0) {
            quantityOfResult.setText("0 kết quả liên quan");
            listResults.setItems(listOb);
        } else {
            quantityOfResult.setText(Math.min(20, n) + " trong " + n + " kết quả");
        }
        if (listIndex.isEmpty()) {
            return;
        }
        listResults.setItems(listOb);

    }

    @FXML
    private void chooseTopicByClick(MouseEvent arg0) {
        if (listDataID.size() == 0 || listTopic.getSelectionModel().getSelectedItem() == null) {
            return;
        }

        int ID_data = -1;
        String getWordByClick = listTopic.getSelectionModel().getSelectedItem().trim();
        for (int i = 0; i < listTopicID.size(); i++) {
            if (getWordByClick.equals(listTopic.getItems().get(i).trim())) {
                ID_data = listDataID.get(i);
                System.out.println(getWordByClick + ID_data);
                break;
            }
        }

        ObservableList<String> listOb = FXCollections.observableArrayList(); // Khởi tạo ObservableList

        listOb = searchTargetTopic(ID_data);
        int n = listOb.size();
        if (n == 0) {
            quantityOfResult.setText("0 kết quả liên quan");
            listResults.setItems(listOb);
        } else {
            quantityOfResult.setText(Math.min(20, n) + " trong " + n + " kết quả");
        }
        if (listIndex.isEmpty()) {
            return;
        }
        listResults.setItems(listOb);
    }


    @FXML
    private void chooseTargetByClick(MouseEvent arg0) {
//        final String[] getWordByClick = {""}; // Sử dụng mảng để lưu giá trị và giữ được final
//
//        int ID_w = -1;
//        listResults.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
//            int selectedIndex = newValue.intValue();
//            if (selectedIndex != -1) {
//                getWordByClick[0] = listResults.getItems().get(selectedIndex).trim();
//                ID_w = listIndex.get(selectedIndex);
//            }
//        });

        if (listIndex.isEmpty()) {
            return;
        }
        int ID_w = -1;
        String getWordByClick = listResults.getSelectionModel().getSelectedItem().trim();
        for (int i = 0; i < listIndex.size(); i++) {
            if (getWordByClick.equals(listResults.getItems().get(i).trim())) {
                ID_w = listIndex.get(i);
                indexTarget = ID_w;
                break;
            }
        }


        System.out.println(getWordByClick);
        target.setText(getWordByClick);
        anchor_image.setVisible(true);
        imageFile.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                insertImage();
            }
        });
        targetImage.setText(getWordByClick);
        target.setEditable(false);

        try {
            Connection conn = DriverManager.getConnection(url_, user, password);
            String sql = "SELECT meaning FROM Dictionary WHERE ID_Word = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, indexTarget);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                explain.setText(rs.getString("meaning"));
            }

            sql = "SELECT image FROM advancedvoca WHERE ID_Word = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, indexTarget);
            rs = stmt.executeQuery();
            if (rs.next()) {
                setImage(rs.getString("image"));
            } else {
                setImage("null.png");
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
       // explain.setText(a.getWord_explain());
        explain.setEditable(false);
        save.setVisible(false);
    }


    @FXML
    private void editExplain() {
        alerts.showAlertInfo("Information", "Hiện tại bạn có thể chỉnh sửa từ này!");
        oldExplain = explain.getText();
        explain.setEditable(true);
        save.setVisible(true);
    }

    @FXML
    private void saveEdit() {
        String newExplain = explain.getText().trim();
        if (newExplain.equals(oldExplain)) {
            alerts.showAlertInfo("Information", "Bạn chưa thay đổi từ này!");
            return;
        }
        if (newExplain.equals("")) {
            alerts.showAlertInfo("Information", "Không được để trống!");
            return;
        }
        Alert selectionAlert = alerts.alertConfirmation(
                "Sửa từ",
                "Bạn chắc chắn muốn cập nhật từ này chứ?"
        );
        selectionAlert.getButtonTypes().setAll(
                new ButtonType("Lưu"),
                ButtonType.CANCEL
        );
        Optional<ButtonType> selection = selectionAlert.showAndWait();

        if (!selection.isPresent()) {
            return;
        }
        if (selection.get() == ButtonType.CANCEL) {
            alerts.showAlertInfo("Information", "Thao tác không được sử dụng.");
            return;
        }
        try (Connection conn = DriverManager.getConnection(url_, user, password)) {
            String sqlUpdate = "UPDATE dictionary SET meaning = ? WHERE ID_word = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sqlUpdate);
            preparedStatement.setString(1, newExplain);
            preparedStatement.setInt(2, indexTarget);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Cập nhật thành công!");
            } else {
                System.out.println("Không có bản ghi nào được cập nhật.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Xử lý exception nếu có
        }
        insertHistoryUpdateVoca(ID_Admin, "Sua nghia tu " + indexTarget + target.getText()
                + " tu\n" + oldExplain + " sang " + newExplain);
        alerts.showAlertInfo("Information", "Cập nhật thành công " + target.getText());
    }

    @FXML
    private void handleClickSoundBtn() {
        // sử dụng thư viện freetts :> nên ko chọn được nhiều giọng
        // có 2 giọng là kevin và kevin 16
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        Voice voice = VoiceManager.getInstance().getVoice("kevin16");
        if (voice != null) {
            voice.allocate();
            voice.speak(target.getText());
        } else throw new IllegalStateException("Cannot find voice: kevin16");
    }

    @FXML
    private void deleteWord() {
        if (indexTarget == -1) {
            return;
        }

        Alert selectionAlert = alerts.alertConfirmation(
                "Xóa từ",
                "Bạn chắc chắn muốn xóa từ này chứ?"
        );
        selectionAlert.getButtonTypes().setAll(
                new ButtonType("Xóa"),
                ButtonType.CANCEL
        );
        Optional<ButtonType> selection = selectionAlert.showAndWait();

        if (!selection.isPresent()) {
            return;
        }
        if (selection.get() == ButtonType.CANCEL) {
            alerts.showAlertInfo("Information", "Thao tác không được sử dụng.");
            return;
        }

        String deleteQuery = "DELETE FROM dictionary WHERE ID_word = ?";
        try (Connection conn = DriverManager.getConnection(url_, user, password);
             PreparedStatement preparedStatement = conn.prepareStatement(deleteQuery)) {

            preparedStatement.setInt(1, indexTarget);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                insertHistoryUpdateVoca(ID_Admin, "Xóa từ " + target.getText() + " có ID cũ là " + indexTarget);
                alerts.showAlertInfo("Information", "Xóa thành công " + target.getText());
                System.out.println("Xóa thành công: " + target.getText());
            } else {
                alerts.showAlertWarning("Error", "Không tìm thấy từ để xóa hoặc xóa không thành công.");
                System.out.println("Không tìm thấy từ để xóa hoặc xóa không thành công.");
            }

        } catch (SQLException e) {
            alerts.showAlertWarning("Error", "Lỗi khi xóa từ: " + e.getMessage());
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

    @FXML
    private void insertImage() {
        successSaveImage.setVisible(false);
        if (imageFile.getText().trim().isEmpty()) {
            saveImage.setVisible(false);
        } else {
            saveImage.setVisible(true);
        }
    }

    @FXML
    private void handleClickSaveImage() {
        setImage(imageFile.getText().trim());
        successSaveImage.setVisible(true);
        insertAvancedVoca(indexTarget, imageFile.getText().trim());
        insertHistoryUpdateVoca(ID_Admin, "Minh họa ảnh cho từ " + target.getText()
                + "\n Chuyển từ sang phần nâng cao để người dùng học tập dễ dàng.");



    }

    @FXML
    private void insertAvancedVoca(int ID_word, String image) {
        try {
            Connection connection = DriverManager.getConnection(url_, user, null);
            String sqlID = "INSERT INTO advancedvoca (ID_word, image) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlID);
            preparedStatement.setInt(1, ID_word); // Giá trị ID_word
            preparedStatement.setString(2, image); // Giá trị image
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        insertHistoryUpdateVoca(ID_Admin, "Thêm từ " + ID_word + ": "
                + target.getText().trim() + " vào bảng anvanced");
    }

    @FXML
    private void setImage(String nameImage_) {
        String path = "file:src/main/resources/image/" + nameImage_;
        Image image_ = new Image(path);
        image_demo.setImage(image_);
    }


//
//    public static void main(String[] args) {
//        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
//        VoiceManager voiceManager = VoiceManager.getInstance();
//        Voice[] voices = voiceManager.getVoices();
//
//        for (Voice voice : voices) {
//            System.out.println("Name: " + voice.getName());
//            System.out.println("Description: " + voice.getDescription());
//            System.out.println();
//        }
//    }




}