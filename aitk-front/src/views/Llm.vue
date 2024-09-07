<template>
    <div class="chat-wrap">
      <div class="breadcrumb">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item v-for="item in menuPath" :key="item">{{item}}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div style="display: flex; align-items: center; margin-bottom: 10px;">
        <span style="color: white">选择模型：</span>
        <el-select v-model="selectedModel" placeholder="请选择" style="flex: none;max-width: 200px;">
          <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value">
          </el-option>
        </el-select>
        <model-loader :modelName="selectedModel" ref="modelLoadRef" style="margin-left: 10px"/>
      </div>
        <div class="chat-container" style="background-color: #000; color: #fff;">
            <el-row>
                <el-col :span="24">
                    <div class="chat-messages" ref="messagesContainer">
                        <div
                                v-for="(msg, index) in messages"
                                :key="index"
                                :class="{ 'self': msg.self }" v-html="msg.text">
                        </div>
                    </div>
                </el-col>
            </el-row>
        </div>
        <div class="input-wrap">
            <el-row>
                <el-col :span="22">
                    <el-input type="textarea"
                              :rows="2"
                              v-model="newMessage"
                              placeholder="Type your message..."
                              @keyup.enter.native="sendMessage"
                    ></el-input>
                </el-col>
                <el-col :span="2">
                    <el-button type="primary" @click="sendMessage">发送</el-button>
                </el-col>
            </el-row>
        </div>
    </div>
</template>

<script>
    import MarkdownIt from 'markdown-it';
    import {getLllModelVOByModelName, getMenuPath, handleMessage} from "../api"


    export default {
        data() {
            return {
                ws: null, // 用于存储WebSocket实例
                messages: [],
                newMessage: '',
                md: new MarkdownIt(), // 初始化Markdown解析器
                options: [],
                selectedModel: '',
                menuPath:[]
            };
        },
        methods: {
            initWebSocket() {
                this.ws = new WebSocket('ws://localhost:8080/websocket/1'); // 替换为你的WebSocket服务器地址

                // 监听WebSocket打开事件
                this.ws.addEventListener('open', () => {
                    console.log('WebSocket连接成功');
                    // 可以在这儿发送认证信息或其他初始化消息
                });

                // 监听WebSocket消息事件
                this.ws.addEventListener('message', (event) => {
                    const message = event.data;
                    console.log('接收到消息:', message);
                    this.receiveMessage(JSON.parse(message));
                });

                // 监听WebSocket关闭事件
                this.ws.addEventListener('close', () => {
                    console.log('WebSocket连接关闭');
                });

                // 监听WebSocket错误事件
                this.ws.addEventListener('error', (error) => {
                    console.error('WebSocket错误:', error);
                });
            },

            sendRemoteMessage(message) {
                if (this.ws && this.ws.readyState === WebSocket.OPEN) {
                    this.ws.send(message);
                } else {
                    console.error('WebSocket连接未建立或已关闭');
                }
            },

            // 可以添加心跳检测和重连逻辑
            heartbeat() {
                // 定义心跳检测和重连的逻辑
            },

            closeWebSocket() {
                if (this.ws) {
                    this.ws.close();
                }
            },

            sendMessage() {
                if(!this.$refs.modelLoadRef.isModelLoad()){
                    this.$message('请先加载模型...');
                    return
                }
                if (this.newMessage.trim()) {
                    const htmlContent = this.md.render(this.newMessage);
                    this.messages.push({historyMsg:this.newMessage,text: htmlContent, self: true});
                    const context = this.messages.map(msg => `${msg.historyMsg}`).join("\n");
                    const chat ={"stream":true,"temperature":0.7,"prompt":context}
                    console.log(context)
                    let formData = new FormData();
                    formData.append('modelId', this.selectedModel);
                    formData.append("text", JSON.stringify(chat));
                    handleMessage(formData, response => {
                    });
                    this.newMessage = ''; // 清空输入框
                    // 这里可以添加发送消息到服务器的逻辑
                }
            },
            renderMarkdown(text) {
                return this.md.render(text);
            },
            receiveMessage(message) {
                if (message.text.length == 0){
                    return;
                }
                const json = JSON.parse(message.text.replace("data:",""));
                console.log(json);
                const messageToUpdate = this.messages.find(msg => msg.id != null && msg.id === message.id);
                if (messageToUpdate) {
                    messageToUpdate.historyMsg = messageToUpdate.historyMsg +json.content;
                    messageToUpdate.text = this.renderMarkdown(messageToUpdate.historyMsg);
                } else {
                    this.messages.push({
                        id: message.id,
                        historyMsg: json.content,
                        text: this.renderMarkdown(json.content),
                        self: false
                    });
                }
                if (this.$refs.messagesContainer) {
                    // 确保messagesContainer是一个可滚动元素且已加载
                    this.$refs.messagesContainer.scrollTop = this.$refs.messagesContainer.scrollHeight - 1;
                } else {
                    console.log('messagesContainer尚未就绪，无法滚动。');
                }
            }
        },
        mounted() {
            const modelType = this.$route.query.label;
            getLllModelVOByModelName({modelName: modelType}, resp => {
                if (resp.data) {
                    resp.data.forEach((v, index) => {
                        this.options.push({label: v.name, value: v.id});
                        this.selectedModel = this.options[0].value;
                    });
                    this.initWebSocket();
                }
            });
            getMenuPath({modelId:this.$route.query.id },resp => {
              if (resp.data && resp.code == 200) {
                this.menuPath=resp.data;
              }
            });

        },
        created() {

        }
    };
