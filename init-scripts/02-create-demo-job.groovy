#!/usr/bin/env groovy

import jenkins.model.*
import hudson.model.*
import hudson.tasks.*

def instance = Jenkins.getInstance()

// 创建一个自由风格的演示任务
def jobName = "scheduled-build-demo"
def job = instance.getItem(jobName)

if (job == null) {
    // 创建新任务
    job = instance.createProject(FreeStyleProject.class, jobName)
    
    // 设置描述
    job.setDescription("预约构建插件演示任务 - 测试预约构建功能")
    
    // 添加参数化构建
    def paramsDef = new ParametersDefinitionProperty(
        new StringParameterDefinition("ENVIRONMENT", "dev", "部署环境"),
        new StringParameterDefinition("VERSION", "1.0.0", "版本号"),
        new BooleanParameterDefinition("ENABLE_NOTIFY", true, "是否发送通知"),
        new ChoiceParameterDefinition("BUILD_TYPE", 
            ["debug", "release", "profile"] as String[], 
            "构建类型")
    )
    job.addProperty(paramsDef)
    
    // 添加Shell构建步骤
    def shellScript = """#!/bin/bash
echo "========================================="
echo "预约构建演示任务开始执行"
echo "========================================="
echo ""
echo "构建参数："
echo "  环境: \${ENVIRONMENT}"
echo "  版本: \${VERSION}"
echo "  通知: \${ENABLE_NOTIFY}"
echo "  类型: \${BUILD_TYPE}"
echo ""
echo "执行时间: \$(date)"
echo ""
echo "模拟构建过程..."
sleep 5
echo ""
echo "构建完成！"
echo "========================================="
"""
    
    job.getBuildersList().add(new Shell(shellScript))
    
    // 保存任务
    job.save()
    
    println "演示任务 '${jobName}' 已创建"
} else {
    println "演示任务 '${jobName}' 已存在"
}

instance.reload()

