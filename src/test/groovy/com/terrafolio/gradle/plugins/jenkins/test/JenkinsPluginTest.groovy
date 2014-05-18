package com.terrafolio.gradle.plugins.jenkins.test

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfiguration
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurationConvention
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsJob
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsJobDefinition
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsServerDefinition
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsView
import com.terrafolio.gradle.plugins.jenkins.tasks.*
import nebula.test.PluginProjectSpec
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class JenkinsPluginTest extends PluginProjectSpec {
    @Override
    String getPluginName() {
        return 'jenkins'
    }

    def setup() {
        project.apply plugin: pluginName
    }

    def "apply applies JenkinsConfigurationConvention" () {
        expect: project.convention.plugins.jenkins instanceof JenkinsConfigurationConvention
    }

    def "apply applies BasePlugin" () {
        expect: project.plugins.hasPlugin(BasePlugin.class)
    }

    def "apply adds JenkinsConfiguration" () {
        expect: project.convention.plugins.jenkins.jenkins instanceof JenkinsConfiguration
    }

    def "apply creates Jenkins Jobs Collection" () {
        expect: project.convention.plugins.jenkins.jenkins.jobs instanceof NamedDomainObjectCollection<JenkinsJob>
    }

    def "apply creates Jenkins Server Definition Collection" () {
        expect: project.convention.plugins.jenkins.jenkins.servers instanceof NamedDomainObjectCollection<JenkinsServerDefinition>
    }

    def "apply creates Jenkins Templates Collection" () {
        expect: project.convention.plugins.jenkins.jenkins.templates instanceof NamedDomainObjectCollection<JenkinsJobDefinition>
    }

    def "apply creates Jenkins Views Collection" () {
        expect: project.convention.plugins.jenkins.jenkins.views instanceof NamedDomainObjectCollection<JenkinsView>
    }

    def "apply creates updateJenkinsJobs task" () {
        expect: project.tasks.findByName('updateJenkinsJobs') instanceof UpdateAllJenkinsJobsTask
    }

    def "apply creates deleteJenkinsJobs task" () {
        expect: project.tasks.findByName('deleteJenkinsJobs') instanceof DeleteAllJenkinsJobsTask
    }

    def "apply creates dumpJenkinsJobs task" () {
        expect: project.tasks.findByName('dumpJenkinsJobs') instanceof DumpJenkinsJobsTask
    }

    def "apply creates retireJenkinsJobs task" () {
        expect: project.tasks.findByName('retireJenkinsJobs') instanceof DeleteJenkinsJobsTask
    }

    def "apply creates validateJenkinsJobs task" () {
        expect: project.tasks.findByName('validateJenkinsJobs') instanceof ValidateJenkinsJobsTask
    }

    def "apply creates dumpRemoteJenkinsItems task" () {
        expect: project.tasks.findByName('dumpRemoteJenkinsItems') instanceof DumpRemoteJenkinsItemsTask
    }

    
}
