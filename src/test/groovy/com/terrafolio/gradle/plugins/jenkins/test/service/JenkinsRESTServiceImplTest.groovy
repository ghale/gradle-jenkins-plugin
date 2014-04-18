package com.terrafolio.gradle.plugins.jenkins.test.service

import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsServiceException
import com.terrafolio.gradle.plugins.jenkins.service.PreemptiveAuthInterceptor
import groovy.mock.interceptor.MockFor
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpResponse
import org.apache.http.HttpVersion
import org.apache.http.message.BasicHttpResponse
import org.apache.http.message.BasicStatusLine
import org.junit.Before
import org.junit.Test

class JenkinsRESTServiceImplTest {
    private MockFor mockRESTClient
    def url = "http://testurl.availity.com"
    def username = "testuser"
    def password = "testpassword"

    @Before
    def void setupProject() {
        mockRESTClient = new MockFor(RESTClient.class)
    }

    @Test
    def void getConfiguration_returnsXmlString() {
        mockRESTClient.demand.with {
            get() { Map<String, ?> map ->
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
                return response
            }
        }

        mockRESTClient.ignore('getClient')

        mockRESTClient.use {
            def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
            def xml = service.getConfiguration("compile", [:])
            assert xml == "<test1><test2>srv value</test2></test1>"
        }
    }

    @Test
    def void getConfiguration_returnsNullOnNotFound() {
        mockRESTClient.demand.with {
            get() { Map<String, ?> map ->
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 404, "Not Found"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
                throw new HttpResponseException(response)
            }
        }

        mockRESTClient.ignore('getClient')

        mockRESTClient.use {
            def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
            def xml = service.getConfiguration("compile", [:])
            assert xml == null
        }
    }

    @Test(expected = JenkinsServiceException.class)
    def void getConfiguration_throwsExceptionOnFailure() {
        mockRESTClient.demand.with {
            get() { Map<String, ?> map ->
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Error"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
                return response
            }
        }

        mockRESTClient.ignore('getClient')

        mockRESTClient.use {
            def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
            def xml = service.getConfiguration("compile", [:])
        }
    }

    @Test
    def void createConfiguration_postsConfigXml() {
        def postMap
        mockRESTClient.demand.with {
            post() { Map<String, ?> map ->
                postMap = map
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
                return response
            }
        }

        mockRESTClient.ignore('getClient')

        mockRESTClient.use {
            def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
            service.createConfiguration("compile", "<test1><test2>srv value</test2></test1>", [uri: "/createItem", params: [name: "compile"]])
            assert postMap.body == "<test1><test2>srv value</test2></test1>"
            assert postMap.query.name == "compile"
            assert postMap.path == "/createItem"
        }
    }

    @Test(expected = JenkinsServiceException.class)
    def void createConfiguration_throwsExceptionOnFailure() {
        mockRESTClient.demand.with {
            post() { Map<String, ?> map ->
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Error"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
                return response
            }
        }

        mockRESTClient.ignore('getClient')

        mockRESTClient.use {
            def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
            service.createConfiguration("compile", "<test1><test2>srv value</test2></test1>", [:])
        }
    }

    @Test
    def void updateConfiguration_postsConfigXml() {
        def postMap
        mockRESTClient.demand.with {
            post() { Map<String, ?> map ->
                postMap = map
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
                return response
            }
        }

        mockRESTClient.ignore('getClient')

        mockRESTClient.use {
            def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
            service.updateConfiguration("compile", "<test1><test2>srv value</test2></test1>", [uri: "/job/compile/config.xml"])
            assert postMap.body == "<test1><test2>srv value</test2></test1>"
            assert postMap.path == "/job/compile/config.xml"
        }
    }

