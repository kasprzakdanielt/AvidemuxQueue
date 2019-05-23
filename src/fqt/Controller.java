package fqt;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    public Button start_button;
    public Button add_button;
    public TextField output_path;
    private String output_audiocodec = "";
    private String filepath = "";
    private String filename = "";
    @FXML
    private ComboBox<String> audiocodec_combobox;
    @FXML
    TableView<TableModel> fileList;
    @FXML
    TableColumn<TableModel, String> filename_column;
    @FXML
    TableColumn<TableModel, String> audio_column;
    @FXML
    TableColumn<TableModel, String> status_column;


    private ObservableList<TableModel> tableData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        audiocodec_combobox.getItems().removeAll();
        audiocodec_combobox.getItems().addAll("ac3", "mp3", "aac", "flac");
        filename_column.setCellValueFactory(new PropertyValueFactory<>("FileName"));
        audio_column.setCellValueFactory(new PropertyValueFactory<>("AudioCodec"));
        status_column.setCellValueFactory(new PropertyValueFactory<>("Status"));
    }


    @FXML
    private void comboboxaction() {
        this.output_audiocodec = audiocodec_combobox.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void path_chooser() {
        Stage stage = (Stage) fileList.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();

        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {

            output_path.setText(selectedDirectory.getAbsolutePath());
        }


    }

    @FXML
    private void start_pressed() {
        if (output_audiocodec.equals("")) {
            warning_dialog("Output codec is not selected");
        }
        if (output_path.getText().isEmpty()) {
            warning_dialog("Destination was not specified");
        } else {
            new Thread(() -> {
                tableData.forEach((temp) -> {
                    filename = temp.getFileName();
                    filepath = temp.getFilepath();
                    String destination = output_path.getText() + "\\" + filename;

                    temp.setStatus(start_conversion(destination, filepath));
                    fileList.refresh();

                });
            }).start();
        }
    }

    private String start_conversion(String destination, String filepath) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        File file = new File("libs\\ffmpeg-4.1.3-win64-static\\bin\\ffmpeg.exe");//full file path URL
        String absolutePath = file.getAbsolutePath();
        String query = String.format("%s -loglevel error -i %s -c:v copy -c:a %s %s", absolutePath, filepath, output_audiocodec, destination);
        System.out.println(query);
        try {
            processBuilder.command("cmd.exe", "/c", query);
            Process p = processBuilder.start();
            p.waitFor();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            if (input.readLine() != null) {
                return "Error";
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return "Success";
    }

    private void warning_dialog(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Careful");
        alert.setHeaderText("User error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            if (event.getDragboard().getFiles().get(0).getName().toLowerCase().endsWith(".mkv")) {
                event.acceptTransferModes(TransferMode.ANY);
            }
        }
    }

    @FXML
    private void handleDrop(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        String audiocodec = "Error";
        filepath = files.get(0).getAbsolutePath();
        String query = String.format("libs\\ffmpeg-4.1.3-win64-static\\bin\\ffprobe.exe -v error -select_streams a:0 -show_entries stream=codec_name -of default=noprint_wrappers=1:nokey=1 %s", files.get(0).getAbsolutePath());
        try {
            Process p = Runtime.getRuntime().exec(query);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            audiocodec = input.readLine();
            filename = files.get(0).getName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tableData.add(new TableModel(filename, audiocodec, "Waiting", filepath));
        fileList.setItems(tableData);


    }
}
