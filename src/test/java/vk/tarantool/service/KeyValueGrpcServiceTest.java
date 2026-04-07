package vk.tarantool.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.vk.tarantool.KeyValueServiceOuterClass.CountResponse;
import com.vk.tarantool.KeyValueServiceOuterClass.Key;
import com.vk.tarantool.KeyValueServiceOuterClass.KeyRange;
import com.vk.tarantool.KeyValueServiceOuterClass.KeyValue;
import com.vk.tarantool.KeyValueServiceOuterClass.Value;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import vk.tarantool.data.DbKeyValue;
import vk.tarantool.mapper.TarantoolMapper;
import vk.tarantool.repository.TarantoolDbRepository;

@ExtendWith(MockitoExtension.class)
public class KeyValueGrpcServiceTest {

  @Spy
  TarantoolMapper tarantoolMapper;

  @Mock
  TarantoolDbRepository repository;

  @InjectMocks
  KeyValueGrpcService grpcService;

  @Test
  void putTest_success_onCompletedExecution(){
    KeyValue keyValue = KeyValue.newBuilder()
        .setKey("key")
        .setValue(ByteString.copyFrom(new byte[]{0, 0, 0, 0}))
        .build();
    StreamObserver<Empty> observerMock = Mockito.mock(StreamObserver.class);
    when(repository.save(any(DbKeyValue.class))).thenReturn(null);

    grpcService.put(keyValue, observerMock);

    verify(tarantoolMapper, times(1)).toEntity(keyValue);
    verify(observerMock, times(1)).onNext(any());
    verify(observerMock, times(1)).onCompleted();
  }

  @Test
  void putTest_internalError_onErrorExecution(){
    KeyValue keyValue = KeyValue.newBuilder()
        .setKey("key")
        .setValue(ByteString.copyFrom(new byte[]{0, 0, 0, 0}))
        .build();
    StreamObserver<Empty> observerMock = Mockito.mock(StreamObserver.class);
    when(repository.save(any(DbKeyValue.class))).thenThrow(RuntimeException.class);

    grpcService.put(keyValue, observerMock);

    verify(observerMock, times(1)).onError(any());
  }

  @Test
  void getTest_keyValueFound_onCompletedExecution(){
    Key key = Key.newBuilder().setKey("key").build();
    DbKeyValue dbKeyValue = new DbKeyValue("key", new byte[]{0, 0, 0, 0});
    StreamObserver<Value> observerMock = Mockito.mock(StreamObserver.class);

    when(repository.findById(anyString())).thenReturn(Optional.of(dbKeyValue));

    grpcService.get(key, observerMock);

    verify(tarantoolMapper, times(1)).toValue(dbKeyValue);
    verify(observerMock, times(1)).onNext(any());
    verify(observerMock, times(1)).onCompleted();
  }

  @Test
  void getTest_keyValueNotFound_onErrorExecution(){
    Key key = Key.getDefaultInstance();
    StreamObserver<Value> observerMock = Mockito.mock(StreamObserver.class);

    when(repository.findById(anyString())).thenReturn(Optional.empty());

    grpcService.get(key, observerMock);

    verify(observerMock, times(1)).onError(any());
  }

  @Test
  void getTest_internalError_onErrorExecution(){
    Key key = Key.getDefaultInstance();
    StreamObserver<Value> observerMock = Mockito.mock(StreamObserver.class);

    when(repository.findById(anyString())).thenThrow(RuntimeException.class);

    grpcService.get(key, observerMock);

    verify(observerMock, times(1)).onError(any());
  }

  @Test
  void deleteTest_success_onCompletedExecution(){
    Key key = Key.getDefaultInstance();

    StreamObserver<Empty> observerMock = Mockito.mock(StreamObserver.class);

    grpcService.delete(key, observerMock);

    verify(observerMock, times(1)).onNext(any());
    verify(observerMock, times(1)).onCompleted();
  }

  @Test
  void rangeTest_success_onCompletedExecution(){
    DbKeyValue dbKV = new DbKeyValue("key", new byte[]{0, 0, 0, 0});
    int streamSize = 10;
    Stream<DbKeyValue> keyValueStream = Stream.generate(() -> dbKV)
        .limit(streamSize);
    KeyRange range = KeyRange.newBuilder()
        .setKeySince("abc")
        .setKeyTo("bcd")
        .build();
    StreamObserver<KeyValue> observerMock = Mockito.mock(StreamObserver.class);

    when(repository.streamByKeyBetween(
        range.getKeySince(), range.getKeyTo(), Limit.of(10)
    )).thenReturn(keyValueStream);

    grpcService.range(range, observerMock);

    verify(tarantoolMapper, times(streamSize)).toKeyValue(any(DbKeyValue.class));
    verify(observerMock, times(streamSize)).onNext(any());
    verify(observerMock, times(1)).onCompleted();
  }

  @Test
  void rangeTest_streamCancelled_successExecutionWithoutObserver(){
    DbKeyValue dbKV = new DbKeyValue("key", new byte[]{0, 0, 0, 0});
    int streamSize = 10;
    Stream<DbKeyValue> keyValueStream = Stream.generate(() -> dbKV)
        .limit(streamSize);
    KeyRange range = KeyRange.newBuilder()
        .setKeySince("abc")
        .setKeyTo("bcd")
        .build();
    ServerCallStreamObserver<KeyValue> serverCall = Mockito.mock(ServerCallStreamObserver.class);

    when(serverCall.isCancelled()).thenReturn(true);
    when(repository.streamByKeyBetween(
        range.getKeySince(), range.getKeyTo(), Limit.of(10)
    )).thenReturn(keyValueStream);

    grpcService.range(range, serverCall);

    verify(tarantoolMapper, never()).toKeyValue(any(DbKeyValue.class));
    verify(serverCall, never()).onNext(any());
    verify(serverCall, never()).onCompleted();
  }

  @Test
  void countTest_success_onCompletedExecution(){
    long count = 5L;
    StreamObserver<CountResponse> observerMock = Mockito.mock(StreamObserver.class);

    when(repository.count()).thenReturn(count);

    grpcService.count(Empty.getDefaultInstance(), observerMock);

    verify(observerMock, times(1)).onNext(any());
    verify(observerMock, times(1)).onCompleted();
  }

  @Test
  void countTest_internalError_onErrorExecution(){
    StreamObserver<CountResponse> observerMock = Mockito.mock(StreamObserver.class);

    when(repository.count()).thenThrow(RuntimeException.class);

    grpcService.count(Empty.getDefaultInstance(), observerMock);

    verify(observerMock, times(1)).onError(any());
  }

}
