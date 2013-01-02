package com.terrafolio.gradle.plugins.jenkins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class DeleteJenkinsJobsTask extends AbstractJenkinsTask {

	@TaskAction
	def doDeleteJobs() {
		project.jenkins.jobs.each { job ->
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
}
