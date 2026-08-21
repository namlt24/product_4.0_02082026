package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.DependencyGraphDto;
import com.bccs.gatewaymanager.dto.GraphEdgeDto;
import com.bccs.gatewaymanager.dto.GraphNodeDto;
import com.bccs.gatewaymanager.entity.BackendStep;
import com.bccs.gatewaymanager.entity.EndpointConfig;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Phat hien quan he phu thuoc giua cac endpoint dua tren "trick": 1 BackendStep
 * co the goi NGUOC vao chinh KrakenD (host = localhost:{krakendPort}) voi
 * url_pattern trung cau truc voi path cua 1 endpoint KHAC dang luu trong he
 * thong - do la 1 canh phu thuoc (khong can nguoi dung khai bao gi them, suy
 * ra thang tu du lieu da co).
 *
 * Dung de:
 * - Ve so do phu thuoc (dependency graph) tren UI.
 * - Phat hien VONG LAP (A goi B, B goi lai A...) - se gay goi vo han khi chay
 *   that, can canh bao truoc khi Deploy.
 * - Tinh "layer" (do sau) va "componentId" (cum lien thong) de FE layout theo
 *   TUNG CUM RIENG BIET thay vi 1 so do khong lo gop chung - quan trong khi so
 *   luong endpoint len toi hang ngan (xem README muc "Scale toi hang ngan API").
 *
 * Do phuc tap: O(E + V) voi V = so endpoint, E = so canh phu thuoc thuc su -
 * KHONG phai O(V^2) (tra cuu endpoint dich qua Map, khong quet tuyen tinh).
 */
@Service
@RequiredArgsConstructor
public class DependencyAnalyzer {

    private final EndpointConfigRepository repository;

    @Value("${gatewaymanager.krakend.port:8080}")
    private int krakendPort;

    @Value("#{'${gatewaymanager.krakend.self-host-aliases:localhost,127.0.0.1,krakend-gateway,krakend}'.split(',')}")
    private List<String> selfHostAliases;

    @Transactional(readOnly = true)
    public DependencyGraphDto buildGraph() {
        List<EndpointConfig> all = repository.findAll();

        // Index O(1): "path da chuan hoa" -> endpoint. Chuan hoa bang cach thay
        // {tenParam} bang ky tu "*" o tung segment, de so sanh CAU TRUC (bo qua
        // ten path-param) ma van tra cuu duoc qua HashMap thay vi quet tuyen tinh
        // qua tung endpoint (se cham dan O(V) cho MOI step khi V len toi hang ngan).
        Map<String, EndpointConfig> pathIndex = buildPathIndex(all);

        Map<String, List<String>> adjacency = new HashMap<>(); // endpointId -> danh sach endpointId no goi toi
        List<GraphEdgeDto> edges = new ArrayList<>();
        Map<String, Integer> usedByCount = new HashMap<>();
        Map<String, Integer> callsCount = new HashMap<>();

        for (EndpointConfig ep : all) {
            List<BackendStep> steps = ep.getSteps().stream()
                    .sorted(Comparator.comparingInt(BackendStep::getStepOrder))
                    .toList();
            for (BackendStep step : steps) {
                if (!isSelfHost(step.getHosts())) {
                    continue;
                }
                EndpointConfig target = matchEndpoint(step.getUrlPattern(), pathIndex);
                if (target == null) {
                    continue; // host tro ve chinh KrakenD nhung khong khop endpoint nao - bo qua, khong phai canh phu thuoc
                }
                edges.add(new GraphEdgeDto(ep.getId(), target.getId(), step.getStepOrder()));
                adjacency.computeIfAbsent(ep.getId(), k -> new ArrayList<>()).add(target.getId());
                usedByCount.merge(target.getId(), 1, Integer::sum);
                callsCount.merge(ep.getId(), 1, Integer::sum);
            }
        }

        Map<String, String> pathById = all.stream()
                .collect(Collectors.toMap(EndpointConfig::getId, EndpointConfig::getPath));

        Set<String> cycleNodes = new HashSet<>();
        List<String> cycleWarnings = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        for (EndpointConfig ep : all) {
            if (!visited.contains(ep.getId())) {
                detectCycle(ep.getId(), adjacency, visited, new LinkedHashSet<>(), cycleNodes, cycleWarnings, pathById);
            }
        }

        Map<String, Integer> layerMemo = new HashMap<>();
        for (EndpointConfig ep : all) {
            computeLayer(ep.getId(), adjacency, layerMemo, new HashSet<>(), cycleNodes);
        }

        Map<String, Integer> componentIdById = computeComponentIds(all, edges);

        List<GraphNodeDto> nodes = all.stream()
                .map(ep -> {
                    int calls = callsCount.getOrDefault(ep.getId(), 0);
                    int usedBy = usedByCount.getOrDefault(ep.getId(), 0);
                    return new GraphNodeDto(
                            ep.getId(), ep.getName(), ep.getPath(), ep.getMethod().name(), ep.isSequential(),
                            ep.getSteps().size(),
                            usedBy,
                            calls,
                            cycleNodes.contains(ep.getId()),
                            layerMemo.getOrDefault(ep.getId(), 0),
                            componentIdById.getOrDefault(ep.getId(), -1),
                            calls == 0 && usedBy == 0);
                })
                .toList();

        return new DependencyGraphDto(nodes, edges, cycleWarnings);
    }

