package test.mongogee

class Document {

    Date dateCreated
    Date lastUpdated
    String id
    String name


    static constraints = {
    }

    static mapWith = "mongo"
    static mapping = {

    }
}
