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
                <comparator class="hudson.util.CaseInsensitiveComparator"/>
              </jobNames>
              <jobFilters/>
              <columns/>
            </hudson.model.ListView>
    """
}
