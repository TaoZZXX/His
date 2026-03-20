<template>
  <div class="register-container">
    <div class="register-hero">
      <div class="hero-content">
        <h2>欢迎加入 HIS</h2>
        <p>创建账号后即可进入医院业务系统</p>
      </div>
    </div>
    <div class="register-panel">
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
          <div class="sub-title">请填写账号信息</div>
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

      <el-button :loading="loading" type="primary" class="register-btn" @click.native.prevent="handleRegister">
        注册
      </el-button>

      <div class="link-row">
        <span class="link" @click="goLogin">已有账号？去登录</span>
      </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { register } from '@/api/stall'
import {Message} from "element-ui";

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
          const res = await register({
            username: this.registerForm.username.trim(),
            password: this.$md5(this.registerForm.password),
            confirmPassword: this.$md5(this.registerForm.confirmPassword)
          })

          if (res.code !== 20000) {
            this.$message.error(res.message)
            return
          }

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
$cursor: #2f3a4d;

@supports (-webkit-mask: none) and (not (caret-color: $cursor)) {
  .register-container .el-input input {
    color: $cursor;
  }
}

/* reset element-ui css */
.register-container {
  .el-input {
    display: inline-block;
    height: 44px;
    width: 88%;

    input {
      background: #fff;
      border: 0px;
      -webkit-appearance: none;
      border-radius: 8px;
      padding: 10px 8px 10px 12px;
      color: #2f3a4d;
      height: 44px;
      caret-color: $cursor;

      &:-webkit-autofill {
        box-shadow: 0 0 0px 1000px #fff inset !important;
        -webkit-text-fill-color: $cursor !important;
      }
    }
  }

  .el-form-item {
    border: 1px solid #e3eaf5;
    background: #f8fbff;
    border-radius: 10px;
    color: #454545;
  }
}
</style>

<style lang="scss" scoped>
$bg:#eef3fb;
$dark_gray:#889aa4;
$light_gray:#eee;

.register-container {
  min-height: 100%;
  width: 100%;
  background: linear-gradient(135deg, #edf3ff, #f7f9fd);
  display: flex;
  align-items: stretch;
  overflow: hidden;

  .register-hero {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(145deg, #245fff, #69a5ff);
    color: #fff;
    .hero-content {
      h2 { font-size: 34px; margin-bottom: 10px; }
      p { font-size: 16px; opacity: .92; }
    }
  }

  .register-panel {
    width: 460px;
    background: #fff;
    box-shadow: -8px 0 26px rgba(40, 57, 87, 0.12);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 34px;
  }

  .register-form {
    width: 100%;
  }

  .svg-container {
    padding: 6px 5px 6px 15px;
    color: $dark_gray;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
  }

  .title-container {
    margin-bottom: 20px;

    .title {
      font-size: 26px;
      color: #26364a;
      margin: 0px auto 6px auto;
      text-align: center;
      font-weight: 700;
    }
    .sub-title { text-align: center; color: #8a97aa; margin-bottom: 8px; }
  }

  .show-pwd {
    position: absolute;
    right: 10px;
    top: 9px;
    font-size: 16px;
    color: $dark_gray;
    cursor: pointer;
    user-select: none;
  }

  .register-btn {
    width:100%;
    margin-bottom:12px;
    height: 44px;
    border-radius: 10px;
  }

  .link-row {
    text-align: right;

    .link {
      color: #c0c4cc;
      cursor: pointer;
      font-size: 13px;
    }

    .link:hover {
      color: #3a7afe;
    }
  }
}

@media (max-width: 980px) {
  .register-container {
    .register-hero { display: none; }
    .register-panel {
      width: 100%;
      box-shadow: none;
    }
  }
}
</style>

