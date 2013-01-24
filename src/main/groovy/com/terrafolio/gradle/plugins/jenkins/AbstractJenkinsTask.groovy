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
			if (project.hasProperty('jenkinsServerFilter')) {
				serverDefinitions = job.serverDefinitions.findAll { it.name =~ project.jenkinsServerFilter }	
			} else {
				serverDefinitions = job.serverDefinitions
			}
		}
		
		serverDefinitions.each { server ->
			server.checkDefinitionValues()
		}
		
		return serverDefinitions
	}
}
