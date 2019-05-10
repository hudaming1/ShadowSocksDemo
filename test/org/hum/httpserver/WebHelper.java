package org.hum.httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WebHelper {

	public static HttpRequest parse(InputStream inputStream) throws IOException {
		HttpRequest request = new HttpRequest();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));

		String requestLine = br.readLine();
		request.setMethod(requestLine.split(" ")[0]);

		String line = null;
		while (!(line = br.readLine()).equals("")) {
			if (line.startsWith("Host")) {
				String[] arr = line.split(":");
				if ("CONNECT".equals(request.getMethod())) {
					request.setHost(arr[1].trim());
					request.setPort(arr.length == 3 ? Integer.parseInt(arr[2]) : 443);
				} else {
					request.setHost(arr[1].trim());
					request.setPort(arr.length == 3 ? Integer.parseInt(arr[2]) : 80);
				}
			}
		}
		
		return request;
	}
}
