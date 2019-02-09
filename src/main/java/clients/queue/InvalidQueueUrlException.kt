package clients.queue

import java.lang.Exception

class InvalidQueueUrlException: Exception("Not a valid queue url. Have you set up the @UsesQueue annotation correctly?")