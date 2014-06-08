package com.terrafolio.gradle.plugins.jenkins.dsl

import groovy.xml.StreamingMarkupBuilder

/**
 * Created by ghale on 6/2/14.
 */
class DefaultXMLSupport implements XMLSupport {
    def String xml

    @Override
    def String override(Closure closure) {
        def newXml = new XmlSlurper().parseText(xml)
        closure.call(newXml)
        def sbuilder = new StreamingMarkupBuilder()
        return sbuilder.bind { mkp.yield newXml }.toString()
    }

    @Override
    void setXml(String xml) {
        this.xml = xml
    }

    @Override
    def void xml(String xml) {
        setXml(xml)
    }

    @Override
    def void xml(File xmlFile) {
        setXml(xmlFile.getText())
    }

    @Override
    def void xml(Closure closure) {
        def sbuilder = new StreamingMarkupBuilder()
        setXml(sbuilder.bind(closure).toString())
    }
}
