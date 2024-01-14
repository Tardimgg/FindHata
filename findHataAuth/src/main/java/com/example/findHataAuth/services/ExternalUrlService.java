package com.example.findHataAuth.services;

import com.example.findHataAuth.entities.NgrokInformation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import jakarta.annotation.PostConstruct;


@Service
@Slf4j
public class ExternalUrlService {

    @Value("${externalUrl}")
    private String externalUrl;

    @Value("${ngrokMode}")
    private boolean ngrokMode;

    private String val;
    private boolean isInit = false;

    @PostConstruct
    private Mono<String> init() {
        if (ngrokMode) {
            WebClient client = WebClient.create();
            return client.get()
                    .uri("http://tunnel:4040/api/tunnels")
                    .retrieve()
                    .bodyToMono(NgrokInformation.class)
                    .map(information -> {
                        if (information != null && information.getTunnels().size() > 0) {
                            val = information.getTunnels().get(0).getPublicUrl();
                            if (val != null) {
                                return val;
                            }
                        }
                        log.error("failed to get information from ngrok");
                        val = externalUrl;
                        return val;
                    });
        } else {
            val = externalUrl;
            return Mono.just(val);
        }
    }

    public synchronized Mono<String> getExternalUrl() {
        if (!isInit) {
            isInit = true;
            return init();
        }
        return Mono.just(val);
    }

}
