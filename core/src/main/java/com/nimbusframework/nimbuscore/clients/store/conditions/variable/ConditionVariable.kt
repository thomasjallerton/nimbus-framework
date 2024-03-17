package com.nimbusframework.nimbuscore.clients.store.conditions.variable

abstract class ConditionVariable {

    abstract fun getValue(): Any

    companion object {

        @JvmStatic
        fun booleanValue(boolean: Boolean): BooleanVariable {
            return BooleanVariable(boolean)
        }

        @JvmStatic
        fun column(fieldName: String): ColumnVariable {
            return ColumnVariable(fieldName)
        }

        @JvmStatic
        fun numeric(number: Number): NumericVariable {
            return NumericVariable(number)
        }

        @JvmStatic
        fun string(string: String): StringVariable {
            return StringVariable(string)
        }

        @JvmStatic
        fun obj(obj: Any): ObjectVariable {
            return ObjectVariable(obj)
        }
    }
}
