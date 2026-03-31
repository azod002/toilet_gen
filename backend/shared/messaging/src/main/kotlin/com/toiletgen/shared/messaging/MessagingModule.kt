package com.toiletgen.shared.messaging

import org.koin.dsl.module

fun messagingModule(bootstrapServers: String, groupId: String, topics: List<String>) = module {
    single { EventPublisher(bootstrapServers) }
    single { EventConsumer(bootstrapServers, groupId, topics) }
}
