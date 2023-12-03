package controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class user_test_controller implements Initializable {

    private static final String url_ = "jdbc:mysql://localhost:3306/lingoland";
    private static final String user = "root";
    private static final String password = null;
    @FXML
    private Button test, finish;

    private int score = 0;
    @FXML
    private Button done1, done2, done3, done4, done5;

    @FXML
    private Button check1, check2, check3, check4, check5;

    private List<String> target = new ArrayList<>();
    private List<String> explain = new ArrayList<>();
    private List<Integer> idword = new ArrayList<>();
    @FXML
    ObservableList<TextField> targetList, explainList;

    @FXML
    private TextField target1, target2, target3, target4, target5;

    @FXML
    private TextField suggest;

    @FXML
    private Label labelScore;

    @FXML
    private TextField explain1, explain2, explain3, explain4, explain5;

    @FXML
    private ImageView image1, image2, image3, image4, image5;

    private int ID_User, ID_study = -1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logController a = new logController();
        ID_User = a.getID_user();
                hidden();

        target1.setEditable(false);
        target2.setEditable(false);
        target3.setEditable(false);
        target4.setEditable(false);
        target5.setEditable(false);
        test.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                insertHistoryStudy();
                score = 0;
                labelScore.setText("0");
                System.out.println("yaya");
                target1.clear();
                target2.clear();
                target3.clear();
                target4.clear();
                target5.clear();
                target1.setEditable(true);
                target2.setEditable(true);
                target3.setEditable(true);
                target4.setEditable(true);
                target5.setEditable(true);
                loadTopic();
                if (target.size() < 5) {
                    System.out.println("<5 :)))");
                    return;
                }

                explain1.setText(explain.get(0));
                explain2.setText(explain.get(1));
                explain3.setText(explain.get(2));
                explain4.setText(explain.get(3));
                explain5.setText(explain.get(4));

                if (target.size() < 5) {
                    System.out.println("<5 :)))");
                    return;
                }
                setTextFieldTarget(target1, done1, check1, target.get(0), idword.get(0));
                setTextFieldTarget(target2, done2, check2, target.get(1), idword.get(1));
                setTextFieldTarget(target3, done3, check3, target.get(2), idword.get(2));
                setTextFieldTarget(target4, done4, check4, target.get(3), idword.get(3));
                setTextFieldTarget(target5, done5, check5, target.get(4), idword.get(4));
            }
        });

        finish.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                hidden();
                updateStudyScore();
            }
        });
    }

    @FXML
    private void hidden() {
        done1.setVisible(false);
        done2.setVisible(false);
        done3.setVisible(false);
        done4.setVisible(false);
        done5.setVisible(false);

        check1.setVisible(false);
        check2.setVisible(false);
        check3.setVisible(false);
        check4.setVisible(false);
        check5.setVisible(false);

        explain1.setEditable(false);
        explain2.setEditable(false);
        explain3.setEditable(false);
        explain4.setEditable(false);
        explain5.setEditable(false);
    }
    @FXML
    private void loadTopic() {
        hidden();
        String sql = "SELECT ID_word FROM advancedvoca ORDER BY RAND() LIMIT 5";

        try (Connection conn = DriverManager.getConnection(url_, user, password);
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            idword.clear();
            target.clear();
            explain.clear();

            while (resultSet.next()) {
                int id_word = resultSet.getInt("ID_word");
                idword.add(id_word);
                System.out.println(id_word);
                String sql2 = "SELECT target, meaning FROM dictionary WHERE ID_word = " + id_word;
               // String sql3 = "SELECT image FROM advancedvoca WHERE ID_word = " + id_word;

                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery(sql2);) {

                    if (rs.next()) {
                        target.add(rs.getString("target"));
                        explain.add(rs.getString("meaning"));
                        System.out.println(target.get(target.size() - 1));
                    }

                    int i = target.size();
                    String imageName = "null.png";
//
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }






    @FXML
    public void setTextFieldTarget(TextField target, Button done, Button check, String en, int id_w) {
        target.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (target.getText() != null && !target.getText().isEmpty()) {
                    check.setVisible(true);
                    check.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            if (target.getText().equals(en)) {
                                suggest.setText("Yeah!!!");
                                done.setVisible(true);
                                target.setEditable(false);
                                check.setVisible(false);
                                score += 20;
                                labelScore.setText(String.valueOf(score));
                                insertHistoryStudyDetail(id_w, "Từ " + id_w + " " + en
                                + " đã được trả lời đúng.");

                            } else {
                                suggest.setText(compare(target.getText(), en));
                                score -= 5;
                                insertHistoryStudyDetail(id_w, "Từ " + id_w + " " + en
                                        + " đã được trả lời sai thành " + target.getText());
                                labelScore.setText(String.valueOf(score));
                            }
                        }
                    });
                } else {
                    done.setVisible(false);
                    check.setVisible(false);
                }
            }
        });
    }

    private String compare(String target, String resTarget) {
        String res = "";
        for (int i = 0; i < resTarget.length(); i++) {
            if (i>=target.length()) {
                res += "-";
            } else if (target.charAt(i) == resTarget.charAt(i)) {
                res+=target.charAt(i);
            } else
                res+="-";
        }
        return res;
    }


    public void insertHistoryStudy() {
        try (Connection conn = DriverManager.getConnection(url_, user, password)) {
            String sql = "INSERT INTO learninghistory (ID_User) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, ID_User);
                stmt.executeUpdate();
            }
            String sql2 = "SELECT ID_study FROM learninghistory ORDER BY ID_study DESC LIMIT 1";
            PreparedStatement statement = conn.prepareStatement(sql2);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                ID_study = rs.getInt("ID_study");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertHistoryStudyDetail(int ID_word, String status) {
        try (Connection conn = DriverManager.getConnection(url_, user, password)) {
            String sql = "INSERT INTO learninghistorydetail (ID_Study, ID_word, status) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, ID_study);
                stmt.setInt(2, ID_word);
                stmt.setString(3, status);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStudyScore() {
        try (Connection conn = DriverManager.getConnection(url_, user, password)) {
            String sql = "UPDATE learninghistory SET score = ? WHERE id_study = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, score);
                stmt.setInt(2, ID_study);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void setImage(String nameImage_, ImageView image_demo) {
        String path = "file:src/main/resources/image/" + nameImage_;
        Image image_ = new Image(path);
        image_demo.setImage(image_);
    }

}