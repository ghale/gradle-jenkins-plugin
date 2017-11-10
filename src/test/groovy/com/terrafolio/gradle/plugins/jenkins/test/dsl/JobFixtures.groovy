package com.terrafolio.gradle.plugins.jenkins.test.dsl

/**
 * Created on 6/27/14.
 */
class JobFixtures {
    protected static final String FREEFORM_DSL_JOB_XML = """
            <project>
                <actions></actions>
                <description></description>
                <keepDependencies>false</keepDependencies>
                <properties></properties>
                <scm class='hudson.scm.NullSCM'></scm>
                <canRoam>true</canRoam>
                <disabled>false</disabled>
                <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                <triggers></triggers>
                <concurrentBuild>false</concurrentBuild>
                <builders></builders>
                <publishers></publishers>
                <buildWrappers></buildWrappers>
            </project>
    """
    protected static final String MAVEN_DSL_JOB_XML = """
            <maven2-moduleset>
              <actions/>
              <description></description>
              <keepDependencies>false</keepDependencies>
              <properties/>
              <scm class="hudson.scm.NullSCM"/>
              <canRoam>true</canRoam>
              <disabled>false</disabled>
              <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
              <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
              <triggers/>
              <concurrentBuild>false</concurrentBuild>
              <aggregatorStyleBuild>true</aggregatorStyleBuild>
              <incrementalBuild>false</incrementalBuild>
              <ignoreUpstremChanges>true</ignoreUpstremChanges>
              <archivingDisabled>false</archivingDisabled>
              <resolveDependencies>false</resolveDependencies>
              <processPlugins>false</processPlugins>
              <mavenValidationLevel>-1</mavenValidationLevel>
              <runHeadless>false</runHeadless>
              <publishers/>
              <buildWrappers/>
            </maven2-moduleset>
    """
    protected static final String MULTIJOB_DSL_JOB_XML = """
            <com.tikal.jenkins.plugins.multijob.MultiJobProject>
              <actions/>
              <description/>
              <keepDependencies>false</keepDependencies>
              <properties/>
              <scm class="hudson.scm.NullSCM"/>
              <canRoam>true</canRoam>
              <disabled>false</disabled>
              <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
              <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
              <triggers/>
              <concurrentBuild>false</concurrentBuild>
              <builders/>
              <publishers/>
              <buildWrappers/>
            </com.tikal.jenkins.plugins.multijob.MultiJobProject>
    """
    protected static final String BUILDFLOW_DSL_JOB_XML = """
            <com.cloudbees.plugins.flow.BuildFlow>
              <actions/>
              <description></description>
              <keepDependencies>false</keepDependencies>
              <properties/>
              <scm class="hudson.scm.NullSCM"/>
              <canRoam>true</canRoam>
              <disabled>false</disabled>
              <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
              <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
              <triggers/>
              <concurrentBuild>false</concurrentBuild>
              <builders/>
              <publishers/>
              <buildWrappers/>
              <icon/>
              <dsl></dsl>
            </com.cloudbees.plugins.flow.BuildFlow>
    """
}
