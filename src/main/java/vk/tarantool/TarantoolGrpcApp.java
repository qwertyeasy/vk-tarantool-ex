package vk.tarantool;

import io.tarantool.spring.data34.repository.config.EnableTarantoolRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableTarantoolRepositories
public class TarantoolGrpcApp {

  public static void main(String[] args) {
    SpringApplication.run(TarantoolGrpcApp.class, args);
  }

}
