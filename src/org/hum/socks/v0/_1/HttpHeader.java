package org.hum.socks.v0._1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HttpHeader {

	private List<String> headers = new ArrayList<String>();

	private String line;
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
		header.line = br.readLine() + "\n";
		StringBuilder reqHeaders = new StringBuilder();
		// 如能识别出请求方式则则继续，不能则退出
		if (header.setRequestLine(header.line) != null) {
			String headerLine = "";
			String lastHeaderLine = "";
			while (!(headerLine = br.readLine()).equals("")) {
				lastHeaderLine = headerLine;
				reqHeaders.append(headerLine).append("\n");
			}
			header.addHeaderString(lastHeaderLine);
		}
		return header;
	}

	/**
	 * 
	 * @param str
	 */
	private void addHeaderString(String str) {
		str = str.replaceAll("\r", "");
		headers.add(str);
		if (str.startsWith("Host")) {// 解析主机和端口
			String[] hosts = str.split(":");
			host = hosts[1].trim();
			if (method.endsWith(METHOD_CONNECT)) {
				port = hosts.length == 3 ? hosts[2] : "443";// https默认端口为443
			} else if (method.endsWith(METHOD_GET) || method.endsWith(METHOD_POST)) {
				port = hosts.length == 3 ? hosts[2] : "80";// http默认端口为80
			}
		}
	}

	/**
	 * 判定请求方式
	 * 
	 * @param str
	 * @return
	 */
	private String setRequestLine(String str) {
		str = str.replaceAll("\r", "");
		if (str.startsWith(METHOD_CONNECT)) {// https链接请求代理
			method = METHOD_CONNECT;
		} else if (str.startsWith(METHOD_GET)) {// http GET请求
			method = METHOD_GET;
		} else if (str.startsWith(METHOD_POST)) {// http POST请求
			method = METHOD_POST;
		}
		return method;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(line);
		for (String str : headers) {
			sb.append(str).append("\r\n");
		}
		sb.append("\r\n");
		return sb.toString();
	}

	public List<String> getHeader() {
		return headers;
	}

	public void setHeader(List<String> header) {
		this.headers = header;
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
