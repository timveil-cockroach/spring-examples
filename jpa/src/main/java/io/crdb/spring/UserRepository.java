package io.crdb.spring;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {

    List<User> findByUpdatedTimestampIsNull();

    @Modifying
    @Query("update User u set u.updatedTimestamp = :timestamp where u.updatedTimestamp is null")
    int updateTimestamp(@Param("timestamp") ZonedDateTime timestamp);
}
