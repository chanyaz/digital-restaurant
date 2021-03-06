package com.drestaurant.courier.domain

import com.drestaurant.common.domain.model.AuditEntry
import org.axonframework.test.saga.FixtureConfiguration
import org.axonframework.test.saga.SagaTestFixture
import org.junit.Before
import org.junit.Test
import java.util.*

class CourierOrderSagaTest {

    private lateinit var testFixture: FixtureConfiguration
    private lateinit var auditEntry: AuditEntry
    private lateinit var orderId: String
    private lateinit var courierId: String
    private val WHO = "johndoe"

    @Before
    fun setUp() {
        testFixture = SagaTestFixture(CourierOrderSaga::class.java)
        auditEntry = AuditEntry(WHO, Calendar.getInstance().time)
        orderId = "orderId"
        courierId = "courierId"
    }

    @Test
    fun courierOrderAssigningInitiatedTest() {

        testFixture.givenNoPriorActivity()
                .whenAggregate(orderId)
                .publishes(CourierOrderAssigningInitiatedEvent(courierId, orderId, auditEntry))
                .expectActiveSagas(1)
                .expectDispatchedCommands(ValidateOrderByCourierCommand(orderId, courierId, auditEntry))
    }

    @Test
    fun courierNotFoundTest() {

        testFixture.givenAggregate(orderId)
                .published(CourierOrderAssigningInitiatedEvent(courierId, orderId, auditEntry))
                .whenPublishingA(CourierNotFoundForOrderEvent(courierId, orderId, auditEntry))
                .expectActiveSagas(0)
                .expectDispatchedCommands(MarkCourierOrderAsNotAssignedCommand(orderId, auditEntry))
    }

    @Test
    fun orderValidatedWithErrorByCourierTest() {

        testFixture.givenAggregate(orderId)
                .published(CourierOrderAssigningInitiatedEvent(courierId, orderId, auditEntry))
                .whenPublishingA(OrderValidatedWithErrorByCourierEvent(courierId, orderId, auditEntry))
                .expectActiveSagas(0)
                .expectDispatchedCommands(MarkCourierOrderAsNotAssignedCommand(orderId, auditEntry))
    }

    @Test
    fun orderValidatedWithSuccessByCourierTest() {

        testFixture.givenAggregate(orderId)
                .published(CourierOrderAssigningInitiatedEvent(courierId, orderId, auditEntry))
                .whenPublishingA(OrderValidatedWithSuccessByCourierEvent(courierId, orderId, auditEntry))
                .expectActiveSagas(0)
                .expectDispatchedCommands(MarkCourierOrderAsAssignedCommand(orderId, courierId, auditEntry))
    }

}
