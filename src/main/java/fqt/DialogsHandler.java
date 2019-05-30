package fqt;

import javafx.scene.control.Alert;

class DialogsHandler {


    static void warning_dialog(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Careful");
        alert.setHeaderText("User error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
