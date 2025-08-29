package com.textneckhub.alter;

import com.slack.api.Slack;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"slack.token=test-token", "slack.channel=test-channel"})
class AlterApplicationTests {

	@MockBean
	private Slack slack;

	@Test
	void contextLoads() {
	}

}
