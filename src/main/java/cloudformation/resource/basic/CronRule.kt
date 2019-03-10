package cloudformation.resource.basic

import persisted.NimbusState
import cloudformation.resource.Resource
import cloudformation.resource.function.FunctionTrigger
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class CronRule(
        private val cron: String,
        private val target: Resource,
        nimbusState: NimbusState
): Resource(nimbusState, target.stage), FunctionTrigger {

    override fun getTriggerType(): String {
        return "events."
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
        targets.add(getRuleTarget(target))
        properties.add("Targets", targets)

        rule.add("Properties", properties)

        rule.add("DependsOn", dependsOn)

        return rule
    }

    override fun getName(): String {
        return "CronRule${target.getName()}"
    }

    private fun getRuleTarget(resource: Resource): JsonObject {
        val eventRule = JsonObject()

        eventRule.add("Arn", resource.getArn())
        eventRule.addProperty("Id", "RuleTarget${resource.getName()}")

        return eventRule
    }
}