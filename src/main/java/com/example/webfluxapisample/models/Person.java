package com.example.webfluxapisample.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Person {
    int id;
    String name;
    int age;
    List<? super Contact> contacts;
}
