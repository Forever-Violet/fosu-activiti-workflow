<template>
  <el-dialog title="流程审批进度跟踪" :visible.sync="visible" align="center" width="90%" fullscreen>
      <el-tabs style="min-height: 500px" type="border-card" >
        <el-tab-pane label="业务单据" v-loading="loading">
            <component :is="currProcessForm" :businessKey="businessKey"></component>
        </el-tab-pane>
        <el-tab-pane label="流程进度">
          <el-table :data="list" border stripe style="width: 90%" max-height="300">
               <el-table-column label="流程审批历史记录" align="center">
                <el-table-column type="index" label="序号" align="center" width="50"></el-table-column>
                <el-table-column prop="taskName" label="任务名称" align="center" ></el-table-column>
                <el-table-column prop="assignee" label="办理人" align="center" ></el-table-column>
                <el-table-column prop="status" label="状态" align="center" ></el-table-column>
                <el-table-column prop="message" label="审批意见" align="center" ></el-table-column>
                <el-table-column prop="startTime" label="开始时间" align="center" ></el-table-column>
                <el-table-column prop="endTime" label="结束时间" align="center" ></el-table-column>
               </el-table-column>
            </el-table>
            <el-image :src="url" style="font-size: 20px; margin: 50px;">
              <div slot="placeholder"><i class="el-icon-loading"></i> 流程审批历史图加载中……</div>
            </el-image>
        </el-tab-pane>
      </el-tabs>
  </el-dialog>
  
</template>

<script>
import api from '@/api/instance'

// 导入所有vue组件 （位于：@/components/Process/Form 目录下）
const allComponents = require.context('@/components/Process/Form', false,/\.vue$/)
// {组件名: 引用的组件}
let res_components = {}
allComponents.keys().forEach(fileName => {
  let comp = allComponents(fileName)
  res_components[fileName.replace(/^\.\/(.*)\.\w+$/, '$1')] = comp.default
})
// import LeaveForm from './Form/LeaveForm.vue'
export default {
    components: res_components,
    
    props: {
      processInstanceId: String,
      businessKey: String, // 业务唯一标识
    },
    

    data() {
      return {
        loading: false,
        visible: false,
        url: null,
        list: [],
        currProcessForm: null, // 当前流程表单组件
      }
    },

    watch: {
      visible(newVal) {
          if(newVal) {
            this.getFormNameByProcInstId()
            // 审批历史数据
            this.getHistoryInfoList()
            // 流程实例审批历史图
            this.url = api.getHistoryProcessImage(this.processInstanceId)
          }
      }
    },

    methods: {
        // 通过流程实例ID获取对应流程业务表单组件名
        async getFormNameByProcInstId() {
          try {
              this.loading = true
              const {data} = await api.getFormNameByProcInstId(this.processInstanceId)
              this.currProcessForm = data
              this.loading = false
          } catch (error) {
              this.loading = false
          }
        },

        // 查询审批历史记录
        async getHistoryInfoList() {
            const { data } = await api.getHistoryInfoList(this.processInstanceId)
            this.list = data
        },
    }

}
</script>