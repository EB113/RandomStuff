/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aer.miscelaneous;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;


import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3;
import org.bouncycastle.jcajce.provider.digest.SHA3.Digest256;

public final class Crypto {
    
    public static byte[] iv = new SecureRandom().generateSeed(16);
    
    public static String randomString() {
        return UUID.randomUUID().toString();
    }
    
    public static byte[] sha3(final String input) {
        final DigestSHA3 sha3 = new Digest256();

        sha3.update(input.getBytes());

        return sha3.digest();
    }
    
    public static Boolean cmpByteArray(byte[] b1, byte[] b2) {
        Boolean bool = false;
        
        if(b1.length == b2.length){
            for(int i=0; i< b1.length; i++) {
                if(b1[i] != b2[i]) return bool;
            }
            bool = true;
        }
        
        return bool;
    }
    
    public static String toHex(byte[] input) {
        
        StringBuffer stringBuffer = new StringBuffer();
        
        for (byte bytes : input) {
            stringBuffer.append(String.format("%02x", bytes & 0xff));
        }
        
        return stringBuffer.toString();
    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    public static byte[] toBytes(String string) {
        int length = string.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4) + Character
                    .digit(string.charAt(i + 1), 16));
        }
        return data;
    }
    
    public static byte[] getId(byte[] input) throws NoSuchAlgorithmException {
        StringBuffer stringBuffer = new StringBuffer();
        
        for (byte bytes : input) {
            stringBuffer.append(String.format("%02x", bytes & 0xff));
        }
        
        return sha3(stringBuffer.toString());
    }
    
    private static KeyPair genPair() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException{
        Security.addProvider(new BouncyCastleProvider());
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
        KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");
        g.initialize(ecSpec, new SecureRandom());
        KeyPair pair = g.generateKeyPair();
        
        return pair;
    }
    
    public static KeyPair genValidPair(int difficulty) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyPair keys    = null;
        byte[] pub      = {0x01};
        byte[] pubId    = {0x01};
        boolean state   = true;
        int aux_counter = difficulty;
        
        while(state){
            keys = genPair();

            pub = keys.getPublic().getEncoded();
            pubId = getId(pub);
            
            while(aux_counter>0){
                aux_counter--;
                if(pubId[aux_counter] != 0x00) {
                    aux_counter = difficulty;
                    state = true;
                    break;
                }
                else state = false;
            }
        }
        
        return keys;
    }
    
    public static String encryptString(SecretKey key, String plainText) {
        
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
            byte[] plainTextBytes = plainText.getBytes("UTF-8");
            byte[] cipherText;

            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            cipherText = new byte[cipher.getOutputSize(plainTextBytes.length)];
            int encryptLength = cipher.update(plainTextBytes, 0,
                    plainTextBytes.length, cipherText, 0);
            encryptLength += cipher.doFinal(cipherText, encryptLength);

            return toHex(cipherText);
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException
                | UnsupportedEncodingException | ShortBufferException
                | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String decryptString(SecretKey key, String cipherText) {
        try {
            Key decryptionKey = new SecretKeySpec(key.getEncoded(),
                    key.getAlgorithm());
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
            byte[] cipherTextBytes = toBytes(cipherText);
            byte[] plainText;

            cipher.init(Cipher.DECRYPT_MODE, decryptionKey, ivSpec);
            plainText = new byte[cipher.getOutputSize(cipherTextBytes.length)];
            int decryptLength = cipher.update(cipherTextBytes, 0,
                    cipherTextBytes.length, plainText, 0);
            decryptLength += cipher.doFinal(plainText, decryptLength);

            return new String(plainText, "UTF-8");
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | BadPaddingException
                | ShortBufferException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static SecretKey generateSharedSecret(PrivateKey privateKey, PublicKey publicKey) {
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "BC");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);

            SecretKey key = keyAgreement.generateSecret("AES");
            return key;
        } catch (InvalidKeyException | NoSuchAlgorithmException
                | NoSuchProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
    
    public static byte[] GenerateSignature(byte[] plaintext, PrivateKey privk) throws SignatureException, UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException{
		Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", "BC");
		ecdsaSign.initSign(privk);
		ecdsaSign.update(plaintext); //plaintext.getBytes("UTF-8")
		byte[] signature = ecdsaSign.sign();
		System.out.println(signature.toString());
		return signature;
	}
	
    public static boolean ValidateSignature(byte[] plaintext, PublicKey pubk, byte[] signature) throws SignatureException, InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchProviderException{
            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA", "BC");
            ecdsaVerify.initVerify(pubk);
            ecdsaVerify.update(plaintext); //plaintext.getBytes("UTF-8")
            return ecdsaVerify.verify(signature);
    }
    
    
}
