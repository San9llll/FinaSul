package me.weishu.kernelsu.aster.ui

/**
 * AsterUI 的枚举 —— 从 Nakour 原样搬过来的。
 *
 * 时钟样式和信息块指标。放在一个文件里是因为它们都很小、
 * 而且只服务于 AsterUI 那一套组件。
 */

/** 时钟长什么样。 */
enum class ClockStyle(val id: String, val label: String, val summary: String) {
    Stacked("stacked", "两行数字", "小时在上、分钟在下。"),
    Inline("inline", "单行数字", "一行时间，下面一行日期。"),
    Analog("analog", "指针式", "用表盘代替数字。"),
    Date("date", "仅日期", "只显示星期和日期，不显示时间。");

    companion object { fun of(id: String?) = entries.firstOrNull { it.id == id } ?: Stacked }
}

/** 侧栏信息块里一行能显示什么 */
enum class InfoMetric(val id: String, val label: String, val summary: String) {
    None("none", "不显示", "这一行留空"),
    BatteryTemp("battery_temp", "电池温度", "电池的摄氏温度，公开 API 就能拿"),
    CpuTemp("cpu_temp", "CPU 温度", "热区里挑一个像 CPU / SOC 的"),
    Ram("ram", "内存占用", "已用 / 总内存的百分比"),
    CpuUsage("cpu_usage", "CPU 使用率", "/proc/stat 两次采样的差值"),
    GpuUsage("gpu_usage", "GPU 占用率", "要看 GPU 性能节点，多半得 root"),
    BatteryPower("battery_power", "电池功率", "充电为正、放电为负"),
    BatteryVi("battery_vi", "电压和电流", "电压 V + 电流 A，窄栏里拆两行写"),
    Network("network", "网络", "运营商名称 + 实时上下行速率");

    /** 窄导轨里一行放不下，改成上下两行 */
    val wide: Boolean get() = this == BatteryVi || this == Network

    companion object { fun of(id: String?) = entries.firstOrNull { it.id == id } ?: None }
}
