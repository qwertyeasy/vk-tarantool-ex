package vk.tarantool.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.tarantool.spring.data34.query.Field;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonIgnoreProperties(ignoreUnknown = true)
@KeySpace("KV")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DbKeyValue {

  @Id
  @JsonProperty("key")
  public String key;

  @Field("value")
  @JsonProperty("value")
  public byte[] value;
}
