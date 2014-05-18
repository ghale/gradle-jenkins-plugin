package com.terrafolio.gradle.plugins.jenkins.tasks

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurable
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurationException
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsServerDefinition
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceFactory
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsServiceFactory
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class AbstractJenkinsTask extends DefaultTask {
    def needsCredentials = true
    def JenkinsServiceFactory serviceFactory = new JenkinsRESTServiceFactory()
    def items = []
    def itemClosures = []
    def servers

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

        return serverDefinitions
    }

    def List<JenkinsConfigurable> getAllItems() {
        def items = items + itemClosures.collect { it.call() }.flatten()

        if (project.hasProperty('jenkinsJobFilter')) {
            return items.findAll { it.name ==~ project.jenkinsJobFilter } as List
        } else {
            return items as List
        }
    }

    def void initialize() {
        //getAllItems().each { item -> getServerDefinitions(item) }
    }

    def void eachServer(JenkinsConfigurable item, Closure closure) {
        def serversToRun = servers
        if (serversToRun == null) {
            serversToRun = getServerDefinitions(item)
        }
        serversToRun.each { server ->
            def service = server.secure ? serviceFactory.getService(server.url, server.username, server.password) : serviceFactory.getService(server.url)
            closure.call(server, service)
        }
    }
}
