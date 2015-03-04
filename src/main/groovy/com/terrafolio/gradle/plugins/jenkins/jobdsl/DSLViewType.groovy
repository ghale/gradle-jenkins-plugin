package com.terrafolio.gradle.plugins.jenkins.jobdsl

import javaposse.jobdsl.dsl.View

enum DSLViewType {
    ListView(javaposse.jobdsl.dsl.views.ListView),
    SectionedView(javaposse.jobdsl.dsl.views.SectionedView),
    NestedView(javaposse.jobdsl.dsl.views.NestedView),
    DeliveryPipelineView(javaposse.jobdsl.dsl.views.DeliveryPipelineView),
    BuildPipelineView(javaposse.jobdsl.dsl.views.BuildPipelineView),
    BuildMonitorView(javaposse.jobdsl.dsl.views.BuildMonitorView)

    final Class<? extends View> viewClass

    DSLViewType(Class<? extends View> viewClass) {
        this.viewClass = viewClass
    }

    static find(String enumName) {
        values().find { it.name().toLowerCase() == enumName.toLowerCase() }
    }
}