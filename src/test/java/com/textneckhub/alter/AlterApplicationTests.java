package com.textneckhub.alter;

import com.textneckhub.alter.domain.port.out.NotifierPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(properties = {
		"spring.kafka.listener.auto-startup=false", // 리스너 기동 방지(안정화)
		"spring.kafka.consumer.group-id=alter-test" // groupId 더미(안전)
})
@ExtendWith(SpringExtension.class)
public class AlterApplicationTests {

	@MockBean
	private NotifierPort notifier; // 구현체(SlackNotifier) 대신 포트 목킹 → 토큰/채널 없어도 OK

	@Test
	void contextLoads() {
	}
}