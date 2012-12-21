package org.gradle.plugins.jenkins

import org.gradle.api.Plugin
import org.gradle.api.Project

class JenkinsPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		applyTasks(project)
		applyConventions(project)
	}
	
	def applyTasks(Project project) {
		project.task('updateJenkinsJobs', type: UpdateJenkinsJobsTask)
		project.task('deleteJenkinsJobs', type: DeleteJenkinsJobsTask)
	}

	def applyConventions(Project project) {
		def jobs = project.container(JenkinsJob) { name ->
			new JenkinsJob(name)
		}
		
		def templates = project.container(JenkinsJobDefinition) { name ->
			new JenkinsJobDefinition(name)
		}
		
		def configuration = new JenkinsConfiguration(jobs, templates)
		project.convention.plugins.jenkins = new JenkinsConfigurationConvention(configuration)
	}
}
