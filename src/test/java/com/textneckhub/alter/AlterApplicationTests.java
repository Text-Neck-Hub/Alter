package com.textneckhub.alter;

import com.slack.api.Slack;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class AlterApplicationTests {

	@MockBean
	private Slack slack;

	@Test
	void contextLoads() {
	}

}
