package ru.ulstu.referencebot.telegram;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "telegram.enabled", havingValue = "true")
public class TelegramPolling {
    private static final Logger LOG = LoggerFactory.getLogger(TelegramPolling.class);
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper json;
    private final TelegramMenuService menu;
    private final String token;
    private long offset;

    public TelegramPolling(ObjectMapper json, TelegramMenuService menu,
            @Value("${TELEGRAM_BOT_TOKEN:}") String token) {
        if (!token.matches("[0-9]+:[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("Set TELEGRAM_BOT_TOKEN before enabling Telegram.");
        }
        this.json = json;
        this.menu = menu;
        this.token = token;
    }

    private JsonNode call(String method, Object body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.telegram.org/bot" + token + "/" + method))
                .timeout(Duration.ofSeconds(35)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode result = json.readTree(response.body());
        if (response.statusCode() != 200 || !result.path("ok").asBoolean()) {
            throw new IllegalStateException("Telegram request failed");
        }
        return result.path("result");
    }

    @Scheduled(fixedDelay = 3000)
    public void poll() {
        try {
            JsonNode updates = call("getUpdates", Map.of("offset", offset, "timeout", 20,
                    "allowed_updates", new String[] {"message", "callback_query"}));
            for (JsonNode update : updates) {
                JsonNode callback = update.path("callback_query");
                if (!callback.isMissingNode()) {
                    call("answerCallbackQuery", Map.of("callback_query_id", callback.path("id").asText()));
                    JsonNode message = callback.path("message");
                    if (isPrivate(message)) {
                        long userId = callback.path("from").path("id").asLong();
                        send(message.path("chat").path("id").asLong(),
                                menu.onCallback(userId, callback.path("data").asText(null)));
                    }
                } else {
                    JsonNode message = update.path("message");
                    if (isPrivate(message)) {
                        long userId = message.path("from").path("id").asLong();
                        String text = message.has("text") ? message.path("text").asText() : null;
                        send(message.path("chat").path("id").asLong(), menu.onText(userId, text));
                    }
                }
                offset = update.path("update_id").asLong() + 1;
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (Exception exception) {
            // Do not log exception messages: HTTP errors can contain the secret bot URL.
            LOG.warn("Telegram unavailable; retrying. Check token, network and absence of another polling instance.");
        }
    }

    private boolean isPrivate(JsonNode message) {
        return message.has("chat") && "private".equals(message.path("chat").path("type").asText());
    }

    private void send(long chatId, TelegramMenuService.Reply reply) throws Exception {
        call("sendMessage", Map.of("chat_id", chatId, "text", reply.text(), "reply_markup", reply.markup()));
    }
}
