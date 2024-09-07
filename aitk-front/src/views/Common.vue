<template>
  <div>
    <div class="breadcrumb">
      <el-breadcrumb separator-class="el-icon-arrow-right">
        <el-breadcrumb-item v-for="item in menuPath" :key="item">{{item}}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-row>
      <el-col :span="3">
        <model-loader :modelName="selectedModel" ref="modelLoadRef" style="margin-left: 10px"/>
      </el-col>
      <el-col :span="2">
        <el-button type="success" @click="inference">推理</el-button>
      </el-col>
    </el-row>
    <!--    <div v-for="item in loadModelParams" :key="item">-->
    <!--      <el-upload v-if="['PNG','JPG','MP3','MAV','MP4','MOV','M4V'].includes(item.fileExtension)"-->
    <!--                 ref="upload"-->
    <!--                 action=""-->
    <!--                 :auto-upload="false"-->
    <!--                 :limit="1"-->
    <!--                 :multiple="false"-->
    <!--                 :on-change="handleChange"-->
    <!--                 :file-list="loadModelData[item.name]"-->
    <!--                 list-type="picture-card">-->
    <!--        <i class="el-icon-plus"></i>-->
    <!--      </el-upload>-->
    <!--      <el-input v-else-->
    <!--                type="textarea"-->
    <!--                :rows="5"-->
    <!--                v-model="loadModelData[item.name]">-->
    <!--      </el-input>-->
    <!--    </div>-->
    <el-divider></el-divider>
    <div v-for="item in requestParams" :key="item">
      <el-upload v-if="['PNG','JPG','MP3','MAV','MP4','MOV','M4V'].includes(item.fileExtension)"
                 ref="upload"
                 action=""
                 :auto-upload="false"
                 :limit="1"
                 :multiple="false"
                 :on-change="(file, fileList) => {handleChange(file, fileList, item)}"
                 list-type="picture-card">
        <i class="el-icon-plus"></i>
      </el-upload>
      <el-input v-else
                type="textarea"
                :rows="5"
                v-model="requestData[item.name]">
      </el-input>
    </div>
    <el-divider></el-divider>
    <div v-for="item in responseParams" :key="item">
      <el-image v-if="['PNG','JPG','MP3','MAV','MP4','MOV','M4V'].includes(item.fileExtension)"
                style="width: 300px; height: 300px"
                :src="responseData[item.name]">
      </el-image>
      <el-input v-else
                type="textarea"
                :rows="10"
                v-model="responseData[item.name]">
      </el-input>
    </div>
  </div>

</template>

<script>

import {getModelParamByModelId, infer,getMenuPath} from "../api"

export default {
  data() {
    return {
      selectedModel: '',
      loadModelParams: [],
      requestParams: [],
      responseParams: [],
      loadModelData: {},
      requestData: {},
      responseData: {},
      menuPath:[]
    };
  },
  methods: {
    inference() {
      if (!this.$refs.modelLoadRef.isModelLoad()) {
        this.$message('请先加载模型...');
        return
      }
      let formData = new FormData();
      this.requestParams.forEach(e => {
        formData.append("modelId", this.selectedModel);
        formData.append(e.name, this.requestData[e.name]);
      })
      infer(formData, resp => {
        this.responseParams.forEach(e => {
          this.$set(this.responseData, e.name, resp[e.name]);
        });

        console.log(this.responseData)
      })
    },
    handleChange(file, fileList, item) {
      this.requestData[item.name] = file.raw;
    }
  },

  mounted() {
    this.selectedModel = this.$route.query.id;
    let param = {modelId: this.selectedModel}
    getModelParamByModelId(param, resp => {
      if (resp.data && resp.code == 200) {
        this.loadModelParams = resp.data.loadModelParams;
        this.requestParams = resp.data.requestParams;
        this.responseParams = resp.data.responseParams;
      }
    });
    getMenuPath(param,resp => {
      if (resp.data && resp.code == 200) {
        this.menuPath=resp.data;
      }
    });
  }
}
</script>

<style scoped>
.breadcrumb {
  margin-bottom: 30px;
}

::v-deep .el-breadcrumb__inner {
  color: white !important;
}
</style>