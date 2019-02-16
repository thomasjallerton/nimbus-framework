package clients.queue

class InvalidQueueUrlException: Exception("Not a valid queue url. Have you set up the @UsesQueue annotation correctly?")