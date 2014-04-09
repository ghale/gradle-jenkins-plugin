package com.terrafolio.gradle.plugins.jenkins.tasks

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsJob
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsServerDefinition
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsService

class DeleteJenkinsJobsTask extends AbstractJenkinsTask {
	def jobsToDelete = []

	def void doExecute() {
		jobsToDelete.each { job ->
			eachServer(job) { JenkinsServerDefinition server, JenkinsService service ->
				def existing = service.getJobConfiguration(job.definition.name, job.serviceOverrides.get)
				if (existing != null) {
					logger.warn('Deleting job ' + job.definition.name + ' on ' + server.url)
					service.deleteJob(job.definition.name, job.serviceOverrides.delete)
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
		def job = new JenkinsJob(jobName, null)
		job.server server
		job.definition {
			name jobName
		}
		delete(job)
	}
}
