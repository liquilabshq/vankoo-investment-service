package com.liquilabs.vankoo.investment.infrastructure.configuration;

import com.liquilabs.vankoo.investment.infrastructure.messaging.outbox.OutboxEventRepository;
import com.liquilabs.vankoo.investment.infrastructure.messaging.outbox.OutboxPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.cloud.stream.function.StreamBridge;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StandaloneMessagingConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(context -> context.getBeanFactory()
                    .setConversionService(ApplicationConversionService.getSharedInstance()))
            .withUserConfiguration(OutboxPublisher.class)
            .withBean(OutboxEventRepository.class, () -> mock(OutboxEventRepository.class))
            .withBean(StreamBridge.class, () -> mock(StreamBridge.class))
            .withBean(Clock.class, Clock::systemUTC);

    @Test
    void disablesOnlyThePublisherWhenMessagingIsOff() {
        runner.withPropertyValues("vankoo.investment.messaging.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(OutboxPublisher.class);
            assertThat(context).hasSingleBean(OutboxEventRepository.class);
        });
    }

    @Test
    void preservesExistingLocalPublisherByDefault() {
        runner.run(context -> assertThat(context).hasSingleBean(OutboxPublisher.class));
    }
}
