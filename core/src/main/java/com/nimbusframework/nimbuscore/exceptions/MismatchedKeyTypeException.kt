package com.nimbusframework.nimbuscore.exceptions

class MismatchedKeyTypeException(tableDef: Any, clientType: Any):
        Exception("Key not expected Type. Client key set as $clientType, table object key as $tableDef")