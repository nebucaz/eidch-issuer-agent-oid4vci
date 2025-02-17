/*
 * SPDX-FileCopyrightText: 2025 Swiss Confederation
 *
 * SPDX-License-Identifier: MIT
 */

package ch.admin.bj.swiyu.issuer.oid4vci.common.config;

import com.nimbusds.jose.jwk.JWKSet;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.text.ParseException;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "application")
@Data
public class ApplicationProperties {

    /**
     * Replacement Values for providing replacement template values for issuer metadata & openid configuration json
     * Key is the value to be replaced, the value is the value it will be replaced with.
     */
    @NotNull
    private Map<String, String> templateReplacement;

    @NotNull
    private String issuerId;

    @NotNull
    private long tokenTTL;

    private String dataIntegrityJwks;

    public JWKSet getDataIntegrityKeySet() throws ParseException {
        return JWKSet.parse(dataIntegrityJwks);
    }
}
