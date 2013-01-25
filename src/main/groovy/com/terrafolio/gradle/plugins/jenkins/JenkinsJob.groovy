package com.terrafolio.gradle.plugins.jenkins

import org.gradle.util.ConfigureUtil

class JenkinsJob {
	def name
	def serverDefinitions = []
	def serverSpecificConfiguration = [:]
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
		if (! serverDefinitions.contains(server)) {
			serverDefinitions += server
		}
	}
	
	def server(JenkinsServerDefinition server, Closure closure) {
		this.server(server)
		if (! serverSpecificConfiguration.containsKey(server)) {
			serverSpecificConfiguration[server] = []
		}
		serverSpecificConfiguration[server] += closure
	}
	
	def getServerSpecificDefinition(JenkinsServerDefinition server) {
		if (serverSpecificConfiguration.containsKey(server)) {
			def newDefinition = new JenkinsJobDefinition(definition.name, definition.xml)
			
			serverSpecificConfiguration[server].each { closure -> 
				ConfigureUtil.configure(closure, newDefinition)
			}
			
			return newDefinition
		} else {
			return definition
		}
	}
}
