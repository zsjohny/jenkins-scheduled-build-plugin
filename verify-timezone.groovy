// Jenkins 时区验证脚本
// 在 Jenkins Script Console 中运行此脚本以验证时区配置

println "=" * 70
println "🌍 Jenkins 时区配置验证"
println "=" * 70
println ""

// 1. 系统时区设置
println "📌 系统时区设置:"
println "   System Property: " + System.getProperty("user.timezone")
println "   Default TimeZone: " + TimeZone.getDefault().getID()
println "   Display Name: " + TimeZone.getDefault().getDisplayName()
println ""

// 2. 当前时间
println "🕐 当前时间:"
def now = new Date()
println "   Date: " + now
println "   ISO 8601: " + now.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
println ""

// 3. 时区偏移
def offset = TimeZone.getDefault().getRawOffset() / 3600000
def dstOffset = TimeZone.getDefault().getDSTSavings() / 3600000
println "⏰ 时区偏移:"
println "   UTC Offset: " + (offset >= 0 ? "+" : "") + offset + " 小时"
if (dstOffset > 0) {
    println "   DST Savings: " + dstOffset + " 小时 (夏令时)"
}
println ""

// 4. 验证是否为上海时区
println "✅ 时区验证:"
def isShanghai = TimeZone.getDefault().getID() == "Asia/Shanghai" || 
                 TimeZone.getDefault().getID() == "Asia/Hong_Kong" ||
                 TimeZone.getDefault().getID() == "Asia/Taipei"

if (isShanghai) {
    println "   ✅ 当前使用中国时区 (CST/UTC+8)"
} else if (offset == 8) {
    println "   ⚠️  使用 UTC+8 时区，但不是标准的中国时区标识"
} else {
    println "   ❌ 未使用中国时区"
    println "   💡 建议设置: -Duser.timezone=Asia/Shanghai"
}
println ""

// 5. Java 版本
println "☕ Java 信息:"
println "   Version: " + System.getProperty("java.version")
println "   Vendor: " + System.getProperty("java.vendor")
println ""

// 6. Jenkins 版本
println "🔧 Jenkins 信息:"
println "   Version: " + Jenkins.getVersion()
println ""

println "=" * 70
println "💡 配置建议:"
println "=" * 70

if (!isShanghai && offset != 8) {
    println """
修改时区的方法:

1. 通过 JAVA_OPTS (推荐):
   在启动脚本中添加: -Duser.timezone=Asia/Shanghai

2. 通过环境变量:
   export TZ=Asia/Shanghai

3. Docker 环境:
   environment:
     - TZ=Asia/Shanghai
     - JAVA_OPTS=-Duser.timezone=Asia/Shanghai

重启 Jenkins 后生效。
"""
} else {
    println """
✅ 时区配置正确！

预约构建功能将使用当前时区进行调度。
"""
}

println "=" * 70

