package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions.UsernameAlreadyExistsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.structUnits.exceptions.StructUnitNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.SearchField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUserRolesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUsersResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PatchUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PostOrPatchUserRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PostUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.UserWorkStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.ObjectUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author JuliWolf
 */
@WebMvcTest(UsersController.class)
@Import(UsersController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class UsersControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UsersService usersService;

  @Test
  @WithMockCustomUser
  public void createUser_EmptyRequiredFields_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"username\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUser_UserAlreadyExists_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"username\": \"some username\" }");

      when(usersService.createUser(any(PostOrPatchUserRequest.class), any(User.class)))
        .thenThrow(DataIntegrityViolationException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUser_LongUserName_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"username\": \"" + StringUtils.repeat('1', 31) + "\", \"user_work_status\": \"ACTIVE\" }");

      when(usersService.createUser(any(PostOrPatchUserRequest.class), any(User.class)))
        .thenThrow(new InvalidFieldLengthException("username", 30));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"username contains too much symbols. Allowed limit is 30\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUser_LongEmail_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"username\": \"some name\", \"email\": \"" + StringUtils.repeat('1', 61)+ "\", \"user_work_status\": \"ACTIVE\" }");

      when(usersService.createUser(any(PostOrPatchUserRequest.class), any(User.class)))
        .thenThrow(new InvalidFieldLengthException("email", 60));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"email contains too much symbols. Allowed limit is 60\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUser_LongFirstName_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"username\": \"some name\", \"first_name\": \"" + StringUtils.repeat('1', 256)+ "\", \"user_work_status\": \"ACTIVE\" }");

      when(usersService.createUser(any(PostOrPatchUserRequest.class), any(User.class)))
        .thenThrow(new InvalidFieldLengthException("first_name", 255));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"first_name contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUser_LongSecondName_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"username\": \"some name\", \"last_name\": \"" + StringUtils.repeat('1', 256)+ "\", \"user_work_status\": \"ACTIVE\" }");

      when(usersService.createUser(any(PostOrPatchUserRequest.class), any(User.class)))
        .thenThrow(new InvalidFieldLengthException("last_name", 255));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"last_name contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUser_Success_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"username\": \"some username\", \"user_work_status\": \"ACTIVE\" }");

      when(usersService.createUser(any(PostOrPatchUserRequest.class), any(User.class)))
        .thenReturn(new PostUserResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUsersBulk_SomeRequiredFieldsAreEmptyException_Test () {
    try {
      List<PostOrPatchUserRequest> requests = new ArrayList<>();
      PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest(null, "firstName", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
      PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, null, null);
      requests.add(firstRequest);
      requests.add(secondRequest);

      ObjectMapper objectMapper = new ObjectMapper();
      String stringRequest = objectMapper.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users/bulk")
        .contentType(MediaType.APPLICATION_JSON)
        .content(stringRequest);

      when(usersService.createUsersBulk(any(List.class), any(User.class)))
        .thenThrow(new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(firstRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Some of required fields are empty.\",\"details\":" + objectMapper.writeValueAsString(firstRequest)+ "}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUsersBulk_InvalidFieldLengthException_Test () {
    try {
      List<PostOrPatchUserRequest> requests = new ArrayList<>();
      PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest("some new name", StringUtils.repeat("*", 256), "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
      PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, null, null);
      requests.add(firstRequest);
      requests.add(secondRequest);

      ObjectMapper objectMapper = new ObjectMapper();
      String stringRequest = objectMapper.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users/bulk")
        .contentType(MediaType.APPLICATION_JSON)
        .content(stringRequest);

      when(usersService.createUsersBulk(any(List.class), any(User.class)))
        .thenThrow(new InvalidFieldLengthException(ObjectUtils.convertObjectToMap(firstRequest), "first_name", 255));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"first_name contains too much symbols. Allowed limit is 255\",\"details\":" + objectMapper.writeValueAsString(firstRequest)+ "}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUsersBulk_InvalidStructUnitId_Test () {
    try {
      List<PostOrPatchUserRequest> requests = new ArrayList<>();
      PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest("some new name", "first name", "second name", "test@test.com", null, "123", UserWorkStatus.ACTIVE, null);
      PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, null, null);
      requests.add(firstRequest);
      requests.add(secondRequest);

      ObjectMapper objectMapper = new ObjectMapper();
      String stringRequest = objectMapper.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users/bulk")
        .contentType(MediaType.APPLICATION_JSON)
        .content(stringRequest);

      when(usersService.createUsersBulk(any(List.class), any(User.class)))
        .thenThrow(new IllegalArgumentException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid params in request\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUsersBulk_DuplicateValueInRequestException_Test () {
    try {
      List<PostOrPatchUserRequest> requests = new ArrayList<>();
      PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest("username", "first name", "second name", "test@test.com", null, null, UserWorkStatus.ACTIVE, null);
      PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, null, null);
      requests.add(firstRequest);
      requests.add(secondRequest);

      ObjectMapper objectMapper = new ObjectMapper();
      String stringRequest = objectMapper.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users/bulk")
        .contentType(MediaType.APPLICATION_JSON)
        .content(stringRequest);

      when(usersService.createUsersBulk(any(List.class), any(User.class)))
        .thenThrow(new DuplicateValueInRequestException("Duplicate username in request", ObjectUtils.convertObjectToMap(firstRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Duplicate username in request\",\"details\":" + objectMapper.writeValueAsString(firstRequest)+ "}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUsersBulk_StructUnitNotFoundException_Test () {
    try {
      List<PostOrPatchUserRequest> requests = new ArrayList<>();
      PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest("username", "first name", "second name", "test@test.com", null, UUID.randomUUID().toString(), UserWorkStatus.ACTIVE, null);
      PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, null, null);
      requests.add(firstRequest);
      requests.add(secondRequest);

      ObjectMapper objectMapper = new ObjectMapper();
      String stringRequest = objectMapper.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users/bulk")
        .contentType(MediaType.APPLICATION_JSON)
        .content(stringRequest);

      when(usersService.createUsersBulk(any(List.class), any(User.class)))
        .thenThrow(new StructUnitNotFoundException(ObjectUtils.convertObjectToMap(firstRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Struct unit not found\",\"details\":" + objectMapper.writeValueAsString(firstRequest)+ "}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUsersBulk_UsernameAlreadyExistsException_Test () {
    try {
      List<PostOrPatchUserRequest> requests = new ArrayList<>();
      PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest("new username", "first name", "second name", "test@test.com", null, UUID.randomUUID().toString(), UserWorkStatus.ACTIVE, null);
      PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, null, null);
      requests.add(firstRequest);
      requests.add(secondRequest);

      ObjectMapper objectMapper = new ObjectMapper();
      String stringRequest = objectMapper.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users/bulk")
        .contentType(MediaType.APPLICATION_JSON)
        .content(stringRequest);

      PostOrPatchUserRequest postRequest = new PostOrPatchUserRequest();
      postRequest.setUsername("new username");
      when(usersService.createUsersBulk(any(List.class), any(User.class)))
        .thenThrow(new UsernameAlreadyExistsException(new DataIntegrityViolationException("Detail: Key (username, deleted_flag)=(new username, f) already exists.")));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"User with this username already exists.\",\"details\":" + objectMapper.writeValueAsString(postRequest)+ "}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUsersBulk_Success_Test () {
    try {
      List<PostOrPatchUserRequest> requests = new ArrayList<>();
      PostOrPatchUserRequest firstRequest = new PostOrPatchUserRequest("new username", "first name", "second name", "test@test.com", null, UUID.randomUUID().toString(), UserWorkStatus.ACTIVE, null);
      PostOrPatchUserRequest secondRequest = new PostOrPatchUserRequest("username", "firstName", "second name", "test@test.com", null, null, null, null);
      requests.add(firstRequest);
      requests.add(secondRequest);

      ObjectMapper objectMapper = new ObjectMapper();
      String stringRequest = objectMapper.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/users/bulk")
        .contentType(MediaType.APPLICATION_JSON)
        .content(stringRequest);

      PostOrPatchUserRequest postRequest = new PostOrPatchUserRequest();
      postRequest.setUsername("new username");
      when(usersService.createUsersBulk(any(List.class), any(User.class)))
        .thenReturn(new ArrayList());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateUser_EmptyBody_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/users/" + UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"username\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Empty request body\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateUser_InvalidUserId_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/users/123" + UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"username\": \"some new name\", \"user_work_status\": \"ACTIVE\" }");

      when(usersService.updateUser(any(UUID.class), any(PostOrPatchUserRequest.class), any(User.class)))
        .thenThrow(IllegalArgumentException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid params\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateUser_UserNotFoundException_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/users/" + UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"username\": \"some new name\" }");

      when(usersService.updateUser(any(UUID.class), any(PostOrPatchUserRequest.class), any(User.class)))
        .thenThrow(UserNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"User not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateUser_StructUnitNotFoundException_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/users/" + UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"username\": \"some new name\" }");

      when(usersService.updateUser(any(UUID.class), any(PostOrPatchUserRequest.class), any(User.class)))
        .thenThrow(StructUnitNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Struct unit not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateUser_UsernameAlreadyExists_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/users/" + UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"username\": \"some new name\", \"user_work_status\": \"ACTIVE\" }");

      when(usersService.updateUser(any(UUID.class), any(PostOrPatchUserRequest.class), any(User.class)))
        .thenThrow(DataIntegrityViolationException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"User with this username already exists.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateUser_Success_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/users/" + UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"username\": \"some new name\", \"user_work_status\": \"ACTIVE\" }");

      when(usersService.updateUser(any(UUID.class), any(PostOrPatchUserRequest.class), any(User.class)))
        .thenReturn(new PatchUserResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getUsersByParamsEmptyParamsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/users")
        .contentType(MediaType.APPLICATION_JSON);

      when(usersService.getUsersByParams(any(String.class), any(SearchField.class), any(SortField.class), any(SortOrder.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetUsersResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getUsersByParamsInvalidSetParamsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/users?search_field=123")
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getUserByIdInvalidUserIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/users/123")
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested user not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getUserByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/users/" + UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON);

      when(usersService.getUserById(any(UUID.class))).thenReturn(new GetUserResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getUserRolesInvalidUserIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/users/123" + UUID.randomUUID() + "/roles")
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested user not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getUserRolesSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/users/" + UUID.randomUUID() + "/roles")
        .contentType(MediaType.APPLICATION_JSON);

      when(usersService.getUserRoles(any(UUID.class))).thenReturn(new GetUserRolesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteUserByIdInvalidUserIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/users/123" + UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested user not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteUserByIdUserNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/users/" + UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(UserNotFoundException.class).doNothing().
      when(usersService).deleteUserById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested user not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteUserByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/users/" + UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().
        when(usersService).deleteUserById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
