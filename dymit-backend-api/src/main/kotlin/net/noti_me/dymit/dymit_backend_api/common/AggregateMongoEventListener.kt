package net.noti_me.dymit.dymit_backend_api.common

import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent
import org.springframework.data.domain.AbstractAggregateRoot
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot

@Component
class AggregateMongoEventListener(
    private val publisher: ApplicationEventPublisher
) : AbstractMongoEventListener<BaseAggregateRoot<*>>() {

    override fun onAfterSave(event: AfterSaveEvent<BaseAggregateRoot<*>>) {
        val aggregateRoot = event.source
        aggregateRoot.listDomainEvents().forEach { event -> publisher.publishEvent(event) }
        aggregateRoot.clearDomainEvents()
    }
}