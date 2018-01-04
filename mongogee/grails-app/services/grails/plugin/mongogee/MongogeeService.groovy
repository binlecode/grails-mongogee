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

import com.mongodb.DB
import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import grails.plugin.mongogee.exception.MongogeeChangeSetException
import grails.plugin.mongogee.exception.MongogeeException
import groovy.util.logging.Slf4j

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@Slf4j
class MongogeeService {
    def mongo  // autowired by Grails

    MongoDatabase mongoDatabase
    DB db

    /**
     * simple or CSV string for one or more base package paths for data migration classes.
     *
     * If both {@link #changeLogsScanPackage} and {@link #changeLogsScanPackageList} are assigned,
     * then {@link #changeLogsScanPackage} is used and {@link #changeLogsScanPackageList} is ignored.
     *
     * @see {@link ChangeLog} and {@link ChangeSet}
     */
    String changeLogsScanPackage
    /**
     * list of base package paths for data migration classes.
     *
     * @see {@link ChangeLog} and {@link ChangeSet}
     */
    List<String> changeLogsScanPackageList
    /**
     * data migration will be skipped if changeEnabled is set to false, default to true
     */
    Boolean changeEnabled = true
    /**
     * if true, the application boot-up will not be stopped by migration error, default to false
     */
    Boolean continueWithError = false

    /**
     * If true, migration locking will be retried, default to true
     */
    Boolean lockingRetryEnabled = true
    /**
     * Time interval between migration locking retrials, default to 5s
     */
    Integer lockingRetryIntervalMillis = 5000
    /**
     * Max number of migration locking retries, default to 120, aka 10min
     */
    Integer lockingRetryMax = 120

    def execute() {
        if (!changeEnabled) {
            log.info 'Mongogee is disabled, skipping data migration.'
            return false
        }


        // if change is enabled, if locking failed, then app boot-up should be stopped
        log.info 'Mongogee is enabled, trying to acquire migration lock'
        if (!ChangeLock.acquireLock()) {
            log.info 'try migration locking failed'

            boolean lockAcquired = false

            if (lockingRetryEnabled) {
                def numberOfRetrails = 1
                while (numberOfRetrails <= lockingRetryMax) {
                    numberOfRetrails += 1

                    log.info "wait for ${lockingRetryIntervalMillis/1000}s before retry migration locking \n ..."
                    new Object().sleep(lockingRetryIntervalMillis) {
                        log.info "received interruption"
                        false   // ignore interruption if set to false
                    }

                    log.info "retry migration locking for the $numberOfRetrails time"
                    if (ChangeLock.acquireLock()) {
                        lockAcquired = true
                        log.info "retry locking successful"
                        break
                    } else {
                        log.info "retry locking failed"
                    }
                }
            }

            if (!lockAcquired) {
                def exMsg = 'Mongogee can not acquire process lock while migration is enabled. Quiting application boot-up.'
                log.warn(exMsg)
                throw new MongogeeException(exMsg)
            }
        }

        log.info "Mongogee acquired process lock, starting the data migration sequence..."

        String errMsg
        try {
            executeMigration()
            log.info "Mongogee data migration has completed."
        } catch (ex) {
            errMsg = "Mongogee data migration failed: ${ex.message ?: ex.toString()}"
            log.error errMsg, ex
        } finally {
            log.info "Mongogee is releasing process lock."
            ChangeLock.releaseLock()
            log.info "Mongogee has released process lock."
        }

        if (errMsg) {
            if (!continueWithError) {
                throw new MongogeeException(errMsg)
            }
            return false
        } else {
            return true
        }
    }

    protected executeMigration() {
        try {
            ChangeAgent changeAgent
            if (changeLogsScanPackage) {
                changeAgent = new ChangeAgent(changeLogsScanPackage)
            } else {
                changeAgent = new ChangeAgent(changeLogsScanPackageList)
            }
            changeAgent.fetchChangeLogs().each { Class<?> changeLogClass ->
                def changeLogInstance = changeLogClass.getConstructor().newInstance()

                changeAgent.fetchChangeSets(changeLogInstance.getClass()).each { Method changeSetMethod ->
                    applyChangeSet(changeSetMethod, changeLogInstance, changeAgent)
                }
            }
        } catch (NoSuchMethodException e) {
            throw new MongogeeException(e.getMessage(), e)
        } catch (IllegalAccessException e) {
            throw new MongogeeException(e.getMessage(), e)
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException()
            throw new MongogeeException(targetException.getMessage(), e)
        } catch (InstantiationException e) {
            throw new MongogeeException(e.getMessage(), e)
        }
    }

    /**
     * Apply change with given changeSet method and changeLog object.
     *
     * @param changeSetMethod  {@link java.lang.reflect.Method} instance whose definition is {@link ChangeSet} annotated
     * @param changeLogInstance  changeLog instance whose definition is {@link ChangeLog} annotated
     * @param changeAgent  {@link ChangeAgent} instance
     */
    protected applyChangeSet(Method changeSetMethod, changeLogInstance, ChangeAgent changeAgent) {
        ChangeEntry changeEntry = buildChangeEntry(changeSetMethod)
        if (!changeEntry) {
            return
        }

        ChangeEntry existingChangeEntry = ChangeEntry.findByChangeSetId(changeEntry.changeSetId)

        if (!existingChangeEntry) {
            log.info "applying new changeSet: $changeEntry"
            invokeChangeSetWithEntry(changeSetMethod, changeEntry, changeLogInstance, changeAgent)

        } else if (changeAgent.isRunAlwaysChangeSet(changeSetMethod)) {

            // merge changeEntry into existing changeEntry and update other attributes
            if (changeEntry.author) {
                existingChangeEntry.author = changeEntry.author
            }
            existingChangeEntry.runCount += 1
            changeEntry = existingChangeEntry

            log.info "reapplying existing changeSet: $changeEntry"
            invokeChangeSetWithEntry(changeSetMethod, changeEntry, changeLogInstance, changeAgent)

        } else {
            log.info "changeSet skipped: $changeEntry"
        }
    }

