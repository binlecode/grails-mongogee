package test.g3.mongodb.datamigration

import com.mongodb.DB
import grails.plugin.mongogee.ChangeLog
import grails.plugin.mongogee.ChangeSet
import groovy.util.logging.Slf4j
import test.g3.mongodb.Document

/**
 * Created by binle on 7/14/17.
 */
@Slf4j
@ChangeLog(order = '001')
class Dm001 {

    @ChangeSet(order = '001', id = 'createTestCollection', author = 'test-mongogee-app')
    def createTestCollection(DB db) {
        def colName = 'testCollection'
        if (!db.collectionExists(colName)) {
            db.createCollection(colName, [:])
            log.info "collection created: $colName"
        } else {
            log.info "collection already exists: $colName, creation skipped"
        }
    }

    @ChangeSet(order = '002', id = 'loadDocumentData', author = 'test-mongogee-app', runAlways = true)
    def loadDocumentData() {
        Document.collection.insert(name: "document at ${new Date()}")
    }

//    @ChangeSet(order = '003', id = '001.003.loadDocumentDataWithException', author = 'test-mongogee-app', runAlways = true)
//    def loadDocumentDataWithException() {
//        throw new RuntimeException('Test exception thrown from changeSet: 001.003.loadDocumentDataWithException')
//    }

    @ChangeSet(order = '004', id = '001.004: test_load_method_name_with_underscore', author = 'test-mongogee-app', runAlways = true)
    def test_load_method_name() {
        log.info "running test_load_method_name_with_underscore"
    }

    @ChangeSet(order = '005', id = '001.005: test-load-method-name-with-hyphen', author = 'test-mongogee-app', runAlways = true)
    def 'test-load-method-name-with-hyphen'() {
        log.info "running test-load-method-name-with-hyphen"
    }

    @ChangeSet(order = '006', id = '001.006: test load method-name wild!', author = 'test-mongogee-app', runAlways = true)
    def 'test load method-name wild!'() {
        log.info "running test load method-name wild!"
    }


}
