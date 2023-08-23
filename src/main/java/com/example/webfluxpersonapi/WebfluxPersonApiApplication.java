package com.example.webfluxpersonapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@SpringBootApplication
@Slf4j
public class WebfluxPersonApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebfluxPersonApiApplication.class, args);
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
				.contacts()
				.stream()
				.forEach(p -> log.info("contacts - {}", p.data()));

		return Mono.fromCallable(() -> person);
	}

}

enum ContactType{
	ADDRESS, PHONE
}
record Phone(int number, ContactType type){}
record Address(String address1, String address2, ContactType type){}
record Contacts<T extends Address, Phone>(){}
record Person(int id, String name, int age, List<Contacts> contacts){}
