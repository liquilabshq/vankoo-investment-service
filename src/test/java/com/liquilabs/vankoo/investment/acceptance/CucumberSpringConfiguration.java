package com.liquilabs.vankoo.investment.acceptance;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring context for the acceptance steps that go through the REST API, such as US11.
 * The steps that only use the domain (US03, US07) keep working without injecting anything.
 */
@CucumberContextConfiguration
@SpringBootTest(properties = "spring.cloud.stream.default-binder=test")
@AutoConfigureMockMvc
@Import(TestChannelBinderConfiguration.class)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}
