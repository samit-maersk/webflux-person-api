This project will give you an idea about the http request / response mapped to Java POJO

### oneOf scenario
`GET POST PUT DELETE /person` will map `contacts` will receive either address type or phone type.
Take a look on this implementation in the code

```yaml
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
      "contactType": "PHONE",
      "number": "+91 0987654321"
    }
  ]
}
```

## array type request / response

`/array` endpoint describe this scenario for your reference.

```yaml
[
    {
        "name" : "john Doe",
        "age" : 30,
    },
    {
        "name" : "john Doe",
        "age" : 30,
    }
]
```

## file Upload (resize Image for thumbnail) / Download / View

`/file/upload` endpoint describe this scenario for your reference.
along with that there are example for `/file/view/{fileName}` and `/file/download/{fileName}` as well.

```yaml
{
    "files" : "file content in byteArray"
}
```
