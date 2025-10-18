#!/usr/bin/env groovy

import jenkins.model.*
import hudson.security.*

def instance = Jenkins.getInstance()

// 创建管理员用户
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount("admin", "admin123")
instance.setSecurityRealm(hudsonRealm)

// 设置授权策略
def strategy = new FullControlOnceLoggedInStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)

instance.save()

println "管理员用户已创建"
println "用户名: admin"
println "密码: admin123"



