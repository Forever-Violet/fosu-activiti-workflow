<template>
    <el-dialog title="确认撤回申请" :visible.sync="visible" width="600px" >
        <el-form v-loading="loading" :rules="rules" ref="formData" :model="formData" status-icon>
            <el-form-item label="撤回原因" prop="message" label-width="120px">
                <el-input type="textarea" v-model="formData.message" maxlength="300"  
                    placeholder="请输入撤回原因" :autosize="{ minRows: 4}" show-word-limit></el-input>
            </el-form-item>
            <el-form-item align="center">
                <el-button type="primary" @click="submitForm('formData')" size="mini">提交</el-button>
                <el-button size="mini" @click="visible=false">取消</el-button>
            </el-form-item>
        </el-form>
    </el-dialog>
</template>
<script>
import api from '@/api/instance'
export default {
    props: {
        businessKey: String,//业务id
        procInstId: String,//流程id
    },
    data() {
        return {
            visible: false, // 弹出窗口，true弹出
            loading: false,
            formData: { // 提交表单数据
                message: null,
            },
            rules: {}, //校验
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
                      // 审批人转为数组
                      const params = {
                            businessKey: this.businessKey,
                            procInstId: this.procInstId,
                            message: this.formData.message
                      }
                      let response = await api.cancelApply(params)
                      if(response.code === 20000) {
                          // 刷新数据
                          this.$parent.fetchData()
                          this.$message.success('撤回成功')
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