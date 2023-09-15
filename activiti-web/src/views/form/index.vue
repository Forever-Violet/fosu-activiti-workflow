<template>
    <div class="modeler">
        <iframe class="iframe" :src="src"></iframe>
    </div>
</template>
<script>
  import Vue from 'vue';
  // import {getToken} from '@/utils/auth';//登陆人的token
  export default{
    computed:{
        src(){
          // return "/modeler.html?modelId=" + this.$store.modelId;//activiti主html路径 
          return "/modeler.html?modelId=77608291-b47c-11eb-990e-eedeec0ffd6c"
        },
        apiUrl() {
          return process.env.VUE_APP_BASE_API;// "/dev-api" 后台部署的api服务 "/dev-api"
        },
        token(){
            // return getToken();//token
        }
    },
    mounted(){
      //全局存入当前vue实例，供activiti调用    
      window.getMyVue = this;
    },
    
    methods:{
      goto(){
        this.$message.success("保存模型成功");
        this.$router.push({name:"/sys/activiti"})    
      }    
    }
  }
</script>
<style scoped>
.iframe {
  width: 100%;
  height: calc(100vh - 53px);
  border: 0px;
}
</style>