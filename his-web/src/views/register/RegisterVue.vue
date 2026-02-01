<template>
  <div class="register-container">
    <el-form
      ref="registerForm"
      :model="registerForm"
      :rules="registerRules"
      class="register-form"
      auto-complete="on"
      label-position="left"
    >
      <div class="title-container">
        <h3 class="title">用户注册</h3>
      </div>

      <el-form-item prop="username">
        <span class="svg-container">
          <i class="el-icon-user"></i>
        </span>
        <el-input
          v-model="registerForm.username"
          placeholder="用户名"
          name="username"
          type="text"
          auto-complete="on"
        />
      </el-form-item>

      <el-form-item prop="password">
        <span class="svg-container">
          <i class="el-icon-lock"></i>
        </span>
        <el-input
          :key="passwordType"
          v-model="registerForm.password"
          :type="passwordType"
          placeholder="密码"
          name="password"
          auto-complete="on"
        />
        <span class="show-pwd" @click="showPwd">
          <i :class="passwordType === 'password' ? 'el-icon-view' : 'el-icon-minus'"></i>
        </span>
      </el-form-item>

      <el-form-item prop="confirmPassword">
        <span class="svg-container">
          <i class="el-icon-lock"></i>
        </span>
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          placeholder="确认密码"
          name="confirmPassword"
          auto-complete="on"
          @keyup.enter.native="handleRegister"
        />
      </el-form-item>

      <el-button :loading="loading" type="primary" style="width:100%;margin-bottom:12px;" @click.native.prevent="handleRegister">
        注册
      </el-button>

      <div class="link-row">
        <span class="link" @click="goLogin">已有账号？去登录</span>
      </div>
    </el-form>
  </div>
</template>

<script>
import { register } from '@/api/user'

export default {
  name: 'Register',
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!value) return callback(new Error('请输入用户名'))
      if (value.length < 2) return callback(new Error('用户名至少2位'))
      callback()
    }

    const validatePassword = (rule, value, callback) => {
      if (!value) return callback(new Error('请输入密码'))
      if (value.length < 6) return callback(new Error('密码不能少于6位'))
      callback()
    }

    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) return callback(new Error('请再次输入密码'))
      if (value !== this.registerForm.password) return callback(new Error('两次密码不一致'))
      callback()
    }

    return {
      registerForm: {
        username: '',
        password: '',
        confirmPassword: ''
      },
      registerRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }],
        confirmPassword: [{ required: true, trigger: 'blur', validator: validateConfirmPassword }]
      },
      loading: false,
      passwordType: 'password'
    }
  },
  methods: {
    showPwd() {
      this.passwordType = this.passwordType === 'password' ? '' : 'password'
    },
    goLogin() {
      this.$router.push('/login')
    },
    handleRegister() {
      this.$refs.registerForm.validate(async valid => {
        if (!valid) return

        this.loading = true
        try {
          // 注意：这里的 URL 在 api/user.js 里定义。
          // 若你的后端注册接口不是 /user/register，请改 api/user.js。
          await register({
            username: this.registerForm.username.trim(),
            password: this.$md5(this.registerForm.password),
            confirmPassword: this.$md5(this.registerForm.confirmPassword)
          })

          this.$message.success('注册成功，请登录')
          this.$router.push('/login')
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style lang="scss">
$bg:#2d3a4b;
$light_gray:#fff;
$cursor: #fff;

@supports (-webkit-mask: none) and (not (caret-color: $cursor)) {
  .register-container .el-input input {
    color: $cursor;
  }
}

/* reset element-ui css */
.register-container {
  .el-input {
    display: inline-block;
    height: 47px;
    width: 85%;

    input {
      background: transparent;
      border: 0px;
      -webkit-appearance: none;
      border-radius: 0px;
      padding: 12px 5px 12px 15px;
      color: $light_gray;
      height: 47px;
      caret-color: $cursor;

      &:-webkit-autofill {
        box-shadow: 0 0 0px 1000px $bg inset !important;
        -webkit-text-fill-color: $cursor !important;
      }
    }
  }

  .el-form-item {
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    color: #454545;
  }
}
</style>

<style lang="scss" scoped>
$bg:#283443;
$dark_gray:#889aa4;
$light_gray:#eee;

.register-container {
  min-height: 100%;
  width: 100%;
  background-color: $bg;
  overflow: hidden;

  .register-form {
    position: relative;
    width: 520px;
    max-width: 100%;
    padding: 140px 35px 0;
    margin: 0 auto;
    overflow: hidden;
  }

  .svg-container {
    padding: 6px 5px 6px 15px;
    color: $dark_gray;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
  }

  .title-container {
    position: relative;

    .title {
      font-size: 26px;
      color: $light_gray;
      margin: 0px auto 40px auto;
      text-align: center;
      font-weight: bold;
    }
  }

  .show-pwd {
    position: absolute;
    right: 10px;
    top: 7px;
    font-size: 16px;
    color: $dark_gray;
    cursor: pointer;
    user-select: none;
  }

  .link-row {
    text-align: right;

    .link {
      color: #c0c4cc;
      cursor: pointer;
      font-size: 13px;
    }

    .link:hover {
      color: #ffffff;
    }
  }
}
</style>

