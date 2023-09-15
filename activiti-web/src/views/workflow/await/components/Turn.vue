<template>
    <el-dialog title="委托他人代办" :visible.sync="visible" width="600px" destroy-on-close>
        <el-form v-loading="loading" :rules="rules" ref="formData" :model="formData" status-icon>
            <el-form-item label="用户名" prop="assigneeUserKey" label-width="120px">
                <!-- <el-input type="text" v-model.trim="formData.assigneeUserKey" maxlength="100" show-word-limit placeholder="请输入代办人用户名"></el-input> -->
                 <el-select v-model="formData.assigneeUserKey" placeholder="请选择">
                    <el-option
                    v-for="item in selectlist"
                    :key="item.id"
                    :label="item.nickName"
                    :value="item.username">
                    </el-option>
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
        taskId: String,
        selectdata:Array,
    },

    data() {
        return {
            visible: false, // 弹出窗口，true弹出
            loading: false,
            selectlist:this.selectdata,
            formData: { // 提交表单数据
                assigneeUserKey: null
            }, 
            rules: {
                assigneeUserKey: [
                    {required: true, message: '请输入代办人用户名', trigger: 'blur'},
                ]
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
                      this.formData.taskId = this.taskId
                      let response = await api.turnTask(this.formData)
                      if(response.code === 20000) {
                          // 刷新数据
                          this.$parent.fetchData()
                          this.$message.success('转办成功')
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