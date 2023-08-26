package com.example.webfluxapisample.models;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class Address extends Contact {
    public Address() {}
    ContactType contactType;
    String address1;
    String address2;
}
