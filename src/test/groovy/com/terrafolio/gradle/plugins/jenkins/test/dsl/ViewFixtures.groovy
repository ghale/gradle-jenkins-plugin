package com.terrafolio.gradle.plugins.jenkins.test.dsl

/**
 * Created on 6/27/14.
 */
class ViewFixtures {
    protected static final String LIST_DSL_VIEW_XML = """
            <hudson.model.ListView>
              <filterExecutors>false</filterExecutors>
              <filterQueue>false</filterQueue>
              <properties class="hudson.model.View\$PropertyList"/>
              <jobNames class="tree-set">
                <comparator class="hudson.util.CaseInsensitiveComparator"></comparator>
              </jobNames>
              <jobFilters/>
              <columns/>
            </hudson.model.ListView>
    """

    protected static final String BUILD_PIPELINE_VIEW_XML = """
            <au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView>
                <filterExecutors>false</filterExecutors>
                <filterQueue>false</filterQueue>
                <properties class='hudson.model.View\$PropertyList'></properties>
                <noOfDisplayedBuilds>1</noOfDisplayedBuilds>
                <buildViewTitle></buildViewTitle>
                <consoleOutputLinkStyle>Lightbox</consoleOutputLinkStyle>
                <cssUrl></cssUrl>
                <triggerOnlyLatestJob>false</triggerOnlyLatestJob>
                <alwaysAllowManualTrigger>false</alwaysAllowManualTrigger>
                <showPipelineParameters>false</showPipelineParameters>
                <showPipelineParametersInHeaders>false</showPipelineParametersInHeaders>
                <refreshFrequency>3</refreshFrequency>
                <showPipelineDefinitionHeader>false</showPipelineDefinitionHeader>
            </au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView>
"""

    protected static final String NESTED_DSL_VIEW_XML = """
            <hudson.plugins.nested__view.NestedView>
                <filterExecutors>false</filterExecutors>
                <filterQueue>false</filterQueue>
                <properties class="hudson.model.View\$PropertyList"></properties>
                <views></views>
            </hudson.plugins.nested__view.NestedView>
    """

    protected static final String SECTIONED_DSL_VIEW_XML = """
            <hudson.plugins.sectioned__view.SectionedView>
                <filterExecutors>false</filterExecutors>
                <filterQueue>false</filterQueue>
                <properties class="hudson.model.View\$PropertyList"/>
                <sections/>
            </hudson.plugins.sectioned__view.SectionedView>
    """
}



