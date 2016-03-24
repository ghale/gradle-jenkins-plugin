package com.terrafolio.gradle.plugins.jenkins.dsl

import com.terrafolio.gradle.plugins.jenkins.jobdsl.DSLJobType
import groovy.xml.StreamingMarkupBuilder
import javaposse.jobdsl.dsl.JobManagement
import org.gradle.util.ConfigureUtil

class JenkinsJob extends AbstractJenkinsConfigurable implements JobDSLSupport, DSLConfigurable, XMLConfigurable {
    def definition
    def type
    final JobManagement jobManagement;

    def defaultOverrides = {
        create([ uri: "createItem", params: [ name: definition.name ] ])
           get([ uri: "job/${definition.name}/config.xml" ])
        update([ uri: "job/${definition.name}/config.xml" ])
        delete([ uri: "job/${definition.name}/doDelete" ])
    }

    JenkinsJob(String name, JobManagement jobManagement) {
        this.name = name
        this.jobManagement = jobManagement
    }

    @Override
    def Closure getDefaultOverrides() {
        return this.defaultOverrides
    }

    @Override
    String getConfigurableName() {
        return this.definition.name
    }

    def void type(String type) {
        setType(type)
    }

    def void setType(String type) {
        if (DSLJobType.find(type) == null) {
            throw new JenkinsConfigurationException("${type} is not a valid jenkins-job-dsl type!")
        }
        this.type = type
    }

    def definition(JenkinsJobDefinition definition) {
        setDefinition(definition)
    }

    def setDefinition(JenkinsJobDefinition definition) {
        this.definition = definition
        if (definition.xml != null) {
            addConfig(name, definition.xml)
        }
    }

    def definition(Closure closure) {
        if (this.definition == null) {
            this.definition = new JenkinsJobDefinition(name)
        }
        ConfigureUtil.configure(closure, definition)
        if (definition.xml != null) {
            addConfig(name, definition.xml)
        }
    }

    @Override
    def void dsl(File dslFile) {
        setParameter("GRADLE_JOB_NAME", name)

        def jobName = evaluateDSL(dslFile)
        def definition = new JenkinsJobDefinition(jobName == null ? name : jobName)
        definition.xml getConfig(jobName)
        setDefinition(definition)
    }

    @Override
    def void dsl(Closure closure) {
        setParameter("GRADLE_JOB_NAME", name)

        def jobName = evaluateDSL(name, type, closure)
        def definition = new JenkinsJobDefinition(jobName)
        definition.xml getConfig(jobName)
        setDefinition(definition)
    }

    @Override
    def String getServerSpecificXml(JenkinsServerDefinition server) {
        if (serverSpecificConfiguration.containsKey(server)) {
            def newDefinition = new JenkinsJobDefinition(definition.name, definition.xml)

            serverSpecificConfiguration[server].each { closure ->
                ConfigureUtil.configure(closure, newDefinition)
            }

            return newDefinition.xml
        } else {
            return definition.xml
        }
    }

    @Override
    String override(Closure closure) {
        if (this.definition == null) {
            this.definition = new JenkinsJobDefinition()
        }
        return this.definition.override(closure)
    }

    @Override
    String getXml() {
        return definition.getXml()
    }

    @Override
    void setXml(String xml) {
        def defn = this.definition
        if (defn == null) {
            defn = new JenkinsJobDefinition()
        }
        defn.xml = xml
        setDefinition(defn)
    }

    @Override
    void xml(String xml) {
        setXml(xml)
    }

    @Override
    void xml(File xmlFile) {
        setXml(xmlFile.getText())
    }

    @Override
    void xml(Closure closure) {
        setXml(new StreamingMarkupBuilder().bind(closure).toString())
    }
}
