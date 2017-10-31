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
