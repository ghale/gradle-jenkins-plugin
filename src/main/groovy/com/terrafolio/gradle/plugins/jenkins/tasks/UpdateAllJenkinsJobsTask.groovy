package com.terrafolio.gradle.plugins.jenkins.tasks


class UpdateAllJenkinsJobsTask extends UpdateJenkinsJobsTask {
	@Override
	def void doExecute() {
		jobsToUpdate = getJobs()
		super.doExecute()
	}
}
