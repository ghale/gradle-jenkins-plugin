package com.terrafolio.gradle.plugins.jenkins

import org.gradle.api.DefaultTask

abstract class AbstractJenkinsTask extends DefaultTask {

	def List<JenkinsServerDefinition> getServerDefinitions(JenkinsJob job) {
		def serverDefinitions = []
		if (job.serverDefinitions == null || job.serverDefinitions.isEmpty()) {
			if (project.jenkins.defaultServer != null) {
				serverDefinitions = [ project.jenkins.defaultServer ]
			} else {
				throw new JenkinsConfigurationException("No servers defined for job ${job.name} and no defaultServer set!")
			}
		} else {
			serverDefinitions = job.serverDefinitions
		}
		
		serverDefinitions.each { server ->
			def console = System.console()
			if (server.url == null) {
				if (console != null) {
					server.url = console.readLine("\nEnter the URL for server \"${server.name}\": ", null)
				} else {
					throw new JenkinsConfigurationException("No URL defined for server \"${server.name}\" and no console available for input.")
				}
			}
			
			if (server.secure) {
				if (server.username == null) {
					if (console != null) {
						server.username = console.readLine("\nEnter the username for server \"${server.name}\": ", null)
					} else {
						throw new JenkinsConfigurationException("No username defined for server \"${server.name}\" and no console available for input.")
					}
				}
				
				if (server.password == null) {
					if (console != null) {
						server.password = new String(console.readPassword("\nEnter the password for server \"${server.name}\": ", null))
					} else {
						throw new JenkinsConfigurationException("No password defined for server \"${server.name}\" and no console available for input.")
					}
				}
			}
		}
		
		return serverDefinitions
	}
}
