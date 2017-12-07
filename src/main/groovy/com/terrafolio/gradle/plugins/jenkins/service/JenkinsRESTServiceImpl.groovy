package com.terrafolio.gradle.plugins.jenkins.service

import com.google.common.annotations.VisibleForTesting
import groovy.xml.StreamingMarkupBuilder
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.XML

class JenkinsRESTServiceImpl implements JenkinsService {
    def RESTClient client
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

    @VisibleForTesting
    public JenkinsRESTServiceImpl() {
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

    def restServiceGET(path, query) {
        def client = getRestClient()

        def response = client.get(path: path, query: query)
        if (response.success) {
            return response.getData()
        } else {
            throw new Exception('REST Service call failed with response code: ' + response.status)
        }
    }

    def restServicePOST(path, query, payload) {
       def client = getRestClient()
       Crumb crumbJson = getCrumb(client)

       def response = client.post(path: path, headers: crumbJson.toMap(), query: query, requestContentType: XML, body: payload)

        if (response) {
            if (response.success) {
                return response.getData()
            } else {
                throw new Exception('REST Service call failed with response code: ' + response.status)
            }
        } else return null;
    }

    Crumb getCrumb(RESTClient client) {
        client.auth.basic(username, password)
        HttpResponseDecorator httpResponse = client.get(path: "${url}crumbIssuer/api/json");
        Crumb crumbJson = new Crumb()
        if (httpResponse.isSuccess()) {
            crumbJson.crumbRequestField = httpResponse.data.get("crumbRequestField")
            crumbJson.crumb = httpResponse.data.get("crumb");
            println "SB:${crumbJson.dump()}"
            println "SB:${crumbJson.toMap().dump()}"
        }
        crumbJson
    }

    @Override
    public String getConfiguration(String jobName, Map overrides)
            throws JenkinsServiceException {
        def responseXml
        try {
            responseXml = restServiceGET(overrides.uri, overrides.params)
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
    public void updateConfiguration(String jobName, String configXml,
                                    Map overrides) throws JenkinsServiceException {
        try {
            restServicePOST(overrides.uri, overrides.params, configXml)
        } catch (Exception e) {
            throw new JenkinsServiceException("Jenkins Service Call failed", e)
        }
    }

    @Override
    public void deleteConfiguration(String jobName, Map overrides)
            throws JenkinsServiceException {
        try {
            restServicePOST(overrides.uri, overrides.params, "")
        } catch (Exception e) {
            throw new JenkinsServiceException("Jenkins Service Call failed", e)
        }

    }

    @Override
    public void createConfiguration(String jobName, String configXml, Map overrides)
            throws JenkinsServiceException {
        try {
            restServicePOST(overrides.uri, overrides.params, configXml)
        } catch (Exception e) {
            throw new JenkinsServiceException("Jenkins Service Call failed", e)
        }

    }

}
