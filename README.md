# Parts API

This is a very simple [spring boot](https://spring.io/projects/spring-boot) app with a single endpoint to get products
and their parts.


## Build

This uses maven. The [maven wrapper](https://maven.apache.org/wrapper/) is included in the project. 
It should be a simple matter of running `./mvnw [targets]`.

`./mvnw spring-boot:run` should build and run the application, which runs on port `8080`. You can adjust `server.port`
in `application.properties` if you need to run on another port.

## Database

The application connects to a postgresql database running in a docker container. You can run it with
[Docker compose](https://docs.docker.com/compose/)

The application includes `spring-boot-docker-compose` as an optional dependency (see 
[the docs](https://docs.spring.io/spring-boot/reference/features/dev-services.html#features.dev-services.docker-compose)).

This _should_ bring up the postgresql database (on port 6543) automatically when the application starts (this can be
disabled by uncommenting the relevant property in `application.properties`). The database is automatically populated 
via the postgresql image's built-in initialization.

You can also bring it up manually by running `docker compose up` and shut it down with `docker compose down`.

## Endpoint

The application has only one endpoint, `/product`. It takes two optional query parameters, `last_id` and `page_size`.
At most `page_size` products will be returned in each request. The default `page_size` is (`10`), and the maximum value 
allowed is 100. Products are ordered by their `id`. You can get to the next page of products by providing the `id` of 
the last product in the previous page as the value of `last_id`.

For example, `/product?page_size=1` should give you product `1287` and `/product?page_size=1&last_id=1278` should give
you product `1310`.

The following may be useful to verify the endpoint (assuming you have [curl](https://curl.se/) and 
[jq](https://jqlang.github.io/jq/) installed):

Get the total number of products (should be `28`):
```shell
curl 'localhost:8080/product?page_size=100' | jq '.[].id' | wc -l
```

Get the total number of parts (should be `444`):
```shell
curl 'localhost:8080/product?page_size=100' | jq '.[].parts[].partNumber' | wc -l
```

## Future improvements

* Tests. The DAO should have tests that run against a live database. The API should have some tests (including one that
verifies pagination).
* More endpoints. Individual product and part might be useful.
* Database persistence. It might be a good idea to add a volume for the data so that changes aren't lost between runs.
That would only really make sense if we were changing the database and wanted to keep those changes around.
* Environments. If we were deploying this to different environments, we'd want to have separate properties for each
environment, setting the active profile appropriately.
