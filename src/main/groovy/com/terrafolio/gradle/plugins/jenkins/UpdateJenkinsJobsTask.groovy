package com.terrafolio.gradle.plugins.jenkins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class UpdateJenkinsJobsTask extends AbstractJenkinsTask {
	def jobsToUpdate = []
	
	def void doExecute() {
		jobsToUpdate.each { job ->
			getServerDefinitions(job).each { server ->
				def service = server.secure ? new JenkinsRESTServiceImpl(server.url, server.username, server.password) : new JenkinsRESTServiceImpl(server.url)
				def existing = service.getJobConfiguration(job.definition.name, job.serviceOverrides.get)
				if (existing == null) {
					logger.warn('Creating new job ' + job.definition.name + ' on ' + server.url)
					service.createJob(job.definition.name, job.getServerSpecificDefinition(server).xml, job.serviceOverrides.create)
				} else {
					logger.warn('Updating job ' + job.definition.name + ' on ' + server.url)
					service.updateJobConfiguration(job.definition.name, job.getServerSpecificDefinition(server).xml, job.serviceOverrides.update)
				}
			}
		}
	}
	
	def void update(JenkinsJob job) {
		jobsToUpdate += job
	}
}
