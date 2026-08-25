package com.viettel.bccs.productcatalog.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Chuẩn hoá OpenAPI/Swagger + validation cho toàn bộ controller/DTO của service, theo 5 quy tắc:
 * (A) mỗi endpoint có ví dụ 200 chạy được, (B) field String có min/max length (+ regex nếu có format
 * rõ ràng), (C) field số có min/max value, (D) field List/mảng có min/max length, (E) DTO không lồng
 * nhau thành chu trình (cycle) vô hạn.
 *
 * Đây là kiểm tra tĩnh (bytecode/annotation), không cần DB/service chạy — khác với
 * OpenApiExampleSmokeIT (integration test thật, xác nhận ví dụ thực sự trả về thành công).
 *
 * LƯU Ý: package `..dto..` loại trừ `..client..` — DTO nằm trong package client (mirror response
 * của service khác gọi qua Feign) không thuộc API contract của chính service này.
 */
class OpenApiComplianceTest {

    private static final String BASE_PACKAGE = "com.viettel.bccs.productcatalog";

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE_PACKAGE);

    private static final Set<String> STRING_TYPES = Set.of(String.class.getName());

    private static final Set<String> NUMERIC_TYPES = Set.of(
            Long.class.getName(), Integer.class.getName(), Short.class.getName(), Byte.class.getName(),
            Double.class.getName(), Float.class.getName(),
            BigDecimal.class.getName(), BigInteger.class.getName(),
            "long", "int", "short", "byte", "double", "float");

    private static final Set<String> COLLECTION_TYPES = Set.of(
            List.class.getName(), java.util.Collection.class.getName(), Set.class.getName());

    // ---------------------------------------------------------------- Rule A

    @Test
    void every_endpoint_has_operation_and_success_example() {
        List<String> violations = new ArrayList<>();
        for (JavaClass clazz : CLASSES) {
            if (!resideInAPackage("..controller..").test(clazz)) {
                continue;
            }
            for (JavaMethod method : clazz.getMethods()) {
                if (!isEndpointMethod(method)) {
                    continue;
                }
                // Dung AnnotatedElementUtils (Spring) thay vi isAnnotatedWith cua ArchUnit - ArchUnit
                // chi doc annotation truc tiep, khong theo duoc chuoi meta-annotation cua cac custom
                // annotation gom @Operation/@ApiResponses (vd @ApiGetByProductCode). AnnotatedElementUtils
                // la chinh co che springdoc dung de resolve annotation khi sinh OpenAPI spec that,
                // nen dung no moi dam bao test phan anh dung hanh vi thuc te.
                if (!AnnotatedElementUtils.isAnnotated(method.reflect(), Operation.class)) {
                    violations.add(method.getFullName() + ": thiếu @Operation");
                    continue;
                }
                if (!hasSuccessExample(method)) {
                    violations.add(method.getFullName()
                            + ": thiếu @ApiResponse(responseCode=\"200\") có @ExampleObject với value không rỗng");
                }
            }
        }
        assertTrue(violations.isEmpty(),
                "Vi phạm chuẩn OpenAPI cho endpoint (mỗi API phải có ví dụ chạy được):\n" + String.join("\n", violations));
    }

    private static boolean isEndpointMethod(JavaMethod method) {
        return method.isAnnotatedWith(GetMapping.class)
                || method.isAnnotatedWith(PostMapping.class)
                || method.isAnnotatedWith(PutMapping.class)
                || method.isAnnotatedWith(DeleteMapping.class)
                || method.isAnnotatedWith(PatchMapping.class)
                || method.isAnnotatedWith(RequestMapping.class);
    }

    private static boolean hasSuccessExample(JavaMethod method) {
        ApiResponses apiResponses = AnnotatedElementUtils.findMergedAnnotation(method.reflect(), ApiResponses.class);
        return apiResponses != null && hasSuccessExample(apiResponses);
    }

    private static boolean hasSuccessExample(ApiResponses apiResponses) {
        for (ApiResponse response : apiResponses.value()) {
            if (!"200".equals(response.responseCode())) {
                continue;
            }
            for (Content content : response.content()) {
                for (ExampleObject example : content.examples()) {
                    if (example.value() != null && !example.value().isBlank()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // ---------------------------------------------------------- Rule B/C/D

    @Test
    void every_dto_field_has_required_bounds() {
        List<String> violations = new ArrayList<>();
        for (JavaClass clazz : CLASSES) {
            if (!isApiDtoClass(clazz)) {
                continue;
            }
            for (JavaField field : clazz.getFields()) {
                if (field.getModifiers().contains(JavaModifier.STATIC)) {
                    continue;
                }
                String rawTypeName = field.getRawType().getFullName();
                String qualifiedField = clazz.getFullName() + "." + field.getName();

                if (STRING_TYPES.contains(rawTypeName)) {
                    if (!field.isAnnotatedWith(Size.class)) {
                        violations.add(qualifiedField + ": field String thiếu @Size(min/max)");
                    }
                    if (looksLikeFormattedField(field.getName()) && !field.isAnnotatedWith(Pattern.class)) {
                        violations.add(qualifiedField
                                + ": field String có format rõ ràng (code/type/status/flag) nhưng thiếu @Pattern");
                    }
                } else if (NUMERIC_TYPES.contains(rawTypeName)) {
                    boolean hasMin = field.isAnnotatedWith(Min.class) || field.isAnnotatedWith(DecimalMin.class);
                    boolean hasMax = field.isAnnotatedWith(Max.class) || field.isAnnotatedWith(DecimalMax.class);
                    if (!hasMin || !hasMax) {
                        violations.add(qualifiedField + ": field số thiếu @Min/@Max (hoặc @DecimalMin/@DecimalMax)");
                    }
                } else if (COLLECTION_TYPES.contains(rawTypeName) || field.getRawType().isArray()) {
                    if (!field.isAnnotatedWith(Size.class)) {
                        violations.add(qualifiedField + ": field List/mảng thiếu @Size(min/max)");
                    }
                }
            }
        }
        assertTrue(violations.isEmpty(),
                "Vi phạm chuẩn validation cho DTO field:\n" + String.join("\n", violations));
    }

    private static boolean looksLikeFormattedField(String fieldName) {
        String lower = fieldName.toLowerCase(Locale.ROOT);
        return lower.contains("code") || lower.contains("type") || lower.contains("status") || lower.contains("flag");
    }

    /**
     * DTO thuộc API contract của chính service: nằm trong "..dto.." nhưng KHÔNG nằm trong
     * "..client..", và KHÔNG phải class Builder do Lombok @Builder tự sinh ra (các field trong
     * *Builder chỉ là bản sao tạm phục vụ fluent API, không mang annotation của field gốc — không
     * thuộc phạm vi kiểm tra).
     */
    private static boolean isApiDtoClass(JavaClass clazz) {
        return resideInAPackage("..dto..").test(clazz)
                && !resideInAPackage("..client..").test(clazz)
                && !clazz.getSimpleName().endsWith("Builder");
    }

    // -------------------------------------------------------------- Rule E

    @Test
    void dto_reference_graph_has_no_cycle() {
        Map<String, JavaClass> dtoClasses = new HashMap<>();
        for (JavaClass clazz : CLASSES) {
            if (isApiDtoClass(clazz) && !clazz.isEnum()) {
                dtoClasses.put(clazz.getFullName(), clazz);
            }
        }

        Map<String, List<String>> graph = new HashMap<>();
        for (JavaClass clazz : dtoClasses.values()) {
            List<String> edges = new ArrayList<>();
            for (JavaField field : clazz.getFields()) {
                if (field.getModifiers().contains(JavaModifier.STATIC)) {
                    continue;
                }
                String targetTypeName = resolveDtoTargetType(field);
                if (targetTypeName != null && dtoClasses.containsKey(targetTypeName)) {
                    edges.add(targetTypeName);
                }
            }
            graph.put(clazz.getFullName(), edges);
        }

        List<String> cycle = findCycle(graph);
        assertTrue(cycle.isEmpty(),
                "Phát hiện DTO lồng nhau thành chu trình (cycle) vô hạn: " + String.join(" -> ", cycle));
    }

    /**
     * Trả về tên class DTO mà field này tham chiếu tới (trực tiếp, hoặc phần tử của List/mảng),
     * bỏ qua String/số/enum/kiểu nguyên thuỷ — chỉ quan tâm tham chiếu DTO-tới-DTO.
     */
    private static String resolveDtoTargetType(JavaField field) {
        String rawTypeName = field.getRawType().getFullName();
        if (STRING_TYPES.contains(rawTypeName) || NUMERIC_TYPES.contains(rawTypeName)) {
            return null;
        }
        if (COLLECTION_TYPES.contains(rawTypeName)) {
            JavaType type = field.getType();
            if (type instanceof JavaParameterizedType parameterizedType
                    && !parameterizedType.getActualTypeArguments().isEmpty()) {
                return parameterizedType.getActualTypeArguments().get(0).toErasure().getFullName();
            }
            return null;
        }
        if (field.getRawType().isArray()) {
            return null;
        }
        return rawTypeName;
    }

    private static List<String> findCycle(Map<String, List<String>> graph) {
        Set<String> visited = new HashSet<>();
        Set<String> inStack = new HashSet<>();
        Deque<String> path = new ArrayDeque<>();
        for (String start : graph.keySet()) {
            if (!visited.contains(start)) {
                List<String> cycle = dfs(start, graph, visited, inStack, path);
                if (!cycle.isEmpty()) {
                    return cycle;
                }
            }
        }
        return List.of();
    }

    private static List<String> dfs(String node, Map<String, List<String>> graph, Set<String> visited,
            Set<String> inStack, Deque<String> path) {
        visited.add(node);
        inStack.add(node);
        path.addLast(node);

        for (String next : graph.getOrDefault(node, List.of())) {
            if (inStack.contains(next)) {
                List<String> cycle = new ArrayList<>(path);
                cycle.add(next);
                return cycle;
            }
            if (!visited.contains(next)) {
                List<String> cycle = dfs(next, graph, visited, inStack, path);
                if (!cycle.isEmpty()) {
                    return cycle;
                }
            }
        }

        path.removeLast();
        inStack.remove(node);
        return List.of();
    }
}
