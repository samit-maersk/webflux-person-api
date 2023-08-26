package com.example.webfluxapisample;

import com.example.webfluxapisample.models.Contact;
import com.example.webfluxapisample.models.Person;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

		//To Convert LinkedHashMap to POJO
//		POJO pojo = mapper.convertValue(singleObject, POJO.class);
//		// or:
//		List<POJO> pojos = mapper.convertValue(listOfObjects, new TypeReference<List<POJO>>() { });


		var phoneAndAddress = new ObjectMapper().convertValue(person.getContacts(), new TypeReference<List<Contact>>() {});
		phoneAndAddress.stream().forEach(System.out::println);

		person
				.getContacts()
				.stream()
				.forEach(System.out::println);

		return Mono.fromCallable(() -> person)	;
	}
}

