/*
 * SPDX-FileCopyrightText: 2025 Swiss Confederation
 *
 * SPDX-License-Identifier: MIT
 */

package ch.admin.bj.swiyu.issuer.oid4vci.infrastructure.web.controller;

import ch.admin.bj.swiyu.issuer.oid4vci.api.OpenIdConfigurationDto;
import ch.admin.bj.swiyu.issuer.oid4vci.common.config.OpenIdIssuerConfiguration;
import ch.admin.bj.swiyu.issuer.oid4vci.infrastructure.web.config.OpenIdIssuerApiConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * Well known Controller
 * <p>
 * Implements the .well-known endpoints
 * <a href="https://openid.github.io/OpenID4VCI/openid-4-verifiable-credential-issuance-wg-draft.html">OID4VCI Spec</a>
 * </p>
 */
@RestController
@AllArgsConstructor
@Slf4j
@Tag(name = "Well-known endpoints", description = "OpenID .well-known endpoints for issuer configuration and credentials API")
public class WellKnownController {

    private final OpenIdIssuerApiConfiguration openIDConfigurationDto;
    private final OpenIdIssuerConfiguration openIDConfiguration;

    /**
     * General information about the issuer
     *
     * @return OpenIdConfigurationDto as defined by OIDConnect and extended by OID4VCI
     */
    @GetMapping(value = {"/.well-known/openid-configuration"})
    @Operation(summary = "OpenID Connect information required for issuing VCs")
    public OpenIdConfigurationDto getOpenIDConfiguration() throws IOException {
        return openIDConfigurationDto.getOpenIdConfiguration();
    }

    /**
     * Data concerning OpenID4VC Issuance
     *
     * @return Issuer Metadata as defined by OID4VCI
     */
    @GetMapping(value = {"/.well-known/openid-credential-issuer"})
    @Operation(summary = "Information about credentials which can be issued.")
    public Map<String, Object> getIssuerMetadata() throws IOException {
        return openIDConfiguration.getIssuerMetadata();
    }
}
