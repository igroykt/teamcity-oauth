package jetbrains.buildServer.auth.oauth

import jetbrains.buildServer.serverSide.ServerSettings
import jetbrains.buildServer.serverSide.auth.AuthModule
import jetbrains.buildServer.serverSide.auth.LoginConfiguration
import spock.lang.Specification

class AuthenticationSchemePropertiesTest extends Specification {

    Map<String, String> configuration = [:];
    AuthenticationSchemeProperties schemeProperties;

    def setup() {
        AuthModule authModule = Mock() {
            getProperties() >> configuration;
        }
        LoginConfiguration loginConfiguration = Mock() {
            getConfiguredAuthModules(_) >> [authModule]
        }
        ServerSettings serverSettings = Mock() {
            getRootUrl() >> 'rooturl'
        }
        schemeProperties = new AuthenticationSchemeProperties(serverSettings, loginConfiguration)
    }

    def "configuration read root url"() {
        expect:
        schemeProperties.getRootUrl() == 'rooturl'
    }

    def "configuration read client settings"() {
        given:
        configuration[ConfigKey.clientId.toString()] = 'clientID'
        configuration[ConfigKey.clientSecret.toString()] = 'clientSsss'
        configuration[ConfigKey.scope.toString()] = 'scope'
        expect:
        schemeProperties.getClientId() == 'clientID'
        schemeProperties.getClientSecret() == 'clientSsss'
        schemeProperties.getScope() == 'scope'
    }

    def "configuration is valid for github preset"() {
        given:
        configuration[ConfigKey.preset.toString()] = 'github'
        expect:
        schemeProperties.getAuthorizeEndpoint() == 'https://github.com/login/oauth/authorize'
        schemeProperties.getTokenEndpoint() == 'https://github.com/login/oauth/access_token'
        schemeProperties.getUserEndpoint() == 'https://api.github.com/user'
    }

    def "configuration is valid for bitbucket preset"() {
        given:
        configuration[ConfigKey.preset.toString()] = 'bitbucket'
        expect:
        schemeProperties.getAuthorizeEndpoint() == 'https://bitbucket.org/site/oauth2/authorize'
        schemeProperties.getTokenEndpoint() == 'https://bitbucket.org/site/oauth2/access_token'
        schemeProperties.getUserEndpoint() == 'https://api.bitbucket.org/2.0/user'
    }


    def "configuration is valid for custom preset"() {
        given:
        configuration[ConfigKey.preset.toString()] = 'custom'
        configuration[ConfigKey.authorizeEndpoint.toString()] = 'http://localhost:8080/oauth/authorize'
        configuration[ConfigKey.tokenEndpoint.toString()] = 'http://localhost:8080/oauth/token'
        configuration[ConfigKey.userEndpoint.toString()] = 'http://localhost:8080/oauth/user'
        expect:
        schemeProperties.getAuthorizeEndpoint() == 'http://localhost:8080/oauth/authorize'
        schemeProperties.getTokenEndpoint() == 'http://localhost:8080/oauth/token'
        schemeProperties.getUserEndpoint() == 'http://localhost:8080/oauth/user'
    }

    def "configuration should favor preset"() {
        given:
        configuration[ConfigKey.preset.toString()] = 'github'
        configuration[ConfigKey.authorizeEndpoint.toString()] = 'http://localhost:8080/oauth/authorize'
        expect:
        schemeProperties.getAuthorizeEndpoint() == 'https://github.com/login/oauth/authorize'
    }
}
