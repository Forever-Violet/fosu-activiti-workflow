<template>
    <el-dialog title="审批" :visible.sync="visible" width="600px" destroy-on-close @close="closeDialog">
        <el-form v-loading="loading" :rules="rules" ref="formData" :model="formData" status-icon >
            <el-form-item label="审批意见" prop="message" label-width="120px">
                <el-input type="textarea" v-model="formData.message" maxlength="300"  placeholder="请输入审批意见" :autosize="{ minRows: 4}" show-word-limit></el-input>
            </el-form-item>
            <el-form-item v-if="nextNodes && nextNodes.length>0" label="下一步审批人" prop="assigneeMap" label-width="120px">
                <div v-for="(item, index) in nextNodes" :key="index"> 
                    <span>【{{item.name}}】：</span>
                    <!-- <el-input  :placeholder="`请输入[${item.name}]用户名`" 
                        v-model="formData.assigneeMap[item.id]" maxlength="100" show-word-limit  type="text" style="margin-bottom: 10px">
                    </el-input> -->
                    <el-select v-model="formData.assigneeMap[item.id]" placeholder="请选择">
                        <el-option
                        v-for="item in selectlist"
                        :key="item.id"
                        :label="item.nickName"
                        :value="item.username">
                        </el-option>
                    </el-select>
                </div>
                <!-- <div>可输入用户名：【mengxuegu,meng,xue,gu】</div>
                <div>可同时指定多个用户（用英文逗号分隔）</div> -->
            </el-form-item>
            <el-form-item align="center">
                <el-button type="primary" @click="submitForm('formData')" size="mini">提交</el-button>
                <el-button size="mini" @click="visible = false">取消</el-button>
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
                message: null,
                assigneeMap: {}
            }, 
            nextNodes: [], // 下一节点是否为结束节点
            rules: {
                assigneeMap: [
                    {required: true, message: '请输入下一步审批人', trigger: 'blur'},
                ]
            }
        }
    },

    watch: {
        async visible(newVal) {
            if(newVal) {
                try{
                    this.loading = true
                    // 获取下一节点信息
                    const {data} = await api.getNextNodeInfo(this.taskId)
                    this.nextNodes = data
                    this.loading =false
                }catch(error) {
                    this.loading = false
                }
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
                      // 审批人转为数组
                      const data = {
                            taskId: this.taskId,
                            message: this.formData.message,
                            assigneeMap: this.formData.assigneeMap // 审批人转为数组
                      }
                      let response = await api.completeTask(data)
                      if(response.code === 20000) {
                          // 刷新数据
                          this.$parent.fetchData()
                          this.$message.success('提交成功')
                          // 将表单清空
                          this.$refs[formName].resetFields()
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

        // 关闭
        closeDialog() {
            // 将表单清空
            this.$refs['formData'].resetFields()
            this.formData = { message: null, assigneeMap: {} },
            this.visible = false
        }

    }
}
</script>