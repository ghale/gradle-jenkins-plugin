package com.terrafolio.gradle.plugins.jenkins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class DeleteJenkinsJobsTask extends AbstractJenkinsTask {
	def jobsToDelete = []

	def void doExecute() {
		jobsToDelete.each { job ->
			getServerDefinitions(job).each { server ->
				def service = server.secure ? new JenkinsRESTServiceImpl(server.url, server.username, server.password) : new JenkinsRESTServiceImpl(server.url)
				def existing = service.getJobConfiguration(job.definition.name)
				if (existing != null) {
					logger.warn('Deleting job ' + job.definition.name + ' on ' + server.url)
					service.deleteJob(job.definition.name)
				} else {
					logger.warn('Jenkins job ' + job.definition.name + ' does not exist on ' + server.url)
				}
			}
		}
	}
	
	def void delete(JenkinsJob job) {
		jobsToDelete += job
	}
	
	def void delete(JenkinsServerDefinition server, String jobName) {
		def job = new JenkinsJob(jobName)
		job.server server
		job.definition {
			name jobName
		}
		delete(job)
	}
}
