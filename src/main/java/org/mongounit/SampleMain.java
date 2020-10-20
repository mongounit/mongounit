package org.mongounit;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import org.assertj.core.util.Files;

public class SampleMain {

  public static void main(String[] args) throws JsonProcessingException {

    File file = new File("/Users/yaakov/persons.json");
    String jsonMongoUnitCollections = Files.contentOf(file, "UTF-8");
    //    System.out.println(jsonMongoUnitCollections);

    //    ObjectMapper mapper = new ObjectMapper();
    //    SimpleModule module = new SimpleModule();
    //    module.addDeserializer(MongoUnitValue.class, new MongoUnitValueDeserializer("$$"));
    //    mapper.registerModule(module);
    //
    //    List<MongoUnitCollection> mongoUnitCollections = mapper.readValue(
    //        jsonMongoUnitCollections,
    //        new TypeReference<List<MongoUnitCollection>>() {
    //        });

    //    System.out.println(mongoUnitCollections);

    System.out.println(MongoUnitUtil.toMongoUnitTypedCollectionsFromJson(jsonMongoUnitCollections));
  }
}
