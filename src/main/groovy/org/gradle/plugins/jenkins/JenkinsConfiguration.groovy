package org.gradle.plugins.jenkins

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.util.Configurable

class JenkinsConfiguration {
	private final NamedDomainObjectContainer<JenkinsJob> jobs
	private final NamedDomainObjectContainer<JenkinsJobDefinition> templates
	
	def url
	def username
	def password
	
	public JenkinsConfiguration(NamedDomainObjectContainer<JenkinsJob> jobs, NamedDomainObjectContainer<JenkinsJobDefinition> templates) {
		this.jobs = jobs
		this.templates = templates
	}
	
	def jobs(Closure closure) {
		jobs.configure(closure)
	}
	
	def templates(Closure closure) {
		templates.configure(closure)
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
}
