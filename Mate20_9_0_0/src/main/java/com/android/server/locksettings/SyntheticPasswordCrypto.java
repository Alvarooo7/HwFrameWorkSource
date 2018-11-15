package com.android.server.locksettings;

import android.security.keystore.KeyProtection.Builder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStore.SecretKeyEntry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SyntheticPasswordCrypto {
    private static final int AES_KEY_LENGTH = 32;
    private static final byte[] APPLICATION_ID_PERSONALIZATION = "application-id".getBytes();
    private static final int PROFILE_KEY_IV_SIZE = 12;
    private static final int USER_AUTHENTICATION_VALIDITY = 15;

    private static byte[] decrypt(SecretKey key, byte[] blob) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        if (blob == null) {
            return null;
        }
        byte[] iv = Arrays.copyOfRange(blob, null, 12);
        byte[] ciphertext = Arrays.copyOfRange(blob, 12, blob.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(2, key, new GCMParameterSpec(128, iv));
        return cipher.doFinal(ciphertext);
    }

    private static byte[] encrypt(SecretKey key, byte[] blob) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (blob == null) {
            return null;
        }
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(1, key);
        byte[] ciphertext = cipher.doFinal(blob);
        byte[] iv = cipher.getIV();
        if (iv.length == 12) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(iv);
            outputStream.write(ciphertext);
            return outputStream.toByteArray();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invalid iv length: ");
        stringBuilder.append(iv.length);
        throw new RuntimeException(stringBuilder.toString());
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x001c A:{Splitter: B:1:0x0017, ExcHandler: java.security.InvalidKeyException (r2_3 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x001c A:{Splitter: B:1:0x0017, ExcHandler: java.security.InvalidKeyException (r2_3 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x001c A:{Splitter: B:1:0x0017, ExcHandler: java.security.InvalidKeyException (r2_3 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x001c A:{Splitter: B:1:0x0017, ExcHandler: java.security.InvalidKeyException (r2_3 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x001c A:{Splitter: B:1:0x0017, ExcHandler: java.security.InvalidKeyException (r2_3 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:4:0x001c, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x001d, code:
            r2.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:6:0x0021, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] encrypt(byte[] keyBytes, byte[] personalisation, byte[] message) {
        try {
            return encrypt(new SecretKeySpec(Arrays.copyOf(personalisedHash(personalisation, keyBytes), 32), "AES"), message);
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x001c A:{Splitter: B:1:0x0017, ExcHandler: java.security.InvalidKeyException (r2_3 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x001c A:{Splitter: B:1:0x0017, ExcHandler: java.security.InvalidKeyException (r2_3 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x001c A:{Splitter: B:1:0x0017, ExcHandler: java.security.InvalidKeyException (r2_3 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x001c A:{Splitter: B:1:0x0017, ExcHandler: java.security.InvalidKeyException (r2_3 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x001c A:{Splitter: B:1:0x0017, ExcHandler: java.security.InvalidKeyException (r2_3 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Missing block: B:4:0x001c, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x001d, code:
            r2.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:6:0x0021, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] decrypt(byte[] keyBytes, byte[] personalisation, byte[] ciphertext) {
        try {
            return decrypt(new SecretKeySpec(Arrays.copyOf(personalisedHash(personalisation, keyBytes), 32), "AES"), ciphertext);
        } catch (GeneralSecurityException e) {
        }
    }

    public static byte[] decryptBlobV1(String keyAlias, byte[] blob, byte[] applicationId) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return decrypt((SecretKey) keyStore.getKey(keyAlias, null), decrypt(applicationId, APPLICATION_ID_PERSONALIZATION, blob));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to decrypt blob", e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x001b A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x001b A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x001b A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x001b A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x001b A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x001b A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x001b A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x001b A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x001b A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:3:0x001b, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:4:0x001c, code:
            r0.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:5:0x0026, code:
            throw new java.lang.RuntimeException("Failed to decrypt blob", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] decryptBlob(String keyAlias, byte[] blob, byte[] applicationId) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return decrypt(applicationId, APPLICATION_ID_PERSONALIZATION, decrypt((SecretKey) keyStore.getKey(keyAlias, null), blob));
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x0068 A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x0068 A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x0068 A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x0068 A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x0068 A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x0068 A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x0068 A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:6:0x0068, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:7:0x0069, code:
            r0.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:8:0x0073, code:
            throw new java.lang.RuntimeException("Failed to encrypt blob", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] createBlob(String keyAlias, byte[] data, byte[] applicationId, long sid, boolean managedProfile) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            Builder builder = new Builder(2).setBlockModes(new String[]{"GCM"}).setEncryptionPaddings(new String[]{"NoPadding"}).setCriticalToDeviceEncryption(true);
            if (sid != 0) {
                builder.setUserAuthenticationRequired(true).setBoundToSpecificSecureUserId(sid).setUserAuthenticationValidityDurationSeconds(15);
            }
            builder.setRollbackResistant(managedProfile);
            keyStore.setEntry(keyAlias, new SecretKeyEntry(secretKey), builder.build());
            return encrypt(secretKey, encrypt(applicationId, APPLICATION_ID_PERSONALIZATION, data));
        } catch (Exception e) {
        }
    }

    public static byte[] createBlob(String keyAlias, byte[] data, byte[] applicationId, long sid) {
        return createBlob(keyAlias, data, applicationId, sid, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:2:0x000e A:{Splitter: B:0:0x0000, ExcHandler: java.security.KeyStoreException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000e A:{Splitter: B:0:0x0000, ExcHandler: java.security.KeyStoreException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000e A:{Splitter: B:0:0x0000, ExcHandler: java.security.KeyStoreException (r0_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:2:0x000e, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:3:0x000f, code:
            r0.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:4:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void destroyBlobKey(String keyAlias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.deleteEntry(keyAlias);
        } catch (Exception e) {
        }
    }

    protected static byte[] personalisedHash(byte[] personalisation, byte[]... message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            if (personalisation.length <= 128) {
                digest.update(Arrays.copyOf(personalisation, 128));
                for (byte[] data : message) {
                    digest.update(data);
                }
                return digest.digest();
            }
            throw new RuntimeException("Personalisation too long");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException for SHA-512", e);
        }
    }
}
