package org.ai.toolkit.aitk.modelzoo.util;

import ai.djl.modality.cv.Image;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.ai.toolkit.aitk.common.constant.PathConstants;
import org.ai.toolkit.aitk.common.errorcode.AitkErrorCode;
import org.ai.toolkit.aitk.common.exception.AitkException;
import org.ai.toolkit.aitk.modelzoo.constant.FileExtension;

public class FileUtil {

    private static final String HOST = "http://localhost:8080";

    public static String saveImage(Image image, FileExtension fileExtension) {
        try {
            String attachmentPath =
                    File.separator + PathConstants.ATTACHMENT_PATH + File.separator + fileExtension.getFileType().name()
                            .toLowerCase();
            String filePath =
                    PathConstants.ATTACHMENT_PARENT_PATH + attachmentPath;
            Path outputDir = Paths.get(filePath);
            Files.createDirectories(outputDir);
            String fileName = UUID.randomUUID().toString() + fileExtension.getSuffix();
            Path imagePath = outputDir.resolve(fileName);
            image.save(Files.newOutputStream(imagePath), fileExtension.name());
            return HOST + attachmentPath + File.separator + fileName;
        } catch (Exception e) {
            throw new AitkException(AitkErrorCode.KNOWN_ERROR, "image save error", e);
        }
    }

    public static Path saveFile(InputStream inputStream, String fileName) {
        try {
            String attachmentPath =
                    File.separator + PathConstants.ATTACHMENT_PATH + File.separator + "models";
            String filePath =
                    PathConstants.ATTACHMENT_PARENT_PATH + attachmentPath;
            Path outputDir = Paths.get(filePath);
            Files.createDirectories(outputDir);
            Path path = outputDir.resolve(fileName);
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            return path;
        } catch (Exception e) {
            throw new AitkException(AitkErrorCode.KNOWN_ERROR, "file save error", e);
        }

    }
}
