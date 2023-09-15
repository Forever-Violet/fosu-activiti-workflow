<template>
    <div class="app-container">
         <!-- 条件查询 -->        
        <el-form :inline="true" :model="query" size="mini">
            <el-form-item label="任务名称:">
                <el-input v-model.trim="query.taskName" ></el-input>
            </el-form-item>
            <el-form-item>
                <el-button icon="el-icon-search" type="primary" @click="queryData">查询</el-button>
                <el-button icon="el-icon-refresh"  @click="reload">重置</el-button>
            </el-form-item>
        </el-form>
         <!-- stripe 带斑马纹 -->
        <el-table :data="list" stripe border style="width: 100%">
            <el-table-column align="center" type="index" label="序号" width="50"></el-table-column>
            <el-table-column align="center" prop="taskName" label="任务名称" min-width="160"></el-table-column>
            <el-table-column align="center" label="所属流程" min-width="120">
              <template slot-scope="{row}">
                {{`${row.processName} - v${row.version}`}}
              </template>
            </el-table-column>
            <el-table-column  align="center" prop="proposer" label="流程发起人" width="180" ></el-table-column>
            <el-table-column  align="center" prop="taskStartTime" label="任务开始日期" width="180"></el-table-column>
            <el-table-column  align="center" prop="taskEndTime" label="任务结束日期" width="180"></el-table-column>
            <el-table-column  align="center" label="操作" fixed="right" width="260">
                <template slot-scope="{row}">
                    <el-button type="text" @click="clickProcessHistory(row)">审批历史</el-button>
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
import History from '@/components/Process/History'
import api from "@/api/task";
export default {
    name: 'Complete', // 和对应路由表中配置的name值一致
    components: {History},
    data() {
       return {
           list:[],
           page:{
            current:1,
            size:10,
            total:0,
           },
           query:{},
           row:{},
       }
    },
    created() {
       this.fetchData()
    },
    methods: {
        async fetchData(){
            const {data}=await api.getCompleteTaskList(this.query,this.page.current,this.page.size)
            this.list=data.records
            this.page.total=data.total
        },
        // 审批历史
        clickProcessHistory(row){
            this.row=row
            this.$refs.historyRef.visible=true

        },
        // 条件查询方法
        queryData() {
            this.page.current = 1
            this.fetchData()
        },

        // 刷新重置
        reload() {
            this.query = {}
            this.page.current=1
            this.page.size=10
            this.fetchData()
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
    },
}
</script>