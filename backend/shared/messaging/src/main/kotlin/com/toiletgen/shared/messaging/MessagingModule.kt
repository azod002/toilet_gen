package com.toiletgen.shared.messaging

import com.toiletgen.shared.messaging.outbox.OutboxEventPublisher
import com.toiletgen.shared.messaging.outbox.OutboxPoller
import org.koin.dsl.module

fun messagingModule(bootstrapServers: String, groupId: String, topics: List<String>) = module {
    single { EventPublisher(bootstrapServers) }
    single { EventConsumer(bootstrapServers, groupId, topics) }
    single { OutboxEventPublisher() }
    single { OutboxPoller(get()) }
}
