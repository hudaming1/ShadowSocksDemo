package org.hum;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketHttpTest {
	
	private static final String s = "GET /sms/mw/cb_success?spid=1123&mtmsgid=12123213 HTTP/1.1\n" + 
			"Host: localhost\n" + 
			"Connection: keep-alive\n" + 
			"Upgrade-Insecure-Requests: 1\n" + 
			"User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36\n" + 
			"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8\n" + 
			"Accept-Encoding: gzip, deflate, br\n" + 
			"Accept-Language: zh-CN,zh;q=0.9\n" + 
			"\n" + 
			"";
	
	private static final String s2 = "GET /mpush/sms/mengwang/test2?spid=1123&mtmsgid=12123213 HTTP/1.1\n" + 
			"Host: localhost:8011\n" + 
			"Connection: keep-alive\n" + 
			"Cache-Control: max-age=0\n" + 
			"Upgrade-Insecure-Requests: 1\n" + 
			"User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36\n" + 
			"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8\n" + 
			"Accept-Encoding: gzip, deflate, br\n" + 
			"Accept-Language: zh-CN,zh;q=0.9\n" + 
			"\n" + 
			"";

	private static final String s3 = "GET / HTTP/1.1\n" + 
			"Host: localhost:8011\n" + 
			"Connection: keep-alive\n" + 
			"Cache-Control: max-age=0\n" + 
			"Upgrade-Insecure-Requests: 1\n" + 
			"User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36\n" + 
			"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8\n" + 
			"Accept-Encoding: gzip, deflate, br\n" + 
			"Accept-Language: zh-CN,zh;q=0.9\n" + 
			"\n" + 
			"";

	public static void main(String[] args) throws UnknownHostException, IOException {
//		Socket socket = new Socket("39.96.83.46", 80);
		Socket socket = new Socket("192.168.161.128", 80);
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		bw.write(s3);
		bw.flush();
		String line = null;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}
		System.exit(-1);
	}
}
