package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class admin_historyUpdate implements Initializable {
    private static final String url_ = "jdbc:mysql://localhost:3306/lingoland";
    private static final String user = "root";
    private static final String password = null;

    @FXML
    private TableView<ObservableList<String>> tableUpdate;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDataFromDatabase("historyUpdateVoca", tableUpdate);

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


}
