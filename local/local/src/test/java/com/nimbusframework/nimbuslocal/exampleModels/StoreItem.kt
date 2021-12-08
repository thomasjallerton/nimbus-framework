package com.nimbusframework.nimbuslocal.exampleModels

import com.nimbusframework.nimbuscore.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.annotations.persistent.Key

class StoreItem(
        @Key val string: String = "",
        @Attribute val boolean: Boolean = true,
        @Attribute val int: Int = 0,
        @Attribute val float: Float = 0f,
        @Attribute val double: Double = 0.0,
        @Attribute val long: Long = 0,
        @Attribute val short: Short = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StoreItem

        if (string != other.string) return false
        if (boolean != other.boolean) return false
        if (int != other.int) return false
        if (float != other.float) return false
        if (double != other.double) return false
        if (long != other.long) return false
        if (short != other.short) return false

        return true
    }

    override fun hashCode(): Int {
        var result = string.hashCode()
        result = 31 * result + boolean.hashCode()
        result = 31 * result + int
        result = 31 * result + float.hashCode()
        result = 31 * result + double.hashCode()
        result = 31 * result + long.hashCode()
        result = 31 * result + short
        return result
    }

    companion object {
        val allAttributes = mapOf(
                Pair("string", StoreItem::class.java.getDeclaredField("string")),
                Pair("boolean", StoreItem::class.java.getDeclaredField("boolean")),
                Pair("int", StoreItem::class.java.getDeclaredField("int")),
                Pair("float", StoreItem::class.java.getDeclaredField("float")),
                Pair("double", StoreItem::class.java.getDeclaredField("double")),
                Pair("long", StoreItem::class.java.getDeclaredField("long")),
                Pair("short", StoreItem::class.java.getDeclaredField("short"))
        )
    }
}