package com.terrafolio.gradle.plugins.jenkins.test

import static org.junit.Assert.*

import com.terrafolio.gradle.plugins.jenkins.DeleteAllJenkinsJobsTask
import com.terrafolio.gradle.plugins.jenkins.DumpJenkinsJobsTask
import com.terrafolio.gradle.plugins.jenkins.JenkinsConfigurationConvention
import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.DeleteJenkinsJobsTask
import com.terrafolio.gradle.plugins.jenkins.JenkinsConfiguration
import com.terrafolio.gradle.plugins.jenkins.JenkinsJob
import com.terrafolio.gradle.plugins.jenkins.UpdateJenkinsJobsTask
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.junit.Test
import org.junit.Before
import org.gradle.testfixtures.ProjectBuilder

class JenkinsPluginTest {
	def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
	def private final JenkinsPlugin plugin = new JenkinsPlugin()

	@Before
	def void setupProject() {
		plugin.apply(project)
	}

	@Test
	def void apply_appliesJenkinsConvention() {
		assert project.convention.plugins.jenkins instanceof JenkinsConfigurationConvention
	}

	@Test
	def void apply_appliesBasePlugin() {
		assert project.plugins.hasPlugin(BasePlugin.class)
	}

	@Test
	void apply_createsJenkinsConfiguration() {
		assert project.convention.plugins.jenkins.jenkins instanceof JenkinsConfiguration
	}

	@Test
	void apply_createsJenkinsJobsCollection() {
		assert project.convention.plugins.jenkins.jenkins.jobs instanceof NamedDomainObjectCollection<JenkinsJob>
	}

	@Test
	void apply_createsJenkinsServerDefinitionCollection() {
		assert project.convention.plugins.jenkins.jenkins.servers instanceof NamedDomainObjectCollection<JenkinsServerDefinition>
	}

	@Test
	void apply_createsJenkinsTemplatesCollection() {
		assert project.convention.plugins.jenkins.jenkins.templates instanceof NamedDomainObjectCollection<JenkinsJobDefinition>
	}

	@Test
	void configure_addsJenkinsJob() {
		project.jenkins {
			jobs {
				testJob
			}
		}

		assert project.convention.plugins.jenkins.jenkins.jobs.findByName('testJob') instanceof JenkinsJob
	}

	@Test
	void apply_createsUpdateJenkinsJobsTask() {
		assert project.tasks.findByName('updateJenkinsJobs') instanceof UpdateJenkinsJobsTask
	}

	@Test
	void apply_createsDeleteJenkinsJobsTask() {
		assert project.tasks.findByName('deleteJenkinsJobs') instanceof DeleteAllJenkinsJobsTask
	}

	@Test
	void apply_createsDumpJenkinsJobsTask() {
		assert project.tasks.findByName('dumpJenkinsJobs') instanceof DumpJenkinsJobsTask
	}
	
	@Test
	void apply_createsRetireJenkinsJobsTask() {
		assert project.tasks.findByName('retireJenkinsJobs') instanceof DeleteJenkinsJobsTask
	}
}
