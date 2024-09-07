<template>
  <div class="button-progress-container">
    <el-button :disabled="loading || loadingAndProcessing || unloading" @click="handleModelLoad">
      {{ modelStateText }}
    </el-button>
    <div v-if="loading" class="progress-right">
      <el-progress :percentage="progress" type="line"></el-progress>
    </div>
  </div>
</template>

<script>
import {getModelStateByModelId, startLoad, unloadModel} from "../api"

export default {
  name: 'ModelLoader',
  props: {
    modelName: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      modelLoaded: false,
      loading: false,
      loadingAndProcessing: true,
      progress: 0,
      intervalId: null, // 存储轮询定时器ID
      unloading: false,
    };
  },
  computed: {
    modelStateText() {
      if (this.loading) {
        return '正在下载';
      } else if (this.loadingAndProcessing) {
        return '正在加载..';
      } else if (this.modelLoaded) {
        return '卸载模型';
      } else if (this.unloading) {
        return '正在卸载..';
      }else {
        return '待加载';
      }
    }
  },
  watch: {
    modelName(newName, oldName) {
      if (newName !== oldName) {
        // 当modelName发生变化时，重新获取模型状态
        this.pollModelStatus();
      }
    }
  },
  methods: {
    isModelLoad() {
      return this.modelLoaded;
    },
    handleModelLoad() {
      if(this.modelLoaded) {
        this.$confirm('是否确定卸载模型?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.modelLoaded = false;
          this.unloadModelProcess();
        }).catch(() => {});
      } else {
        this.$confirm('是否确定加载模型?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.modelLoaded = false;
          this.startModelLoadProcess();
        }).catch(() => {});
      }
    },
    startModelLoadProcess() {
      this.loading = true;
      this.progress = 0;
      this.startReload();
    },
    unloadModelProcess() {
      this.loading = false;
      this.progress = 0;
      this.unloading = true;
      this.startUnload();
    },
    pollModelStatus() {
      this.fetchModelStatus()
          .then(() => {
            if ((this.loading || this.loadingAndProcessing || this.unloading) && !this.modelLoaded) {
              // 如果模型还在下载或加载中，继续轮询
              this.intervalId = setTimeout(() => {
                this.pollModelStatus();
              }, 2000); // 每2秒轮询一次
            }
          })
          .catch(error => {
            console.error('Error fetching model status:', error);
          });
    },
    fetchModelStatus() {
      let formData = new FormData();
      formData.append('modelId', this.modelName);
      if(!this.modelName) {
        return
      }
      return getModelStateByModelId(formData, response => {
        const modelStatus = response.data;
        if (modelStatus.state === 0) {
          this.loading = false;
          this.modelLoaded = false;
          this.unloading = false;
          this.loadingAndProcessing = false;
        } else if (modelStatus.state === 1) {
          this.loading = true;
          this.progress = modelStatus.progress;
        } else if (modelStatus.state === 2) {
          this.loading = false;
          this.loadingAndProcessing = true;
          this.modelLoaded = false;
          clearTimeout(this.intervalId);
          this.intervalId = null;
        } else if (modelStatus.state === 10) {
          this.loading = false;
          this.modelLoaded = true;
          clearTimeout(this.intervalId);
          this.intervalId = null;
          this.completeModelLoad();
        } else if (modelStatus.state === 20) {
          this.loading = false;
          this.modelLoaded = false;
          this.unloading = true;
          this.loadingAndProcessing = false;
        }
      });
    },
    startUnload() {
      if(!this.modelName) {
        return
      }
      let formData = new FormData();
      formData.append('modelId', this.modelName);
      unloadModel(formData, response => {
        this.modelLoaded = false;
        this.pollModelStatus();
      });
    },
    startReload() {
      if(!this.modelName) {
        return
      }
      let formData = new FormData();
      formData.append('modelId', this.modelName);
      startLoad(formData, response => {
        this.modelLoaded = false;
        this.pollModelStatus();
      });
    },
    completeModelLoad() {
      // 模型加载完成后的处理逻辑（预留未来扩展）
      this.loadingAndProcessing = false;
    }
  },
  beforeDestroy() {
    // 组件销毁前清除定时器，避免内存泄漏
    clearTimeout(this.intervalId);
    this.intervalId = null;
  }
};
</script>

<style scoped>
.button-progress-container {
  display: flex;
  align-items: center;
}

.progress-right {
  margin-left: 16px;
  flex-shrink: 0; /* 防止进度条在容器变窄时缩小 */
  width: 150px;
}
</style>