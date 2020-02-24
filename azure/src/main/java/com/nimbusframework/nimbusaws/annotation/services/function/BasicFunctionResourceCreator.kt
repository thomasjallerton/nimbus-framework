package com.nimbusframework.nimbusaws.annotation.services.function

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.arm.ArmFiles
import com.nimbusframework.nimbusaws.arm.resources.filestorage.StorageAccount
import com.nimbusframework.nimbusaws.arm.resources.function.ApplicationInsights
import com.nimbusframework.nimbusaws.arm.resources.function.FunctionApp
import com.nimbusframework.nimbusaws.arm.resources.function.ServerFarm
import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.BasicServerlessFunctions
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.lang.model.element.Element

class BasicFunctionResourceCreator(
        armFileStages: MutableMap<String, ArmFiles>,
        nimbusState: NimbusState
) : FunctionResourceCreator(
        armFileStages,
        nimbusState,
        BasicServerlessFunction::class.java,
        BasicServerlessFunctions::class.java
) {

    override fun handleElement(type: Element, results: MutableList<FunctionInformation>) {
        val basicFunctions = type.getAnnotationsByType(BasicServerlessFunction::class.java)

        for (basicFunction in basicFunctions) {

            for (stage in basicFunction.stages) {

                val createTemplate = armFileStages.getOrPut(stage) { ArmFiles(nimbusState, stage) }.createTemplate

                val storageAccount = StorageAccount(nimbusState, stage)
                val applicationInsights = ApplicationInsights(nimbusState, stage)
                val serverFarm = ServerFarm(nimbusState, stage)
                val functionResource = FunctionApp(storageAccount, applicationInsights, serverFarm, createTemplate.variables, nimbusState, stage)

                createTemplate.addResource(storageAccount)
                createTemplate.addResource(applicationInsights)
                createTemplate.addResource(serverFarm)
                createTemplate.addResource(functionResource)

                results.add(FunctionInformation(type, functionResource))
            }
        }
    }
}