package com.example.webApiClient;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class CustomInserter<T> implements BodyInserter<T, ReactiveHttpOutputMessage> {

    private T body;

    private CustomInserter(T body) {
        this.body = body;
    }

    public static <T> CustomInserter<T> fromValue(T body) {
        return new CustomInserter<T>(body);
    }

    public T getBody() {
        return this.body;
    }

    @Override
    public Mono<Void> insert(ReactiveHttpOutputMessage outputMessage, Context context) {
        Mono<T> publisher = Mono.just(this.body);
        MediaType mediaType = outputMessage.getHeaders().getContentType();
        ResolvableType bodyType = ResolvableType.forInstance(this.body);
        return context.messageWriters().stream()
                .filter(messageWriter -> messageWriter.canWrite(bodyType, mediaType))
                .findFirst()
                .map(item -> (HttpMessageWriter<T>) item)
                .map(writer -> this.write(publisher, bodyType, mediaType, outputMessage, context, writer))
                .orElseGet(() -> Mono.error(unsupportedError(bodyType, context, mediaType)));
    }

    private Mono<Void> write(Publisher<? extends T> input, ResolvableType type,
                             @Nullable MediaType mediaType, ReactiveHttpOutputMessage message,
                             Context context, HttpMessageWriter<T> writer) {

        return context.serverRequest()
                .map(request -> {
                    ServerHttpResponse response = (ServerHttpResponse) message;
                    return writer.write(input, type, type, mediaType, request, response, context.hints());
                })
                .orElseGet(() -> writer.write(input, type, mediaType, message, context.hints()));
    }

    private UnsupportedMediaTypeException unsupportedError(ResolvableType bodyType,
                                                           Context context, @Nullable MediaType mediaType) {

        List<MediaType> supportedMediaTypes = context.messageWriters().stream()
                .flatMap(reader -> reader.getWritableMediaTypes(bodyType).stream())
                .collect(Collectors.toList());

        return new UnsupportedMediaTypeException(mediaType, supportedMediaTypes, bodyType);
    }
}
