package com.terrafolio.gradle.plugins.jenkins.dsl

/**
 * Created by ghale on 6/2/14.
 */
public interface XMLConfigurable {

    def XMLSupport getXMLSupport()

    def void setXMLSupport(XMLSupport support)

    def String override(Closure closure)

    def String getXml()

    def void setXml(String xml)

    def void xml(String xml)

    def void xml(File xmlFile)

    def void xml(Closure closure)
}