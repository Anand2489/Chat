package com.example.dell.myapplication;


public class Util {

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String EMAIL = "email";
    public static final String USER_NAME = "user_name";

    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public final static String SENDER_ID = "898495346888";

    public static String base_url = "http://192.168.1.21/gcm_demo/";

    public final static String  register_url=base_url+"register.php";
    public final static String  send_chat_url=base_url+"sendChatmessage.php";

    // xmpp variables
    public static final String SERVER ="172.27.22.251";
    public static final String DOMAIN = "172.27.22.251";
    public static final String XMPP_PASSWORD = "anand2489";
    public static final String XMPP_SECREAT_KEY = "K1UaJQkQAG6nIa4m";   //K1UaJQkQAG6nIa4m=restapi secret key   W32DAHFM=userService

    public static final String SUFFIX_CHAT = "@" + DOMAIN;
    public static final String SUFFIX_CHAT_GROUP = "@conference." + DOMAIN;



}
