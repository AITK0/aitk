package org.ai.toolkit.aitk.modelzoo;

import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.repository.zoo.Criteria;

import java.util.ArrayList;
import java.util.List;
import org.ai.toolkit.aitk.modelzoo.bean.ModelBasicInfo;
import org.ai.toolkit.aitk.modelzoo.bean.Param;
import org.ai.toolkit.aitk.modelzoo.constant.EngineEnum;
import org.ai.toolkit.aitk.modelzoo.constant.ModelTypeEnum;

public interface ModelDefinition<P, Q> {

    String getId();

    List<String> getModelFileList();

    ModelBasicInfo getModelBasicInfo();

    P postProcessBeforeModel(Input input) throws Exception;

    Output postProcessAfterModel(Input input, Q modelOutput) throws Exception;

    List<Criteria> getCriteriaList();

    List<EngineEnum> getEngineList();

    ModelTypeEnum getModelType();

   default List<Param> getLoadModelParams(){
       return new ArrayList<>();
   }

    List<Param> getRequestParams();

    List<Param> getResponseParams();
}
