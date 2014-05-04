package com.terrafolio.gradle.plugins.jenkins.dsl

import javaposse.jobdsl.dsl.*
import org.gradle.util.ConfigureUtil

class JenkinsJob extends JenkinsConfigurable {
    def definition
    def type
    protected JobManagement jm

    def defaultOverrides = {
        create([ uri: "/createItem", params: [ name: definition.name ] ])
           get([ uri: "/job/${definition.name}/config.xml" ])
        update([ uri: "/job/${definition.name}/config.xml" ])
        delete([ uri: "/job/${definition.name}/doDelete" ])
    }

    JenkinsJob(String name, JobManagement jm) {
        this.name = name
        this.jm = jm
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
        if (JobType.find(type) == null) {
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
            jm.createOrUpdateConfig(name, definition.xml, true)
        }
    }

    def definition(Closure closure) {
        if (this.definition == null) {
            this.definition = new JenkinsJobDefinition(name)
        }
        ConfigureUtil.configure(closure, definition)
        if (definition.xml != null) {
            jm.createOrUpdateConfig(name, definition.xml, true)
        }
    }

    def dsl(File dslFile) {
        jm.getParameters().put("GRADLE_JOB_NAME", name)

        ScriptRequest request = new ScriptRequest(dslFile.name, null, dslFile.parentFile.toURI().toURL(), false)
        GeneratedItems generatedItems = DslScriptLoader.runDslEngine(request, jm)

        if (generatedItems.jobs.size() != 1) {
            throw new JenkinsConfigurationException("The DSL script ${dslFile.path} did not generate exactly one job (${generatedItems.jobs.size()})!  Use the general dsl form to generate multiple jobs from dsl.")
        } else {
            GeneratedJob generatedJob = generatedItems.getJobs().iterator().next()
            this.definition = new JenkinsJobDefinition(generatedJob.jobName == null ? name : generatedJob.jobName)
            this.definition.xml jm.getConfig(generatedJob.jobName)
        }
    }

    def dsl(Closure closure) {
        jm.getParameters().put("GRADLE_JOB_NAME", name)

        def Job job = new Job(jm, ['type': type])
        // Load the existing xml as a template if it exists
        if (jm.getConfig(name) && job.templateName == null) {
            job.using(name)
        }
        job.with(closure)
        def String resultXml = job.xml
        jm.createOrUpdateConfig(name, resultXml, true)
        this.definition = new JenkinsJobDefinition(job.name == null ? name : job.name)
        this.definition.xml resultXml
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
}
