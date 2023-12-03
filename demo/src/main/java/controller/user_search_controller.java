package controller;
import alerts.Alerts;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
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
import javafx.scene.layout.AnchorPane;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
//import com.sun.speech.freetts.Voice;
//import com.sun.speech.freetts.VoiceManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class user_search_controller implements Initializable {
    private static final String url_ = "jdbc:mysql://localhost:3306/lingoland";
    private static final String user = "root";
    private static final String password = null;
    private ObservableList<String> listOb;

    @FXML
    private Button sound;

    Alerts alerts = new Alerts();
    @FXML
    private Label result, notFound;

    @FXML
    private TextArea explain;

    @FXML
    private TextField searchBar, target, quantityOfResult, targetImage;

    @FXML
    private ListView<String> listResults, listTopic;

    private int ID_user = 0;

    private List<String> listString;

    private List<Integer> listIndex, listDataID, listTopicID;

    private int indexTarget;

    private String oldExplain;

    @FXML
    private ImageView image_demo = new ImageView();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logController a = new logController();
        ID_user = a.getID_user();
        target.setEditable(false);
        explain.setEditable(false);
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
        if (listIndex.size() == 0) {
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
    }



    //    @FXML
//    private void handleClickSoundBtn() {
//        // sử dụng thư viện freetts :> nên ko chọn được nhiều giọng
//        // có 2 giọng là kevin và kevin 16
//        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
//        Voice voice = VoiceManager.getInstance().getVoice("kevin16");
//        if (voice != null) {
//            voice.allocate();
//            voice.speak(target.getText());
//        } else throw new IllegalStateException("Cannot find voice: kevin16");
//    }
//


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
    private void setImage(String nameImage_) {
        String path = "file:src/main/resources/image/" + nameImage_;
        Image image_ = new Image(path);
        image_demo.setImage(image_);
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


}