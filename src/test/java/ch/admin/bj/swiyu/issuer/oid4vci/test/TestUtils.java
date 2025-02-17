/*
 * SPDX-FileCopyrightText: 2025 Swiss Confederation
 *
 * SPDX-License-Identifier: MIT
 */

package ch.admin.bj.swiyu.issuer.oid4vci.test;

import ch.admin.bj.swiyu.issuer.oid4vci.common.config.SdjwtProperties;
import ch.admin.bj.swiyu.issuer.oid4vci.domain.openid.credentialrequest.holderbinding.DidJwk;
import com.authlete.sd.Disclosure;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestUtils {
    public static Map<String, Object> fetchOAuthToken(MockMvc mock, String preauthCode) throws Exception {
        var response = mock.perform(post("/token")
                        .param("grant_type", "urn:ietf:params:oauth:grant-type:pre-authorized_code")
                        .param("pre-authorized_code", preauthCode))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("expires_in")))
                .andExpect(content().string(containsString("access_token")))
                .andExpect(content().string(containsString("BEARER")))
                .andReturn();
        return new ObjectMapper().readValue(response.getResponse().getContentAsString(), HashMap.class);
    }

    public static ResultActions requestCredential(MockMvc mock, String token, String credentialRequestString) throws Exception {
        return mock.perform(post("/credential")
                .header("Authorization", String.format("BEARER %s", token))
                .contentType("application/json")
                .content(credentialRequestString)
        );
    }

    public static String createHolderProof(ECKey holderPrivateKey, String issuerUri, String nonce, String proofTypeString, boolean useDidJwk) throws JOSEException {
        var headerBuilder = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .type(new JOSEObjectType(proofTypeString));
        if (useDidJwk) {
            headerBuilder.keyID(DidJwk.createFromJsonString(holderPrivateKey.toPublicJWK().toJSONString()).getDidJwk());
        } else {
            headerBuilder.jwk(holderPrivateKey.toPublicJWK());
        }
        JWSHeader header = headerBuilder
                .build();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("nonce", nonce)
                .claim("aud", issuerUri)
                .issueTime(new Date())
                .build();
        JWSSigner signer = new ECDSASigner(holderPrivateKey);
        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(signer);
        return jwt.serialize();
    }

    public static String getCredential(MockMvc mock, Object token, String credentialRequestString) throws Exception {
        var response = requestCredential(mock, (String) token, credentialRequestString)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("credential")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        return JsonParser.parseString(response.getResponse().getContentAsString()).getAsJsonObject().get("credential").getAsString();
    }

    public static JsonObject requestFailingCredential(MockMvc mock, Object token, String credentialRequestString) throws Exception {
        var response = requestCredential(mock, (String) token, credentialRequestString)
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        return JsonParser.parseString(response.getResponse().getContentAsString()).getAsJsonObject();
    }

    public static void verifyVC(SdjwtProperties sdjwtProperties, String vc, Map<String, String> credentialSubjectData) throws Exception {

        var keyPair = ECKey.parseFromPEMEncodedObjects(sdjwtProperties.getPrivateKey());
        var publicJWK = keyPair.toPublicJWK();
        var sdJwtTokenParts = vc.split("~");
        var jwt = sdJwtTokenParts[0];
        var disclosures = List.of(sdJwtTokenParts).subList(1, sdJwtTokenParts.length);

        // vc must end with "~" as it has no holder binding
        assert (vc.endsWith("~"));

        assertTrue(verifyToken(jwt, publicJWK.toJSONString()));

        List<Disclosure> disclosureList = disclosures.stream().map(Disclosure::parse).toList();

        assertEquals(credentialSubjectData.size(), disclosureList.size());

        disclosureList.forEach(disclosure -> {
            assertNotNull(disclosure.getClaimName());
            assertEquals(credentialSubjectData.get(disclosure.getClaimName()), disclosure.getClaimValue());
        });
    }

    public static boolean verifyToken(String token, String publicKeyJwk) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Parse the public key JWK
            JWK jwk = JWK.parse(publicKeyJwk);

            // Create a JWSVerifier with the public key
            JWSVerifier verifier = new ECDSAVerifier(jwk.toECKey());

            // Verify the signature
            return signedJWT.verify(verifier);
        } catch (ParseException | JOSEException e) {
            return false;
        }
    }
}
