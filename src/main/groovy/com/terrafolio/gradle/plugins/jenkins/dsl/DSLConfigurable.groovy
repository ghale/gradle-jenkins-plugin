package com.terrafolio.gradle.plugins.jenkins.dsl

/**
 * Created by ghale on 6/2/14.
 */
public interface DSLConfigurable {

    def void dsl(File dslFile)

    def void dsl(Closure closure)

    def DSLSupport getDSLSupport()

    def void setDSLSupport(DSLSupport support)
}