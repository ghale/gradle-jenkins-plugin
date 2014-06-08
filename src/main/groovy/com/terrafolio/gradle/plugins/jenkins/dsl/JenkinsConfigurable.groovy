package com.terrafolio.gradle.plugins.jenkins.dsl

/**
 * Created by ghale on 6/2/14.
 */
public interface JenkinsConfigurable {

    def void serviceOverrides(Closure closure)

    def void serviceOverrides(JenkinsOverrides overrides)

    def JenkinsOverrides getServiceOverrides()

    def void server(JenkinsServerDefinition server)

    def void server(JenkinsServerDefinition server, Closure closure)

    def String getServerSpecificXml(JenkinsServerDefinition server)

    def Closure getDefaultOverrides()

    def String getConfigurableName()
}