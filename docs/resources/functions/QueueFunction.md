---
id: QueueFunction
title: Queue Function
sidebar_label: Queue Function
---



## Annotation Specification
### @QueueServerlessFunction
#### Required Parameters
* `batchSize` - How many items per serverless function invocation
* `id` - Name of the queue


#### Optional Parameters
* `timeout` - How long the function is allowed to run for before timing out, in seconds. Defaults to 10.
* `memory` - The amount of memory the function runs with, in MB. Defaults to 1024.
* `stages` - The stages that the function is deployed to.