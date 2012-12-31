package org.gradle.plugins.jenkins

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.util.Configurable

class JenkinsConfiguration {
	private final NamedDomainObjectContainer<JenkinsJob> jobs
	private final NamedDomainObjectContainer<JenkinsServerDefinition> servers
	private final NamedDomainObjectContainer<JenkinsJobDefinition> templates
	
	public JenkinsConfiguration(NamedDomainObjectContainer<JenkinsJob> jobs, NamedDomainObjectContainer<JenkinsJobDefinition> templates, NamedDomainObjectContainer<JenkinsServerDefinition> servers) {
		this.jobs = jobs
		this.servers = servers
		this.templates = templates
	}
	
	def jobs(Closure closure) {
		jobs.configure(closure)
	}
	
	def templates(Closure closure) {
		templates.configure(closure)
	}
	
	def servers(Closure closure) {
		servers.configure(closure)
	}
}
