package vk.tarantool.mapper;

import com.google.protobuf.ByteString;
import com.vk.tarantool.KeyValueServiceOuterClass.CountResponse;
import com.vk.tarantool.KeyValueServiceOuterClass.KeyValue;
import com.vk.tarantool.KeyValueServiceOuterClass.Value;
import org.springframework.stereotype.Component;
import vk.tarantool.data.DbKeyValue;

@Component
public class TarantoolMapper {

  public Value toValue(DbKeyValue keyValue){
    return Value.newBuilder()
            .setValue(ByteString.copyFrom(keyValue.getValue()))
            .build();
  }

  public KeyValue toKeyValue(DbKeyValue keyValue){
    return KeyValue.newBuilder()
        .setKey(keyValue.getKey())
        .setValue(ByteString.copyFrom(keyValue.getValue()))
        .build();
  }

  public CountResponse toCountResponse(long count){
    return CountResponse.newBuilder()
        .setCount(count)
        .build();
  }

  public DbKeyValue toEntity(KeyValue request){
    if(request == null) {
      return null;
    } else {
      return new DbKeyValue(
          request.getKey(),
          request.getValue().toByteArray());
    }
  }
}
