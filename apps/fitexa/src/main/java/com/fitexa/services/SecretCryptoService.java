package com.fitexa.services;

public interface SecretCryptoService {
    String encrypt(String value);
    String decrypt(String value);
}
