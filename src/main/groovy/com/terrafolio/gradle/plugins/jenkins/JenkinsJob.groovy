package com.terrafolio.gradle.plugins.jenkins

import org.gradle.util.ConfigureUtil

class JenkinsJob {
	def name
	def serverDefinitions = []
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
	
	def server(JenkinsServerDefinition server) {
		serverDefinitions += server
	}
}
