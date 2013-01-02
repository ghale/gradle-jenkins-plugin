package org.gradle.plugins.jenkins

import groovy.xml.StreamingMarkupBuilder
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseException
import static groovyx.net.http.ContentType.*

class JenkinsRESTServiceImpl implements JenkinsService {
	private RESTClient client
	def url
	def username
	def password
	
	public JenkinsRESTServiceImpl(String url, String username, String password) {
		this.url = url
		this.username = username
		this.password = password
	}
	
	public JenkinsRESTServiceImpl(String url) {
		this.url = url
	}
	
	def getRestClient() {
		if (client == null) {
			client = new RESTClient(url)
			if (username != null) {
				client.client.addRequestInterceptor(new PreemptiveAuthInterceptor(username, password))
			}
		}
		
		return client
	}
	
	def restServiceGET(path) {
		def client = getRestClient()
		
		def response = client.get(path: path) 
		if (response.success) {
			return response.getData()
		} else {
			throw new Exception('REST Service call failed with response code: ' + response.status)
		}
	}
	
	def restServicePOST(path, query, payload) {
		def client = getRestClient()
		def lastException

		def response = client.post(path: path, query: query, requestContentType: XML, body: payload)
		if (response.success) {
			return response.getData()
		} else {
			throw new Exception('REST Service call failed with response code: ' + response.status)
		}
	}
	
	@Override
	public String getJobConfiguration(String jobName) throws JenkinsServiceException {
		def responseXml
		try {
			responseXml = restServiceGET("/job/${jobName}/config.xml")
		} catch (HttpResponseException hre) {
			if (hre.response.status == 404) {
				responseXml = null
			} else {
				throw new JenkinsServiceException("Jenkins Service Call failed", hre)
			}
		} catch (Exception e) {
			throw new JenkinsServiceException("Jenkins Service Call failed", e)
		}
		
		if (responseXml != null) {
			def sbuilder = new StreamingMarkupBuilder()
			return sbuilder.bind { mkp.yield responseXml }.toString()
		} else {
			return null
		}
		
	}

	@Override
	public void updateJobConfiguration(String jobName, String configXml) throws JenkinsServiceException {
		def response
		try {
			response = restServicePOST("/job/${jobName}/config.xml", [:], configXml)
		} catch (Exception e) {
			throw new JenkinsServiceException("Jenkins Service Call failed", e)
		}
	}

	@Override
	public void deleteJob(String jobName) throws JenkinsServiceException {
		def response
		try {
			response = restServicePOST("/job/${jobName}/doDelete", [:], "")
		} catch (Exception e) {
			throw new JenkinsServiceException("Jenkins Service Call failed", e)
		}
	}

	@Override
	public void createJob(String jobName, String configXml) throws JenkinsServiceException {
		def response
		try {
			response = restServicePOST("/createItem", [ name : jobName ], configXml)
		} catch (Exception e) {
			throw new JenkinsServiceException("Jenkins Service Call failed", e)
		}
		
		if (! response.success) {
			throw new JenkinsServiceException("Jenkins Service Call failed with status: ${response.status}")
		}
	}

}
