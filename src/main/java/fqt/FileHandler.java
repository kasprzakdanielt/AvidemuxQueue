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

import static fqt.DialogsHandler.warningDialog;

class FileHandler {
    private static final String FFPROBE_PATH = System.getProperty("user.dir") + "/ffmpeg-4.2.1-win64-static/bin/ffprobe.exe";
    private static final String FFMPEG_PATH = System.getProperty("user.dir") + "/ffmpeg-4.2.1-win64-static/bin/ffmpeg.exe";
    private static double DoneFiles = 0.0;
    private static ObservableList<TableModel> tableData = FXCollections.observableArrayList();


    static ObservableList<TableModel> ffmpegGetCodecs(DragEvent event){
        List<File> files = event.getDragboard().getFiles();

        new Thread(() -> {
            files.forEach(file -> {
                String filepath = file.getAbsolutePath();
                String query = String.format("%s -v error -select_streams a:0 -show_entries stream=codec_name -of default=noprint_wrappers=1:nokey=1 %s", FFPROBE_PATH, files.get(0).getAbsolutePath());
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


    static void startConversion(TableView<TableModel> fileList, String outputAudiocodec, String outputPath, ProgressBar progressBar){
        if (outputAudiocodec.equals("")) {
            warningDialog("Output codec is not selected");
        }
        if (outputPath.isEmpty()) {
            warningDialog("Destination was not specified");
        } else {
            progressBar.setProgress(0.0);

            new Thread(() -> {
                DoneFiles = 0.0;
                Integer tableSize = FileHandler.tableData.size();
                FileHandler.tableData.forEach((temp) -> {
                    String filename = temp.getFileName();
                    String filepath = temp.getFilepath();
                    String destination = outputPath + "\\" + filename;
                    temp.setStatus(startConversion(destination, filepath, outputAudiocodec));
                    fileList.refresh();
                    progress(tableSize, progressBar);
                });
            }).start();

        }


    }

    private static void progress(Integer tableSize, ProgressBar progressBar) {
        DoneFiles += 1.0;
        progressBar.setProgress((DoneFiles / tableSize));
    }

    private static String startConversion(String destination, String filepath, String outputAudiocodec) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String query = String.format("%s -loglevel error -i %s -c:v copy -c:a %s %s", FFMPEG_PATH, filepath, outputAudiocodec, destination);
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
