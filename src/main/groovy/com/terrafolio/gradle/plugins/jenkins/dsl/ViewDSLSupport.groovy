package com.terrafolio.gradle.plugins.jenkins.dsl

import javaposse.jobdsl.dsl.*
import javaposse.jobdsl.dsl.views.BuildPipelineView
import javaposse.jobdsl.dsl.views.ListView

/**
 * Created by ghale on 6/2/14.
 */
class ViewDSLSupport implements DSLSupport {
    def JobManagement jobManagement

    private static final Map<ViewType, Class<? extends View>> VIEW_TYPE_MAPPING = [
            (null)                      : ListView.class,
            (ViewType.ListView)         : ListView.class,
            (ViewType.BuildPipelineView): BuildPipelineView.class,
    ]

    ViewDSLSupport(JobManagement jobManagement) {
        this.jobManagement = jobManagement
    }

    @Override
    String evaluateDSL(File dslFile) {
        ScriptRequest request = new ScriptRequest(dslFile.name, null, dslFile.parentFile.toURI().toURL(), false)
        GeneratedItems generatedItems = DslScriptLoader.runDslEngine(request, jobManagement)

        if (generatedItems.views.size() != 1) {
            throw new JenkinsConfigurationException("The DSL script ${dslFile.path} did not generate exactly one view (${generatedItems.views.size()})!  Use the general dsl form to generate multiple jobs from dsl.")
        } else {
            GeneratedView generatedView = generatedItems.getViews().iterator().next()
            return generatedView.name
        }
    }

    @Override
    String evaluateDSL(String name, String type, Closure closure) {
        Class<? extends View> viewClass = VIEW_TYPE_MAPPING[type as ViewType]
        View view = viewClass.newInstance()
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
