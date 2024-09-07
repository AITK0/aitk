package org.ai.toolkit.aitk.modelzoo.constant;

import java.util.stream.Stream;

public enum FileExtension {

    PNG(IOTypeEnum.Image, ".png"),
    JPG(IOTypeEnum.Image, ".jpg"),
    MP3(IOTypeEnum.VOICE, ".mp3"),
    MAV(IOTypeEnum.VOICE, ".mav"),
    MP4(IOTypeEnum.VIDEO, ".mp4"),
    MOV(IOTypeEnum.VIDEO, ".mov"),
    M4V(IOTypeEnum.VIDEO, ".m4v"),
    TEXT(IOTypeEnum.TEXT, null),
    STREAM(IOTypeEnum.STREAM, null);

    private IOTypeEnum fileType;

    private String suffix;

    FileExtension(IOTypeEnum fileType, String suffix) {
        this.fileType = fileType;
        this.suffix = suffix;
    }

    public IOTypeEnum getFileType() {
        return fileType;
    }

    public void setFileType(IOTypeEnum fileType) {
        this.fileType = fileType;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
