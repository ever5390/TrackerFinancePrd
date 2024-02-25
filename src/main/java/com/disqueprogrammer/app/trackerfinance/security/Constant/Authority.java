package com.disqueprogrammer.app.trackerfinance.security.Constant;

public class Authority {

    public static final String[] SUPER_ADMIN_AUTHORITIES = { "all:read", "all:create", "all:update", "all:delete" };
    public static final String[] ADMIN_AUTHORITIES = { "inworkspace:read", "inworkspace:create", "inworkspace:update", "inworkspace:delete"  };
    public static final String[] USER_AUTHORITIES = { "transaction:read", "transaction:create", "transaction:update", "transaction:delete"  };

}
