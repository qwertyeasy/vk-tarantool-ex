package vk.tarantool.mapper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import com.google.protobuf.ByteString;
import com.vk.tarantool.KeyValueServiceOuterClass.CountResponse;
import com.vk.tarantool.KeyValueServiceOuterClass.KeyValue;
import com.vk.tarantool.KeyValueServiceOuterClass.Value;
import org.junit.jupiter.api.Test;
import vk.tarantool.data.DbKeyValue;

class TarantoolMapperTest {

  TarantoolMapper tarantoolMapper = new TarantoolMapper();

  @Test
  void toValueTest(){
    byte[] bytes = {5, 2, 6, 7, 4};
    DbKeyValue dbKV = new DbKeyValue("key", bytes);

    Value value = tarantoolMapper.toValue(dbKV);

    assertEquals(ByteString.copyFrom(bytes), value.getValue());
  }

  @Test
  void toKeyValueTest(){
    byte[] bytes = {5, 2, 6, 7, 4};
    DbKeyValue dbKV = new DbKeyValue("key", bytes);

    KeyValue keyValue = tarantoolMapper.toKeyValue(dbKV);

    assertEquals(ByteString.copyFrom(bytes), keyValue.getValue());
    assertEquals("key", keyValue.getKey());
  }

  @Test
  void toCountResponseTest(){
    long count = 5L;

    CountResponse response = tarantoolMapper.toCountResponse(count);

    assertEquals(count, response.getCount());
  }

  @Test
  void toEntityTest_requestIsNull_returnsNull(){
    DbKeyValue entity = tarantoolMapper.toEntity(null);

    assertNull(entity);
  }

  @Test
  void toEntityTest_requestNotNull_returnsEntity(){
    byte[] bytes = new byte[]{4, 7, 5, 4};
    KeyValue keyValue = KeyValue.newBuilder()
        .setKey("key")
        .setValue(ByteString.copyFrom(bytes))
        .build();

    DbKeyValue entity = tarantoolMapper.toEntity(keyValue);

    assertEquals("key", entity.getKey());
    assertArrayEquals(bytes, entity.getValue());
  }
}
