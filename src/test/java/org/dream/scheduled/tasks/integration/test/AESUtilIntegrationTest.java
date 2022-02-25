package org.dream.scheduled.tasks.integration.test;

import org.dream.scheduled.tasks.util.AESUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AESUtilIntegrationTest {
    
    @Test
    public void encryptThenDecryptShouldGetSameText() {
        String originalText = "TEST";
        String encryptedText = AESUtil.encrypt(originalText);
        String decryptedText = AESUtil.decrypt(encryptedText);

        Assertions.assertEquals(originalText, decryptedText);
    }

}
