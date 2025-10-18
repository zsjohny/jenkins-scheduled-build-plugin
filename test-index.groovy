import jenkins.model.Jenkins
import hudson.PluginWrapper

println "=== 检查扩展点索引 ==="

def pm = Jenkins.get().pluginManager
def plugin = pm.getPlugin("scheduled-build")

if (plugin) {
    println "\n插件信息:"
    println "  名称: ${plugin.shortName}"
    println "  版本: ${plugin.version}"
    println "  激活: ${plugin.isActive()}"
    
    // 尝试读取索引文件
    println "\n尝试读取扩展点索引:"
    
    def servicesUrl = plugin.classLoader.getResource("META-INF/services/hudson.Extension")
    if (servicesUrl) {
        println "  ✅ 找到 services/hudson.Extension"
        println "  位置: ${servicesUrl}"
        servicesUrl.openStream().withReader { reader ->
            println "  内容:"
            reader.eachLine { line ->
                println "    - ${line}"
            }
        }
    } else {
        println "  ❌ 未找到 services/hudson.Extension"
    }
    
    def annotationsUrl = plugin.classLoader.getResource("META-INF/annotations/hudson.Extension")
    if (annotationsUrl) {
        println "  ✅ 找到 annotations/hudson.Extension"
    } else {
        println "  ❌ 未找到 annotations/hudson.Extension"
    }
}
