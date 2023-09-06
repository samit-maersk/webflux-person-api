package com.example.webfluxapisample.models;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class Phone extends Contact {
    public Phone() {}
    //ContactType contactType;
    String number;
}
