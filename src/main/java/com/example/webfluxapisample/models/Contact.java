package com.example.webfluxapisample.models;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

// A good tutorial to get referenced from https://www.logicbig.com/tutorials/misc/jackson/json-type-info-with-logical-type-name.html

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "contactType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Address.class, name = "ADDRESS"),
        @JsonSubTypes.Type(value = Phone.class, name = "PHONE") }
)

/* The JSON payload will look like this:
"contacts": [
    {
      "contactType": "ADDRESS",
      "address1": "a",
      "address2": "London"
    },
    {
      "contactType": "PHONE",
      "number": "+91 1234567890"
    }
  ]
 */

//@JsonIgnoreProperties(ignoreUnknown = true)
//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
//@JsonSubTypes({
//        @JsonSubTypes.Type(value = Address.class, name = "Address"),
//        @JsonSubTypes.Type(value = Phone.class, name = "Phone") }
//)
// For this JSON payload will look like this:
/*
"contacts": [
    {
      "@type": "Address",
      "contactType": "ADDRESS",
      "address1": "a",
      "address2": "London"
    },
    {
      "@type": "Phone",
      "contactType": "PHONE",
      "number": "+91 1234567890"
    }
  ]
 */
public abstract class Contact {
    ContactType contactType;
}
