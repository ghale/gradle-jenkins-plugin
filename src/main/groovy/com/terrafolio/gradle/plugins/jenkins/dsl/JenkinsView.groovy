package com.terrafolio.gradle.plugins.jenkins.dsl

import javaposse.jobdsl.dsl.JobManagement
import org.gradle.util.ConfigureUtil

/**
 * Created by ghale on 4/11/14.
 */
class JenkinsView extends AbstractJenkinsConfigurable implements XMLSupport, ViewDSLSupport, DSLConfigurable, XMLConfigurable {
    def String type
    final JobManagement jobManagement

    def defaultOverrides = {
        create([ uri: "createView", params: [ name: name ] ])
           get([ uri: "view/${name}/config.xml" ])
        update([ uri: "view/${name}/config.xml" ])
        delete([ uri: "view/${name}/doDelete" ])
    }

    JenkinsView(String name, JobManagement jobManagement) {
        this.name = name
        this.jobManagement = jobManagement
    }

    def type(String type) {
        this.type = type
    }

    @Override
    def Closure getDefaultOverrides() {
        return this.defaultOverrides
    }

    @Override
    String getConfigurableName() {
        return name
    }

    @Override
    String getServerSpecificXml(JenkinsServerDefinition server) {
        if (serverSpecificConfiguration.containsKey(server)) {
            def JenkinsView serverSpecificView = new JenkinsView(name, jobManagement)

            serverSpecificView.xml = xml

            serverSpecificConfiguration[server].each { closure ->
                ConfigureUtil.configure(closure, serverSpecificView)
            }

            return serverSpecificView.xml
        } else {
            return xml
        }
    }

    @Override
    def void dsl(File dslFile) {
        def viewName = evaluateDSL(dslFile)
        setXml(getConfig(viewName))
    }

    @Override
    def void dsl(Closure closure) {
        def viewName = evaluateDSL(name, type, closure)
        setXml(getConfig(viewName))
    }

    @Override
    void setXml(String xml) {
        XMLSupport.super.setXml(xml)
        addConfig(name, xml)
    }
}
