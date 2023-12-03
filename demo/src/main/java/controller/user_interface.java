package controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class user_interface {

    @FXML
    private AnchorPane container, anchorPane;

    @FXML
    private Button profile, game, test, search, quit, logout;

    public void initialize() {
        //loadDataFromDatabase("data");
        showComponent("/view/view_user/seach.fxml");
        setButton(search, "/view/view_user/seach.fxml");
        //setButton(game, "/view/view_user/game.fxml");
        setButton(test, "/view/view_user/test.fxml");
        logout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/signIn.fxml"));
                    Parent root = loader.load();
                    Scene scene = new Scene(root);
                    Stage stage = (Stage) anchorPane.getScene().getWindow();
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        quit.setOnMouseClicked(e -> System.exit(0));
    }



    @FXML
    private void setButton(Button a, String fileFXML) {
        a.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                showComponent(fileFXML);
                //loadDataFromDatabase(tableName);
            }
        });
    }
    @FXML
    private void showComponent(String path) {
        try {// load path vao anchorpane
            AnchorPane component = FXMLLoader.load(getClass().getResource(path));
            container.getChildren().clear(); // xoa children cua container - pane.. ben trong
            container.getChildren().add(component); // them a..pane moi vao
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
