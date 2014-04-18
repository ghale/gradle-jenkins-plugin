package com.terrafolio.gradle.plugins.jenkins.test

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfiguration
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurationConvention
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsJob
import com.terrafolio.gradle.plugins.jenkins.tasks.*
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

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
    void apply_createsJenkinsViewsCollection() {
        assert project.convention.plugins.jenkins.jenkins.views instanceof NamedDomainObjectCollection<JenkinsView>
    }

    @Test
    void apply_createsUpdateJenkinsJobsTask() {
        assert project.tasks.findByName('updateJenkinsJobs') instanceof UpdateAllJenkinsJobsTask
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

    @Test
    void apply_createValidateJenkinsJobsTask() {
        assert project.tasks.findByName('validateJenkinsJobs') instanceof ValidateJenkinsJobsTask
    }
}
