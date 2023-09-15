<template>
    <el-dialog title="提交申请" :visible.sync="visible" width="500px" destroy-on-close>
        <el-form v-loading="loading" :rules="rules" ref="formData" :model="formData"  size="mini"  status-icon>
            <el-form-item label="请输入审批人用户名" prop="assignees" label-width="150px">
                <!-- <el-input type="text" v-model.trim="formData.assignees" maxlength="100" show-word-limit></el-input>
                <div>可输入用户名：【mengxuegu,meng,xue,gu】</div>
                <div>可同时指定多个用户（用英文逗号分隔）</div> -->
                  <el-select v-model="formData.assignees" placeholder="请选择">
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
import api from '@/api/instance'

export default {
    props: {
        row: Object,
        selectdata:Array,
    },

    data() {
        return {
            visible: false, // 弹出窗口，true弹出
            loading: false,
            selectlist:this.selectdata,
            formData: {}, // 提交表单数据
            rules: {
                assignees: [
                    {required: true, message: '请输入审批人用户名', trigger: 'blur'},
                ]
            }
        }
    },
    methods: {
        // 提交表单数据
        submitForm(formName) {
            this.$refs[formName].validate( async (valid) => {
                if (valid) {
                    try {
                        // 校验通过，提交表单数据
                        this.loading = true
                        const data = {
                            businessRoute: this.$route.name, // 当前业务路由名
                            businessKey: this.row.id, // 业务id
                            variables: {entity: this.row}, // 变量
                            assignees: this.formData.assignees.split(',') // 审批人转为数组
                        }
                        let response = await api.startProcessApply(data)
                        if(response.code === 20000) {
                            console.log(response);
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