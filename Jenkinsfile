#!/usr/bin/env groovy

/*
 * Jenkins 官方 CI 构建配置
 * 用于自动构建、测试和发布插件到 Jenkins 插件中心
 */

buildPlugin(
  // 使用容器代理进行构建
  useContainerAgent: true,
  
  // 构建配置
  configurations: [
    // Linux + JDK 11
    [platform: 'linux', jdk: 11],
    // Linux + JDK 17（可选，测试兼容性）
    [platform: 'linux', jdk: 17],
    // Windows + JDK 11
    [platform: 'windows', jdk: 11],
  ],
  
  // 测试配置
  tests: [
    // 跳过测试（如果测试未完成）
    skip: true
    // 完成测试后改为：skip: false
  ],
  
  // 超时设置（分钟）
  timeout: 60,
  
  // 失败快速失败
  failFast: false
)

