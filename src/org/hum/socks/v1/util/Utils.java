package org.hum.socks.v1.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

public class Utils {

	public static void main(String[] args) throws Exception {
//		byte[] bytes = new byte[5];
//		bytes[0] = 1;
//		bytes[1] = 123;
//		bytes[2] = 0;
//		bytes[3] = -121;
//		bytes[4] = -38;
//		print(bytes);
//		byte[] encrypt = encrypt(bytes);
//		print(encrypt);
//		print(decrypt(encrypt));
		System.out.println("=================================");
		byte[] bs = new byte[] { 53, -7, -16, -19, -112, -22, -87, 108, 121, -20, 75, 18, 71, 75, -71, -11, 1, -69, 27, -38, -14, 98, 109, 19, -81, 5, -65, -92, 51, -120, -30, -15, 96, 31, 73, -76, 83, 112, 120, -14, 101, -112, -21, 83, 14, -127, -85, 78, 14, 13, -7, -47, 101, 63, 55, -2, -106, 24, -116, 104, -86, 106, 108, 33, 115, -93, -68, 35, -61, -128, 5, 126, -115, 25, 93, -9, 85, -80, 109, 105, 103, -72, 117, -93, 68, 25, 89, 86, -98, 8, 45, -124, -33, -68, 56, 44, -78, -92, 109, 106, 86, -12, -57, 95, -34, 76, -128, 127, 48, -37, 74, 68, -59, 30, -118, 33, 0, 6, -127, 107, 39, 11, -71, -42, -16, 48, 103, 75, 57, -120, 106, 111, -84, 120, 47, -122, 26, 47, 20, 103, 3, 109, -15, 121, 34, 99, 91, -1, 118, -43, 18, 76, -114, 0, -113, 84, -124, -48, 15, -114, 45, -110, -65, -10, 109, 41, -70, 91, -102, 120, -24, 104, 113, -2, 21, -59, 120, 114, 19, -53, 77, 87, 106, 102, -56, 103, -68, -119, -97, -125, 2, 53, 60, -111, -43, 104, 114, 93, 95, -96, -61, 43, -21, 19, 109, -114, -108, -40, -35, -18, -52, -128, 91, -6, -117, 18, -57, -47, -117, 58, 34, 73, 73, 17, 93, -77, 13, -44, 30, -110, -92, -64, 105, 38, -118, 44, -42, -54, -72, 2, -51, 18, -11, 45, -44, 81, -92, -3, -120, 28, -36, -80, -8, 127, 103, -111, -32, -104, -101, -71, -9, -100, -48, 39, -123, 30, -64, 101, 41, 91, 64, -80, -42, -58, -78, 102, 35, 127, 70, 81, 8, 126, 23, 34, -8, 102, -17, 5, -88, -125, 39, 92, 41, -15, -97, -115, 69, 5, 9, -29, -70, -88, -67, 118, -2, 55, 0 };
//		System.out.println(Arrays.toString(bs));
		System.out.println(Arrays.toString(encrypt(bs)));
		System.out.println(Arrays.toString(decrypt(encrypt(bs))));
	}
	
	public static byte[] baseEncrypt(byte[] _bytes) {
		byte[] bytes = new byte[_bytes.length];
		System.arraycopy(_bytes, 0, bytes, 0, _bytes.length);
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ((bytes[i] + 3));
		}
		return bytes;
	}

	public static byte[] encrypt(byte[] _bytes) {
		try {
			return AESCoder.encrypt(_bytes);
//			return baseEncrypt(_bytes);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("decrypt error, byte=" + Arrays.toString(_bytes));
		}
	}

	public static byte[] decrypt(byte[] _bytes) {
		try {
			return AESCoder.decrypt(_bytes);
//			return baseDecrypt(_bytes);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("decrypt error, byte=" + Arrays.toString(_bytes));
		}
	}

	static byte[] aesCoderEncrypt(byte[] _bytes) {
		try {
			return AESCoder.encrypt(_bytes);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("decrypt error, byte=" + Arrays.toString(_bytes));
		}
	}

	public static byte[] baseDecrypt(byte[] _bytes) {
		byte[] bytes = new byte[_bytes.length];
		System.arraycopy(_bytes, 0, bytes, 0, _bytes.length);
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ((bytes[i] - 3));
		}
		return bytes;
	}

	static byte[] aesDecrypt(byte[] _bytes) {
		try {
			return AESCoder.decrypt(_bytes);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("decrypt error, byte=" + Arrays.toString(_bytes));
		}
	}

	static void print(byte[] bytes) {
		System.out.println(Arrays.toString(bytes));
	}


	public static byte[] readEncryptBytes(DataInputStream inputStream) throws IOException {
		int readedLength = 0;
		byte[] arr = new byte[inputStream.readInt()];
		while (readedLength < arr.length) {
			int available = inputStream.available();
			int readCount = (arr.length - readedLength);
			byte[] buffer = new byte[readCount];
			int len = inputStream.read(buffer);
			try {
				System.arraycopy(buffer, 0, arr, readedLength, len);
			} catch (ArrayIndexOutOfBoundsException ce) {
				System.out.println("out of bounds, arr.length=" + arr.length + ", readedLength=" + readedLength + ", available=" + available + ", len=" + len);
				throw ce;
			}
			readedLength += len;
		}
		return arr;
	}

}
