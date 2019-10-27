package com.nimbusframework.nimbuscore.persisted

enum class ClientType {
    DocumentStore,
    KeyValueStore,
    FileStorage,
    EnvironmentVariable,
    BasicFunction,
    Notification,
    Queue,
    Database,
    WebSocket;
}