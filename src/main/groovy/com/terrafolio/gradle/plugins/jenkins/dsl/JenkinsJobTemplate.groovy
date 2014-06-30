package com.terrafolio.gradle.plugins.jenkins.dsl

import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.JobType

/**
 * Created by ghale on 4/8/14.
 */
class JenkinsJobTemplate implements DSLConfigurable, XMLConfigurable {
    def String name
    def String type

    protected DSLSupport dslSupport
    protected XMLSupport xmlSupport

    JenkinsJobTemplate(String name, JobManagement jm) {
        this.name = name
        dslSupport = new JobDSLSupport(jm)
        xmlSupport = new DefaultXMLSupport()
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

    def void setTemplateXml(String xml) {
        xmlSupport.setXml(xml)
        dslSupport.addConfig(name, xml)
    }

    @Override
    void dsl(File dslFile) {
        dslSupport.setParameter("GRADLE_JOB_NAME", name)
        def jobName = dslSupport.evaluateDSL(dslFile)
        setTemplateXml(dslSupport.getConfig(jobName))
    }

    @Override
    void dsl(Closure closure) {
        dslSupport.setParameter("GRADLE_JOB_NAME", name)
        def jobName = dslSupport.evaluateDSL(name, type, closure)
        setTemplateXml(dslSupport.getConfig(jobName))
    }

    @Override
    DSLSupport getDSLSupport() {
        return dslSupport
    }

    @Override
    void setDSLSupport(DSLSupport support) {
        this.dslSupport = support
    }

    @Override
    String override(Closure closure) {
        return xmlSupport.override(closure)
    }

    @Override
    String getXml() {
        return xmlSupport.getXml()
    }

    @Override
    void setXml(String xml) {
        setTemplateXml(xml)
    }

    @Override
    void xml(String xml) {
        setTemplateXml(xml)
    }

    @Override
    void xml(File xmlFile) {
        xmlSupport.xml(xmlFile)
        setTemplateXml(xmlSupport.xml)
    }

    @Override
    void xml(Closure closure) {
        xmlSupport.xml(closure)
        setTemplateXml(xmlSupport.xml)
    }

    @Override
    XMLSupport getXMLSupport() {
        return xmlSupport
    }

    @Override
    void setXMLSupport(XMLSupport support) {
        this.xmlSupport = support
    }
}
