package com.terrafolio.gradle.plugins.jenkins.service

import org.apache.http.HttpException
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor

class PreemptiveAuthInterceptor implements HttpRequestInterceptor {
	def String username
	def String password
	
	public PreemptiveAuthInterceptor(String username, String password) {
		this.username = username
		this.password = password
	}

	@Override
	public void process(HttpRequest request,
			org.apache.http.protocol.HttpContext context) throws HttpException,
			IOException {
		request.addHeader('Authorization', 'Basic ' + "${username}:${password}".toString().bytes.encodeBase64().toString())
	}
}
	