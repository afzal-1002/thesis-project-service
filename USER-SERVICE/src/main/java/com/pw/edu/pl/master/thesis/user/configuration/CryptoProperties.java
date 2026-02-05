package com.pw.edu.pl.master.thesis.user.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "crypto")
public class CryptoProperties {
    private String password;
    private String salt;
}
