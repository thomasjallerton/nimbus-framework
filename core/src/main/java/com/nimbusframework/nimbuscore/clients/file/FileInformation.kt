package com.nimbusframework.nimbuscore.clients.file

import java.time.Instant

data class FileInformation(val lastModified: Instant, val size: Long, val path: String)
