package com.terrafolio.gradle.plugins.jenkins

import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.Difference
import org.custommonkey.xmlunit.XMLUnit

class ValidateJenkinsJobsTask extends AbstractJenkinsTask {
	def failOnDifference = true

	@Override
	public void doExecute() {
		def success = true
		getJobs().each { job ->
			eachServer(job) { JenkinsServerDefinition server, JenkinsService service ->
				def serverJob = service.getJobConfiguration(job.definition.name, job.serviceOverrides.get)
				if (serverJob == null) {
					logger.warn('Jenkins job ' + job.definition.name + ' does not exist on ' + server.url)
					success = false
				} else {
					XMLUnit.setIgnoreWhitespace(true)
					def xmlDiff = new DetailedDiff(new Diff(job.definition.xml, serverJob))
					if (xmlDiff.similar()) {
						logger.info('Jenkins job ' + job.definition.name + ' matches the version on ' + server.url) 
					} else {
						logger.warn('Jenkins job ' + job.definition.name + ' differs from the version on ' + server.url)
						xmlDiff.getAllDifferences().each { Difference difference ->
							logger.info(difference.toString())
						}
						success = false
					} 
				}
			}
		}
		
		if ((! success) && failOnDifference) {
			throw new JenkinsValidationException("Jenkins job validation failed!", null)
		}
	}

}
