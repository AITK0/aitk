package org.ai.toolkit.aitk.modelzoo.paddle;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.Image.Flag;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.NoBatchifyTranslator;
import ai.djl.translate.TranslatorContext;
import ai.djl.util.Utils;
import org.ai.toolkit.aitk.common.git.GitUtil;
import org.ai.toolkit.aitk.modelzoo.ModelRepositoryType;

import java.io.FileInputStream;
import java.util.List;


public class MobileNetTranslator implements NoBatchifyTranslator<Image, Classifications> {
    private List<String> classes;

    private ModelRepositoryType modelRepositoryType;

    public MobileNetTranslator(ModelRepositoryType modelRepositoryType) {
        this.modelRepositoryType = modelRepositoryType;
    }

    @Override
    public void prepare(TranslatorContext ctx) throws Exception {
        if (classes == null) {
            classes = Utils.readLines(new FileInputStream(GitUtil.getModelBasePath(modelRepositoryType.getDefaultGitEnum()) + "/cv/classification/paddle/animals_classification/labelList.txt"));
        }
    }

    @Override
    public Classifications processOutput(TranslatorContext ctx, NDList list) throws Exception {
        NDArray probabilitiesNd = list.singletonOrThrow();
        probabilitiesNd = probabilitiesNd.softmax(1);
        return new Classifications(classes, probabilitiesNd);
    }


    @Override
    public NDList processInput(TranslatorContext ctx, Image input) throws Exception {
        NDArray array = input.toNDArray(ctx.getNDManager(), Flag.COLOR);
        array =
            NDImageUtils.resize(
                array, 256, 256);
        array=NDImageUtils.centerCrop(array,224,224);
        array = array.transpose(2, 0, 1).toType(DataType.FLOAT32,false).div(255f);
        NDArray mean =
            array.getManager().create(new float[]{0.485f, 0.456f, 0.406f}, new Shape(3, 1, 1));
        NDArray std =
            array.getManager().create(new float[]{0.229f, 0.224f, 0.225f}, new Shape(3, 1, 1));
        array = array.sub(mean).div(std);
        array = array.expandDims(0);
        return new NDList(array);
    }
}
