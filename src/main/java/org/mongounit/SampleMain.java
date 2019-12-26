package org.mongounit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.File;
import java.util.List;
import org.assertj.core.util.Files;
import org.mongounit.model.MongoUnitCollection;
import org.mongounit.model.MongoUnitValue;
import org.mongounit.model.MongoUnitValueDeserializer;

public class SampleMain {

  public static void main(String[] args) throws JsonProcessingException {

    File file = new File("/Users/yaakov/testMongoUnit.json");
    String jsonMongoUnitCollections = Files.contentOf(file, "UTF-8");
    //    System.out.println(jsonMongoUnitCollections);

    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(MongoUnitValue.class, new MongoUnitValueDeserializer("$$"));
    mapper.registerModule(module);

    List<MongoUnitCollection> mongoUnitCollections = mapper.readValue(
        jsonMongoUnitCollections,
        new TypeReference<List<MongoUnitCollection>>() {
        });

    System.out.println(mongoUnitCollections);

  }

}
