package test.g3.mongodb

import grails.core.GrailsApplication

class BootStrap {
    GrailsApplication grailsApplication

    def init = { servletContext ->

        log.info "mongodb url: ${grailsApplication.config.grails.mongodb.url}"

        log.info "mongogee.changeLogsScanPackage: ${grailsApplication.config.mongogee.changeLogsScanPackage}"
        log.info "mongogee.changeEnabled: ${grailsApplication.config.mongogee.changeEnabled}"
        log.info "mongogee.continueWithError: ${grailsApplication.config.mongogee.continueWithError}"
        log.info "mongogee.lockingRetryEnabled: ${grailsApplication.config.mongogee.lockingRetryEnabled}"
        log.info "mongogee.lockingRetryIntervalMillis: ${grailsApplication.config.mongogee.lockingRetryIntervalMillis}"
        log.info "mongogee.lockingRetryMax: ${grailsApplication.config.mongogee.lockingRetryMax}"
    }

    def destroy = {
    }
}
