package test.mongogee

import grails.plugin.mongogee.MongogeeService

class BootStrap {
    MongogeeService mongogeeService

    def init = { servletContext ->

        log.info "execute mongogee"
        mongogeeService.execute()

    }
    def destroy = {
    }
}
