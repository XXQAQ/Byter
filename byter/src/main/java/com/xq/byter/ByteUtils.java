package com.xq.byter;

import java.nio.ByteBuffer;

public final class ByteUtils {

    public static byte[] subBytes(byte[] bytes, int start, int end) {
        if (bytes == null) {
            return null;
        }
        if (start < 0) {
            start = 0;
        }
        if (end > bytes.length) {
            end = bytes.length;
        }
        int newSize = end - start;
        byte[] subArray = new byte[newSize];
        System.arraycopy(bytes, start, subArray, 0, newSize);
        return subArray;
    }

    public static byte[] concatByte(byte... bytes){
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        for (byte b : bytes)
            byteBuffer.put(b);
        return byteBuffer2Bytes(byteBuffer);
    }

    public static byte[] concatBytes(byte[]... bytesArray){
        int allLength = 0;
        for (byte[] bytes : bytesArray){
            allLength += bytes.length;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(allLength);
        for (byte[] bytes : bytesArray)
            byteBuffer.put(bytes);
        return byteBuffer2Bytes(byteBuffer);
    }

    public static byte[] reverseBytes(byte[] a){
        byte[] b = new byte[a.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = a[b.length - i - 1];
        }
        return b;
    }

    public static byte[] byteBuffer2Bytes(ByteBuffer byteBuffer){
        int len = byteBuffer.position() > 0? byteBuffer.position() : byteBuffer.limit() == byteBuffer.capacity()?0 : byteBuffer.limit();
        byte[] bytes = new byte[len];
        System.arraycopy(byteBuffer.array(),0,bytes,0,len);
        return bytes;
    }

    public static byte[] byte2Bytes(byte b){
        return new byte[]{b};
    }

    public static byte[] char2Bytes(char c) {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0xFF00) >> 8);
        b[1] = (byte) (c & 0xFF);
        return b;
    }

    public static char bytes2Char(byte[] b) {
        char c = (char) (((b[0] & 0xFF) << 8) | (b[1] & 0xFF));
        return c;
    }

    public static byte[] short2Bytes(short s) {
        byte[] b = new byte[2];
        b[0] = (byte) ((s & 0xFF00) >> 8);
        b[1] = (byte) (s & 0xFF);
        return b;
    }

    public static short bytes2Short(byte[] b) {
        short s = (short) (((b[0] & 0xFF) << 8) | (b[1] & 0xFF));
        return s;
    }

    public static int bytes2Int(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24
                ;
    }

    public static byte[] int2Bytes(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    public static byte[] long2Bytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytes2Long(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    public static boolean bytes2Boolean(byte[] bytes){
        return bytes[0] == (byte) 1;
    }

    public static byte[] boolean2Bytes(boolean b){
        return new byte[]{b?(byte) 1:(byte) 0};
    }

    public static double bytes2Double(byte[] bytes){
        return Double.longBitsToDouble(bytes2Long(bytes));
    }

    public static byte[] double2bytes(double d){
        return long2Bytes(Double.doubleToLongBits(d));
    }

    public static float bytes2Float(byte[] bytes){
        return Float.intBitsToFloat(bytes2Int(bytes));
    }

    public static byte[] float2bytes(float f){
        return int2Bytes(Float.floatToIntBits(f));
    }

    private static final char HEX_DIGITS[] =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String bytes2HexString(final byte[] bytes) {
        if (bytes == null) return "";
        int len = bytes.length;
        if (len <= 0) return "";
        char[] ret = new char[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            ret[j++] = HEX_DIGITS[bytes[i] >> 4 & 0x0f];
            ret[j++] = HEX_DIGITS[bytes[i] & 0x0f];
        }
        return new String(ret);
    }

    public static byte[] hexString2Bytes(String hexString) {
        if (isSpace(hexString)) return null;
        int len = hexString.length();
        if (len % 2 != 0) {
            hexString = "0" + hexString;
            len = len + 1;
        }
        char[] hexBytes = hexString.toUpperCase().toCharArray();
        byte[] ret = new byte[len >> 1];
        for (int i = 0; i < len; i += 2) {
            ret[i >> 1] = (byte) (hex2Dec(hexBytes[i]) << 4 | hex2Dec(hexBytes[i + 1]));
        }
        return ret;
    }

    public static int hex2Dec(final char hexChar) {
        if (hexChar >= '0' && hexChar <= '9') {
            return hexChar - '0';
        } else if (hexChar >= 'A' && hexChar <= 'F') {
            return hexChar - 'A' + 10;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
