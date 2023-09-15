<template>
    <div class="app-container">
        <!-- 条件查询 -->        
        <el-form :inline="true" :model="query" size="small">
            <el-form-item label="流程名称">
                <el-input v-model.trim="query.name" placeholder="请输入名称"></el-input>
            </el-form-item>
            <el-form-item label="标识Key">
              <el-input v-model.trim="query.key" placeholder="请输入标识Key"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button icon="el-icon-search" type="primary" @click="queryData">查询</el-button>
                <el-button icon="el-icon-refresh"  @click="reload">重置</el-button>
            </el-form-item>
        </el-form>
        <el-button icon="el-icon-upload" size="small" type="primary" @click="clickDeploy">部署流程文件</el-button>
        <el-alert style="margin:10px 0" title="说明：当有多个相同标识Key的流程时，只显示其最新版本流程。" type="info" show-icon :closable="false" />

        <!-- stripe 带斑马纹 -->
        <el-table :data="list" stripe border style="width: 100%">
            <el-table-column align="center" type="index" label="序号" width="50"></el-table-column>
            <el-table-column align="center" prop="name" label="流程名称" min-width="160"></el-table-column>
            <el-table-column  align="center" prop="key" label="标识Key" min-width="120"></el-table-column>
            <el-table-column  align="center" prop="state" label="状态" width="90" >
                <template slot-scope="{row}">
                    <!-- 流程定义状态，已启动、已暂停 -->
                    <el-tag :type="row.state == '已启动' ? 'success': 'danger'">{{row.state}}</el-tag>
                </template>
            </el-table-column>
            <el-table-column  align="center" prop="xmlName" label="流程XML" min-width="190" >
                <template slot-scope="{row}">
                    <!-- 注意组件上使用原生事件，要加 .active -->
                    <el-link type="primary" @click.native="clickExportXML(row.id)">{{row.xmlName}}</el-link>
                </template>
            </el-table-column>
            <el-table-column  align="center" prop="pngName" label="流程图片" min-width="240" >
                <template slot-scope="{row}">
                    <el-link type="primary" @click="clickPreviewImg(row.id)">{{row.pngName}}</el-link>
                </template>
            </el-table-column>
            <el-table-column  align="center" prop="version" label="版本号" width="90" >
              <template slot-scope="{row}"> v{{row.version}}.0</template>
            </el-table-column>
            <el-table-column  align="center" prop="businessRoute" label="关联业务路由名" min-width="130"></el-table-column>
            <el-table-column  align="center" prop="formName" label="关联表单组件名" min-width="130"></el-table-column>
            <el-table-column  align="center" prop="deploymentTime" label="部署日期" width="160"></el-table-column>
            <el-table-column  align="center" label="操作" fixed="right" width="230">
                <template slot-scope="{row}">
                    <el-button type="primary" size="mini" @click="clickProcessConfig(row.key)">流程配置</el-button>
                    <el-popconfirm :title="row.state == '已启动' ? 
                        `暂停后，此流程下的所有任务都不允许往后流转，您确定暂停【${row.name || row.key}】吗？`: 
                        `启动后，此流程下的所有任务都允许往后流转，您确定启动【${row.name || row.key}】吗？`" 
                        @onConfirm="updateState(row.id)" >
                        <el-button v-if="row.state == '已启动'" slot="reference" type="warning" size="mini">暂停</el-button>
                        <el-button v-else slot="reference" type="primary" size="mini">启动</el-button>
                    </el-popconfirm>
                    <!-- 注意：当前element版本使用confirm事件无效，使用onConfirm才有效 -->
                    <el-popconfirm :title="`您确定删除【${row.name || row.key}】吗？`" @onConfirm="clickDelete(row)" >
                        <el-button slot="reference" type="danger" size="mini">删除</el-button>
                    </el-popconfirm>
                </template>
            </el-table-column>
        </el-table>

        <!-- 分页组件 -->
         <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="page.current"
            :page-sizes="[10, 20, 50]"
            :page-size="page.size"
            layout="total, sizes, prev, pager, next, jumper"
            :total="page.total">
        </el-pagination>

        <!-- 部署流程文件 -->
        <process-deploy ref="deployProcess" ></process-deploy>

        <!-- 预览图片 -->
        <process-preview  ref="previewRef" :url="url"></process-preview>

        <!-- 流程配置 -->
        <process-config ref="configRef" :processKey="key"></process-config>
    </div>
