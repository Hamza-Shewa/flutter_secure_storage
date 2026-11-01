package com.it_nomads.fluttersecurestorage;

import android.content.Context;

import com.it_nomads.fluttersecurestorage.ciphers.StorageCipher;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class CipherCacheTest {

    private static final StorageCipher CIPHER = new StorageCipher() {
        @Override
        public byte[] encrypt(byte[] input) {
            return input;
        }

        @Override
        public byte[] decrypt(byte[] input) {
            return input;
        }

        @Override
        public void deleteKey(Context context) {
        }
    };

    private FlutterSecureStorageConfig config(boolean requireBiometricsPerOperation) {
        Map<String, Object> options = new HashMap<>();
        options.put("enforceBiometrics", "true");
        options.put("requireBiometricsPerOperation", String.valueOf(requireBiometricsPerOperation));
        return new FlutterSecureStorageConfig(options);
    }

    @Test
    public void perOperation_neverCachesAMigratedCipher() {
        // A cached cipher would serve every later read and write without a prompt (#1303).
        assertNull(FlutterSecureStorage.cipherToCache(config(true), CIPHER));
    }

    @Test
    public void sessionMode_keepsTheAuthenticatedCipher() {
        assertSame(CIPHER, FlutterSecureStorage.cipherToCache(config(false), CIPHER));
    }
}
