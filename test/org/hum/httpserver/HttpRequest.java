package org.hum.httpserver;

public class HttpRequest {

	private String host;
	private Integer port;
	private String method;
	private String fullRequest;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getFullRequest() {
		return fullRequest;
	}

	public void setFullRequest(String fullRequest) {
		this.fullRequest = fullRequest;
	}

	@Override
	public String toString() {
		return "HttpRequest [host=" + host + ", port=" + port + ", method=" + method + ", fullRequest=" + fullRequest + "]";
	}
}
