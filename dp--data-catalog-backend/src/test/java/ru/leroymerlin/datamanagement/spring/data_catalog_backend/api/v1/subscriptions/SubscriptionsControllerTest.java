package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions;

import java.net.HttpURLConnection;
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
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.exceptions.InvalidCronExpressionException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.exceptions.SubscriptionNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.get.GetSubscriptionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.get.GetSubscriptionsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PatchSubscriptionRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PatchSubscriptionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PostSubscriptionRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PostSubscriptionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author juliwolf
 */

@WebMvcTest(SubscriptionsController.class)
@Import(SubscriptionsController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class SubscriptionsControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SubscriptionsService subscriptionsService;

  @Test
  @WithMockCustomUser
  public void createSubscriptionEmptyUserIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/subscriptions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"user_id\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_FORBIDDEN))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createSubscriptionEmptyAssetIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/subscriptions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"user_id\": \""+ new UUID(5, 5)+"\", \"asset_id\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset_id is empty\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createSubscriptionInvalidAssetIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/subscriptions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"user_id\": \""+ new UUID(5, 5)+"\", \"asset_id\": \"123\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid request params\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createSubscriptionEmptyNotificationScheduleTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/subscriptions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"user_id\": \""+ new UUID(5, 5)+"\", \"asset_id\": \"" + UUID.randomUUID() + "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"notification_schedule is empty\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createSubscriptionUserNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/subscriptions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"user_id\": \""+ new UUID(5, 5)+"\", \"asset_id\": \"" + UUID.randomUUID() + "\", \"notification_schedule\": \"* */5 * * *\" }");

      when(subscriptionsService.createSubscription(any(PostSubscriptionRequest.class), any(User.class)))
        .thenThrow(UserNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createSubscriptionAssetNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/subscriptions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"user_id\": \""+ new UUID(5, 5)+"\", \"asset_id\": \"" + UUID.randomUUID() + "\", \"notification_schedule\": \"* */5 * * *\" }");

      when(subscriptionsService.createSubscription(any(PostSubscriptionRequest.class), any(User.class)))
        .thenThrow(AssetNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createSubscriptionInvalidCronExpressionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/subscriptions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"user_id\": \""+ new UUID(5, 5)+"\", \"asset_id\": \"" + UUID.randomUUID() + "\", \"notification_schedule\": \"* */5 * * *\" }");

      when(subscriptionsService.createSubscription(any(PostSubscriptionRequest.class), any(User.class)))
        .thenThrow(InvalidCronExpressionException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid cron expression\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createSubscriptionSubscriptionAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/subscriptions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"user_id\": \""+ new UUID(5, 5)+"\", \"asset_id\": \"" + UUID.randomUUID() + "\", \"notification_schedule\": \"* */5 * * *\" }");

      when(subscriptionsService.createSubscription(any(PostSubscriptionRequest.class), any(User.class)))
        .thenThrow(DataIntegrityViolationException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Subscription with these parameters already exists.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createSubscriptionSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/subscriptions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"user_id\": \""+ new UUID(5, 5)+"\", \"asset_id\": \"" + UUID.randomUUID() + "\", \"notification_schedule\": \"* */5 * * *\" }");

      when(subscriptionsService.createSubscription(any(PostSubscriptionRequest.class), any(User.class)))
        .thenReturn(new PostSubscriptionResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateSubscriptionInvalidSubscriptionIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/subscriptions/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"notification_schedule\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"invalid subscriptionId\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateSubscriptionNotificationScheduleIsEmptyTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/subscriptions/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"notification_schedule\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"notification_schedule is empty\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateSubscriptionSubscriptionNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/subscriptions/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"notification_schedule\": \"* */5 * * *\" }");

      when(subscriptionsService.updateSubscription(any(UUID.class), any(PatchSubscriptionRequest.class), any(User.class)))
        .thenThrow(SubscriptionNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Subscription not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateSubscriptionInvalidCronTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/subscriptions/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"notification_schedule\": \"* */5 * * *\" }");

      when(subscriptionsService.updateSubscription(any(UUID.class), any(PatchSubscriptionRequest.class), any(User.class)))
        .thenThrow(InvalidCronExpressionException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid request params\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateSubscriptionSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/subscriptions/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"notification_schedule\": \"* */5 * * *\" }");

      when(subscriptionsService.updateSubscription(any(UUID.class), any(PatchSubscriptionRequest.class), any(User.class)))
        .thenReturn(new PatchSubscriptionResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getSubscriptionByIdInvalidSubscriptionIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/subscriptions/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"invalid subscriptionId\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getSubscriptionByIdSubscriptionNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/subscriptions/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(subscriptionsService.getSubscriptionById(any(UUID.class)))
        .thenThrow(SubscriptionNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Subscription not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getSubscriptionByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/subscriptions/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(subscriptionsService.getSubscriptionById(any(UUID.class)))
        .thenReturn(new GetSubscriptionResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getSubscriptionsByParamsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/subscriptions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(subscriptionsService.getSubscriptionsByParams(nullable(UUID.class), nullable(UUID.class), nullable(List.class),nullable(List.class), nullable(List.class), nullable(SortField.class), nullable(SortOrder.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetSubscriptionsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteSubscriptionByIdInvalidSubscriptionIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/subscriptions/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"invalid subscriptionId\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteSubscriptionByIdSubscriptionNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/subscriptions/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(SubscriptionNotFoundException.class).doNothing()
        .when(subscriptionsService).deleteSubscriptionById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Subscription not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteSubscriptionByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/subscriptions/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing()
        .when(subscriptionsService).deleteSubscriptionById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
