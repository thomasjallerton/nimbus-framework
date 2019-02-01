package clients.keyvalue

class MismatchedKeyException(expectedType: String):
        Exception("Key input not expected Type. Expected $expectedType")