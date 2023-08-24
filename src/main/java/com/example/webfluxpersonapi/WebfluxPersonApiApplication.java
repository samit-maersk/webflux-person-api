package com.example.webfluxpersonapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
		ObjectMapper mapper = new ObjectMapper();
		person
				.getContacts()
				.stream()
				.forEach(p -> {
					log.info("contacts {}", p);
				});

		return Mono.fromCallable(() -> person)	;
	}
}

enum ContactType {
	ADDRESS, PHONE
}

//record Phone(Integer number,ContactType type) {}
//record Address (String address1, String address2, ContactType type) {}
//record Contact<T>(T data) {}

abstract class Contact {
	ContactType contactType;
}

@Setter @Getter @AllArgsConstructor @NoArgsConstructor
class Phone extends Contact {
	String number;
}
@Setter @Getter @AllArgsConstructor @NoArgsConstructor
class Address extends Contact {
	String address1;
	String address2;
}

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
class Person {
	int id;
	String name;
	int age;
	List<? super Contact> contacts;
}