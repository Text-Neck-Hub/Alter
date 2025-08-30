package com.textneckhub.alter.application.usecase;

import com.textneckhub.alter.domain.model.LogMessage;
import com.textneckhub.alter.domain.port.out.NotifierPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogMessageUseCaseTest {

    @InjectMocks
    private LogMessageUseCase logMessageUseCase;

    @Mock
    private NotifierPort notifierPort;

    private LogMessage createLogMessage(String level) {
        // 네 도메인 시그니처에 맞춰 생성자/빌더 사용
        return new LogMessage("test message", level, "TEST_SERVICE");
    }

    @Nested
    @DisplayName("INFO 레벨 로그가 주어지면")
    class When_Info_Level {

        @Test
        @DisplayName("알림을 보내고 스트림이 정상 완료한다(아이템 방출은 선택적)")
        void givenInfoLogMessage_whenHandle_thenSendNotification_andComplete() {
            // given
            LogMessage logMessage = createLogMessage("INFO");

            // NotifierPort의 반환 제네릭(Mono<Void> 또는 Mono<LogMessage>)과 무관하게 컴파일되도록 캐스팅 사용
            when(notifierPort.sendSlackAlert(any(LogMessage.class)))
                    .thenReturn((Mono) Mono.empty());

            // when
            @SuppressWarnings("unchecked")
            Mono<Object> result = (Mono<Object>) (Mono) logMessageUseCase.handle(logMessage);

            List<Object> emitted = new ArrayList<>();
            StepVerifier.create(result)
                    .recordWith(() -> emitted) // 방출된 아이템(있으면) 수집
                    .thenConsumeWhile(o -> true) // 전부 소비
                    .verifyComplete(); // 완료 신호 필수

            // then
            // 유즈케이스가 Mono<LogMessage>를 반환하는 구현이라면, 원본 메시지가 방출되었는지 추가 확인
            if (!emitted.isEmpty()) {
                assertThat(emitted).hasSize(1);
                assertThat(emitted.get(0)).isEqualTo(logMessage);
            }
            verify(notifierPort).sendSlackAlert(logMessage);
        }
    }

    @Nested
    @DisplayName("DEBUG 레벨 로그가 주어지면")
    class When_Debug_Level {

        @Test
        @DisplayName("알림을 보내지 않고 스트림이 정상 완료한다")
        void givenDebugLogMessage_whenHandle_thenDoNotSendNotification_andComplete() {
            // given
            LogMessage logMessage = createLogMessage("DEBUG");

            // when
            @SuppressWarnings("unchecked")
            Mono<Object> result = (Mono<Object>) (Mono) logMessageUseCase.handle(logMessage);

            StepVerifier.create(result)
                    .verifyComplete(); // 아이템 방출 여부와 무관하게 완료만 확인

            verify(notifierPort, never()).sendSlackAlert(any(LogMessage.class));
        }
    }
}