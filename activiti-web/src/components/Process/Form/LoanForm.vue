<template>
     <el-form v-loading="loading" :rules="rules" :model="formData"  ref="formData" label-suffix=":"
            label-width="170px" size="small" align="left" style="max-width:900px">
        <el-form-item v-if="operate == '详情'" label="借款人" prop="loanDate">
            <span>{{formData.userId}}</span>
        </el-form-item>  
        <el-row :gutter="20">
            <el-col :span="8">
                <el-form-item label="借款金额" prop="money">
                    <el-input-number v-model="formData.money" :precision="2" :min="0" :max="999999999999" size="medium" controls-position="right"></el-input-number>
                </el-form-item>
            </el-col>
            <el-col :span="16">
                <el-form-item label="金额大写">
                    <span>{{rmbMinToMax(formData.money)}}</span>
                </el-form-item>
            </el-col>
        </el-row>
        <el-form-item label="借款日期" prop="loanDate">
                <el-date-picker v-model="formData.loanDate" type="date" placeholder="选择日期">
            </el-date-picker>
        </el-form-item>
        <el-form-item label="借款用途" prop="purpose">
            <el-input type="textarea" v-model="formData.purpose" maxlength="500" :autosize="{minRows: 5}" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
            <el-input type="textarea" v-model="formData.remark" maxlength="500" :autosize="{minRows: 5}" show-word-limit></el-input>
        </el-form-item>
        <el-form-item align="right" v-if="operate != '详情'">
            <el-button  type="primary" @click="submitForm('formData')" size="small">确定</el-button>
            <el-button  @click="clickClose()" size="small">关闭</el-button>
        </el-form-item>
    </el-form>
</template>
<script>
import api from '@/api/loan'
export default {
    props: {
        operate: { //操作标识：新增，编辑，详情
            type: String,
            default: '详情'
        },
        businessKey: {
            type: String,
            default: null,
        }, // 业务id
    },
    data() {
        return {
            loading: false,
            formData: {},
            rules: {
                loanDate: [
                    { required: true, message: '请选择借款日期', trigger: 'change' }
                ],
                money: [
                    { required: true, message: '请输入借款金额', trigger: 'change' }
                ]
            }
        }
    },
    watch: {
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
        
        // 人民币小写转大写
        rmbMinToMax(number) {
            if(!number) return 
            //浏览器默认为整数型,将数字转为2位有效数字的float类型再转为字符串
            number =  parseFloat(number).toFixed(2).toString();
            var CN_MONEY = "";
            var CN_UNIT = "仟佰拾亿仟佰拾万仟佰拾元角分";
            var index = number.indexOf('.');//将从小数点开始分开
            if(index >=0 ){
                number = number.substring(0, index) + number.substr(index+1, 2);
                CN_UNIT = CN_UNIT.substr(CN_UNIT.length-number.length);
                for (var i = 0; i < number.length; i++){
                    CN_MONEY += '零壹贰叁肆伍陆柒捌玖'.substr(number.substr(i, 1), 1) + CN_UNIT.substr(i, 1);
                }
                return CN_MONEY.replace(/零角零分$/, '整').replace(/零[仟佰拾]/g, '零')
                    .replace(/零{2,}/g, '零').replace(/零([亿|万])/g, '$1').replace(/零+元/, '元')
                    .replace(/亿零{0,3}万/, '亿').replace(/^元/, "").replace(/零[角|分]/, "");
            }
        }
        
    }
}
</script>