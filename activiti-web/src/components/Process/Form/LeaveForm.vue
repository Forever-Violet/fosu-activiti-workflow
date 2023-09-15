<template>
        <el-form v-loading="loading" :rules="rules" :model="formData"  ref="formData"  label-suffix=":"
             label-width="170px" size="small" align="left" style="max-width:900px">
            <el-form-item v-if="operate == '详情'" label="请假人" prop="loanDate">
                <span>{{formData.username}}</span>
            </el-form-item> 
            <el-form-item label="请假类型" prop="leaveType" >
                <!-- 请假类型：1病假，2事假，3年假，4婚假，5产假，6丧假，7探亲，8调休，9其他 -->
                <el-radio-group v-model="formData.leaveType" size="mini" >
                  <el-radio v-for="item in leaveTypes" :key="item.value" :label="item.value">{{item.label}}</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="标题" prop="title">
                <el-input type="text" v-model="formData.title" maxlength="100" show-word-limit></el-input>
            </el-form-item>
            <el-form-item label="请假事由" prop="leaveReason">
                <el-input type="textarea" v-model="formData.leaveReason" maxlength="500" :autosize="{minRows: 5}" show-word-limit></el-input>
            </el-form-item>

            <el-form-item label="请假时间" prop="leaveDate" required>
                <el-date-picker v-model="formData.leaveDate" format="yyyy-MM-dd HH:mm"  :default-time="['00:00:00', '23:59:59']"
                    type="datetimerange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" align="right">
                </el-date-picker>
            </el-form-item>
            <el-form-item label="请假时长" prop="duration">
                <el-input-number v-model="formData.duration" :step="0.1" :min="0" controls-position="right"></el-input-number> 天
            </el-form-item>
            <el-row :gutter="20">
                <el-col :span="9">
                    <el-form-item label="休息期间联系电话" prop="contactPhone">
                        <el-input v-model="formData.contactPhone" maxlength="25" ></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="10">
                    <el-form-item label="休息期间应急工作委托人" prop="principal" label-width="200px">
                        <el-input v-model="formData.principal" maxlength="25"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-form-item align="right" v-if="operate != '详情'">
                <el-button  type="primary" @click="submitForm('formData')" size="small">确定</el-button>
                <el-button  @click="clickClose()" size="small">关闭</el-button>
            </el-form-item>
        </el-form>
</template>
<script>
// 请假类型 1病假，2事假，3年假，4婚假，5产假，6丧假，7探亲，8调休，9其他
const leaveTypes = [
    {label: '病假', value: 1},
    {label: '事假', value: 2},
    {label: '年假', value: 3},
    {label: '婚假', value: 4},
    {label: '产假', value: 5},
    {label: '丧假', value: 6},
    {label: '探亲', value: 7},
    {label: '调休', value: 8},
    {label: '其他', value: 9}
]
import api from '@/api/leave'
export default {
    props: {
        operate: { //操作标识：新增，编辑，详情
            type: String,
            default: '详情'
        },
        businessKey: String, // 业务id
    },
    data() {
        return {
            loading: false,
            leaveTypes, // 类型
            formData: { // 提交表单数据
                leaveType: 1,
                duration: 0, // 请假时长（小时）
            }, 
            rules: {
                leaveType: [
                    {required: true, message: '请选择请假类型', trigger: 'change'},
                ],
                title: [
                    {required: true, message: '请输入标题', trigger: 'change'},
                ],
                leaveReason: [
                    {required: true, message: '请输入请假事由', trigger: 'blur'},
                    { min: 3, max: 500, message: '长度在 3 到 500 个字符', trigger: 'blur' }
                ],
                leaveDate: [
                    { required: true, message: '请选择请假时间', trigger: 'change' }
                ],
                duration: [
                    { required: true, message: '请输入请假时长', trigger: 'blur' }
                ]
            }
        }
    },
    watch: {
        'formData.leaveDate': {
            handler(newDate) {
                if(newDate && (newDate[0] instanceof Date)  && (newDate[1] instanceof Date)) {
                    // 请假开始时间、结束时间
                    this.formData.startDate = newDate[0]
                    this.formData.endDate = newDate[1]
                    var dateDiff = newDate[1].getTime() - newDate[0].getTime();
                    this.formData.duration = Math.ceil(dateDiff / (24 * 60 * 60 * 1000));
                }
            }
        },
        businessKey: {
            immediate: true, // 很重要！！！
            handler (newVal) {
                if(newVal) {
                    this.getById()
                }
            }
        }
    },
    methods: {
        async getById() {
            const {data} = await api.getById(this.businessKey)
            // 格式化时间
            data.leaveDate = [ data.startDate, data.endDate]
            this.formData = data
        },
        // 提交表单数据
        submitForm(formName) {
            this.$refs[formName].validate( async (valid) => {
                if (valid) {
                    // 校验通过，提交表单数据
                    this.loading = true
                    try {
                      let response = {}
                      if('新增' == this.operate) {
                            response = await api.add(this.formData)
                      }else if('编辑' == this.operate) {
                            response = await api.update(this.formData)
                      }
                      if(response.code === 20000) {
                          // 将表单清空
                          this.$refs['formData'].resetFields()
                          // 关闭窗口
                          this.clickClose(true)
                          this.$message.success('提交成功')
                      }
                      this.loading = false
                    } catch(e) {
                      this.loading = false
                    }
                }
            })
        },
        clickClose(refresh = false) {
            // 将表单清空
            this.$refs['formData'].resetFields()
            this.$emit('close', refresh)
        },
    }
}
</script>

<style >

</style>