    @Test(expected = JenkinsServiceException.class)
    def void updateConfiguration_throwsExceptionOnFailure() {
        mockRESTClient.demand.with {
            post() { Map<String, ?> map ->
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Error"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
                return response
            }
        }

        mockRESTClient.ignore('getClient')

        mockRESTClient.use {
            def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
            service.updateConfiguration("compile", "<test1><test2>srv value</test2></test1>", [:])
        }
    }

    @Test
    def void deleteConfiguration_postsToUrl() {
        def postMap
        mockRESTClient.demand.with {
            post() { Map<String, ?> map ->
                postMap = map
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
                return response
            }
        }

        mockRESTClient.ignore('getClient')

        mockRESTClient.use {
            def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
            service.deleteConfiguration("compile", [uri: "/job/compile/doDelete"])
            assert postMap.path == "/job/compile/doDelete"
        }
    }

    @Test(expected = JenkinsServiceException.class)
    def void deleteConfiguration_throwsExceptionOnFailure() {
        mockRESTClient.demand.with {
            post() { Map<String, ?> map ->
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Error"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
                return response
            }
        }

        mockRESTClient.ignore('getClient')

        mockRESTClient.use {
            def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
            service.deleteConfiguration("compile", [:])
        }
    }

    @Test
    def void getConfiguration_doesNotAddInterceptorForInSecureServer() {
        mockRESTClient.demand.with {
            get() { Map<String, ?> map ->
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
                return response
            }
        }

        mockRESTClient.ignore('getClient')

        mockRESTClient.use {
            def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url)
            def xml = service.getConfiguration("compile", [:])
            assert xml == "<test1><test2>srv value</test2></test1>"
            for (int i = 0; i < service.client.client.getRequestInterceptorCount(); i++) {
                assert !(service.client.client.getRequestInterceptor(i) instanceof PreemptiveAuthInterceptor)
            }
        }
    }

    @Test
    def void createConfiguration_postsToCustomUrl() {
        def postMap
        mockRESTClient.demand.with {
            post() { Map<String, ?> map ->
                postMap = map
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
                return response
            }
        }

        mockRESTClient.ignore('getClient')

        mockRESTClient.use {
            def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
            service.createConfiguration("compile", "<test1><test2>srv value</test2></test1>", [ uri: "/custom/createItem", params: [ name: "mycompile" ] ])
            assert postMap.path == "/custom/createItem"
            assert postMap.query.name == "mycompile"
        }
    }

    @Test
    def void deleteConfiguration_postsToCustomUrl() {
        def postMap
        mockRESTClient.demand.with {
            post() { Map<String, ?> map ->
                postMap = map
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
                return response
            }
        }

        mockRESTClient.ignore('getClient')

        mockRESTClient.use {
            def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
            service.deleteConfiguration("compile", [ uri: "/custom/doDelete", params: [ name: "mycompile" ] ])
            assert postMap.path == "/custom/doDelete"
            assert postMap.query.name == "mycompile"
        }
    }

    @Test
    def void updateConfiguration_postsToCustomUrl() {
        def postMap
        mockRESTClient.demand.with {
            post() { Map<String, ?> map ->
                postMap = map
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
                return response
            }
        }

        mockRESTClient.ignore('getClient')

        mockRESTClient.use {
            def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
            service.updateConfiguration("compile", "<test1><test2>srv value</test2></test1>", [ uri: "/custom/job/compile", params: [ name: "mycompile" ] ])
            assert postMap.path == "/custom/job/compile"
            assert postMap.query.name == "mycompile"
        }
    }

    @Test
    def void getConfiguration_getsFromCustomUrl() {
        def getMap
        mockRESTClient.demand.with {
            get() { Map<String, ?> map ->
                getMap = map
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
                return response
            }
        }

        mockRESTClient.ignore('getClient')

        mockRESTClient.use {
            def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
            service.getConfiguration("compile", [ uri: "/custom/job/compile", params: [ name: "mycompile" ] ])
            assert getMap.path == "/custom/job/compile"
            assert getMap.query.name == "mycompile"
        }
    }
}
