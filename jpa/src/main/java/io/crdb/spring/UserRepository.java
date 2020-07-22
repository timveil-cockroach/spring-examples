package io.crdb.spring;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {

    @Modifying
    @Query(value = "SELECT crdb_internal.force_retry('1s')", nativeQuery = true)
    void forceRetry();

}
