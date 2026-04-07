package vk.tarantool.service;

import com.google.protobuf.Empty;
import com.vk.tarantool.KeyValueServiceGrpc.KeyValueServiceImplBase;
import com.vk.tarantool.KeyValueServiceOuterClass.CountResponse;
import com.vk.tarantool.KeyValueServiceOuterClass.Key;
import com.vk.tarantool.KeyValueServiceOuterClass.KeyRange;
import com.vk.tarantool.KeyValueServiceOuterClass.KeyValue;
import com.vk.tarantool.KeyValueServiceOuterClass.Value;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.grpc.server.service.GrpcService;
import vk.tarantool.data.DbKeyValue;
import vk.tarantool.exception.StreamCancelledException;
import vk.tarantool.mapper.TarantoolMapper;
import vk.tarantool.repository.TarantoolDbRepository;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class KeyValueGrpcService extends KeyValueServiceImplBase {

  private final TarantoolDbRepository tarantoolDbRepository;
  private final TarantoolMapper tarantoolMapper;

  @Override
  public void put(KeyValue request, StreamObserver<Empty> responseObserver) {
    try {
      DbKeyValue keyValueEntity = tarantoolMapper.toEntity(request);
      tarantoolDbRepository.save(keyValueEntity);

      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();

    } catch (Exception ex){
      handleInternalError(responseObserver, ex);
    }
  }

  @Override
  public void get(Key request, StreamObserver<Value> responseObserver) {
    String key = request.getKey();
    try {
      Optional<DbKeyValue> keyValueOptional = tarantoolDbRepository.findById(key);

      if(keyValueOptional.isPresent()) {
        DbKeyValue keyValue = keyValueOptional.get();
        Value response = tarantoolMapper.toValue(keyValue);

        responseObserver.onNext(response);
        responseObserver.onCompleted();

      } else {
        log.debug("Provided key not found {}", key);
        responseObserver.onError(Status.NOT_FOUND
            .withDescription("Key '" + key + "' not found")
            .asException());
      }

    } catch (Exception ex){
      handleInternalError(responseObserver, ex);
    }
  }

  @Override
  public void delete(Key request, StreamObserver<Empty> responseObserver) {
    try {
      tarantoolDbRepository.deleteById(request.getKey());

      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();

    } catch (Exception ex){
      handleInternalError(responseObserver, ex);
    }
  }

  @Override
  public void range(KeyRange request, StreamObserver<KeyValue> responseObserver) {
    try (Stream<DbKeyValue> dbKeyValueStream = tarantoolDbRepository.streamByKeyBetween(
        request.getKeySince(), request.getKeyTo(), Limit.of(10))) {

      dbKeyValueStream.forEach(kv -> {
        if (responseObserver instanceof ServerCallStreamObserver<?> serverCall
            && serverCall.isCancelled()) {
          throw new StreamCancelledException();
        }
        KeyValue keyValue = tarantoolMapper.toKeyValue(kv);
        responseObserver.onNext(keyValue);
      });
      responseObserver.onCompleted();

    } catch (StreamCancelledException ex){
      log.warn("Stream request for range '{} - {}' was cancelled",
          request.getKeySince(), request.getKeyTo());

    } catch (Exception ex){
      handleInternalError(responseObserver, ex);
    }
  }

  @Override
  public void count(Empty request, StreamObserver<CountResponse> responseObserver) {
    try {
      long count = tarantoolDbRepository.count();
      CountResponse countResponse = tarantoolMapper.toCountResponse(count);

      responseObserver.onNext(countResponse);
      responseObserver.onCompleted();

    } catch (Exception ex){
      handleInternalError(responseObserver, ex);
    }
  }

  private <T> void handleInternalError(
      StreamObserver<T> responseObserver, Exception ex
  ) {
    log.error("Internal error in gRPC service", ex);
    responseObserver.onError(Status.INTERNAL
        .withDescription("Internal error while processing request")
        .withCause(ex)
        .asException());
  }
}
