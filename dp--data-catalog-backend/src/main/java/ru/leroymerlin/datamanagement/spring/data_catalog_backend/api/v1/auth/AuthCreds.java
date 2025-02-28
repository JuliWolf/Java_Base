package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

/**
 * @author JuliWolf
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthCreds implements Request {
  String login;

  String password;
}
