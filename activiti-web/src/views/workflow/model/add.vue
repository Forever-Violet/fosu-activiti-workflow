<template>
    <el-dialog title="新增空白流程模型" :visible.sync="visible" width="500px" >
        <el-form v-loading="loading" :rules="rules" ref="formData" :model="formData" label-width="100px" label-position="right" status-icon>
            <el-form-item label="模型名称：" prop="name"  >
                <el-input v-model="formData.name"  maxlength="20" show-word-limit/>
            </el-form-item>

            <el-form-item label="标识Key：" prop="key">
                <el-input v-model="formData.key" maxlength="20" show-word-limit/>
            </el-form-item>

            <el-form-item label="备注：" prop="description">
                <el-input type="textarea" v-model="formData.description" maxlength="30" show-word-limit></el-input>
            </el-form-item>
            
            <el-form-item align="center">
                <el-button type="primary" @click="submitForm('formData')" size="mini">提交</el-button>
                <el-button size="mini" @click="visible=false">取消</el-button>
            </el-form-item>
            </el-form>
    </el-dialog>
</template>
<script>
import api from '@/api/model'

export default {
    data() {
        return {
            visible: false, // 弹出窗口，true弹出
            loading: false,
            formData: { // 提交表单数据
                name: null,
                key: null,
                description: null
            }, 
            rules: {
                name: [
                    {required: true, message: '请输入模型名称', trigger: 'blur'},
                    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
                ],
                key: [
                    {required: true, message: '请输入标识Key', trigger: 'blur'},
                    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
                ],
                description: [
                    { max: 30, message: '长度不能超过30个字符', trigger: 'blur' }
                ],
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
                      let response = await api.add(this.formData)
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