package clients.rdbms

import java.sql.Connection
import java.sql.DriverManager

class DatabaseClientRds {

    fun getConnection(databaseObject: Any): Connection {
        return DriverManager.getConnection("jdbc:mysql://testdb.cvghjo0rbr3a.eu-west-1.rds.amazonaws.com/testdb", "root", "Lock4tree359")
    }

}

fun main() {
    val client = DatabaseClientRds()
    val connection = client.getConnection("")
    val statement = connection.createStatement()
    val results = statement.executeQuery("SHOW TABLES")
    while (results.next()) {
        println(results.getString(0))
    }
}
