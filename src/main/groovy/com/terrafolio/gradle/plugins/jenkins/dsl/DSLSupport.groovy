package com.terrafolio.gradle.plugins.jenkins.dsl

/**
 * Created by ghale on 6/2/14.
 */
public interface DSLSupport {
    def String evaluateDSL(File dslFile)

    def String evaluateDSL(String name, String type, Closure closure)

    def void addConfig(String name, String config)

    def String getConfig(String name)

    def void setParameter(String name, Object value)
}