package fqt;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static fqt.FileHandler.startConversion;
import static fqt.FileHandler.ffmpegGetCodecs;


public class Controller implements Initializable {

    public Button startButton;
    public Button addButton;
    public TextField outputPath;
    public ProgressBar progressBar;

    private String outputAudiocodec = "";

    @FXML
    private ComboBox<String> audiocodecCombobox;
    @FXML
    TableView<TableModel> fileList;
    @FXML
    TableColumn<TableModel, String> filenameColumn;
    @FXML
    TableColumn<TableModel, String> audioColumn;
    @FXML
    TableColumn<TableModel, String> statusColumn;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        audiocodecCombobox.getItems().removeAll();
        audiocodecCombobox.getItems().addAll("ac3", "mp3", "aac", "flac");
        filenameColumn.setCellValueFactory(new PropertyValueFactory<>("FileName"));
        audioColumn.setCellValueFactory(new PropertyValueFactory<>("AudioCodec"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("Status"));
    }

    @FXML
    private void comboboxaction() {
        this.outputAudiocodec = audiocodecCombobox.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void pathChooser() {
        Stage stage = (Stage) fileList.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            outputPath.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void startPressed() {
        startConversion(fileList, outputAudiocodec, outputPath.getText(), progressBar);
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            final Boolean[] allowDrop = {true};
            event.getDragboard().getFiles().forEach(item -> {
                if (!item.getName().toLowerCase().endsWith(".mp4")) {
                    allowDrop[0] = false;
                }
            });
            if (allowDrop[0]) {
                event.acceptTransferModes(TransferMode.ANY);
            }
        }
    }

    @FXML
    private void handleDrop(DragEvent event) {
        fileList.setItems(ffmpegGetCodecs(event));
    }
}
