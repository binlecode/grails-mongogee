package grails.plugin.mongogee

import com.mongodb.DB
import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import grails.plugin.mongogee.exception.MongoSeaChangeSetException
import grails.plugin.mongogee.exception.MongoSeaException
import groovy.util.logging.Slf4j

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@Slf4j
class MongoSeaService {
    def grailsApplication  // autowired by Grails
    def mongo  // autowired by Grails

    MongoDatabase mongoDatabase
    DB db

    /**
     * base package path for data migration classes.
     *
     * @see {@link ChangeLog} and {@link ChangeSet}
     */
    String changeLogsScanPackage
    /**
     * data migration will be skipped if changeEnabled is set to false, default to true
     */
    Boolean changeEnabled = true
    /**
     * if true, the application boot-up will not be stopped by migration error, default to false
     */
    Boolean continueWithError = false

    def execute() {
        if (!changeEnabled) {
            log.info 'MongoSea is disabled, skipping data migration.'
            return false
        }

        if (!ChangeLock.acquireLock()) {
            log.info "MongoSea can not acquire process lock. Exiting."
            return false
        }

        log.info "MongoSea acquired process lock, starting the data migration sequence..."

        String errMsg
        try {
            executeMigration()
            log.info "MongoSea data migration has completed."
        } catch (ex) {
            errMsg = "MongoSea data migration failed: ${ex.message ?: ex.toString()}"
            log.error errMsg, ex
        } finally {
            log.info "MongoSea is releasing process lock."
            ChangeLock.releaseLock()
            log.info "MongoSea has released process lock."
        }

        if (errMsg) {
            if (!continueWithError) {
                throw new MongoSeaException(errMsg)
            }
            return false
        } else {
            return true
        }
    }

    protected executeMigration() {
        try {
            ChangeAgent changeAgent = new ChangeAgent(changeLogsScanPackage)
            changeAgent.fetchChangeLogs().each { Class<?> changeLogClass ->
                def changeLogInstance = changeLogClass.getConstructor().newInstance()

                changeAgent.fetchChangeSets(changeLogInstance.getClass()).each { Method changeSetMethod ->
                    applyChangeSet(changeSetMethod, changeLogInstance, changeAgent)
                }
            }
        } catch (NoSuchMethodException e) {
            throw new MongoSeaException(e.getMessage(), e)
        } catch (IllegalAccessException e) {
            throw new MongoSeaException(e.getMessage(), e)
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException()
            throw new MongoSeaException(targetException.getMessage(), e)
        } catch (InstantiationException e) {
            throw new MongoSeaException(e.getMessage(), e)
        }
    }

    protected applyChangeSet(Method changeSetMethod, changeLogInstance, ChangeAgent changeAgent) {
        ChangeEntry changeEntry = buildChangeEntry(changeSetMethod)
        if (!changeEntry) {
            return // return from current iteration
        }

        ChangeEntry existingChangeEntry = ChangeEntry.findByChangeSetId(changeEntry.changeSetId)

        if (!existingChangeEntry) {
            log.info "applying new changeSet: $changeEntry"
            invokeChangeSetMethod(changeSetMethod, changeLogInstance)

            ChangeEntry.withNewTransaction {
                changeEntry.save(failOnError: true, flush: true)
                buildChangeEntryLog(changeEntry).save(failOnError: true, flush: true)
            }
            log.info " ... changeSet applied"

        } else if (changeAgent.isRunAlwaysChangeSet(changeSetMethod)) {
            log.info "reapplying changeSet: $changeEntry"
            invokeChangeSetMethod(changeSetMethod, changeLogInstance)

            ChangeEntry.withNewTransaction {
                if (changeEntry.author) {
                    existingChangeEntry.author = changeEntry.author
                }
                existingChangeEntry.save(failOnError: true, flush: true)
                buildChangeEntryLog(existingChangeEntry).save(failOnError: true, flush: true)
            }
            log.info " ... changeSet reapplied"

        } else {
            log.info "changeSet skipped: $changeEntry"
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
            throw new MongoSeaChangeSetException("ChangeSet method ${changeSetMethod.getName()} has wrong arguments list. Please see docs for more info!")
        }
    }

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
        ChangeEntryLog cel = new ChangeEntryLog()
        cel.properties = changeEntry.properties
        return cel
    }

}
