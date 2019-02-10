package com.terrafolio.gradle.plugins.jenkins.dsl

import com.terrafolio.gradle.plugins.jenkins.jobdsl.DSLJobFactory
import com.terrafolio.gradle.plugins.jenkins.jobdsl.DefaultDSLJobFactory
import javaposse.jobdsl.dsl.*

/**
 * Created by ghale on 6/2/14.
 */
trait JobDSLSupport implements DSLSupport {
    DSLJobFactory jobFactory = new DefaultDSLJobFactory()

    @Override
    def String evaluateDSL(File dslFile) {
        ScriptRequest request = new ScriptRequest(dslFile.text)
        DslScriptLoader scriptLoader = new DslScriptLoader(jobManagement)
        GeneratedItems generatedItems = scriptLoader.runScripts([request])

        if (generatedItems.jobs.size() != 1) {
            throw new JenkinsConfigurationException("The DSL script ${dslFile.path} did not generate exactly one job (${generatedItems.jobs.size()})!  Use the general dsl form to generate multiple jobs from dsl.")
        } else {
            GeneratedJob generatedJob = generatedItems.getJobs().iterator().next()
            return generatedJob.jobName
        }
    }

    @Override
    def String evaluateDSL(String name, String type, Closure closure) {
        def Job job = jobFactory.create(jobManagement, type, name)
        // Load the existing xml as a template if it exists
        if (jobManagement.getConfig(name) && job.templateName == null) {
            job.using(name)
        }
        job.with(closure)
        def jobName = job.name == null ? name : job.name
        def String resultXml = job.xml
        jobManagement.createOrUpdateConfig(jobName, resultXml, true)
        return jobName

    }

    @Override
    void addConfig(String name, String config) {
        jobManagement.createOrUpdateConfig(name, config, true)
    }

    @Override
    String getConfig(String name) {
        return jobManagement.getConfig(name)
    }

    @Override
    void setParameter(String name, Object value) {
        jobManagement.getParameters().put(name, value)
    }
}
