package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Trigger KrakenD ap dung cau hinh moi bang cach goi thang Docker Engine API
 * qua Unix domain socket (/var/run/docker.sock), KHONG can cai dat Docker CLI
 * trong image backend (Java 16+ ho tro UnixDomainSocketAddress nativement qua
 * java.nio - xem SocketChannel.open(StandardProtocolFamily.UNIX)).
 *
 * Co che reload: KrakenD Community Edition KHONG co hot-reload built-in (khong
 * tu dong theo doi thay doi file config). Cach thuc te va an toan nhat de ap
 * dung config moi la RESTART container KrakenD - vi KrakenD la stateless nen
 * downtime chi ~1-2 giay (thoi gian container khoi dong lai va nap file JSON
 * moi). De dat zero-downtime that su trong production, khuyen nghi chay >= 2
 * replica KrakenD sau 1 load balancer va rolling-restart tung replica mot
 * (xem README phan "Zero-downtime deployment").
 */
@Slf4j
@Service
public class DockerReloadService {

    @Value("${gatewaymanager.docker.socket-path:/var/run/docker.sock}")
    private String socketPath;

    @Value("${gatewaymanager.krakend.container-name:krakend-gateway}")
    private String containerName;

    @Value("${gatewaymanager.docker.api-version:v1.43}")
    private String apiVersion;

    /** Goi POST /containers/{name}/restart tren Docker Engine API. */
    public void restartKrakend() {
        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(Path.of(socketPath));

        try (SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
            channel.connect(address);

            String requestPath = "/" + apiVersion + "/containers/" + containerName + "/restart?t=5";
            String httpRequest = "POST " + requestPath + " HTTP/1.1\r\n"
                    + "Host: docker\r\n"
                    + "Connection: close\r\n"
                    + "Content-Length: 0\r\n\r\n";
            channel.write(ByteBuffer.wrap(httpRequest.getBytes(StandardCharsets.UTF_8)));

            String response = readAll(channel);
            String statusLine = response.split("\r\n", 2)[0];
            log.info("Docker Engine API restart '{}' -> {}", containerName, statusLine);

            // 204 No Content = restart thanh cong. 404 = container khong ton tai (ten sai / chua chay).
            if (!statusLine.contains(" 204 ")) {
                throw new SystemException("Docker Engine API tra ve loi khi restart KrakenD ('"
                        + containerName + "'): " + statusLine
                        + " - kiem tra ten container va quyen truy cap /var/run/docker.sock.");
            }
        } catch (IOException e) {
            throw new SystemException("Khong the ket noi Docker socket (" + socketPath
                    + ") de reload KrakenD. Dam bao docker.sock da duoc mount vao container backend: " + e.getMessage(), e);
        }
    }

    private String readAll(SocketChannel channel) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4096);
        StringBuilder sb = new StringBuilder();
        int read;
        while ((read = channel.read(buf)) != -1) {
            buf.flip();
            sb.append(StandardCharsets.UTF_8.decode(buf));
            buf.clear();
            if (read == 0) break;
        }
        return sb.toString();
    }
}
