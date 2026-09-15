package com.bccs.gatewaymanager.cache;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * deleteByPattern() la trai tim cua fix "cache cu con song sau khi bat/tat
 * cacheEnabled" (xem EndpointService.purgeStaleCacheIfChanged(),
 * RemoteConfigSyncService.invalidateChangedEndpointCaches()) - mock
 * RedisConnection/RedisKeyCommands/Cursor thay vi Redis that (khong co Redis
 * trong moi truong unit test), dung StringRedisTemplate.execute(RedisCallback)
 * dung y het cach production code goi.
 */
class GatewayCacheServiceTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final GatewayCacheService service = new GatewayCacheService(redisTemplate);

    @SuppressWarnings("unchecked")
    private RedisKeyCommands stubExecuteAndReturnKeyCommands() {
        RedisConnection connection = mock(RedisConnection.class);
        RedisKeyCommands keyCommands = mock(RedisKeyCommands.class);
        when(connection.keyCommands()).thenReturn(keyCommands);
        // deleteByPattern() gio chi checkout 1 connection DUY NHAT cho ca SCAN lan DEL
        // (xem GatewayCacheService) - 1 lan stub la du cho callback do.
        when(redisTemplate.execute((RedisCallback<Object>) any())).thenAnswer(inv -> {
            RedisCallback<?> callback = inv.getArgument(0);
            return callback.doInRedis(connection);
        });
        return keyCommands;
    }

    @Test
    void deleteByPattern_coKeyKhopPattern_scanRoiXoaHetCacKeyDo() {
        byte[] key1 = "gwm:cache:svc:GET:/x".getBytes(StandardCharsets.UTF_8);
        byte[] key2 = "gwm:cache:svc:GET:/y".getBytes(StandardCharsets.UTF_8);
        Cursor<byte[]> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(key1, key2);

        RedisKeyCommands keyCommands = stubExecuteAndReturnKeyCommands();
        // any() KHONG dinh kieu se khop nham overload scan(KeyScanOptions) (default method
        // cua RedisKeyCommands) thay vi scan(ScanOptions) ma production code that goi -
        // stub khong khop, scan() that tra ve null -> NullPointerException bi nuot boi
        // catch(RuntimeException) cua deleteByPattern(), test fail kho hieu (da gap that
        // luc viet test nay - ghi lai de khong ai lap lai). PHAI dinh kieu ro rang.
        when(keyCommands.scan(any(ScanOptions.class))).thenReturn(cursor);

        service.deleteByPattern("gwm:cache:svc:GET:*");

        verify(keyCommands).del(key1, key2);
    }

    @Test
    void deleteByPattern_khongKeyNaoKhop_khongGoiDel() {
        Cursor<byte[]> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(false);

        RedisKeyCommands keyCommands = stubExecuteAndReturnKeyCommands();
        // any() KHONG dinh kieu se khop nham overload scan(KeyScanOptions) (default method
        // cua RedisKeyCommands) thay vi scan(ScanOptions) ma production code that goi -
        // stub khong khop, scan() that tra ve null -> NullPointerException bi nuot boi
        // catch(RuntimeException) cua deleteByPattern(), test fail kho hieu (da gap that
        // luc viet test nay - ghi lai de khong ai lap lai). PHAI dinh kieu ro rang.
        when(keyCommands.scan(any(ScanOptions.class))).thenReturn(cursor);

        service.deleteByPattern("gwm:cache:svc:GET:*");

        verify(keyCommands, never()).del((byte[][]) any());
    }

    /** Fail-open giong get()/put() - loi Redis (mat ket noi, timeout...) KHONG duoc chan luong luu cau hinh. */
    @Test
    void deleteByPattern_loiRedis_khongThrow() {
        when(redisTemplate.execute((RedisCallback<Object>) any())).thenThrow(new RuntimeException("Redis khong ket noi duoc"));

        service.deleteByPattern("gwm:cache:svc:GET:*");
        // Khong assert gi them - test PASS nghia la khong co exception nao thoat ra ngoai.
    }
}
