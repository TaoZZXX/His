<template>
  <div class="login-container">
    <div class="login-hero">
      <div class="hero-content">
        <h2>HIS 智慧医疗平台</h2>
        <p>统一挂号、就诊与权限管理</p>
      </div>
    </div>
    <div class="login-panel">
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form" auto-complete="on" label-position="left">
        <div class="title-container">
          <h3 class="title">欢迎登录</h3>
          <div class="sub-title">医院信息管理系统</div>
        </div>

        <el-form-item prop="username">
          <span class="svg-container">
            <i class="el-icon-user"></i>
          </span>
          <el-input
            ref="username"
            v-model="loginForm.username"
            placeholder="用户名"
            name="username"
            type="text"
            tabindex="1"
            auto-complete="on"
          />
        </el-form-item>

        <el-form-item prop="password">
          <span class="svg-container">
            <i class="el-icon-lock"></i>
          </span>
          <el-input
            :key="passwordType"
            ref="password"
            v-model="loginForm.password"
            :type="passwordType"
            placeholder="密码"
            name="password"
            tabindex="2"
            auto-complete="on"
            @keyup.enter.native="handleLogin"
          />
          <span class="show-pwd" @click="showPwd">
            <i :class="passwordType === 'password' ? 'el-icon-view' : 'el-icon-minus'"></i>
          </span>
        </el-form-item>

        <el-button :loading="loading" type="primary" class="login-btn" @click.native.prevent="handleLogin">
          登录
        </el-button>

        <div class="link-row">
          <span class="link" @click="goRegister">没有账号？去注册</span>
        </div>

      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入正确的用户名'))
      } else {
        callback()
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error('密码不能少于6位'))
      } else {
        callback()
      }
    }
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }]
      },
      loading: false,
      passwordType: 'password',
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  methods: {
    showPwd() {
      if (this.passwordType === 'password') {
        this.passwordType = ''
      } else {
        this.passwordType = 'password'
      }
      this.$nextTick(() => {
        this.$refs.password.focus()
      })
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          this.$store.dispatch('user/login', this.loginForm).then(() => {
            this.$router.push('/')
            this.loading = false
          }).catch(() => {
            this.loading = false
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    goRegister() {
      this.$router.push('/register')
    }
  }
}
</script>

<style lang="scss">
$bg: #2d3a4b;
$light_gray: #fff;
$cursor: #2f3a4d;

@supports (-webkit-mask: none) and (not (cater-color: $cursor)) {
  .login-container .el-input input {
    color: $cursor;
  }
}

.login-container {
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

.login-container {
  min-height: 100%;
  width: 100%;
  background: linear-gradient(135deg, #edf3ff, #f7f9fd);
  display: flex;
  overflow: hidden;
  align-items: stretch;

  .login-hero {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(145deg, #1f5eff, #64a0ff);
    color: #fff;
    .hero-content {
      text-align: left;
      h2 { font-size: 34px; margin-bottom: 10px; }
      p { font-size: 16px; opacity: .92; }
    }
  }

  .login-panel {
    width: 460px;
    background: #fff;
    box-shadow: -8px 0 26px rgba(40, 57, 87, 0.12);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 34px;
  }

  .login-form {
    width: 100%;
  }

  .tips {
    font-size: 14px;
    color: #fff;
    margin-bottom: 10px;

    span {
      &:first-of-type {
        margin-right: 16px;
      }
    }
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
      font-size: 28px;
      color: #26364a;
      margin: 0 auto 6px auto;
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

  .login-btn {
    width: 100%;
    margin-bottom: 12px;
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
  .login-container {
    .login-hero { display: none; }
    .login-panel {
      width: 100%;
      box-shadow: none;
    }
  }
}
</style>