    /** Danh sach canh bao vong lap (dung lai o ConfigController de chen vao warnings cua preview/deploy). */
    @Transactional(readOnly = true)
    public List<String> detectCycleWarningsOnly() {
        return buildGraph().cycleWarnings();
    }

    private boolean isSelfHost(List<String> hosts) {
        if (hosts == null) {
            return false;
        }
        for (String h : hosts) {
            try {
                URI uri = URI.create(h.trim());
                String hostName = uri.getHost();
                int port = uri.getPort();
                if (hostName != null && port == krakendPort
                        && selfHostAliases.stream().anyMatch(alias -> alias.trim().equalsIgnoreCase(hostName))) {
                    return true;
                }
            } catch (Exception ignored) {
                // host khong parse duoc dang URI hop le - chac chan khong phai self-call, bo qua
            }
        }
        return false;
    }

    /** Xay Map "path chuan hoa" -> endpoint, 1 lan duy nhat cho toan bo danh sach - tra cuu O(1) thay vi quet O(V). */
    private Map<String, EndpointConfig> buildPathIndex(List<EndpointConfig> all) {
        Map<String, EndpointConfig> index = new HashMap<>();
        for (EndpointConfig ep : all) {
            // putIfAbsent: neu 2 endpoint (hiem gap, path literal khac nhau nhung
            // cung cau truc, vi du "/v1/foo/{a}" va "/v1/foo/{b}") vo tinh trung
            // normalize-key, uu tien endpoint duoc quet truoc - giu dung hanh vi
            // nhu ban O(n) truoc day (luon chon endpoint dau tien tim thay).
            index.putIfAbsent(normalizePath(ep.getPath()), ep);
        }
        return index;
    }

    private EndpointConfig matchEndpoint(String urlPattern, Map<String, EndpointConfig> pathIndex) {
        String cleanPattern = urlPattern.contains("?") ? urlPattern.substring(0, urlPattern.indexOf('?')) : urlPattern;
        return pathIndex.get(normalizePath(cleanPattern));
    }

    /** Thay tung segment dang {tenParam} bang "*" de so sanh CAU TRUC (bo qua ten param) qua 1 key duy nhat. */
    private String normalizePath(String path) {
        String[] segments = path.split("/");
        StringBuilder normalized = new StringBuilder();
        for (String seg : segments) {
            normalized.append('/');
            normalized.append(seg.startsWith("{") && seg.endsWith("}") ? "*" : seg);
        }
        return normalized.toString();
    }

