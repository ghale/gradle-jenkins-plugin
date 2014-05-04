package com.terrafolio.gradle.plugins.jenkins.dsl

import org.gradle.util.ConfigureUtil

/**
 * Created by ghale on 4/11/14.
 */
abstract class JenkinsConfigurable {

    protected def serverSpecificConfiguration = [:]
    protected def serverDefinitions = []
    def JenkinsOverrides serviceOverrides
    def String name

    def serviceOverrides(Closure closure) {
        if (this.serviceOverrides == null) {
            this.serviceOverrides = new JenkinsOverrides()
        }
        ConfigureUtil.configure(closure, serviceOverrides)
    }

    def serviceOverrides(JenkinsOverrides overrides) {
        this.serviceOverrides = overrides
    }

    def JenkinsOverrides getServiceOverrides() {
        if (! serviceOverrides) {
            serviceOverrides getDefaultOverrides()
        }

        return serviceOverrides
    }

    def server(JenkinsServerDefinition server) {
        if (! serverDefinitions.contains(server)) {
            serverDefinitions += server
        }
    }

    def server(JenkinsServerDefinition server, Closure closure) {
        this.server(server)
        if (! serverSpecificConfiguration.containsKey(server)) {
            serverSpecificConfiguration[server] = []
        }
        serverSpecificConfiguration[server] += closure
    }

    def abstract String getServerSpecificXml(JenkinsServerDefinition server)

    def abstract Closure getDefaultOverrides()

    def abstract String getConfigurableName()
}
