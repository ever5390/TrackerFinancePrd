package com.disqueprogrammer.app.trackerfinance.security.Constant;

public class Authority {

    public static final String[] ADMIN_AUTHORITIES = { "user:read", "user:create", "user:update", "user:delete"  };
    public static final String[] USER_AUTHORITIES = { "user:read", "user:create", "user:update", "user:delete"  };
    public static final String[] USER_LEVEL_TWO_AUTHORITIES = { "user:read", "user:create", "user:update" };
    public static final String[] USER_LEVEL_THREE_AUTHORITIES = { "user:read"};
}
