package grails.plugin.mongogee

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ChangeLock {
    static final String STATUS_LOCKED = 'LOCKED'
    static final String STATUS_FREE = 'FREE'

    static Logger log = LoggerFactory.getLogger(this.class)

    Date dateCreated
    Date lastUpdated
    String id
    /** locking status, 'LOCKED' or 'FREE' */
    String status
    /** name or ip of the host node */
    String host = 'localhost'

    static constraints = {
        status inList: [STATUS_LOCKED, STATUS_FREE], unique: true
    }

    static mapWith = 'mongo'
    static mapping = {
        collection 'mongogeeChangeLock'
    }

    static boolean acquireLock() {
        ChangeLock lock = this.findByStatus(STATUS_LOCKED)
        if (lock) {
            log.debug "changeLock locking failed: lock exists"
            return false
        }
        try {
            ChangeLock.withNewTransaction {
                ChangeLock changeLock = this.findByStatus(STATUS_FREE)
                if (!changeLock) {
                    changeLock = new ChangeLock(status: STATUS_LOCKED, host: getHostName())
                } else {
                    changeLock.status = STATUS_LOCKED
                    changeLock.host = getHostName()
                }
                changeLock.save(failOnError: true, flush: true)
            }
            log.debug "changeLock locked successful"
            return true
        } catch (ex) {
            log.debug "changeLock locking failed: ${ex.message ?: ex.toString()}"
        }
        return false
    }

    static boolean releaseLock() {
        try {
            ChangeLock.withNewTransaction {
                ChangeLock lock = this.findByStatus(STATUS_LOCKED)
                if (lock) {
                    lock.status = STATUS_FREE
                    lock.save(failOnError: true, flush: true)
                    log.debug "changeLock releasing successful"
                } else {
                    log.warn "changeLock releasing skipped: lock not found"
                }
            }
            return true
        } catch (ex) {
            log.error "changeLock releasing failed: ${ex.message ?: ex.toString()}"
        }
        return false
    }

    static protected getHostName() {
        InetAddress.getLocalHost().getHostName()
    }

}
