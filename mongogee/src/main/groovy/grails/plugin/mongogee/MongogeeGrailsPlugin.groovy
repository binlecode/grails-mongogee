/* Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.mongogee

import grails.plugin.mongogee.exception.MongogeeException
import grails.plugins.Plugin
import groovy.util.logging.Slf4j

@Slf4j
class MongogeeGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.2.3 > *"
    // make sure mongodb plugin is loaded first
    def loadAfter = ['mongodb']
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Mongogee MongoDB data migration Grails plugin" // Headline display name of the plugin
    def author = "Bin Le"
    def authorEmail = "bin.le.code@gmail.com"
    def description = '''Mongogee is a Grails 3 plugin to manage MongoDB data migrations.'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/mongogee"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    Closure doWithSpring() { {->
            // TODO Implement runtime spring config (optional)
        }
    }

    void doWithDynamicMethods() {
    }

    void doWithApplicationContext() {

        def mongogeeServiceBean = applicationContext.mongogeeService
        def enabledValue = grailsApplication.config.mongogee.changeEnabled.toString()
        if (enabledValue) {
            mongogeeServiceBean.changeEnabled = enabledValue.toBoolean()
        }
        log.info "set mongogeeService.changeEnabled = ${mongogeeServiceBean.changeEnabled}"

        def continueWithErrorValue = grailsApplication.config.mongogee.continueWithError.toString()
        if (continueWithErrorValue) {
            mongogeeServiceBean.continueWithError = continueWithErrorValue.toBoolean()
        }
        log.info "set mongogeeService.continueWithError = ${mongogeeServiceBean.continueWithError}"

        def mongoDbUrl = grailsApplication.config.grails.mongodb.url.toString()
        if (mongoDbUrl) {
            def databaseName = grailsApplication.config.grails.mongodb.databaseName.toString()
            if (!databaseName) {
                databaseName = mongoDbUrl.split('/')[-1]
            }
            if (databaseName) {
                mongogeeServiceBean.db = applicationContext.mongo.getDB(databaseName)
                mongogeeServiceBean.mongoDatabase = applicationContext.mongo.getDatabase(databaseName)
            }
        }
        log.info "set mongogeeService mongoDbUrl = ${mongoDbUrl}"

        mongogeeServiceBean.changeLogsScanPackage = grailsApplication.config.mongogee.changeLogsScanPackage.toString()
        if (!mongogeeServiceBean.changeLogsScanPackage) {
            throw new MongogeeException('changeLogsScanPackage value not set')
        }
        log.info "set mongogeeService.changeLogsScanPackage = ${mongogeeServiceBean.changeLogsScanPackage}"

        def lockingRetryEnabled = grailsApplication.config.mongogee.lockingRetryEnabled.toString()
        if (lockingRetryEnabled) {
            mongogeeServiceBean.lockingRetryEnabled = Boolean.parseBoolean(lockingRetryEnabled)
        }
        log.info "set mongogeeService.lockingRetryEnabled = ${mongogeeServiceBean.lockingRetryEnabled}"

        def lockingRetryIntervalMillis = grailsApplication.config.mongogee.lockingRetryIntervalMillis
        if (lockingRetryIntervalMillis) {
            mongogeeServiceBean.lockingRetryIntervalMillis = lockingRetryIntervalMillis
        }
        log.info "set mongogeeService.lockingRetryIntervalMillis = ${mongogeeServiceBean.lockingRetryIntervalMillis}"

        def lockingRetryMax = grailsApplication.config.mongogee.lockingRetryMax
        if (lockingRetryMax) {
            mongogeeServiceBean.lockingRetryMax = lockingRetryMax
        }
        log.info "set mongogeeService.lockingRetryMax = ${mongogeeServiceBean.lockingRetryMax}"

    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
