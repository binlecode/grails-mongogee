package test.g3.mongodb

import grails.core.GrailsApplication
import grails.plugin.mongogee.MongogeeService

class BootStrap {
    GrailsApplication grailsApplication
    MongogeeService mongogeeService

    def init = { servletContext ->

        log.info "mongodb url: ${grailsApplication.config.grails.mongodb.url}"

        log.info "mongogee.changeLogsScanPackage: ${grailsApplication.config.grails.mongogee.changeLogsScanPackage}"
        log.info "mongogee.changeEnabled: ${grailsApplication.config.mongogee.changeEnabled}"
        log.info "mongogee.continueWithError: ${grailsApplication.config.mongogee.continueWithError}"
        log.info "mongogee.lockingRetryEnabled: ${grailsApplication.config.mongogee.lockingRetryEnabled}"
        log.info "mongogee.lockingRetryIntervalMillis: ${grailsApplication.config.mongogee.lockingRetryIntervalMillis}"
        log.info "mongogee.lockingRetryMax: ${grailsApplication.config.mongogee.lockingRetryMax}"

        mongogeeService.execute()

    }

    def destroy = {
    }
}
