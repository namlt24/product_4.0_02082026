● Code thay đổi cụ thể trong ProductCatalogRedisService.java:

  Trước (3 vấn đề):

  // ❌ Vấn đề 1: TTL null → IllegalArgumentException
  private static final Duration PERMANENT_TTL = null;
  
  // ❌ Vấn đề 2: Không inject ObjectMapper
  private final BccsRedisService redisService;

  // ❌ Vấn đề 3: setObject/getObject dùng GenericJackson2JsonRedisSerializer
  // → serialize object trực tiếp → deserialize thành LinkedHashMap → type mismatch
  public <T> void setObject(String key, T object) {
      redisService.value().set(key, object, PERMANENT_TTL); // object không serialize đúng
  }

  public <T> Optional<T> getObject(String key, Class<T> clazz) {
      return redisService.value().get(key, clazz); // LinkedHashMap ≠ ShopResponse
  }

  Sau (fix):

  // ✅ Fix 1: TTL hợp lệ
  private static final Duration PERMANENT_TTL = Duration.ofDays(365);

  // ✅ Fix 2: Inject ObjectMapper (có JavaTimeModule → hỗ trợ LocalDate)
  private final BccsRedisService redisService;
  private final ObjectMapper objectMapper;

  // ✅ Fix 3: Serialize → JSON string → lưu; Deserialize → JSON string → object
  public <T> void setObject(String key, T object) {
      try {
          String json = objectMapper.writeValueAsString(object);  // ShopResponse → "{\"shopId\":100,...}"
          redisService.value().set(key, json, PERMANENT_TTL);      // lưu JSON string
      } catch (JsonProcessingException e) {
          log.error("JSON serialize failed for key {}: {}", key, e.getMessage());
      }
  }

  public <T> Optional<T> getObject(String key, Class<T> clazz) {
      try {
          String json = redisService.value().get(key, String.class).orElse(null); // đọc JSON string
          if (json == null) return Optional.empty();
          return Optional.of(objectMapper.readValue(json, clazz));                  // JSON → ShopResponse
      } catch (Exception e) {
          log.error("JSON deserialize failed for key {}: {}", key, e.getMessage());
          return Optional.empty();
      }
  }

  Luồng hoạt động đúng:

  getById(100021869)
    ├─ redisService.getObject("PRODUCT_OFFERING_100021869", ShopResponse.class)
    │    └─ get JSON string từ Redis → objectMapper.readValue(json, ShopResponse.class)
    │         ✓ deserialize đúng type → return ShopResponse
      }
  }

  Luồng hoạt động đúng:

  getById(100021869)
    ├─ redisService.getObject("PRODUCT_OFFERING_100021869", ShopResponse.class)
    │    └─ get JSON string từ Redis → objectMapper.readValue(json, ShopResponse.class)
    │         ✓ deserialize đúng type → return ShopResponse
    │    └─ get JSON string từ Redis → objectMapper.readValue(json, ShopResponse.class)
    │         ✓ deserialize đúng type → return ShopResponse
    │
    └─ (cache miss) → fetch DB → mapper.toResponse()
         └─ redisService.setObject("PRODUCT_OFFERING_100021869", response)
              └─ objectMapper.writeValueAsString(response) → JSON → lưu vào Redis

  Root cause gốc: GenericJackson2JsonRedisSerializer deserialize Java record thành LinkedHashMap (vì record không có default constructor và không có type-info đúng). Khi getObject(key, 
  ShopResponse.class) so sánh LinkedHashMap với ShopResponse.class → Optional.empty() → luôn cache miss.
