package com.terrafolio.gradle.plugins.jenkins.jobdsl

import com.google.common.collect.Maps
import javaposse.jobdsl.dsl.AbstractJobManagement
import javaposse.jobdsl.dsl.ConfigFile
import javaposse.jobdsl.dsl.ConfigFileType
import javaposse.jobdsl.dsl.ConfigurationMissingException
import javaposse.jobdsl.dsl.Item
import javaposse.jobdsl.dsl.JobConfigurationNotFoundException
import javaposse.jobdsl.dsl.NameNotProvidedException
import hudson.util.VersionNumber
import javaposse.jobdsl.dsl.UserContent
import javaposse.jobdsl.dsl.helpers.ExtensibleContext

/**
 * Created by ghale on 4/6/14.
 */
class MapJobManagement extends AbstractJobManagement {
    Map map
    Map parameters

    MapJobManagement(Map map, PrintStream out = System.out) {
        super(out)
        this.map = map
        parameters = new Maps().newHashMap()
    }

    @Override
    String getConfig(String jobName) throws JobConfigurationNotFoundException {
        return map.get(jobName)
    }

    @Override
    boolean createOrUpdateConfig(String jobName, String config, boolean ignoreExisting) throws NameNotProvidedException, ConfigurationMissingException {
        validateUpdateArgs(jobName, config)
        map.put(jobName, config)
        return true
    }

    @Override
    String createOrUpdateConfigFile(ConfigFile configFile, boolean ignoreExisting){
        return null
    }

    @Override
    void createOrUpdateView(String viewName, String config, boolean ignoreExisting) throws NameNotProvidedException, ConfigurationMissingException {
        validateUpdateArgs(viewName, config)
        map.put(viewName, config)
    }

    @Override
    void logPluginDeprecationWarning(String pluginShortName, String minimumVersion) { }

    @Override
    boolean isMinimumPluginVersionInstalled(String pluginShortName, String version) {
        VersionNumber actualVersion = getPluginVersion(pluginShortName)
        if (actualVersion == null) {
            return false
        }
        VersionNumber minimumVersion = new VersionNumber(version)
        return !actualVersion.isOlderThan(minimumVersion)
    }

    @Override
    void requireMinimumPluginVersion(String pluginShortName, String version){
    }

    @Override
    void requireMinimumPluginVersion(String pluginShortName, String version, boolean failIfMissing) {
    }

    @Override
    VersionNumber getPluginVersion(String pluginShortName){
        return null
    }

    @Override
    Integer getVSphereCloudHash(String name){
        return null
    }

    @Override
    String getConfigFileId(ConfigFileType type, String name){
        return null
    }

    @Override
    void renameJobMatching(String previousNames, String destination) throws IOException {
    }

    @Override
    boolean createOrUpdateConfig(Item item, boolean ignoreExisting) throws NameNotProvidedException {
        String jobName = item.name
        String config = item.xml
        return createOrUpdateConfig(jobName, config, ignoreExisting)
    }

    @Override
    void createOrUpdateUserContent(UserContent userContent, boolean ignoreExisting) { }

    @Override
    void queueJob(String jobName) throws NameNotProvidedException { }

    @Override
    InputStream streamFileInWorkspace(String filePath) throws IOException {
        return null
    }

    @Override
    String readFileInWorkspace(String filePath) throws IOException {
        return null
    }

    @Override
    String readFileInWorkspace(String jobName, String filePath) throws IOException {
        return null
    }

    @Override
    void requirePlugin(String pluginShortName) { }

    @Override
    void requirePlugin(String pluginShortName, boolean failIfMissing) { }

    @Override
    void requireMinimumCoreVersion(String version) { }

    @Override
    VersionNumber getJenkinsVersion() {
        return null
    }

    @Override
    Set<String> getPermissions(String authorizationMatrixPropertyClassName) {
        return null
    }

    @Override
    Node callExtension(String name, Item item, Class<? extends ExtensibleContext> contextType, Object... args) {
        return null
    }
}
