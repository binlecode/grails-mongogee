package test.mongogee

import com.mongodb.Mongo
import grails.core.GrailsApplication
import grails.plugin.mongogee.ChangeLock
import grails.plugin.mongogee.MongogeeService

class BootStrap {
    GrailsApplication grailsApplication
    MongogeeService mongogeeService
    Mongo mongo

    def init = { servletContext ->

        println mongo.databaseNames



        log.info "execute mongogee"
        mongogeeService.execute()


    }
    def destroy = {
    }
}
