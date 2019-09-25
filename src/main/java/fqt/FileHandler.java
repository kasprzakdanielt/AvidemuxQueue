package fqt;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.input.DragEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static fqt.DialogsHandler.warning_dialog;

class FileHandler {
    private static String ffprobe_path = System.getProperty("user.dir") + "/ffmpeg-4.2.1-win64-static/bin/ffprobe.exe";
    private static String ffmpeg_path = System.getProperty("user.dir") + "/ffmpeg-4.2.1-win64-static/bin/ffmpeg.exe";
    private static double done_files = 0.0;
    static ObservableList<TableModel> tableData = FXCollections.observableArrayList();


    static ObservableList<TableModel> ffmpeg_get_codecs(DragEvent event){
        List<File> files = event.getDragboard().getFiles();

        new Thread(() -> {
            files.forEach(file -> {
                String filepath = file.getAbsolutePath();
                String query = String.format("%s -v error -select_streams a:0 -show_entries stream=codec_name -of default=noprint_wrappers=1:nokey=1 %s", ffprobe_path, files.get(0).getAbsolutePath());
                try {
                    Process p = Runtime.getRuntime().exec(query);
                    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String audiocodec = input.readLine();
                    String filename = file.getName();
                    tableData.add(new TableModel(filename, audiocodec, "Waiting", filepath));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        }).start();
        return tableData;

    }


    static void Start_conversion(TableView<TableModel> fileList, String output_audiocodec, String output_path, ProgressBar progress_bar){
        if (output_audiocodec.equals("")) {
            warning_dialog("Output codec is not selected");
        }
        if (output_path.isEmpty()) {
            warning_dialog("Destination was not specified");
        } else {
            progress_bar.setProgress(0.0);

            new Thread(() -> {
                done_files = 0.0;
                Integer table_size = FileHandler.tableData.size();
                FileHandler.tableData.forEach((temp) -> {
                    String filename = temp.getFileName();
                    String filepath = temp.getFilepath();
                    String destination = output_path + "\\" + filename;
                    temp.setStatus(start_conversion(destination, filepath, output_audiocodec));
                    fileList.refresh();
                    progress(table_size, progress_bar);
                });
            }).start();

        }


    }

    static void progress(Integer table_size, ProgressBar progress_bar) {
        done_files += 1.0;
        progress_bar.setProgress((done_files / table_size));
    }

    static private String start_conversion(String destination, String filepath, String output_audiocodec) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String query = String.format("%s -loglevel error -i %s -c:v copy -c:a %s %s", ffmpeg_path, filepath, output_audiocodec, destination);
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


}
