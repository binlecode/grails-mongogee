package test.g3.mongodb

import grails.plugin.mongogee.MongogeeService

class BootStrap {

    MongogeeService mongogeeService


    def init = { servletContext ->

        mongogeeService.execute()

    }

    def destroy = {
    }
}
