package com.w2mem.app.unused;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class CryptHelper{
	
	public static  byte[] encrypt(byte[] data){
	PBEKeySpec pbeKeySpec;
    PBEParameterSpec pbeParamSpec;
    SecretKeyFactory keyFac = null;

    // Salt
    byte[] salt = {
        (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
        (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
    };

    // Iteration count
    int count = 20;

    // Create PBE parameter set
    pbeParamSpec = new PBEParameterSpec(salt, count);


    pbeKeySpec = new PBEKeySpec(new char[]{'1', '2', '3'});
    try {
		keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
	    SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
	    // Create PBE Cipher
	    Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
	    // Initialize PBE Cipher with key and parameters
	    pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

	    // Encrypt the cleartext
	    byte[] cipherdata = pbeCipher.doFinal(data);
	    

	    return cipherdata;
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

		return null;
	}
	
	public static byte[] decrypt(byte[] data)
	{
		PBEKeySpec pbeKeySpec;
	    PBEParameterSpec pbeParamSpec;
	    SecretKeyFactory keyFac = null;

	    // Salt
	    byte[] salt = {
	        (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
	        (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
	    };

	    // Iteration count
	    int count = 20;

	    // Create PBE parameter set
	    pbeParamSpec = new PBEParameterSpec(salt, count);


	    pbeKeySpec = new PBEKeySpec(new char[]{'1', '2', '3'});
	    try {
			keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		    SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
		    // Create PBE Cipher
		    Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		    // Initialize PBE Cipher with key and parameters
		    pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);

		    // Encrypt the cleartext
		    byte[] cipherdata = pbeCipher.doFinal(data);

		    return cipherdata;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

			return null;
	}
	

}
