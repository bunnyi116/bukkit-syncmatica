package com.github.bunnyi.syncmatica;

public enum Feature {
    CORE,       // 作为 0.1.0 一部分的每个功能 - 进一步划分这些功能是没有意义的，因为与未来版本的 0.0 兼容无法维护，版本非常alpha。
    FEATURE,    // 在版本交换期间报告自己的功能的可能性
    MODIFY,     // 命令以修改同步放置的位置在服务器上的位置
    MESSAGE,    // 能够将消息从服务器发送到客户端以显示
    QUOTA,      // 客户端上载到服务器的配额
    DEBUG,      // 能够配置调试
    CORE_EX;    // 扩展的基本功能 - 例如展示位置和次区域共享的所有者

    public static Feature fromString(final String s) {
        for (final Feature f : Feature.values()) {
            if (f.toString().equals(s)) {
                return f;
            }
        }
        return null;
    }
}
