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
            <el-table-column  align="center" prop="processStatus" label="流程状态" width="90" ></el-table-column>
            <el-table-column  align="center" prop="taskCreateTime" label="任务开始日期" width="180"></el-table-column>
            <el-table-column  align="center" label="操作" fixed="right" width="260">
                <template slot-scope="{row}">
                    <!-- 没有办理人则要先签收 -->
                    <el-popconfirm v-if="!row.taskAssignee" title="确定签收该任务吗?" @onConfirm="clickClaim(row.taskId)">
                        <el-button slot="reference" type="text">签收 &nbsp;</el-button>
                    </el-popconfirm>
                    <el-button v-else type="text" @click="clickComplete(row)">通过</el-button>
                    <el-button v-if="row.taskAssignee" type="text" @click="clickBack(row)">驳回</el-button>
                    <el-button v-if="row.taskAssignee" type="text" @click="clickTurn(row)">转办</el-button>
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
        <!-- 通过 -->
        <verify ref="verifyRef" :taskId="row.taskId" v-if="selectdata.length" :selectdata="selectdata"></verify>
        <!-- 转办 -->
        <turn ref="turnRef" :taskId="row.taskId" v-if="selectdata.length" :selectdata="selectdata"></turn>
        <!-- 驳回 -->
        <back ref="backRef" :task="row" v-if="selectdata.length" :selectdata="selectdata"></back>
    </div>
</template>
<script>
import api from "@/api/task";
import History from '@/components/Process/History'
import Verify from "./components/Verify.vue"
import Turn from "./components/Turn.vue"
import Back from "./components/Back.vue"
import {getall} from '@/api/getuser'
export default {
    name: 'Await', // 和对应路由表中配置的name值一致
     components: {History,Verify,Turn,Back},
    data() {
       return {
            list: [
            ],
           page: {
                current: 1,
                size: 10,
                total: 0
            },
            query:{},
            row:{},
            selectdata:[],
       }
    },
    created() {
       this.fetchData()
       this.getelect()
    },
    methods: {
        getelect(){
            getall().then((res)=>{
                this.selectdata=res.data
            })
        },
        async fetchData(){
            const {data}=await api.getWaitTaskList(this.query,this.page.current,this.page.size)
            this.list=data.records
            this.page.total=data.total
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
      // 点击通过
        clickComplete(row) {
            if (this.checkProcessTask(row)) {
                this.$refs.verifyRef.visible = true
            }
        },

        // 点击转办
        clickTurn(row) {
            if (this.checkProcessTask(row)) {
                this.$refs.turnRef.visible = true
            }
        },

        // 驳回
        clickBack(row) {
            if (this.checkProcessTask(row)) {
                this.$refs.backRef.visible = true
            }
        },

        // 校验流程是否已启动
        checkProcessTask(row) {
            if(row.processStatus == '已启动') {
                this.row = row
                return true
            }
            this.$message.warning(`【${row.processName}】流程已暂停，启动流程后才可操作！`)
            return false
        },

        // 点击审批进度
        clickProcessHistory(row) {
            this.row = row
            this.$refs.historyRef.visible = true
        },

        // 点击签收
       async clickClaim(taskId) {
           this.loading = true
           try {
                const {code} = await api.claimTask( {taskId} )
                this.loading = false
                if(code === 20000) {
                    // 刷新数据
                    this.fetchData()
                    this.$message.success('签收成功')
                }
           } catch (error) {
               this.loading = false
           }
        },
    }
}
</script>
