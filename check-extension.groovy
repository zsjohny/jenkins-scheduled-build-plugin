import jenkins.model.Jenkins
import jenkins.model.TransientActionFactory
import hudson.model.Job

println "=== 检查扩展点注册 ==="

// 1. 检查所有 TransientActionFactory
def factories = Jenkins.get().getExtensionList(TransientActionFactory.class)
println "\n共 ${factories.size()} 个 TransientActionFactory:"
factories.each { factory ->
    println "  - ${factory.class.name}"
    if (factory.class.name.contains("ScheduledBuild")) {
        println "    ✅ 找到预约构建扩展点！"
        println "    类型: ${factory.type()}"
    }
}

// 2. 检查具体任务的 Actions
def job = Jenkins.get().getItemByFullName("scheduled-build-demo")
if (job) {
    println "\n任务: ${job.fullName}"
    def actions = job.getAllActions()
    println "共有 ${actions.size()} 个 Actions:"
    actions.each { action ->
        println "  - ${action.class.name}"
        if (action.class.name.contains("ScheduledBuild")) {
            println "    ✅ 找到预约构建 Action！"
        }
    }
}

// 3. 检查扩展点索引文件
println "\n=== 检查插件类加载 ==="
def pluginManager = Jenkins.get().pluginManager
def plugin = pluginManager.getPlugin("scheduled-build")
if (plugin) {
    println "插件信息:"
    println "  名称: ${plugin.shortName}"
    println "  版本: ${plugin.version}"
    println "  激活: ${plugin.isActive()}"
    println "  启用: ${plugin.isEnabled()}"
} else {
    println "❌ 未找到插件"
}
