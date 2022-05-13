package services

import org.apache.maven.plugin.MojoFailureException
import java.net.URI

interface StackService {

    fun findExport(exportName: String): FindExportResponse

    fun updateStack(projectName: String, uri: URI): Boolean

    fun createStack(projectName: String, stage: String, compiledSourcesPath: String): CreateStackResponse

    fun deleteStack(stackName: String): Boolean

    fun getStackStatus(stackName: String): String

    fun getStackErrorReason(stackName: String): String

    fun isErrorStatus(stackStatus: String): Boolean

    fun canContinue(stackStatus: String): ContinueResponse

    @Throws(MojoFailureException::class)
    fun pollStackStatus(projectName: String, count: Int = 0)

    data class FindExportResponse(val successful: Boolean, val result: String)

    data class ContinueResponse(val canContinue: Boolean, val needErrorMessage: Boolean)

    data class CreateStackResponse(val successful: Boolean, val alreadyExists: Boolean)
}