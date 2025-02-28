package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

/**
 * @author JuliWolf
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostAssetTypeStatusesAssignmentsRequest implements Request {
  List<PostAssetTypeStatusAssignmentRequest> status_assignment = new ArrayList<>();
}
