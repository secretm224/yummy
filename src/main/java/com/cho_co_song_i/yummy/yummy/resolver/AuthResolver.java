package com.cho_co_song_i.yummy.yummy.resolver;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class AuthResolver {

    @SchemaMapping(typeName = "Query", field = "hello")
    public String getHello() {
        return "안녕하세요, GraphQL!";
    }

    @SchemaMapping(typeName = "Query", field = "helloWithName")
    public String getHelloWithName(@Argument("name") String name) {
        return "안녕하세요, " + (name != null ? name : "손님") + "!";
    }

}
