package com.secondhand.common.tool;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Abel on 2016/6/16.
 */
public class DigestUtils {
    public final static String ENCODING = "UTF-8";
    public final static String DIGEST_KEY = "AVuu7SWwv99YCbb11IHlk5ONlq77YXba3HLlp57RRvu99Bbe13HHkk55RQuu99i3";
    public final static String SALT = "263517af-dded-11e5-858f-00163e0056f8";

    public static String decode_base64(String data) {
        byte[] _date = data.getBytes();
        try {
            return new String(Base64.decodeBase64(_date), ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encoding unknown", e);
        }
    }

    public static String decode_base64(String data, String encoding) {
        byte[] _date = data.getBytes();
        try {
            return new String(Base64.decodeBase64(_date), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encoding unknown", e);
        }
    }

    public static String encode_base64(String data) {
        byte[] _date = data.getBytes();
        try {
            return new String(Base64.encodeBase64(_date), ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encoding unknown", e);
        }
    }

    public static byte[] encode_base64_to_byte(byte[] data) {
        try {
            return Base64.encodeBase64(data);
        } catch (Exception e) {
            throw new RuntimeException("encoding unknown", e);
        }
    }

    public static String encode_base64(byte[] date) {
        try {
            return new String(Base64.encodeBase64(date), ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encoding unknown", e);
        }
    }

    public static byte[] decode_base64(byte[] date) {
        return Base64.decodeBase64(date);
    }


    public static String encode_base64(String data, String encoding) {
        byte[] _date = data.getBytes();
        try {
            return new String(Base64.encodeBase64(_date), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encoding unknown", e);
        }
    }

    public static String encode_base64(String data, String byteEncoding, String encoding) {
        try {
            byte[] _date = data.getBytes(byteEncoding);
            return new String(Base64.encodeBase64(_date), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encoding unknown", e);
        }
    }

    public static String md5(String data) {
        return org.apache.commons.codec.digest.DigestUtils.md5Hex(data);
    }

    public static String md5(byte[] data) {
        return org.apache.commons.codec.digest.DigestUtils.md5Hex(data);
    }

    public static String md5(InputStream data) {
        String _date = null;
        try {
            _date = org.apache.commons.codec.digest.DigestUtils.md5Hex(data);
        } catch (IOException ignored) {
        }
        return _date;
    }

    public static String sha1(String data) {
        return org.apache.commons.codec.digest.DigestUtils.sha1Hex(data);
    }


    public static String urlEncode(String data, String encoding) {
        try {
            return URLEncoder.encode(data, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encoding unknown", e);
        }
    }

    public static String urlDecode(String data, String encoding) {
        try {
            return URLDecoder.decode(data, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encoding unknown", e);
        }
    }


}
