<template>
    <div class="app-container">
        <!-- 条件查询 -->        
        <el-form :inline="true" :model="query" size="small">
            <el-form-item label="模型名称">
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
        <el-row style="margin-bottom:20px">
          <el-button icon="el-icon-plus" size="small" type="primary" @click="clickAdd">新增流程模型</el-button>
        </el-row>

        <!-- stripe 带斑马纹 -->
        <el-table :data="list" stripe border style="width: 100%">
            <el-table-column fixed align="center" type="index" label="序号" width="50"></el-table-column>
            <el-table-column fixed align="center" prop="name" label="模型名称"  min-width="160"></el-table-column>
            <el-table-column  align="center" prop="key" label="标识Key"  min-width="120"></el-table-column>
            <el-table-column align="center" prop="version" label="版本号" width="90" >
              <template slot-scope="{row}"> v{{row.version}}.0</template>
            </el-table-column>
            <el-table-column  align="center" prop="description" label="备注说明"  min-width="130"></el-table-column>
            <el-table-column  align="center" prop="createDate" label="创建时间" width="160"></el-table-column>
            <el-table-column  align="center" prop="updateDate" label="更新时间" width="160"></el-table-column>
            <el-table-column  align="center" label="操作" fixed="right" width="245">
                <template slot-scope="{row}">
                    <el-button type="text" size="small" @click="clickDesign(row.id)">设计流程</el-button>
                    <el-popconfirm :title="`您确定部署【${row.name}】吗？`" @onConfirm="clickDeploy(row.id)" style="margin: 0 5px" >
                        <el-button slot="reference" type="text" size="small">部署流程</el-button>
                    </el-popconfirm>
                    <el-button type="text" size="small" @click="clickExportZip(row.id)">导出ZIP</el-button>
                    <!-- 注意：当前element版本使用confirm事件无效，使用onConfirm才有效 -->
                    <el-popconfirm :title="`您确定删除【${row.name}】吗？`" @onConfirm="clickDelete(row.id)" style="margin: 0 5px" >
                        <el-button slot="reference" type="text" size="small">删除</el-button>
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

        <!-- 新增模型 -->
        <add ref="addModel" />
        <!-- 设计流程 -->
        <design ref="designModel" :modelId="modelId"/>
    </div>
</template>
<script>
import api from '@/api/model'
import Add from './add'
import Design from './design'

export default {
    name: 'Model', // 和对应路由表中配置的name值一致
    components: {Add, Design},
    data() {
        return {
            list: [
              {
                "id":"60005",
                "name":"报销流程xx",
                "key":"leave3",
                "version": 3,
                "description":"报销流程",
                "createDate":"2020-10-28 16:49:17",
                "updateDate":"2021-05-13 16:35:13"
              },
              {
                "id":"47514",
                "name":"测试流程yuanzc修改2",
                "key":"leave5",
                "version": 5,
                "description":"流程编写测试",
                "createDate":"2020-01-24 17:07:16",
                "updateDate":"2021-04-15 15:37:36"
              },
            ],
            page: {
                current: 1,
                size: 10,
                total: 0
            },
            query: {}, // 查询条件
            modelId: null, // 模型id
        }
    },

    created() {
        this.fetchData()
    },

    methods: {
        // 分页条件查询文章列表
        async fetchData() {
            const { data } = 
                await api.getList(this.query, this.page.current, this.page.size)
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

        // 新增
        clickAdd() {
            this.$refs.addModel.visible = true
        },

        // 删除
        async clickDelete(id) {
            // 发送删除请求
            const {code} = await api.deleteById(id)
            if(code === 20000) {
                this.$message.success('删除成功')
                // 刷新列表数据
                this.fetchData()
            }
        },

        // 部署流程
        async clickDeploy(id) {
            const {code} = await api.deployById(id)
            if(code === 20000) {
                this.$message.success('部署成功')
                // 刷新列表数据
                this.fetchData()
            }
        },

        // 设计流程
        clickDesign(id) {
            this.modelId = id
            this.$refs.designModel.visible = true
        },

        // 导出模型
        async clickExportZip(id) {
            window.open(process.env.VUE_APP_BASE_URL + `/model/export/zip/${id}`)
        }

    }
}
</script>