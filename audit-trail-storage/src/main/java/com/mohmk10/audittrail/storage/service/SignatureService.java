package com.mohmk10.audittrail.storage.service;

import java.security.KeyPair;

public interface SignatureService {

    String sign(String hash);

    boolean verify(String hash, String signature);

    KeyPair generateKeyPair();
}
