package org.gradle.plugins.jenkins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class DeleteJenkinsJobsTask extends DefaultTask {

	@TaskAction
	def doDeleteJobs() {
		service = new JenkinsRESTServiceImpl(project.jenkins.url, project.jenkins.username, project.jenkins.password)
		
		project.jenkins.jobs.each { job ->
			def existing = service.getJobConfiguration(job.definition.name)
			if (existing != null) {
				logger.warn('Deleting job ' + job.definition.name)
				service.deleteJob(job.name)
			} else {
				logger.warn('Jenkins job ' + job.definition.name + ' does not exist')
			}
		}
	}
}
