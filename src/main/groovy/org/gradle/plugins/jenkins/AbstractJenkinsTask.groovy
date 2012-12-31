package org.gradle.plugins.jenkins

import org.gradle.api.DefaultTask

abstract class AbstractJenkinsTask extends DefaultTask {

	def List<JenkinsServerDefinition> getServerDefinitions(List<String> serverNames) {
		if (serverNames == null || serverNames.isEmpty()) {
			serverNames = [ "default" ]
		}
		
		def serverDefinitions = []
		serverNames.each { serverName ->
			def server = project.jenkins.servers.findByName(serverName)
			if (server == null) {
				throw new JenkinsConfigurationException("No server definition named " + serverName + " found!") 
			} else {
				serverDefinitions += server
			}
		}
		
		return serverDefinitions
	}
}
