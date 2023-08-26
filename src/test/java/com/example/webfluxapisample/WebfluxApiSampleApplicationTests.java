package com.example.webfluxapisample;

import com.example.webfluxapisample.models.Address;
import com.example.webfluxapisample.models.Contact;
import com.example.webfluxapisample.models.ContactType;
import com.example.webfluxapisample.models.Person;
import com.example.webfluxapisample.models.Phone;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.LinkedList;
import java.util.List;

@SpringBootTest
@AutoConfigureWebTestClient
class WebfluxApiSampleApplicationTests {
	@Autowired
	WebTestClient webTestClient;
	@Test
	void contextLoads() {
	}

	@Test
	@DisplayName("/person post Test")
	void test01() {
		webTestClient
				.post()
				.uri("/person")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
												{
						        "id": 1,
						        "name": "john Doe",
						        "age": 30,
						        "contacts": [
						          {
						          	"@type": "Address",
						            "contactType": "ADDRESS",
						            "address1": "a",
						            "address2": "London"
						          },
						          {
						          	"@type": "Address",
						            "contactType": "ADDRESS",
						            "address1": "Khau Gali 15",
						            "address2": "Delhi"
						          },
						          {
						          	"@type": "Phone",
						            "contactType": "PHONE",
						            "number": "+91 1234567890"
						          },
						          {
						          	"@type": "Phone",
						            "contactType": "PHONE",
						            "number": "+91 0987654321"
						          }
						        ]
						      }
						""")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.json("""
					{
						"id": 1,
						"name": "john Doe",
						"age": 30,
						"contacts": [
							{
								"contactType": "ADDRESS",
								"address1": "a",
								"address2": "London"
							},
							{
								"contactType": "ADDRESS",
								"address1": "Khau Gali 15",
								"address2": "Delhi"
							},
							{
								"contactType": "PHONE",
								"number": "+91 1234567890"
							},
							{
								"contactType": "PHONE",
								"number": "+91 0987654321"
							}
						]
					}
				""");
	}
}
