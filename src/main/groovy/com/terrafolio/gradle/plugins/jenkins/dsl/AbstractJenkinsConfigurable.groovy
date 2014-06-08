package com.terrafolio.gradle.plugins.jenkins.dsl

import org.gradle.util.ConfigureUtil

/**
 * Created by ghale on 4/11/14.
 */
abstract class AbstractJenkinsConfigurable implements JenkinsConfigurable {
    protected def serverSpecificConfiguration = [:]
    def serverDefinitions = []
    def JenkinsOverrides serviceOverrides
    def String name

    @Override
    def void serviceOverrides(Closure closure) {
        if (this.serviceOverrides == null) {
            this.serviceOverrides = new JenkinsOverrides()
        }
        ConfigureUtil.configure(closure, serviceOverrides)
    }

    @Override
    def void serviceOverrides(JenkinsOverrides overrides) {
        this.serviceOverrides = overrides
    }

    @Override
    def JenkinsOverrides getServiceOverrides() {
        if (! serviceOverrides) {
            serviceOverrides getDefaultOverrides()
        }

        return serviceOverrides
    }

    @Override
    def void server(JenkinsServerDefinition server) {
        if (! serverDefinitions.contains(server)) {
            serverDefinitions += server
        }
    }

    @Override
    def void server(JenkinsServerDefinition server, Closure closure) {
        this.server(server)
        if (! serverSpecificConfiguration.containsKey(server)) {
            serverSpecificConfiguration[server] = []
        }
        serverSpecificConfiguration[server] += closure
    }
}
