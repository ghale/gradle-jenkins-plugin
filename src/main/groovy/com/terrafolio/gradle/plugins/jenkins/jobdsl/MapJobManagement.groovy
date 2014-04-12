package com.terrafolio.gradle.plugins.jenkins.jobdsl

import com.google.common.collect.Maps
import javaposse.jobdsl.dsl.AbstractJobManagement
import javaposse.jobdsl.dsl.ConfigurationMissingException
import javaposse.jobdsl.dsl.JobConfigurationNotFoundException
import javaposse.jobdsl.dsl.NameNotProvidedException

/**
 * Created by ghale on 4/6/14.
 */
class MapJobManagement extends AbstractJobManagement {
    Map map
    Map parameters

    MapJobManagement(Map map, PrintStream out = System.out) {
        this.map = map
        this.out = out
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
    void createOrUpdateView(String viewName, String config, boolean ignoreExisting) throws NameNotProvidedException, ConfigurationMissingException {
        validateUpdateArgs(viewName, config)
        map.put(viewName, config)
    }
}
