package com.terrafolio.gradle.plugins.jenkins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class AbstractJenkinsTask extends DefaultTask {
	@TaskAction
	def void executeTask() {
		initialize()
		doExecute()
	}
	
	def abstract void doExecute()
	
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
				serverDefinitions = job.serverDefinitions.findAll { it.name ==~ project.jenkinsServerFilter }	
			} else {
				serverDefinitions = job.serverDefinitions
			}
		}
		
		serverDefinitions.each { server ->
			server.checkDefinitionValues()
		}
		
		return serverDefinitions
	}
	
	def List<JenkinsJob> getJobs() {
		def jobs = []
		if (project.hasProperty('jenkinsJobFilter')) {
			jobs = project.jenkins.jobs.findAll { it.name ==~ project.jenkinsJobFilter } as List
		} else {
			jobs = project.jenkins.jobs as List
		}
		
		return jobs
	}
	
	def void initialize() {
		getJobs().each { job ->	getServerDefinitions(job) }
	}
}
