package co.com.crediya.consumer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "adapter.restconsumer")
public class RestConsumerProperties {
    
    private String url;
    private int timeout;
    private Endpoints endpoints = new Endpoints();
    
    @Getter
    @Setter
    public static class Endpoints {
        private String userExists;
        private String userExistsByQuery;
        private String userById;
    }
}
