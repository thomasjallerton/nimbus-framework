package com.nimbusframework.nimbuscore.clients.dynamo

class MismatchedKeyTypeException(tableDef: Any, clientType: Any):
        Exception("Key not expected Type. Client key set as $clientType, table object key as $tableDef")