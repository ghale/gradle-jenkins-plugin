package org.gradle.plugins.jenkins

class JenkinsServerDefinition {
	def name
	def url
	def username
	def password
	def secure = true
	
	JenkinsServerDefinition(String name) {
		this.name = name
	}
	
	def url(String url) {
		this.url = url
	}
	
	def username(String username) {
		this.username = username
	}
	
	def password(String password) {
		this.password = password
	}
	
	def secure(Boolean secure) {
		this.secure = secure
	}
}
