package ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.extract;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.models.MetaMessage;

/**
 * @author juliwolf
 */

@Configuration
public class KafkaConsumerConfiguration {
  @Value(value = "${spring.kafka.properties.sasl.mechanism}")
  private String saslMechanism;

  @Value(value = "${spring.kafka.properties.security.protocol}")
  private String securityProtocol;

  @Value(value = "${spring.kafka.properties.serverHost}")
  private String serverHost;

  @Value("${spring.kafka.properties.ssl.truststore.location}")
  private String truststoreLocation;

  @Value("${spring.kafka.properties.ssl.truststore.password}")
  private String truststorePassword;

  @Value("${spring.kafka.properties.username}")
  private String username;

  @Value("${spring.kafka.properties.password}")
  private String password;

  public Map<String, Object> consumerProps () {
    Map<String, Object> props = new HashMap<>();

    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverHost + ":9091");

    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
    props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
    props.put(SaslConfigs.SASL_JAAS_CONFIG, getSaslJaasConfig());
    props.put("ssl.truststore.location", truststoreLocation);
    props.put("ssl.truststore.password", truststorePassword);

    return props;
  }

  @Bean
  public ConsumerFactory<String, MetaMessage> metaConsumerFactory () {
    Map<String, Object> props = consumerProps();
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

    return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(MetaMessage.class, false));
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, MetaMessage> metaTopicListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, MetaMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(metaConsumerFactory());
    return factory;
  }

  @Bean
  public ConsumerFactory<String, GenericRecord> consumerWithAvroFactory () {
    Map<String, Object> props = consumerProps();
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);

    props.put("schema.registry.url", "https://" + serverHost + ":443");
    props.put("schema.registry.basic.auth.credentials.source", "SASL_INHERIT");
    props.put("schema.registry.ssl.truststore.location", truststoreLocation);
    props.put("schema.registry.ssl.truststore.password", truststorePassword);
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "8000");

    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, GenericRecord> avroListenerContainerFactory () {
    ConcurrentKafkaListenerContainerFactory<String, GenericRecord> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerWithAvroFactory());
    return factory;
  }

  private String getSaslJaasConfig () {
    return "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"" + username + "\" password=\"" + password + "\";";
  }
}
