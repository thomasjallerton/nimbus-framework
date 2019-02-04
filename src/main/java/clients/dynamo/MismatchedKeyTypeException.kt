package clients.dynamo

class MismatchedKeyTypeException(tableDef: annotation.annotations.keyvalue.KeyType, clientType: Any):
        Exception("Key not expected Type. Client key set as ${clientType.javaClass.simpleName}, table object key as ${tableDef.name}")