package com.github.bunnyi.syncmatica.communication;

import com.github.bunnyi.syncmatica.util.Identifier;

public enum PacketType {
    REGISTER_METADATA("syncmatica:register_metadata"),
    // 一个数据包将负责发送syncmatic的全部元数据
    // 它标志着syncmatic的创建，目前它也负责
    // 用于更改syncmatic服务器和客户端

    CANCEL_SHARE("syncmatica:cancel_share"),
    // 共享失败时发送到客户端
    // 客户端可以取消上传，或者在完成发送删除数据包后

    REQUEST_LITEMATIC("syncmatica:request_download"),
    // 另一组数据包将负责下载整个
    // litematic从下载请求开始

    SEND_LITEMATIC("syncmatica:send_litematic"),
    // 负责发送一个litematic比特的数据包（准确地说是16千字节（minecraft最多在一个数据包中发送的一半））

    RECEIVED_LITEMATIC("syncmatica:received_litematic"),
    // 负责触发litematic的另一次发送的数据包
    // 通过等待回复，我希望我们能够确保
    // 我们不会淹没客户端与服务器的连接

    FINISHED_LITEMATIC("syncmatica:finished_litematic"),
    // 一种用来标记litematic结束的数据包
    // 传输

    CANCEL_LITEMATIC("syncmatica:cancel_litematic"),
    // 负责取消正在进行的上传/下载的数据包
    // 将在几种情况下发送-主要是在出现错误时

    REMOVE_SYNCMATIC("syncmatica:remove_syncmatic"),
    // 删除syncmatic后将发送给客户端的数据包
    // 如果特定客户端打算从服务器中删除litematic，则由客户端发送到服务器

    REGISTER_VERSION("syncmatica:register_version"),
    // 该数据包将在客户端加入服务器时发送给客户端
    // 在接收到该数据包后，客户端将检查服务器版本
    // 在客户端初始化syncmatica
    // 如果它可以使用服务器上的版本运行，那么它将使用自己的版本进行响应
    // 如果服务器能够处理服务器将发送的客户端版本

    CONFIRM_USER("syncmatica:confirm_user"),
    // 确认用户数据包
    // 在成功的版本交换后发送
    // 在客户端完全启动syncmatica
    // 将所有服务器位置一起发送到客户端

    FEATURE_REQUEST("syncmatica:feature_request"),
    // 请求合作伙伴发送其功能列表
    // 不需要完全完成握手

    FEATURE("syncmatica:feature"),
    // 将功能集合发送给合作伙伴
    // 在版本交换期间发送，以检查这两个版本是否兼容，并且没有
    // 可用于传输版本的默认功能集
    // 之后，使用功能集合与合作伙伴进行通信

    MODIFY("syncmatica:modify"),
    // 向客户端发送更新的放置数据，反之亦然

    MODIFY_REQUEST("syncmatica:modify_request"),
    // 从客户端发送到服务器以请求编辑放置值
    // 用于确保一次只有一个人可以编辑，从而防止各种内容

    MODIFY_REQUEST_DENY("syncmatica:modify_request_deny"),
    // 修改请求拒绝

    MODIFY_REQUEST_ACCEPT("syncmatica:modify_request_accept"),
    // 修改请求接受

    MODIFY_FINISH("syncmatica:modify_finish"),
    // 从客户端发送到服务器，以标记放置值的编辑已结束
    // 随放置的最终数据一起发送

    MESSAGE("syncmatica:mesage");
    // 将消息从客户端发送到服务器 - 允许将来的兼容性
    // 无法修复这里的拼写错误lol

    public final Identifier identifier;

    PacketType(final String id) {
        identifier = new Identifier(id);
    }

    public static boolean containsIdentifier(final Identifier id) {
        for (final PacketType p : PacketType.values()) {
            if (id.equals(p.identifier)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return identifier.toString();
    }
}
