package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users;

import java.util.List;
import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions.UsernameAlreadyExistsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.structUnits.exceptions.StructUnitNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.SearchField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUserRolesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUsersResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PatchUserRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PatchUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PostOrPatchUserRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PostUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;

public interface UsersService {
  void deleteUserById(UUID userId, User user) throws UserNotFoundException;

  PostUserResponse createUser (PostOrPatchUserRequest userRequest, User user) throws InvalidFieldLengthException, StructUnitNotFoundException, UsernameAlreadyExistsException;

  GetUsersResponse getUsersByParams (
    String name,
    SearchField searchField,
    SortField sortField,
    SortOrder sortOrder,
    Integer pageNumber,
    Integer pageSize
  );

  GetUserResponse getUserById (UUID userId) throws UserNotFoundException;

  GetUserRolesResponse getUserRoles (UUID userId);

  PatchUserResponse updateUser (UUID userId, PostOrPatchUserRequest userRequest, User user) throws UserNotFoundException, StructUnitNotFoundException;

  List<PostUserResponse> createUsersBulk (
    List<PostOrPatchUserRequest> userRequests,
    User user
  ) throws
    InvalidFieldLengthException,
    StructUnitNotFoundException,
    UsernameAlreadyExistsException,
    DuplicateValueInRequestException,
    SomeRequiredFieldsAreEmptyException;

  List<PatchUserResponse> updateUsersBulk (
    List<PatchUserRequest> userRequests,
    User user
  ) throws
    UserNotFoundException,
    InvalidFieldLengthException,
    StructUnitNotFoundException,
    UsernameAlreadyExistsException;
}