    /** DFS phat hien cycle (mau trang/xam/den kinh dien), gom lai danh sach node nam trong 1 vong lap + mo ta de nguoi doc hieu. */
    private void detectCycle(String nodeId, Map<String, List<String>> adjacency, Set<String> visited,
                              LinkedHashSet<String> pathStack, Set<String> cycleNodes,
                              List<String> cycleWarnings, Map<String, String> pathById) {
        visited.add(nodeId);
        pathStack.add(nodeId);

        for (String next : adjacency.getOrDefault(nodeId, List.of())) {
            if (pathStack.contains(next)) {
                List<String> cycleIds = new ArrayList<>();
                boolean collecting = false;
                for (String id : pathStack) {
                    if (id.equals(next)) {
                        collecting = true;
                    }
                    if (collecting) {
                        cycleIds.add(id);
                    }
                }
                cycleIds.add(next);
                cycleNodes.addAll(cycleIds);
                String desc = cycleIds.stream().map(id -> pathById.getOrDefault(id, id)).collect(Collectors.joining(" -> "));
                cycleWarnings.add("Phat hien VONG LAP phu thuoc: " + desc
                        + " - se gay goi vo han lan khi chay that. PHAI sua truoc khi Deploy.");
            } else if (!visited.contains(next)) {
                detectCycle(next, adjacency, visited, pathStack, cycleNodes, cycleWarnings, pathById);
            }
        }

        pathStack.remove(nodeId);
    }

    /** Layer = duong di dai nhat toi 1 "la" (endpoint khong goi endpoint nao khac). La = 0, cang goi nhieu tang cang cao. */
    private int computeLayer(String nodeId, Map<String, List<String>> adjacency, Map<String, Integer> memo,
                              Set<String> visiting, Set<String> cycleNodes) {
        if (memo.containsKey(nodeId)) {
            return memo.get(nodeId);
        }
        if (cycleNodes.contains(nodeId) || visiting.contains(nodeId)) {
            memo.put(nodeId, 0);
            return 0;
        }
        visiting.add(nodeId);
        int maxChildLayer = -1;
        for (String next : adjacency.getOrDefault(nodeId, List.of())) {
            maxChildLayer = Math.max(maxChildLayer, computeLayer(next, adjacency, memo, visiting, cycleNodes));
        }
        visiting.remove(nodeId);
        int layer = maxChildLayer + 1;
        memo.put(nodeId, layer);
        return layer;
    }

    /**
     * Union-Find (Disjoint Set Union) tren canh VO HUONG (khong quan tam ai goi ai,
     * chi quan tam co lien quan hay khong) de gom endpoint thanh cac "cum lien
     * thong" (connected component). FE ve moi cum thanh 1 khoi rieng, tranh gop
     * toan bo he thong (hang ngan endpoint) thanh 1 so do duy nhat.
     */
    private Map<String, Integer> computeComponentIds(List<EndpointConfig> all, List<GraphEdgeDto> edges) {
        Map<String, String> parent = new HashMap<>();
        for (EndpointConfig ep : all) {
            parent.put(ep.getId(), ep.getId());
        }
        for (GraphEdgeDto edge : edges) {
            union(parent, edge.fromEndpointId(), edge.toEndpointId());
        }

        int[] counter = {0};
        Map<String, Integer> componentIdByRoot = new HashMap<>();
        Map<String, Integer> componentIdById = new HashMap<>();
        for (EndpointConfig ep : all) {
            String root = find(parent, ep.getId());
            int compId = componentIdByRoot.computeIfAbsent(root, r -> counter[0]++);
            componentIdById.put(ep.getId(), compId);
        }
        return componentIdById;
    }

    private String find(Map<String, String> parent, String x) {
        String root = x;
        while (!parent.get(root).equals(root)) {
            root = parent.get(root);
        }
        while (!parent.get(x).equals(root)) {
            String next = parent.get(x);
            parent.put(x, root);
            x = next;
        }
        return root;
    }

    private void union(Map<String, String> parent, String a, String b) {
        String rootA = find(parent, a);
        String rootB = find(parent, b);
        if (!rootA.equals(rootB)) {
            parent.put(rootA, rootB);
        }
    }
}
