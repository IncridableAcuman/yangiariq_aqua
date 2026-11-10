package com.aquaai.shared.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties governing the controlled initial bootstrap
 * of the root SUPER_ADMIN administrator account.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.bootstrap.super-admin")
public class SuperAdminBootstrapProperties {

    /**
     * Flag to enable bootstrap of the initial Super Administrator account.
     * Permanently ignored and disabled once an active Super Administrator exists.
     */
    private boolean enabled = false;

    /**
     * Email address for the root Super Administrator account.
     */
    private String email;

    /**
     * Password for the root Super Administrator account.
     * Must meet strong password requirements (min 12 chars, upper, lower, digit, symbol).
     * Never logged or stored in plaintext.
     */
    private String password;

    /**
     * Contact phone number for the root Super Administrator account (E.164 format).
     */
    private String phoneNumber = "+998900000001";

    /**
     * First name of the Super Administrator.
     */
    private String firstName = "Super";

    /**
     * Last name of the Super Administrator.
     */
    private String lastName = "Administrator";
}
