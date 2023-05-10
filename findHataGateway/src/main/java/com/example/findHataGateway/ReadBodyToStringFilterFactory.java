package com.example.findHataGateway;


import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.stereotype.Component;

@Component
class ReadBodyToStringFilterFactory extends AbstractGatewayFilterFactory<ReadBodyToStringFilterFactory.Config> {

    public ReadBodyToStringFilterFactory() {
        super(Config.class);
    }

    public GatewayFilter apply(final Config config) {
        return (exchange, chain) -> ServerWebExchangeUtils.cacheRequestBody(exchange,
                (serverHttpRequest) -> chain.filter(exchange.mutate().request(serverHttpRequest).build()));
    }

    public static class Config {
    }
}