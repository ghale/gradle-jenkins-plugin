package org.gradle.plugins.jenkins.test;

import static org.junit.Assert.*;

import org.gradle.plugins.jenkins.JenkinsConfigurationConvention
import org.gradle.plugins.jenkins.JenkinsPlugin
import org.gradle.plugins.jenkins.DeleteJenkinsJobsTask
import org.gradle.plugins.jenkins.JenkinsConfiguration
import org.gradle.plugins.jenkins.JenkinsJob
import org.gradle.plugins.jenkins.UpdateJenkinsJobsTask
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project;
import org.junit.Test;
import org.junit.Before;
import org.gradle.testfixtures.ProjectBuilder

class JenkinsPluginTest {
	def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
	def private final JenkinsPlugin plugin = new JenkinsPlugin()
	
	@Before
	def void setupProject() {
		plugin.apply(project)
	}
	
	@Test
	def void apply_appliesAvailityJenkinsConvention() {
		assert project.convention.plugins.jenkins instanceof JenkinsConfigurationConvention
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
		assert project.tasks.findByName('deleteJenkinsJobs') instanceof DeleteJenkinsJobsTask
	}
	

}
