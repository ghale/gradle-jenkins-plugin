package com.terrafolio.gradle.plugins.jenkins.tasks

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsJob
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsServerDefinition
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsService
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit

class UpdateJenkinsJobsTask extends AbstractJenkinsTask {
	def jobsToUpdate = []
	
	def void doExecute() {
		jobsToUpdate.each { job ->
			eachServer(job) { JenkinsServerDefinition server, JenkinsService service ->
				def existing = service.getJobConfiguration(job.definition.name, job.serviceOverrides.get)
				if (existing == null) {
					logger.warn('Creating new job ' + job.definition.name + ' on ' + server.url)
					service.createJob(job.definition.name, job.getServerSpecificDefinition(server).xml, job.serviceOverrides.create)
				} else {
					XMLUnit.setIgnoreWhitespace(true)
					def Diff xmlDiff = new Diff(job.definition.xml, existing)
					if ((! xmlDiff.similar()) || (project.hasProperty('forceJenkinsJobsUpdate') && Boolean.valueOf(project.forceJenkinsJobsUpdate))) {
						logger.warn('Updating job ' + job.definition.name + ' on ' + server.url)
						service.updateJobConfiguration(job.definition.name, job.getServerSpecificDefinition(server).xml, job.serviceOverrides.update)
					} else {
						logger.warn('Jenkins job ' + job.definition.name + ' has no changes to the existing job on ' + server.url)
					}
				}
			}
		}
	}
	
	def void update(JenkinsJob job) {
		jobsToUpdate += job
	}
}
