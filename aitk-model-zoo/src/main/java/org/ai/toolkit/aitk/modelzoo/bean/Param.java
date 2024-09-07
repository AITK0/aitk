package org.ai.toolkit.aitk.modelzoo.bean;

import java.io.Serializable;
import org.ai.toolkit.aitk.modelzoo.constant.FileExtension;

public class Param implements Serializable {

    private static final long serialVersionUID = -4676008816155352269L;

    private String name;

    private FileExtension fileExtension;

    public Param() {
    }

    public Param(String name, FileExtension fileExtension) {
        this.name = name;
        this.fileExtension = fileExtension;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileExtension getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(FileExtension fileExtension) {
        this.fileExtension = fileExtension;
    }
}
