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
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.*;
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
						.POST("/upload", contentType(MULTIPART_FORM_DATA),this::fileUpload)
						.GET("/view/{fileName}", this::viewFile)
						.GET("/download/{fileName}", this::downloadFile))
				.POST("/form-data",contentType(MULTIPART_FORM_DATA), this::formData)
				.POST("/x-www-form-urlencoded", contentType(APPLICATION_FORM_URLENCODED), this::formDataUrlEncoded)
				.build();

	}

	private Mono<ServerResponse> formDataUrlEncoded(ServerRequest request) {
		// x-www-form-urlencoded
		return request.formData().flatMap(formData -> {
			return ServerResponse.ok().bodyValue(formData.toSingleValueMap());
		});
	}

	private Mono<ServerResponse> formData(ServerRequest request) {
		// multipart/form-data
		return request
				.multipartData()
				.flatMap(part -> {
					var stringPartMap = part.toSingleValueMap();
					var filePart = (FilePart) stringPartMap.get("files");
					var id = (FormFieldPart) stringPartMap.get("id");
					var fileName = filePart.filename();

					var map = Map.of(
								"id" , id.value() ,
								"fileName", fileName
							);

					return Mono.fromRunnable(() -> System.out.println(map));
//					return Mono.just(map);
				})
				.then(ServerResponse.ok().bodyValue("File Uploaded Successfully"));
				//.flatMap(map -> ServerResponse.ok().bodyValue(map));

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
				.contentType(APPLICATION_OCTET_STREAM)
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
				.contentType(IMAGE_JPEG)
				.body(stringFlux, byte[].class);
	}

	private Mono<ServerResponse> fileUpload(ServerRequest request) {
		return request
				.multipartData()
				.flatMap(part -> {
					var stringPartMap = part.toSingleValueMap();
					var filePart = (FilePart) stringPartMap.get("files");

					if (Objects.isNull(filePart)) {
						return Mono.error(new BadRequestException("Invalid File"));
					}

					var fileName = filePart.filename();
					var fileExtn = fileName.substring(fileName.indexOf(".") + 1);

					Stream.of("png", "jpg", "jpeg")
							.filter(s -> s.equalsIgnoreCase(fileExtn))
							.findFirst()
							.orElseThrow(() -> new BadRequestException("Invalid File Type"));

					Path originalFilePath = Path.of("/tmp/upload/" + fileName);
					Path thumbnailFilePath = Path.of("/tmp/upload/thumbnail_" + fileName);

					return filePart
							.transferTo(originalFilePath)
							.then(Mono.fromCallable(() -> filePart))
							.flatMap(filePart1 ->
									createThumbnail(originalFilePath, thumbnailFilePath)
											.thenReturn(filePart1)
							);

				})
				.then(ServerResponse.ok().bodyValue("File Uploaded Successfully"));
	}

	private Mono<Void> createThumbnail(Path originalFilePath, Path thumbnailFilePath) {
		return Mono.fromCallable(() -> {
					// Load the original image and create a thumbnail
					BufferedImage originalImage = ImageIO.read(originalFilePath.toFile());
					int thumbnailWidth = 150; // Adjust the width as needed
					int thumbnailHeight = (int) ((double) thumbnailWidth / originalImage.getWidth() * originalImage.getHeight());
					BufferedImage thumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
					thumbnail.getGraphics().drawImage(originalImage, 0, 0, thumbnailWidth, thumbnailHeight, null);

					// Save the thumbnail
					ImageIO.write(thumbnail, "jpeg", thumbnailFilePath.toFile());

					return thumbnailFilePath;
				})
				.then();
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

