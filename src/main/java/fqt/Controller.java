package fqt;

import javafx.collections.ObservableList;
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

import static fqt.FileHandler.Start_conversion;
import static fqt.FileHandler.ffmpeg_get_codecs;


public class Controller implements Initializable {

    public Button start_button;
    public Button add_button;
    public TextField output_path;
    public ProgressBar progress_bar;

    private String output_audiocodec = "";
    private String filepath = "";
    private String filename = "";
    private Double done_files = 0.0;
    private String ffprobe_path = System.getProperty("user.dir") + "/ffmpeg-4.2.1-win64-static/bin/ffprobe.exe";
    private String ffmpeg_path = System.getProperty("user.dir") + "/ffmpeg-4.2.1-win64-static/bin/ffmpeg.exe";


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
        Start_conversion(fileList, output_audiocodec, output_path.getText(), progress_bar);
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            final Boolean[] allow_drop = {true};
            event.getDragboard().getFiles().forEach(item -> {
                if (!item.getName().toLowerCase().endsWith(".mkv")) {
                    allow_drop[0] = false;
                }
            });
            if (allow_drop[0]) {
                event.acceptTransferModes(TransferMode.ANY);
            }
        }
    }

    @FXML
    private void handleDrop(DragEvent event) {
        fileList.setItems(ffmpeg_get_codecs(event));
    }
}
