package com.example;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mbtest.javabank.Client;
import org.mbtest.javabank.fluent.ImposterBuilder;
import org.mbtest.javabank.http.imposters.Imposter;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = UiDemoApplication.class)
@WebIntegrationTest
public class UiDemoApplicationIT {

	private static final int IMPOSTER_PORT = 8886;
	private static final String TEST_RESPONSE = "IMPOSTER-RESPONSE";

	@Before
	public void before() {
	  this.setupImposters();
	}

	@Test
	public void contextLoads() {
		final String result = new RestTemplate().getForObject("http://localhost:" + IMPOSTER_PORT , String.class);
		assertThat(result, equalTo(TEST_RESPONSE));
	}

	private void setupImposters() {

		if (!Client.isMountebankRunning()) {
			Assert.fail("Mountebank is not running, failing test");
		}

		Client.deleteAllImposters();

		final Imposter imposter = ImposterBuilder.anImposter().onPort(IMPOSTER_PORT)
				.stub()
				.response()
				.is()
				.body(TEST_RESPONSE)
				.statusCode(200)
				.header("Content-Type", "application/text")
				.end()
				.end()
				.predicate()
				.equals()
				.path("/")
				.method("GET")
				.end()
				.end().end().build();

		final int status = Client.createImposter(imposter);
		if (!(201 == status)) {
			throw new RuntimeException("error creating imposter.");
		}
	}

}
