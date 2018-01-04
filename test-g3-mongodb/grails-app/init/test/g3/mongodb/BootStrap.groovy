package test.g3.mongodb

import grails.core.GrailsApplication

class BootStrap {
    GrailsApplication grailsApplication

    def init = { servletContext ->
        log.info "mongodb url: ${grailsApplication.config.grails.mongodb.url}"
    }

    def destroy = {
    }
}
