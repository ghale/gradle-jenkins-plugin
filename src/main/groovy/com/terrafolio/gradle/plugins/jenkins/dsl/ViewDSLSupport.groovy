package com.terrafolio.gradle.plugins.jenkins.dsl

import com.terrafolio.gradle.plugins.jenkins.jobdsl.DSLViewFactory
import com.terrafolio.gradle.plugins.jenkins.jobdsl.DefaultDSLViewFactory
import javaposse.jobdsl.dsl.*

/**
 * Created by ghale on 6/2/14.
 */
trait ViewDSLSupport implements DSLSupport {
    final DSLViewFactory viewFactory = new DefaultDSLViewFactory()

    @Override
    String evaluateDSL(File dslFile) {
        ScriptRequest request = new ScriptRequest(dslFile.text)
        DslScriptLoader scriptLoader = new DslScriptLoader(jobManagement)
        GeneratedItems generatedItems = scriptLoader.runScripts([request])

        if (generatedItems.views.size() != 1) {
            throw new JenkinsConfigurationException("The DSL script ${dslFile.path} did not generate exactly one view (${generatedItems.views.size()})!  Use the general dsl form to generate multiple jobs from dsl.")
        } else {
            GeneratedView generatedView = generatedItems.getViews().iterator().next()
            return generatedView.name
        }
    }

    @Override
    String evaluateDSL(String name, String type, Closure closure) {
        View view = viewFactory.createView(jobManagement, type, name)
        view.with(closure)
        jobManagement.createOrUpdateView(name, view.xml, true)
        return name
    }

    @Override
    void addConfig(String name, String config) {
        jobManagement.createOrUpdateView(name, config, true)
    }

    @Override
    String getConfig(String name) {
        jobManagement.getConfig(name)
    }

    @Override
    void setParameter(String name, Object value) {
        jobManagement.getParameters().put(name, value)
    }
}
