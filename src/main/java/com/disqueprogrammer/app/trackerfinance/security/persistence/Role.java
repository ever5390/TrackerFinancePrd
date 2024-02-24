package com.disqueprogrammer.app.trackerfinance.security.persistence;

import static com.disqueprogrammer.app.trackerfinance.security.Constant.Authority.*;

public enum Role {

	ROLE_SUPER_ADMIN(ADMIN_AUTHORITIES),
	ROLE_ADMIN(ADMIN_AUTHORITIES),
	ROLE_USER(USER_AUTHORITIES),
	ROLE_USER_LEVEL_TWO(USER_LEVEL_TWO_AUTHORITIES),
	ROLE_USER_LEVEL_THREE(USER_LEVEL_THREE_AUTHORITIES);

	private String[] authorities;

	Role(String... authorities) {
		this.authorities = authorities;
	}

	public String[] getAuthorities() {
		return authorities;
	}
}
