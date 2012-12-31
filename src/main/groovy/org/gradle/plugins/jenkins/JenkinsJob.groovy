package org.gradle.plugins.jenkins

import org.gradle.util.ConfigureUtil

class JenkinsJob {
	def name
	def servers = []
	def definition
	
	JenkinsJob(String name) {
		this.name = name
	}
	
	def definition(JenkinsJobDefinition definition) {
		this.definition = definition
	}
	
	def definition(Closure closure) {
		this.definition = new JenkinsJobDefinition()
		ConfigureUtil.configure(closure, definition)
	}
	
	def server(String serverName) {
		servers += serverName
	}
}
