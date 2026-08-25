package com.viettel.bccs.policy.mapactiveinfo.openapi;

import com.viettel.bccs.common.api.response.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(operationId = "reindexElasticsearch",
        summary = "Đồng bộ toàn bộ MAP_ACTIVE_INFO từ Oracle sang Elasticsearch",
        description = "Đọc toàn bộ bảng MAP_ACTIVE_INFO (phân trang) và bulk-index sang Elasticsearch. "
)
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công - trả về số document đã index",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = MapActiveInfoQuerryControllerExamples.REINDEX_ELASTICSEARCH_EXAMPLE))),
        @ApiResponse(responseCode = "503", description = "Elasticsearch chưa được bật hoặc chưa kết nối được")
})
public @interface ApiReindexElasticsearch {
}
