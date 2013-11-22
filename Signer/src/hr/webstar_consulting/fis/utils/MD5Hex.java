package hr.webstar_consulting.fis.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Hex {
	
    public static String MD5Hex(String s) {        
        return MD5Hex(s.getBytes());
    }
    
    public static String MD5Hex(byte[] digest) {
        String result = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");            
            result = toHex(md5.digest(digest));
        }
        catch (NoSuchAlgorithmException e) {
            // this won't happen, we know Java has MD5!
        }
        return result;
    }    

    public static String toHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (int i = 0; i < a.length; i++) {
            sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(a[i] & 0x0f, 16));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("MD5 for abcde:\t" + MD5Hex("abcde"));
        System.out.println("MD5 for bbb:\t" + MD5Hex("bbb"));
        System.out.println("MD5 for abcde:\t" + MD5Hex("abcde"));
        System.out.println("MD5 for 12345:\t" + MD5Hex("12345"));
    }
}