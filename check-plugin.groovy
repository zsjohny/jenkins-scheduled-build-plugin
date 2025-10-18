import jenkins.model.*
import hudson.PluginWrapper

def pm = Jenkins.get().pluginManager
def plugin = pm.getPlugin("scheduled-build")

println "=== 插件详细信息 ==="
if (plugin) {
    println "插件名称: ${plugin.shortName}"
    println "版本: ${plugin.version}"
    println "激活: ${plugin.isActive()}"
    println "启用: ${plugin.isEnabled()}"
    println "插件类加载器: ${plugin.classLoader.class.name}"
    
    println "\n=== 尝试加载扩展点索引 ==="
    def url = plugin.classLoader.getResource("META-INF/services/hudson.Extension")
    if (url) {
        println "✅ 找到索引文件: ${url}"
        println "\n文件内容:"
        url.openStream().withReader { reader ->
            reader.eachLine { line ->
                println "  ${line}"
            }
        }
    } else {
        println "❌ 未找到索引文件"
    }
} else {
    println "❌ 插件未找到"
}
