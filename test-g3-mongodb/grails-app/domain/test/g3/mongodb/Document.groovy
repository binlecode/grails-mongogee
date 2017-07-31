package test.g3.mongodb

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
