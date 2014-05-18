package com.terrafolio.gradle.plugins.jenkins.tasks

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurable
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurationException
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsJob
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsServerDefinition
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceFactory
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsService
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsServiceFactory
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class AbstractJenkinsTask extends DefaultTask {
    def needsCredentials = true
    def JenkinsServiceFactory serviceFactory = new JenkinsRESTServiceFactory()

    AbstractJenkinsTask() {
        super()
        group = 'Jenkins Management'
    }

    @TaskAction
    def void executeTask() {
        initialize()
        doExecute()
    }

    def abstract void doExecute()

    def List<JenkinsServerDefinition> getServerDefinitions(JenkinsConfigurable item) {
        def serverDefinitions = []
        if (item.serverDefinitions == null || item.serverDefinitions.isEmpty()) {
            if (project.jenkins.defaultServer != null) {
                serverDefinitions = [ project.jenkins.defaultServer ]
            } else {
                throw new JenkinsConfigurationException("No servers defined for item ${item.name} and no defaultServer set!")
            }
        } else {
            if (project.hasProperty('jenkinsServerFilter')) {
                serverDefinitions = item.serverDefinitions.findAll { it.name ==~ project.jenkinsServerFilter }
            } else {
                serverDefinitions = item.serverDefinitions
            }
        }

        if (needsCredentials) {
            serverDefinitions.each { server ->
                server.checkDefinitionValues()
            }
        }

        return serverDefinitions
    }

    def List<JenkinsJob> getJobs() {
        def jobs
        if (project.hasProperty('jenkinsJobFilter')) {
            jobs = project.jenkins.jobs.findAll { it.name ==~ project.jenkinsJobFilter } as List
        } else {
            jobs = project.jenkins.jobs as List
        }

        return jobs
    }

    def List<JenkinsJob> getViews() {
        def views
        if (project.hasProperty('jenkinsJobFilter')) {
            views = project.jenkins.views.findAll { it.name ==~ project.jenkinsJobFilter } as List
        } else {
            views = project.jenkins.views as List
        }

        return views
    }

    def void initialize() {
        getJobs().each { job -> getServerDefinitions(job) }
    }

    def void eachServer(JenkinsConfigurable item, Closure closure) {
        getServerDefinitions(item).each { server ->
            def service = server.secure ? serviceFactory.getService(server.url, server.username, server.password) : serviceFactory.getService(server.url)
            closure.call(server, service)
        }
    }
}
