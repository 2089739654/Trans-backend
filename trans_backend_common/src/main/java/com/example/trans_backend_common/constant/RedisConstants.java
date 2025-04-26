package com.example.trans_backend_common.constant;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final String SHOP_TYPE = "shop:type";
    public static final Long LOGIN_USER_TTL = 36000L;

    public static final Long CACHE_NULL_TTL = 2L;

    public static final Long CACHE_SHOP_TTL = 30L;
    public static final String CACHE_SHOP_KEY = "cache:shop:";

    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String SECKILL_ORDER_KEY = "seckill:order:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final String USER_SIGN_KEY = "sign:";
    public static final String LOCK = "lock:";
    public static final String ORDER = "order:";
    public static final long SIXTY_SECONDS= 60*1000L;
    public static final long ONE_WEEK=60*60*24*7*1000L;
    public static final long ONE_DAY=60*60*24*1000L;
    public static final long FIVE_MINUTES=60*5*1000L;
    public static final long TEN_SECONDS=10*1000L;
    public static final long MOVE_BITS=32;
}
