package grails.plugin.mongogee

import groovy.transform.ToString

/**
 * Entry in the changes collection log
 * Type: entity class.
 *
 * @author lstolowski
 * @author binle
 * @since 27/07/2014
 */
@ToString(includeNames = true, includePackage = false)
class ChangeEntry {

    Date dateCreated
    Date lastUpdated
    String id
    String changeLogClassName
    String changeSetMethodName
    String changeSetId
    String author

    static constraints = {
        changeSetId blank: false, unique: true
        author nullable: true
    }

    static mapWith = 'mongo'
    static mapping = {
        collection 'mongogeeChangeEntry'
        version true
        changeSetId index: true
        changeLogClassName index: true
        changeSetMethodName index: true
    }

    // disable delete
    def beforeDelete() {
        throw new UnsupportedOperationException('Delete not allowed for this domain')
    }

}
