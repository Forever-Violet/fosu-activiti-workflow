<template>
    <div class="app-container">
          <!-- 条件查询 -->        
        <el-form  :inline="true" :model="query" size="small">
            <el-form-item label="流程名称">
                <el-input v-model.trim="query.processName" placeholder="请输入流程名称"></el-input>
            </el-form-item>
            <el-form-item label="流程发起人">
              <el-input v-model.trim="query.proposer" placeholder="请输入流程发起人"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button icon="el-icon-search" type="primary" @click="queryData">查询</el-button>
                <el-button icon="el-icon-refresh"  @click="reload">重置</el-button>
            </el-form-item>
        </el-form>
        <!-- stripe 带斑马纹 -->
        <el-table :data="list" stripe border style="width: 100%">
            <el-table-column align="center" type="index" label="序号" width="50"></el-table-column>
            <el-table-column align="center" prop="proposer" label="流程发起人" width="180" ></el-table-column>
            <el-table-column align="center" prop="processInstanceName" label="流程名称" min-width="100"></el-table-column>
            <el-table-column align="center" prop="status" label="业务状态" width="80" ></el-table-column>
            <el-table-column align="center" prop="deleteReason" label="原因详情" min-width="170" ></el-table-column>
            <el-table-column align="center" prop="processKey" label="流程标识Key" width="150"></el-table-column>
            <el-table-column align="center" label="版本号" width="90" >
                <template slot-scope="{row}"> v{{row.version}}.0</template>
            </el-table-column>
            <!-- <el-table-column align="center" prop="processInstanceId" label="流程实例ID" min-width="150"></el-table-column> -->
            <el-table-column align="center" prop="startTime" label="开始日期" width="160"></el-table-column>
            <el-table-column align="center" prop="endTime" label="结束日期" width="160"></el-table-column>
             <el-table-column align="center" label="操作" fixed="right" width="230">
                <template slot-scope="{row}">
                    <el-button @click="clickProcessHistory(row)" type="text" >审批历史</el-button>
                    <el-popconfirm :title="`您确定删除【${row.processInstanceName || row.processKey}】和审批历史记录吗？`" @onConfirm="clickDelete(row.processInstanceId)" >
                        <el-button slot="reference" type="text" >删除</el-button>
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
          <!-- 审批历史 -->
        <history ref="historyRef" :businessKey="row.businessKey" :processInstanceId="row.processInstanceId" ></history>
    </div>
</template>
<script>
import api from '@/api/instance'
import History from '@/components/Process/History'
export default {
    name: 'Finish', // 和对应路由表中配置的name值一致
    components: {History},
    data() {
       return {
             list: [],
            page: {
                current: 1,
                size: 10,
                total: 0
            },
            query: {}, // 查询条件
            row:{}
       }
    },
    created() {
        this.fetchData()
    },
    methods: {
         // 分页条件查询列表
        async fetchData() {
            const { data } = await api.getProcInstListFinish(this.query, this.page.current, this.page.size)
            this.list = data.records
            this.page.total = data.total
        },
         // 查询
        queryData(){
            this.page.current=1
            this.fetchData()
        },
        // 重置
        reload(){
            this.query={}
            this.fetchData()
        },
        // 每一页多少条
        handleSizeChange(value){
            this.page.size=value
            this.fetchData()
        },
        // 切换页码
        handleCurrentChange(value){
            this.page.current=value
            this.fetchData()
        },
         // 点击审批历史
        clickProcessHistory(row) {
            this.row = row
            this.$refs.historyRef.visible = true
        },

        // 删除流程实例和审批历史记录
        async clickDelete(procInstId) {
            // 发送删除请求
            const {code} = await api.deleteProcInstAndHistory(procInstId)
            if(code === 20000) {
                this.$message.success('删除成功')
                // 刷新列表数据
                this.fetchData()
            }
        },
    }
}
</script>