</template>
<script>
import api from '@/api/process'
import ProcessDeploy from './components/Deploy'
import ProcessPreview from './components/Preview'
import ProcessConfig from './components/Config'


export default {
    name: 'Deploy', // 和对应路由表中配置的name值一致

    components: {ProcessDeploy, ProcessPreview, ProcessConfig},

    data() {
        return {
            url: null, // 流程图
            key: null, // 流程key
            list: [
              {
                "id":"leave2:3:62509",
                "name":"请假分支演示",
                "key":"leave2",
                "version":3,
                "state": '已启动',
                "xmlName":"请假分支演示.bpmn20.xml",
                "pngName":"请假分支演示.leave2.png",
                "businessRoute": "Leave", // 业务列表路由名
                "formName": "LeaveForm", // 表单组件名
                "deploymentTime":"2020-01-24 17:07:16",
            },
            {
                "id":"test:1:60011",
                "name":"连线注释显示测试",
                "key":"test",
                "version":1,
                "state": '已暂停',
                "xmlName":"连线注释显示测试.bpmn20.xml",
                "pngName":"连线注释显示测试.test.png",
                "businessRoute": "Line", //业务列表路由名
                "formName": "lineForm", // 表单组件名
                "deploymentTime":"2020-12-24 11:07:10",
            },
            ],
            page: {
                current: 1,
                size: 20,
                total: 0
            },
            query: {}, // 查询条件
        }
    },

    created() {
        this.fetchData()
    },

    methods: {
        // 分页条件查询文章列表
        async fetchData() {
            const { data } = await api.getList(this.query, this.page.current, this.page.size)
            this.list = data.records
            this.page.total = data.total
        },

        // 当每页显示多少条改变后触发
        handleSizeChange(val) {
            this.page.size = val
            this.fetchData()
        },
        // 切换页码触发
        handleCurrentChange(val) {
            this.page.current = val
            this.fetchData()
        },

        // 条件查询方法
        queryData() {
            this.page.current = 1
            this.fetchData()
        },

        // 刷新重置
        reload() {
            this.query = {}
            this.fetchData()
        },

        // 弹出部署流程文件窗口
        clickDeploy() {
            this.$refs.deployProcess.visible=true
        },

        // 删除
        async clickDelete(row) {
            // 发送删除请求
            const {code} = await api.deleteByDeploymentId(row.deploymentId, row.key)
            if(code === 20000) {
                this.$message.success('删除成功')
                // 刷新列表数据
                this.fetchData()
            }
        },

        // 挂起或激活流程定义
        async updateState(definitionId) {
            const {code} = await api.updateProcessDefinitionState(definitionId)
            if(code === 20000) {
                this.$message.success('操作成功')
                // 刷新列表数据
                this.fetchData()
            }
        },

        // 导出xml流程文件
        clickExportXML(id) {
            // window.open(process.env.VUE_APP_BASE_URL + `/process/export/xml/${id}`)
            window.open(api.getProcessFileURL('xml', id))
        },

        // 预览图片
        clickPreviewImg(id) {
            // this.id = id
            this.url = api.getProcessFileURL('png', id)
            this.$refs.previewRef.visible = true
            // window.open(process.env.VUE_APP_BASE_URL + `/process/export/png/${id}`)
        },

        // 流程配置
        clickProcessConfig(key) {
            this.key = key
            this.$refs.configRef.visible = true
        }

    }
}
</script>