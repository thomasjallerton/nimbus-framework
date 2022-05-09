package com.nimbusframework.nimbusaws.cloudformation.model.resource.basic

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionTrigger
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource

class CronRule(
    private val cron: String,
    private val target: FunctionResource,
    nimbusState: NimbusState
): Resource(nimbusState, target.stage), FunctionTrigger {

    override fun getTriggerType(): String {
        return "events."
    }

    override fun getTriggerName(): String {
        return "Cron"
    }

    override fun getTriggerArn(): JsonObject {
        return getArn()
    }

    init {
        addDependsOn(target)
    }

    override fun toCloudFormation(): JsonObject {
        val rule = JsonObject()
        rule.addProperty("Type", "AWS::Events::Rule")
        val properties = getProperties()

        properties.addProperty("ScheduleExpression", cron)


        val targets = JsonArray()
        targets.add(getRuleTarget())
        properties.add("Targets", targets)

        rule.add("Properties", properties)

        rule.add("DependsOn", dependsOn)

        return rule
    }

    override fun getName(): String {
        return "CronRule${target.getShortName()}"
    }

    private fun getRuleTarget(): JsonObject {
        val eventRule = JsonObject()

        eventRule.add("Arn", target.getArn())
        eventRule.addProperty("Id", "RuleTarget${target.getShortName()}")

        return eventRule
    }
}
