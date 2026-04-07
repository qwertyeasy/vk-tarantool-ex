package vk.tarantool.repository;

import java.util.stream.Stream;
import org.springframework.data.domain.Limit;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import vk.tarantool.data.DbKeyValue;

@Repository
public interface TarantoolDbRepository
    extends CrudRepository<DbKeyValue, String>,
    PagingAndSortingRepository<DbKeyValue, String> {

  Stream<DbKeyValue> streamByKeyBetween(String keySince, String keyTo, Limit limit);
}
