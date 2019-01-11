package wrappers.keyvalue.exceptions

import java.lang.Exception

class ConditionFailedException: Exception("Unable to delete item as condition failed")