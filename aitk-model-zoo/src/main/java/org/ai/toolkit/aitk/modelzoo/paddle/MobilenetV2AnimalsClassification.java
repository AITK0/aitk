package org.ai.toolkit.aitk.modelzoo.paddle;

import ai.djl.modality.Classifications;
import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.Criteria;
import org.ai.toolkit.aitk.common.errorcode.AitkErrorCode;
import org.ai.toolkit.aitk.common.exception.AitkException;
import org.ai.toolkit.aitk.modelzoo.AbstractBaseModelDefinition;
import org.ai.toolkit.aitk.modelzoo.bean.ModelBasicInfo;
import org.ai.toolkit.aitk.modelzoo.bean.Param;
import org.ai.toolkit.aitk.modelzoo.constant.EngineEnum;
import org.ai.toolkit.aitk.modelzoo.constant.FileExtension;
import org.ai.toolkit.aitk.modelzoo.constant.ModelTypeEnum;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class MobilenetV2AnimalsClassification extends AbstractBaseModelDefinition<Image, Classifications> {

    @Override
    public String getId() {
        return "cv/image_classification/mobilenetV2AnimalsClassification";
    }

    @Override
    public List<String> getModelFileList() {
        return Arrays.asList(
                "cv/classification/paddle/animals_classification/animals.zip",
                "cv/classification/paddle/animals_classification/labelList.txt"
        );
    }

    @Override
    public ModelBasicInfo getModelBasicInfo() {
        return new ModelBasicInfo("Paddle7978种动物分类", "");
    }

    @Override
    public Image postProcessBeforeModel(Input input) throws Exception {
        Param param = getRequestParams().get(0);
        byte[] bytes = input.getAsBytes(param.getName());
        return ImageFactory.getInstance().fromInputStream(new ByteArrayInputStream(bytes));
    }

    @Override
    public Output postProcessAfterModel(Input input, Classifications modelOutput) throws Exception {
        Output output = new Output();
        output.add("text", modelOutput.best().toString());
        return output;
    }


    @Override
    public List<Criteria> getCriteriaList() {
        try {
            Path modelPath = getModelPath("cv/classification/paddle/animals_classification/animals.zip");
            Criteria<Image, Classifications> criteria =
                    Criteria.builder()
                            .setTypes(Image.class, Classifications.class)
                            .optModelPath(modelPath)
                            .optEngine("PaddlePaddle").optTranslator(new MobileNetTranslator(modelRepositoryType))
                            .build();
            return Arrays.asList(criteria);
        } catch (Exception e) {
            throw new AitkException(AitkErrorCode.MODEL_LOAD_ERROR, e);
        }
    }

    @Override
    public List<EngineEnum> getEngineList() {
        return Arrays.asList(EngineEnum.PaddlePaddle);
    }

    @Override
    public ModelTypeEnum getModelType() {
        return ModelTypeEnum.VISION_CLASSIFICATION;
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
}
