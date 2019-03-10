package annotation.services.resources

import cloudformation.CloudFormationDocuments
import javax.annotation.processing.RoundEnvironment

abstract class CloudResourceResourceCreator(
        protected val roundEnvironment: RoundEnvironment,
        protected val cfDocuments: MutableMap<String, CloudFormationDocuments>
) {

    abstract fun create()

    protected fun determineTableName(givenName: String, className: String): String {
        return if (givenName == "") {
            className
        } else {
            givenName
        }
    }
}