    /**
     * A wrapper of {@link #invokeChangeSetWithEntry(Method, ChangeEntry, Object)} )}
     * Invoke the changeSet with changeEntry info, and control error by changeSet annotation 'continueWithError'
     * attribute.
     */
    protected invokeChangeSetWithEntry(
            Method changeSetMethod, ChangeEntry changeEntry, changeLogInstance, ChangeAgent changeAgent) {
        try {
            invokeChangeSetWithEntry(changeSetMethod, changeEntry, changeLogInstance)
        } catch (ex) {
            if (changeAgent.isContinueWithError(changeSetMethod)) {
                // log error msg and swallow exp
                log.warn 'changeSet continueWithError = true => invocation error logged and migration continued'
            } else {
                // bubble up exp
                log.warn 'changeSet continueWithError = false => invocation error bubbled and migration stopped'
                throw ex
            }
        }
    }

    /**
     * Invoke the chagneSet, save changeEntry info, and log success or failure, when there's exception, the exception
     * is saved to change entry log, and then bubbled up to main execution flow.
     */
    protected invokeChangeSetWithEntry(Method changeSetMethod, ChangeEntry changeEntry, changeLogInstance) {
        try {
            invokeChangeSetMethod(changeSetMethod, changeLogInstance)
            log.info "changeSet invoked: ${changeSetMethod.name}"

            ChangeEntry.withNewTransaction {
                changeEntry.save(failOnError: true, flush: true)
                def changeEntryLog = buildChangeEntryLog(changeEntry).save(failOnError: true)
                log.debug "changeEntry saved [id: ${changeEntry.id}], and logged [id: ${changeEntryLog.id}]"
            }
        } catch (ex) {
            log.error "changeSet invocation error: ${ex.message ?: ex.toString().take(255)}"
            // save error information to change entry log
            ChangeEntryLog.withNewTransaction {
                def changeEntryLog = buildChangeEntryErrorLog(changeEntry, ex).save(failOnError: true)
                log.debug "changeSet invocation error logged [id: ${changeEntryLog.id}]"
            }
            // bubble exp to main flow
            throw ex
        }
    }

    /**
     * Invoke changeSet method based on method argument specification.
     */
    protected invokeChangeSetMethod(Method changeSetMethod, changeLogInstance) {
        if (changeSetMethod.getParameterTypes().length == 1
                && changeSetMethod.getParameterTypes()[0] == MongoClient.class) {
            log.debug("invoke method with MongoClient argument")
            return changeSetMethod.invoke(changeLogInstance, (MongoClient)mongo)
        }
        if (changeSetMethod.getParameterTypes().length == 1
                && changeSetMethod.getParameterTypes()[0] == DB.class) {
            log.debug("invoke method with DB argument")
            return changeSetMethod.invoke(changeLogInstance, db)

        } else if (changeSetMethod.getParameterTypes().length == 1
                && changeSetMethod.getParameterTypes()[0] == MongoDatabase.class) {
            log.debug("invoke method with DB argument")
            return changeSetMethod.invoke(changeLogInstance, mongoDatabase)

        } else if (changeSetMethod.getParameterTypes().length == 0) {
            log.debug("invoke method with no argument")
            return changeSetMethod.invoke(changeLogInstance)

        } else {
            throw new MongogeeChangeSetException("ChangeSet method ${changeSetMethod.getName()} has wrong arguments list. Please see docs for more info!")
        }
    }

    /**
     * Builds a change entry object from the input annotated {@link ChangeSet} method.
     * If the input method is not valid, return null.
     *
     * @param changeSetMethod   {@link ChangeSet} annotated {@link java.lang.reflect.Method} object
     * @return {@link ChangeEntry} object, or null
     */
    protected ChangeEntry buildChangeEntry(Method changeSetMethod) {
        if (changeSetMethod.isAnnotationPresent(ChangeSet.class)) {
            ChangeSet annotation = changeSetMethod.getAnnotation(ChangeSet.class)
            return new ChangeEntry(
                    author: annotation.author(),
                    changeSetId: annotation.id(),
                    changeLogClassName: changeSetMethod.getDeclaringClass().getName(),
                    changeSetMethodName: changeSetMethod.getName()
            )
        } else {
            return null
        }
    }

    protected ChangeEntryLog buildChangeEntryLog(ChangeEntry changeEntry) {
        ChangeEntryLog cel = new ChangeEntryLog(
                changeLogClassName: changeEntry.changeLogClassName,
                changeSetMethodName: changeEntry.changeSetMethodName,
                author: changeEntry.author,
                changeSetId: changeEntry.changeSetId,
                host: ChangeLock.getHostName()
        )
        return cel
    }

    protected ChangeEntryLog buildChangeEntryErrorLog(ChangeEntry changeEntry, Exception exp) {
        ChangeEntryLog cel = buildChangeEntryLog(changeEntry)
        cel.status = 'fail'
        cel.message = exp.message ?: exp.toString().take(255)
        return cel
    }

}
