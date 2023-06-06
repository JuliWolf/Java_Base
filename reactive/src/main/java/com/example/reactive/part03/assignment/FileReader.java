package com.example.reactive.part03.assignment;

import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author JuliWolf
 * @date 06.06.2023
 */
public class FileReader {

  private Callable<BufferedReader> openReader (Path path) {
    return () -> Files.newBufferedReader(path);
  }

  private BiFunction<BufferedReader, SynchronousSink<String>, BufferedReader> read () {
    return (bufferedReader, synchronousSink) -> {
      try {
        String line = bufferedReader.readLine();

        if (Objects.isNull(line)) {
          synchronousSink.complete();
        } else {
          synchronousSink.next(line);
        }
      } catch (IOException e) {
        synchronousSink.error(e);
      }

      return bufferedReader;
    };
  }

  private Consumer<BufferedReader> closeReader () {
    return bufferedReader -> {
      try {
        bufferedReader.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };
  }

  public Flux<String> read (Path path) {
    return Flux.generate(
        openReader(path),
        read(),
        closeReader()
    );
  }
}
