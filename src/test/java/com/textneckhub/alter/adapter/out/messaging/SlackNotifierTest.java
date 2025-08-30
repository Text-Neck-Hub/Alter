package com.textneckhub.alter.adapter.out.messaging;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.textneckhub.alter.domain.model.LogMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlackNotifierTest {

    @InjectMocks
    private SlackNotifier slackNotifier;

    @Mock
    private Slack slack;

    @Mock
    private MethodsClient methodsClient;

    @BeforeEach
    void setUp() {
        // @Value 주입 필드 대체
        ReflectionTestUtils.setField(slackNotifier, "slackToken", "test-token");
        ReflectionTestUtils.setField(slackNotifier, "slackChannel", "test-channel");
        when(slack.methods("test-token")).thenReturn(methodsClient);
    }

    private LogMessage createLogMessage() {
        // 네 도메인 시그니처에 맞게 생성자/빌더 사용
        return new LogMessage("test message", "INFO", "TEST_SERVICE");
    }

    @Nested
    @DisplayName("sendSlackAlert 메소드는")
    class Describe_sendSlackAlert {

        @Nested
        @DisplayName("성공적으로 메시지를 보내면")
        class Context_with_successful_api_call {
            @BeforeEach
            void setUp() throws Exception {
                ChatPostMessageResponse successResponse = new ChatPostMessageResponse();
                successResponse.setOk(true);
                successResponse.setTs("12345.67890");
                when(methodsClient.chatPostMessage(any(ChatPostMessageRequest.class)))
                        .thenReturn(successResponse);
            }

            @Test
            @DisplayName("정상 완료한다(필요 시 방출된 LogMessage도 검증)")
            void it_completes_and_optionally_emits_log_message() throws IOException, SlackApiException {
                LogMessage logMessage = createLogMessage();

                // 반환 타입이 Mono<Void>든 Mono<LogMessage>든 모두 허용
                Mono<Object> mono = slackNotifier.sendSlackAlert(logMessage).cast(Object.class);

                List<Object> emitted = new ArrayList<>();
                StepVerifier.create(mono)
                        .recordWith(() -> emitted)
                        .thenConsumeWhile(o -> true) // 방출된 게 있으면 다 소비
                        .verifyComplete(); // 완료 신호만 필수

                // 만약 아이템을 방출하는 구현(Mono<LogMessage>)이면 그 값이 원본과 같은지 검증
                if (!emitted.isEmpty()) {
                    assertThat(emitted).hasSize(1);
                    assertThat(emitted.get(0)).isEqualTo(logMessage);
                }

                // Slack API 요청 내용 검증
                ArgumentCaptor<ChatPostMessageRequest> requestCaptor = ArgumentCaptor
                        .forClass(ChatPostMessageRequest.class);
                verify(methodsClient).chatPostMessage(requestCaptor.capture());

                ChatPostMessageRequest capturedRequest = requestCaptor.getValue();
                assertThat(capturedRequest.getChannel()).isEqualTo("test-channel");
                assertThat(capturedRequest.getText()).contains(logMessage.getMessage());
                assertThat(capturedRequest.getText()).contains(logMessage.getService());
            }
        }

        @Nested
        @DisplayName("메시지 전송에 실패하면")
        class Context_with_failed_api_call {
            @BeforeEach
            void setUp() throws Exception {
                ChatPostMessageResponse errorResponse = new ChatPostMessageResponse();
                errorResponse.setOk(false);
                errorResponse.setError("api_error");
                when(methodsClient.chatPostMessage(any(ChatPostMessageRequest.class)))
                        .thenReturn(errorResponse);
            }

            @Test
            @DisplayName("RuntimeException을 포함하는 Mono 에러를 반환한다")
            void it_returns_a_mono_error_with_runtime_exception() {
                LogMessage logMessage = createLogMessage();

                StepVerifier.create(slackNotifier.sendSlackAlert(logMessage))
                        .expectError(RuntimeException.class)
                        .verify();
            }
        }
    }
}