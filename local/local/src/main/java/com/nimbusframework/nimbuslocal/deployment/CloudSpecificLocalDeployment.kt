package com.nimbusframework.nimbuslocal.deployment

import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import com.nimbusframework.nimbuslocal.deployment.services.resource.LocalCreateResourcesHandler
import com.nimbusframework.nimbuslocal.deployment.services.usesresources.LocalUsesResourcesHandler

interface CloudSpecificLocalDeployment {

    fun getLocalCreateResourcesHandlers(localResourceHolder: LocalResourceHolder, stageService: StageService): List<LocalCreateResourcesHandler>

    fun getLocalUsesResourcesHandlers(localResourceHolder: LocalResourceHolder, stageService: StageService): List<LocalUsesResourcesHandler>

}
