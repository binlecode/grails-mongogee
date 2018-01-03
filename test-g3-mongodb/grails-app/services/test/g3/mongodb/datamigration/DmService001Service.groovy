package test.g3.mongodb.datamigration

import grails.plugin.mongogee.ChangeLog
import grails.plugin.mongogee.ChangeSet
import test.g3.mongodb.Document

// Note: Grails @Transactional annotation is not effective during mongogee changelog invocation.
// This is because mongogee uses reflection to resolve the 'original' method, not the Grails AST transformed
// annotation wrapper method. And mongogee doesn't manage transaction directly.
// The recommended way is apply programmatic transactional control in ChangeSet methods.
@ChangeLog(order = '002')
class DmService001Service {

    @ChangeSet(order = '001', id = '002.001-testLoadDocuments', author = 'test-mongogee-app', runAlways = true)
    def testLoadDocuments() {
        String docName = '002.001 test-load-document name'
        Document.withNewTransaction {
            Document doc = Document.findByName(docName)
            if (!doc) {
                log.info "document doesn't exist with name: $docName, inserting document"
                new Document(name: docName).save(failOnError: true)
            } else {
                log.info "document exists with name: $docName, insert skipped"
            }
        }
    }

    @ChangeSet(order = '002', id = '002.002 test method with error', author = 'test-mongogee-app', runAlways = true, continueWithError = true)
//    @ChangeSet(order = '002', id = '002.002 test method with error', author = 'test-mongogee-app', runAlways = true)
    def testLoadMethodWithError() {
        log.info '002.002 test load method with error'
        throw new RuntimeException('002.002 test load method with exception')
    }

    @ChangeSet(order = '003', id = '002.003 test load-doc method name wild!', author = 'test-mongogee-app', runAlways = true)
    def 'test load-doc method name wild!'() {
        log.info "run changeSet: test load-doc method name wild!"
    }

}