</script>

<style scoped>
    .chat-wrap {
        position: relative; /* 为包含元素设置相对定位 */
        width: 100%; /* 确保聊天包裹容器宽度适应页面 */
        height: 100%; /* 或根据需要设定高度 */
    }

    .input-wrap {
        width: 100%; /* 输入框容器宽度与聊天容器相同 */
        /*position: absolute; !* 设置绝对定位 *!*/
        bottom: 0; /* 紧贴底部 */
        left: 0; /* 左侧对齐 */
        box-sizing: border-box; /* 包含内边距和边框的宽度计算 */
        padding: 10px; /* 根据需要调整内边距 */
        background-color: #303030; /* 与聊天窗口背景颜色相匹配，可选 */
    }

    .chat-container {
        height: 80vh;
        display: flex;
        flex-direction: column;
        background-image: linear-gradient(to bottom, #303030, #141414);
        position: relative;
        max-height: calc(100vh - 200px); /* 举例：减去头部、底部和一些额外空间的高度 */
        overflow-y: auto;
    }

    .chat-messages {
        flex-grow: 1; /* 允许该元素在可用空间中增长 */
        overflow-y: auto; /* 当内容超出时自动显示垂直滚动条 */
        padding: 1rem;
        display: flex;
        flex-direction: column; /* 让最新消息始终在底部 */
    }

    .self {
        text-align: left;
    }

    .input-row {
        position: absolute;
        bottom: 0;
        width: 100%;
        justify-content: space-between;
        padding: 10px;
        box-sizing: border-box;
    }

    .chat-messages > div {
        /* 基础样式 */
        padding: 10px;
        margin: 10px 0;
        max-width: 75%; /* 控制气泡的最大宽度 */
        clear: both; /* 确保消息换行 */
        white-space: normal; /* 允许文本正常换行 */
        word-wrap: break-word; /* 允许长单词换行到下一行 */
        word-break: break-word; /* 对于非中日韩文本，建议使用这个以更好地处理长单词换行 */
    }

    /* 发送者（自己）的气泡样式 */
    .chat-messages > div.self {
        align-self: flex-end; /* 将气泡置于右侧 */
        background-color: rgba(255, 255, 255, 0.15); /* 浅灰色背景 */
        border-radius: 15px 15px 15px 0; /* 右侧大圆角 */
        position: relative; /* 用于定位三角形箭头 */
    }

    /* 接收者（他人）的气泡样式 */
    .chat-messages > div:not(.self) {
        align-self: flex-start; /* 将气泡置于左侧 */
        background-color: rgba(255, 255, 255, 0.1); /* 更浅的灰色背景 */
        border-radius: 15px 15px 0 15px; /* 左侧大圆角 */
        position: relative; /* 用于定位三角形箭头 */
    }

    /* 添加三角形箭头 */
    .chat-messages > div::after {
        content: "";
        position: absolute;
        width: 0;
        height: 0;
        border-style: solid;
    }

    .chat-messages > div.self::after {
        left: 100%;
        top: 50%;
        margin-top: -5px;
        border-width: 5px 10px 5px 0;
        border-color: transparent rgba(255, 255, 255, 0.15) transparent transparent;
    }

    .chat-messages > div:not(.self)::after {
        right: 100%;
        top: 50%;
        margin-top: -5px;
        border-width: 5px 0 5px 10px;
        border-color: transparent transparent transparent rgba(255, 255, 255, 0.1);
    }
    .breadcrumb {
      margin-bottom: 30px;
    }

    ::v-deep .el-breadcrumb__inner {
      color: white !important;
    }
</style>