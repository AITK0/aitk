<template>
    <div id="app">
        <el-container style="height: 100vh ;  display: flex;  border: 1px solid #eee">
            <el-header class="header">
                AITK
            </el-header>
            <el-container>
                <el-aside width="300px" class="sidebar">
                    <el-input
                            placeholder="请输入搜索内容"
                            v-model="filterText"
                            @input="handleFilter"
                            class="text"
                    ></el-input>
                    <el-tree
                            :data="menuData"
                            :props="defaultProps"
                            :filter-node-method="filterNode"
                            ref="tree"
                            :default-expand-all="true"
                            @node-click="handleNodeClick"
                            class="custom-tree"
                    ></el-tree>
                </el-aside>
                <el-main class="main-content">
                    <router-view :key="routeKey"></router-view>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<script>
    import {getModelTreeData} from "./api"

    export default {
        data() {
            return {
                filterText: '', // 搜索文本  ,
                menuData: [],
                routeKey: 0,
                defaultProps: {
                    children: 'children',
                    label: 'label'
                }
            };
        },
        methods: {
            handleNodeClick(data) {
                if (data.path) {
                    this.routeKey++;
                    if (data.path == 'LLM') {
                        this.$router.push({name:"Llm", query:{id:data.id,path:data.path,label:data.label}});
                    } else {
                        this.$router.push({name:"Common", query:{id:data.id,path:data.path,label:data.label}});
                    }

                }
            },
            // 过滤树节点的方法
            filterNode(value, data) {
                if (!value) return true; // 如果没有搜索文本，显示所有节点
                return data.label.toLowerCase().includes(value.toLowerCase()); // 不区分大小写的包含检查
            },
            handleFilter() {
                this.$refs.tree.filter(this.filterText);
            }
        },
        created() {
            getModelTreeData(null, resp => {
                this.menuData = resp.data;
            });
        },
        watch: {
            '$route': function(to, from) {
                this.routeKey++;
            }
        }
    };
</script>

<style scoped>
    html, body {
        height: 100%;
        margin: 0;
        overflow: hidden;
    }

    #app {
        height: 100%;
        background: radial-gradient(circle at center, #303030 0%, #141414 100%);
        /* 金属黑渐变背景 */
    }

    .header {
        background-color: #222831; /* 深邃金属黑 */
        color: #C5C5C5; /* 亮银灰文字 */
        line-height: 60px;
        padding: 0 20px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15); /* 添加轻微阴影，增强立体感 */
    }

    .main-content {
        background: linear-gradient(to bottom, #4A4A4A, #222831); /* 从浅灰色渐变到深金属黑 */
        padding: 20px;
        border-radius: 10px;
        overflow-y: auto;
    }

    .sidebar {
        background-color: #333333; /* 调整为稍浅的金属黑 */
        color: #C5C5C5;
        padding: 20px;
        border-right: 1px solid #141414;
        border-radius: 10px 0 0 10px;
        overflow-y: auto;
    }

    /* 可选：增加鼠标悬停在侧边栏菜单项时的反馈效果 */
    .el-tree-node__content:hover {
        background-color: rgba(255, 255, 255, 0.1);
    }

    .el-tree-node__content:hover {
        background-color: rgba(255, 255, 255, 0.05);
    }

    /* 输入框和按钮样式调整，以适应金属黑主题 */
    .el-input__inner {
        background-color: #272727;
        color: #C5C5C5;
        border-color: #3A3A3A;
    }

    .el-button--primary {
        background-color: #4CAF50; /* 绿色按钮，与金属黑形成对比 */
        border-color: #4CAF50;
        color: #fff;
    }

    .el-button--primary:hover {
        background-color: #45a049;
        border-color: #45a049;
    }

    /* 添加样式以在输入框和树之间创建间距 */
    .text {
        margin-bottom: 10px; /* 在输入框下方添加间距 */
    }

    /* 为输入框和树形控件添加圆角 */
    .el-input__inner {
        border-radius: 5px; /* 输入框圆角 */
    }

    .el-tree {
        border-radius: 5px; /* 树形控件容器圆角 */
    }

    /* 可选：为树形控件的根节点添加内边距，避免内容紧贴边缘 */
    .el-tree::before {
        content: '';
        display: block;
        padding-top: 10px; /* 或根据实际情况调整 */
    }
</style>