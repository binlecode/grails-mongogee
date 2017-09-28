package grails.plugin.mongogee

import groovy.transform.ToString

/**
 * Domain for logging change entries. Every change entry applied to database is logged in this collection.
 *
 * @author binle
 * @since 10/07/2017
 */
@ToString(includeNames = true, includePackage = false)
class ChangeEntryLog {

    Date dateCreated
    String id
    String changeLogClassName
    String changeSetMethodName
    String changeSetId
    String author
    String host = 'localhost'

    String status
    String message

    static constraints = {
        author nullable: true
        status nullable: true, inList: ['success', 'fail']
        message nullable: true
    }

    static mapWith = 'mongo'
    static mapping = {
        collection 'mongogeeChangeEntryLog'
        version false
        changeSetId index: true
        changeLogClassName index: true   // support filter by changeLogClass
    }

    // disable update
    def beforeUpdate() {
        throw new UnsupportedOperationException('Update not allowed for this domain')
    }
    // ensure no deletion allowed
    def beforeDelete() {
        throw new UnsupportedOperationException('Delete not allowed for this domain')
    }

}
