package com.example.webfluxapisample;

import com.example.webfluxapisample.models.Address;
import com.example.webfluxapisample.models.Contact;
import com.example.webfluxapisample.models.Person;
import com.example.webfluxapisample.models.Phone;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

@SpringBootApplication
@Slf4j
public class WebfluxApiSampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebfluxApiSampleApplication.class, args);
	}
	@Bean
	RouterFunction<ServerResponse> routerFunction() {
		return RouterFunctions
				.route()
				.path("/person", builder -> builder
						.GET("", request -> ServerResponse.noContent().build())
						.POST("", this::persistPerson)
						.GET("{id}", request -> ServerResponse.noContent().build())
						.PUT("{id}", request -> ServerResponse.noContent().build())
						.DELETE("{id}", request -> ServerResponse.noContent().build())
				)
				.path("/array", builder -> builder
						.POST("", this::receivedAndReturnArray))
				.path("/file", builder -> builder
						.POST("/upload", contentType(MediaType.MULTIPART_FORM_DATA),this::fileUpload)
						.GET("/view/{fileName}", this::viewFile)
						.GET("/download/{fileName}", this::downloadFile))
				.build();

	}

	private Mono<ServerResponse> downloadFile(ServerRequest request) {
		var fileName = request.pathVariable("fileName");
		Path ipPath = Paths.get("/tmp/upload/" + fileName);

		if(!Files.exists(ipPath)) {
			return ServerResponse.notFound().build();
		}

		var stringFlux = Flux.fromStream(Stream.of(new File("/tmp/upload/" + fileName)))
				.map(file -> {
					try {
						return Files.readAllBytes(file.toPath());
					} catch (Exception e) {
						return new byte[0];
					}
				});

		return ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
				.body(stringFlux, byte[].class);
	}

	private Mono<ServerResponse> viewFile(ServerRequest request) {
		var fileName = request.pathVariable("fileName");
		Path ipPath = Paths.get("/tmp/upload/" + fileName);

		if(!Files.exists(ipPath)) {
			return ServerResponse.notFound().build();
		}

		var stringFlux = Flux.fromStream(Stream.of(new File("/tmp/upload/" + fileName)))
				.publishOn(Schedulers.boundedElastic())
				.map(file -> {
					try {
						return Files.readAllBytes(file.toPath());
					} catch (Exception e) {
						return new byte[0];
					}
				});

		return ServerResponse
				.ok()
				.contentType(MediaType.IMAGE_JPEG)
				.body(stringFlux, byte[].class);
	}

	private Mono<ServerResponse> fileUpload(ServerRequest request) {
		return request
				.multipartData()
				.flatMap(part -> {
					var stringPartMap= part.toSingleValueMap();
					var filePart = (FilePart) stringPartMap.get("files");

					if(Objects.isNull(filePart)) {
						return Mono.error(new RuntimeException("Invalid File"));
					}

					var fileName = filePart.filename();
					var fileExtn = fileName.substring(fileName.indexOf(".")+1);

					Stream.of("png", "jpg", "jpeg")
							.filter(s -> s.equalsIgnoreCase(fileExtn))
							.findFirst()
							.orElseThrow(() -> new RuntimeException("Invalid File Type"));

					//create thumnail

					/*try {
						var image = ImageIO.read(filePart.content());
						var thumbnail = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
						var graphics = thumbnail.createGraphics();
						graphics.drawImage(image, 0, 0, 100, 100, null);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ImageIO.write(thumbnail, "jpg", baos);
						baos.flush();
						var imageInByte = baos.toByteArray();
						baos.close();
						Files.write(Path.of("/tmp/upload/thumbnail_" + fileName), imageInByte);
					} catch (IOException e) {
						e.printStackTrace();
					}*/

					return filePart.transferTo(Path.of("/tmp/upload/" + fileName));


				})
				.then(ServerResponse.ok().bodyValue("File Uploaded Successfully"));
	}

	private Mono<ServerResponse> receivedAndReturnArray(ServerRequest request) {
		return request
				.bodyToMono(Person[].class)
				.flatMap(person -> ServerResponse.ok().bodyValue(person));
	}

	private Mono<ServerResponse> persistPerson(ServerRequest request) {
		return request
				.bodyToMono(Person.class)
				.flatMap(this::dbPersist)
				.flatMap(person -> ServerResponse.ok().bodyValue(person))
				.onErrorResume(Mono::error)
				.doOnSuccess(s -> log.info("persistPerson SUCCESS"))
				.doOnError(e -> log.error("persistPerson FAILED"));
	}

	private Mono<Person> dbPersist(Person person) {

		//To Convert LinkedHashMap to POJO
//		POJO pojo = mapper.convertValue(singleObject, POJO.class);
//		// or:
//		List<POJO> pojos = mapper.convertValue(listOfObjects, new TypeReference<List<POJO>>() { });


		var phoneAndAddress = new ObjectMapper().convertValue(person.getContacts(), new TypeReference<List<Contact>>() {});
		phoneAndAddress.stream().forEach(d -> {
			if(d instanceof Address) {
				System.out.println("Address");
			}
			else if(d instanceof Phone) {
				System.out.println("Phone");
			}
			System.out.println(d);
		});

		person
				.getContacts()
				.stream()
				.forEach(System.out::println);

		return Mono.fromCallable(() -> person)	;
	}
}

