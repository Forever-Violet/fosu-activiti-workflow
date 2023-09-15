<template>
    <el-dialog title="审批驳回" :visible.sync="visible" width="600px" destroy-on-close>
        <el-form v-loading="loading" :rules="rules" ref="formData" :model="formData" status-icon>
            <el-form-item label="驳回到" prop="targetActivityId" label-width="120px">
                <el-select v-model="formData.targetActivityId" placeholder="请选择">
                    <el-option v-for="item in options" :key="item.activityId" 
                        :value="item.activityId" :label="item.activityName">
                    </el-option>
                    <!-- <el-option
                    v-for="item in selectlist"
                    :key="item.id"
                    :label="item.nickName"
                    :value="item.username">
                    </el-option> -->
                </el-select>
            </el-form-item>
            <el-form-item align="center">
                <el-button type="primary" @click="submitForm('formData')" size="mini">提交</el-button>
                <el-button size="mini" @click="visible=false">取消</el-button>
            </el-form-item>
        </el-form>
    </el-dialog>
</template>
<script>
import api from '@/api/task'

export default {
    props: {
        // processInstanceId: String,
        selectdata:Array,
        task: Object,
    },

    data() {
        return {
            visible: false, // 弹出窗口，true弹出
            loading: false,
            selectlist:this.selectdata,
            formData: {
                targetActivityId: null,
            },
            rules: {
                targetActivityId: [
                    {required: true, message: '请选择驳回目标', trigger: 'change'},
                ]
            },
            options: [], // 驳回目标选项
        }
    },

    watch: {
      async visible(newVal) {
          if(newVal) {
            this.options = []
            // 查询可驳回节点
            const {data} = await api.getBackNodes(this.task.taskId)
            this.formData.targetActivityId = data[0].activityId
            this.options = data
          }
      }
    },

    methods: {
        // 提交表单数据
        submitForm(formName) {
            this.$refs[formName].validate( async (valid) => {
                if (valid) {
                    // 校验通过，提交表单数据
                    this.loading = true
                    try {
                      const params = {taskId: this.task.taskId, targetActivityId: this.formData.targetActivityId }
                      let response = await api.backTask(params)
                      if(response.code === 20000) {
                          // 刷新数据
                          this.$parent.fetchData()
                          this.$message.success('提交成功')
                          // 将表单清空
                          this.$refs['formData'].resetFields()
                          // 关闭窗口
                          this.visible = false
                      }
                      this.loading = false
                    } catch(e) {
                      this.loading = false
                    }
                }
            })
        },

    }
}
</script>