package com.terrafolio.gradle.plugins.jenkins

import com.terrafolio.gradle.plugins.jenkins.dsl.*
import com.terrafolio.gradle.plugins.jenkins.jobdsl.MapJobManagement
import com.terrafolio.gradle.plugins.jenkins.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

class JenkinsPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.plugins.apply(BasePlugin.class)
		applyTasks(project)
		applyConventions(project)
	}
	
	def applyTasks(Project project) {
		project.task('updateJenkinsJobs', type: UpdateAllJenkinsJobsTask)
		project.task('deleteJenkinsJobs', type: DeleteAllJenkinsJobsTask)
		project.task('dumpJenkinsJobs', type: DumpJenkinsJobsTask)
		project.task('retireJenkinsJobs', type: DeleteJenkinsJobsTask)
		project.task('validateJenkinsJobs', type: ValidateJenkinsJobsTask)
	}

	def applyConventions(Project project) {
        def MapJobManagement jm = new MapJobManagement(new HashMap<String, String>())

		def jobs = project.container(JenkinsJob) { name ->
			new JenkinsJob(name, jm)
		}
		
		def templates = project.container(JenkinsJobTemplate) { name ->
			new JenkinsJobTemplate(name, jm)
		}
		
		def servers = project.container(JenkinsServerDefinition) { name ->
			new JenkinsServerDefinition(name)
		}
		
		def configuration = new JenkinsConfiguration(jobs, templates, servers, jm)
		project.convention.plugins.jenkins = new JenkinsConfigurationConvention(configuration)
	}
}
