/*
 * SPDX-FileCopyrightText: 2025 Swiss Confederation
 *
 * SPDX-License-Identifier: MIT
 */

package ch.admin.bj.swiyu.issuer.oid4vci.common.exception;

import lombok.Getter;

@Getter
public class Oid4vcException extends RuntimeException {

    private final CredentialRequestError error;

    public Oid4vcException(CredentialRequestError error, String message) {
        super(message);
        this.error = error;
    }

    public Oid4vcException(Throwable cause, CredentialRequestError error, String message) {
        super(message, cause);
        this.error = error;
    }

}
