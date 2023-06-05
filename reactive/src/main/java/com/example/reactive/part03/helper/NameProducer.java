package com.example.reactive.part03.helper;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.function.Consumer;

/**
 * @author JuliWolf
 * @date 05.06.2023
 */
public class NameProducer implements Consumer<FluxSink<String>> {
  private FluxSink<String> fluxSink;

  @Override
  public void accept(FluxSink<String> stringFluxSink) {
    this.fluxSink = stringFluxSink;
  }


  public void produce () {
    String name = Util.faker().name().fullName();
    String thread = Thread.currentThread().getName();
    this.fluxSink.next(thread + " : " +name);
  }
}
