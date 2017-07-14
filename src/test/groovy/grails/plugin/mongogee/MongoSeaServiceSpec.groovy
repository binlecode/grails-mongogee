package grails.plugin.mongogee

import grails.plugin.mongogee.exception.MongoSeaException
import grails.test.mixin.Mock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

@Mock(ChangeLock)
class MongoSeaServiceSpec extends Specification {
    static Logger log = LoggerFactory.getLogger(this.class)
    MongoSeaService mongoSeaService

    def setup() {
        mongoSeaService = new MongoSeaService()
    }

    def cleanup() {
    }

    void "test enabled flag"() {
        setup:
        mongoSeaService.metaClass.executeMigration = { ->
            log.info 'mock executeMigration() => true'
            return true
        }
        when: 'enabled is set to false'
        mongoSeaService.changeEnabled = false
        def result = mongoSeaService.execute()
        then: 'execution is skipped, returning false'
        !result

        when: 'enabled is set to true'
        mongoSeaService.changeEnabled = true
        result = mongoSeaService.execute()
        then: 'execution is carried, returning true'
        result
    }

    void "test continueWithError flag"() {
        setup:
        mongoSeaService.changeEnabled = true
        mongoSeaService.metaClass.executeMigration = { ->
            log.info 'mock executeMigration() => throw exception'
            throw new MongoSeaException("Test exception for MongoSeaService#executeMigration")
        }
        when:
        mongoSeaService.continueWithError = true
        def result = mongoSeaService.execute()
        then:
        notThrown(MongoSeaException)
        result != null
        result == false

        when: 'continueWithError is set to false'
        mongoSeaService.continueWithError = false
        def result2 = mongoSeaService.execute()
        then: 'exception is thrown, and no return'
        thrown(MongoSeaException)
        result2 == null
    }




}
