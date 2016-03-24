package com.terrafolio.gradle.plugins.jenkins.dsl

import com.terrafolio.gradle.plugins.jenkins.jobdsl.DSLJobType
import javaposse.jobdsl.dsl.JobManagement

/**
 * Created by ghale on 4/8/14.
 */
class JenkinsJobTemplate implements JobDSLSupport, XMLSupport, DSLConfigurable, XMLConfigurable {
    def String name
    def String type

    final JobManagement jobManagement

    JenkinsJobTemplate(String name, JobManagement jobManagement) {
        this.name = name
        this.jobManagement = jobManagement
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

    @Override
    void dsl(File dslFile) {
        setParameter("GRADLE_JOB_NAME", name)
        def jobName = evaluateDSL(dslFile)
        setXml(getConfig(jobName))
    }

    @Override
    void dsl(Closure closure) {
        setParameter("GRADLE_JOB_NAME", name)
        def jobName = evaluateDSL(name, type, closure)
        setXml(getConfig(jobName))
    }

    @Override
    void setXml(String xml) {
        XMLSupport.super.setXml(xml)
        addConfig(name, xml)
    }
}
