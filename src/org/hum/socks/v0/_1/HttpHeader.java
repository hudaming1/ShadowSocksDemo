package org.hum.socks.v0._1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpHeader {

	private String method;
	private String host;
	private String port;

	public static final int MAXLINESIZE = 4096000;

	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";
	public static final String METHOD_CONNECT = "CONNECT";

	private HttpHeader() {
	}

	/**
	 * 从数据流中读取请求头部信息，必须在放在流开启之后，任何数据读取之前
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static final HttpHeader readHeader(InputStream in) throws IOException {
		HttpHeader header = new HttpHeader();
		BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
		header.method = br.readLine().split(" ")[0];
		String headerLine = "";
		while (!(headerLine = br.readLine()).equals("")) {
			// 解析主机和端口
			if (headerLine.startsWith("Host")) {
				String[] hosts = headerLine.split(":");
				header.host = hosts[1].trim();
				if (header.method.endsWith(METHOD_CONNECT)) {
					header.port = hosts.length == 3 ? hosts[2] : "443";// https默认端口为443
				} else if (header.method.endsWith(METHOD_GET) || header.method.endsWith(METHOD_POST)) {
					header.port = hosts.length == 3 ? hosts[2] : "80";// http默认端口为80
				}
			}
		}
		return header;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
}
