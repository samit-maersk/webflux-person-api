package com.example.webfluxapisample;

import com.example.webfluxapisample.models.Address;
import com.example.webfluxapisample.models.Person;
import com.example.webfluxapisample.models.Phone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Slf4j
public class WebfluxApiSampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebfluxApiSampleApplication.class, args);
	}
	@Bean
	RouterFunction routerFunction() {
		return RouterFunctions
				.route()
				.path("/person", builder -> builder
						.GET("", request -> ServerResponse.noContent().build())
						.POST("", this::persistPerson)
						.GET("{id}", request -> ServerResponse.noContent().build())
						.PUT("{id}", request -> ServerResponse.noContent().build())
						.DELETE("{id}", request -> ServerResponse.noContent().build())
				)
				.build();

	}

	private Mono<ServerResponse> persistPerson(ServerRequest request) {
		return request
				.bodyToMono(Person.class)
				.flatMap(this::dbPersist)
				.flatMap(person -> ServerResponse.ok().bodyValue(person))
				.onErrorResume(throwable -> Mono.error(throwable))
				.doOnSuccess(s -> log.info("persistPerson SUCCESS"))
				.doOnError(e -> log.error("persistPerson FAILED"));
	}

	private Mono<Person> dbPersist(Person person) {

		person
				.getContacts()
				.stream()
				.forEach(System.out::println);

		return Mono.fromCallable(() -> person)	;
	}
}

//record Phone(Integer number,ContactType type) {}
//record Address (String address1, String address2, ContactType type) {}
//record Contact<T>(T data) {}

