package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TableModel {
    private final StringProperty fileName;
    private final StringProperty audioCodec;
    private final StringProperty status;

    public TableModel() {
        this(null, null, null);
    }

    public TableModel(String fileName, String audioCodec, String status) {
        this.fileName = new SimpleStringProperty(fileName);
        this.audioCodec = new SimpleStringProperty(audioCodec);
        this.status = new SimpleStringProperty(status);
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public String getAudioCodec() {
        return audioCodec.get();
    }

    public StringProperty audioCodecProperty() {
        return audioCodec;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public String getStatus() {
        return status.get();
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public void setAudioCodec(String audioCodec) {
        this.audioCodec.set(audioCodec);
    }

    public void setStatus(String status) {
        this.status.set(status);
    }
}
