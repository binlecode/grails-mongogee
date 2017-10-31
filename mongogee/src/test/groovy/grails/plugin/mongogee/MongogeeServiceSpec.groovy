/*******************************************************************************
 *  Copyright 2017 Bin Le
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/

package grails.plugin.mongogee

import grails.plugin.mongogee.exception.MongogeeException
import grails.test.mixin.Mock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

@Mock(ChangeLock)
class MongogeeServiceSpec extends Specification {
    static Logger log = LoggerFactory.getLogger(this.class)
    MongogeeService mongogeeService

    def setup() {
        mongogeeService = new MongogeeService()
    }

    def cleanup() {
    }

    void "test enabled flag"() {
        setup:
        mongogeeService.metaClass.executeMigration = { ->
            log.info 'mock executeMigration() => true'
            return true
        }
        when: 'enabled is set to false'
        mongogeeService.changeEnabled = false
        def result = mongogeeService.execute()
        then: 'execution is skipped, returning false'
        !result

        when: 'enabled is set to true'
        mongogeeService.changeEnabled = true
        result = mongogeeService.execute()
        then: 'execution is carried, returning true'
        result
    }

    void "test continueWithError flag"() {
        setup:
        mongogeeService.changeEnabled = true
        mongogeeService.metaClass.executeMigration = { ->
            println 'mock executeMigration() => throw exception'
            throw new MongogeeException("Test exception for MongogeeService#executeMigration")
        }
        when:
        mongogeeService.continueWithError = true
        def result = mongogeeService.execute()
        then:
        notThrown(MongogeeException)
        result != null
        result == false

        when: 'continueWithError is set to false'
        mongogeeService.continueWithError = false
        def result2 = mongogeeService.execute()
        then: 'exception is thrown, and no return'
        thrown(MongogeeException)
        result2 == null
    }

    void "test lock fail retrying logic"() {
        setup:
        ChangeLock.metaClass.'static'.acquireLock = { ->
            println 'mock ChangeLock.acquireLock() = false'
            false
        }
        mongogeeService.metaClass.executeMigration = { ->
            println 'mock executeMigration() => true'
            return true
        }
        when:  'retry is disabled'
        mongogeeService.lockingRetryEnabled = false
        mongogeeService.execute()
        then:  'fail immediately'
        def ex = thrown(MongogeeException)
        ex.message.startsWith('Mongogee can not acquire process lock while migration is enabled')

        when: 'retry is enabled'
        mongogeeService.lockingRetryEnabled = true
        mongogeeService.lockingRetryMax = 2
        mongogeeService.lockingRetryIntervalMillis = 100
        mongogeeService.execute()
        then: 'fail after retrial max reached'
        ex = thrown(MongogeeException)
        ex.message.startsWith('Mongogee can not acquire process lock while migration is enabled')
    }




}
