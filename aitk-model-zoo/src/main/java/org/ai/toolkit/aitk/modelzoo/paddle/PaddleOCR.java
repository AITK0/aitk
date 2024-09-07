package org.ai.toolkit.aitk.modelzoo.paddle;

import ai.djl.modality.Classifications;
import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.paddlepaddle.zoo.cv.imageclassification.PpWordRotateTranslatorFactory;
import ai.djl.paddlepaddle.zoo.cv.objectdetection.PpWordDetectionTranslatorFactory;
import ai.djl.paddlepaddle.zoo.cv.wordrecognition.PpWordRecognitionTranslatorFactory;
import ai.djl.repository.zoo.Criteria;
import org.ai.toolkit.aitk.modelzoo.AbstractBaseModelDefinition;
import org.ai.toolkit.aitk.modelzoo.bean.ModelBasicInfo;
import org.ai.toolkit.aitk.modelzoo.bean.Param;
import org.ai.toolkit.aitk.modelzoo.constant.EngineEnum;
import org.ai.toolkit.aitk.modelzoo.constant.FileExtension;
import org.ai.toolkit.aitk.modelzoo.constant.ModelTypeEnum;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class PaddleOCR extends AbstractBaseModelDefinition<Image, DetectedObjects> {


    @Override
    public String getId() {
        return "cv/ocr/paddleOCR";
    }

    @Override
    public List<String> getModelFileList() {
        return Arrays.asList(
                "cv/ocr/paddle/word_detection/inference.pdiparams",
                "cv/ocr/paddle/word_detection/inference.pdiparams.info",
                "cv/ocr/paddle/word_detection/inference.pdmodel",
                "cv/ocr/paddle/word_recognition/inference.pdiparams",
                "cv/ocr/paddle/word_recognition/inference.pdiparams.info",
                "cv/ocr/paddle/word_recognition/inference.pdmodel",
                "cv/ocr/paddle/word_recognition/ppocr_keys_v1.txt",
                "cv/ocr/paddle/word_rotation/inference.pdiparams",
                "cv/ocr/paddle/word_rotation/inference.pdiparams.info",
                "cv/ocr/paddle/word_rotation/inference.pdmodel"
        );
    }

    @Override
    public ModelBasicInfo getModelBasicInfo() {
        return new ModelBasicInfo("Paddle OCR", "");
    }

    @Override
    public Image postProcessBeforeModel(Input input) throws Exception {
        Param param = getRequestParams().get(0);
        byte[] bytes = input.getAsBytes(param.getName());
        return ImageFactory.getInstance().fromInputStream(new ByteArrayInputStream(bytes));
    }

    @Override
    public Output postProcessAfterModel(Input input, DetectedObjects modelOutput) throws Exception {
        List<DetectedObjects.DetectedObject> boxes = modelOutput.items();
        Param param = getRequestParams().get(0);
        byte[] bytes = input.getAsBytes(param.getName());
        Image img = ImageFactory.getInstance().fromInputStream(new ByteArrayInputStream(bytes));
        StringBuilder stringBuilder = new StringBuilder();
        for (DetectedObjects.DetectedObject box : boxes) {
            Image subImg = getSubImage(img, box.getBoundingBox());
            if (subImg.getHeight() * 1.0 / subImg.getWidth() > 1.5) {
                subImg = rotateImg(subImg);
            }
            Classifications rotator = inferenceExecutor.asyncExecute(getId(), subImg, 2);
            Classifications.Classification result = rotator.best();
            if ("Rotate".equals(result.getClassName()) && result.getProbability() > 0.8) {
                subImg = rotateImg(subImg);
            }
            String text = inferenceExecutor.asyncExecute(getId(), subImg, 1);
            stringBuilder.append(text).append("\n");
        }
        Output output = new Output();
        output.add("text", stringBuilder.toString());
        return output;
    }

    @Override
    public List<Criteria> getCriteriaList() {
        Criteria<Image, DetectedObjects> criteriaDetection =
                Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        .optModelPath(getModelPath("cv/ocr/paddle/word_detection"))
                        .optTranslatorFactory(new PpWordDetectionTranslatorFactory())
                        .build();

        Criteria<Image, String> criteriaRecognition =
                Criteria.builder()
                        .setTypes(Image.class, String.class)
                        .optModelPath(getModelPath("cv/ocr/paddle/word_recognition"))
                        .optTranslatorFactory(new PpWordRecognitionTranslatorFactory())
                        .build();

        Criteria<Image, Classifications> criteriaRotation =
                Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                        .optTranslatorFactory(new PpWordRotateTranslatorFactory())
                        .optModelPath(getModelPath("cv/ocr/paddle/word_rotation"))
                        .build();
        return Arrays.asList(criteriaDetection, criteriaRecognition, criteriaRotation);
    }

    @Override
    public List<EngineEnum> getEngineList() {
        return Arrays.asList(EngineEnum.PaddlePaddle, EngineEnum.PaddlePaddle, EngineEnum.PaddlePaddle);
    }

    @Override
    public ModelTypeEnum getModelType() {
        return ModelTypeEnum.OCR;
    }

    @Override
    public List<Param> getRequestParams() {
        List<Param> params = new ArrayList<>();
        params.add(new Param("file", FileExtension.PNG));
        return params;
    }

    @Override
    public List<Param> getResponseParams() {
        List<Param> params = new ArrayList<>();
        params.add(new Param("text", FileExtension.TEXT));
        return params;
    }


    private Image rotateImg(Image image) {
        try (NDManager manager = NDManager.newBaseManager()) {
            NDArray rotated = NDImageUtils.rotate90(image.toNDArray(manager), 1);
            return ImageFactory.getInstance().fromNDArray(rotated);
        }
    }

    private Image getSubImage(Image img, BoundingBox box) {
        Rectangle rect = box.getBounds();
        double[] extended = extendRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        int width = img.getWidth();
        int height = img.getHeight();
        int[] recovered = {
                (int) (extended[0] * width),
                (int) (extended[1] * height),
                (int) (extended[2] * width),
                (int) (extended[3] * height)
        };
        return img.getSubImage(recovered[0], recovered[1], recovered[2], recovered[3]);
    }

    private double[] extendRect(double xmin, double ymin, double width, double height) {
        double centerx = xmin + width / 2;
        double centery = ymin + height / 2;
        if (width > height) {
            width += height * 2.0;
            height *= 3.0;
        } else {
            height += width * 2.0;
            width *= 3.0;
        }
        double newX = centerx - width / 2 < 0 ? 0 : centerx - width / 2;
        double newY = centery - height / 2 < 0 ? 0 : centery - height / 2;
        double newWidth = newX + width > 1 ? 1 - newX : width;
        double newHeight = newY + height > 1 ? 1 - newY : height;
        return new double[]{newX, newY, newWidth, newHeight};
    }
}
