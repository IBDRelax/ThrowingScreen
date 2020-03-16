package com.throwing.screen.constant;

public class Constant {

    public final static int PUSH_MSG_PORT = 9999;//推送消息端口

    public final static int FIND_PORT = 10001;//搜索端口
    public final static String FIND_BROADCAST_IP="255.255.255.255";//搜索组播地址

    public final static int MSG_TYPE_LENGTH = 4;//消息类型占byte长度
    public final static int MSG_SEQ_LENGTH = 36;//消息seq占byte长度
    public final static int BUFFER_LENGTH = 4096;//

    public final static String MSG_SUFFIX = "msg_end";

    public final static String SEARCH = "search";

}
