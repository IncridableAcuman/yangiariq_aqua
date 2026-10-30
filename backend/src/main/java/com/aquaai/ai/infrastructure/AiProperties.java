package com.aquaai.ai.infrastructure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private String provider = "mock";
    private GeminiProperties gemini = new GeminiProperties();

    @Getter
    @Setter
    public static class GeminiProperties {
        private String apiKey = "";
        private String model = "gemini-1.5-flash";
    }
}
