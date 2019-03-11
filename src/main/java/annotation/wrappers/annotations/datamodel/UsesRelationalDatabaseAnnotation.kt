package annotation.wrappers.annotations.datamodel

import annotation.annotations.database.UsesRelationalDatabase

class UsesRelationalDatabaseAnnotation(private val relationalDatabase: UsesRelationalDatabase): DataModelAnnotation() {

    override val stages = relationalDatabase.stages

    override fun internalDataModel(): Class<out Any> {
        return relationalDatabase.dataModel.java
    }
}