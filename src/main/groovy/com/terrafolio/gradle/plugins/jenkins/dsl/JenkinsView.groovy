package com.terrafolio.gradle.plugins.jenkins.dsl

import javaposse.jobdsl.dsl.JobManagement
import org.gradle.util.ConfigureUtil

/**
 * Created by ghale on 4/11/14.
 */
class JenkinsView extends AbstractJenkinsConfigurable implements DSLConfigurable, XMLConfigurable {
    def String type
    def DSLSupport dslSupport
    def XMLSupport xmlSupport

    def defaultOverrides = {
        create([ uri: "createView", params: [ name: name ] ])
           get([ uri: "view/${name}/config.xml" ])
        update([ uri: "view/${name}/config.xml" ])
        delete([ uri: "view/${name}/doDelete" ])
    }

    JenkinsView(String name) {
        this.name = name
        this.xmlSupport = new DefaultXMLSupport()
    }

    JenkinsView(String name, JobManagement jm) {
        this(name)
        this.dslSupport = new ViewDSLSupport(jm)
    }

    def type(String type) {
        this.type = type
    }

    void setViewXml(String xml) {
        xmlSupport.setXml(xml)
        dslSupport.addConfig(name, xml)
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
            def JenkinsView serverSpecificView = new JenkinsView(name)
            serverSpecificView.setDslSupport(dslSupport)

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
        def viewName = dslSupport.evaluateDSL(dslFile)
        setViewXml(dslSupport.getConfig(viewName))
    }

    @Override
    def void dsl(Closure closure) {
        def viewName = dslSupport.evaluateDSL(name, type, closure)
        setViewXml(dslSupport.getConfig(viewName))
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
    XMLSupport getXMLSupport() {
        return xmlSupport
    }

    @Override
    void setXMLSupport(XMLSupport support) {
        this.xmlSupport = support
    }

    @Override
    String override(Closure closure) {
        return xmlSupport.override(closure)
    }

    @Override
    String getXml() {
        return xmlSupport.xml
    }

    @Override
    void setXml(String xml) {
        setViewXml(xml)
    }

    @Override
    void xml(String xml) {
        setViewXml(xml)
    }

    @Override
    void xml(File xmlFile) {
        xmlSupport.xml(xmlFile)
        setViewXml(xmlSupport.xml)
    }

    @Override
    void xml(Closure closure) {
        xmlSupport.xml(closure)
        setViewXml(xmlSupport.xml)
    }
}
