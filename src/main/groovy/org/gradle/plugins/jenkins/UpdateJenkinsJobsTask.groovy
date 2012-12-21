package org.gradle.plugins.jenkins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class UpdateJenkinsJobsTask extends DefaultTask {
	def JenkinsService service

	@TaskAction
	def doUpdate() {
		service = new JenkinsRESTServiceImpl(project.jenkins.url, project.jenkins.username, project.jenkins.password)
		
		project.jenkins.jobs.each { job ->
			def existing = service.getJobConfiguration(job.definition.name)
			if (existing == null) {
				logger.warn('Creating new job ' + job.definition.name)
				service.createJob(job.definition.name, job.definition.xml)
			} else {
				logger.warn('Updating job ' + job.definition.name)
				service.updateJobConfiguration(job.definition.name, job.definition.xml)
			}
		}
	}
}
