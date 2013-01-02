package com.terrafolio.gradle.plugins.jenkins;

public interface JenkinsService {
	public String getJobConfiguration(String jobName) throws JenkinsServiceException;
	
	public void createJob(String jobName, String configXml) throws JenkinsServiceException;
	
	public void updateJobConfiguration(String jobName, String configXml) throws JenkinsServiceException;
	
	public void deleteJob(String jobName) throws JenkinsServiceException;
}
