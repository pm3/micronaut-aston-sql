package eu.aston.micronaut.sql;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import eu.aston.micronaut.sql.aop.Query;
import eu.aston.micronaut.sql.aop.SqlApi;
import eu.aston.micronaut.sql.entity.Format;
import eu.aston.micronaut.sql.where.ICondition;
import jakarta.inject.Singleton;

@Singleton
@SqlApi("a1")
public interface UserApi {

    User loadById(String id);

    void save(User user);

    void insert(User user);

    void update(User user);

    void delete(User user);

    @Query("select * from test_user where id=:id")
    User load1(String id);

    @Query("select * from test_user where id=:id")
    Optional<User> loadO1(String id);

    @Query("select id from test_user where id=:id")
    Optional<String> loadO2(String id);

    @Query("select * from test_user")
    List<User> loadN1();

    @Query("select * from test_user")
    Stream<User> loadN2();

    @Query("select * from test_user where :where")
    List<User> loadN3(ICondition where);

    @Query("insert into test_user (id, name, props) values (:id, :name, :props)")
    void insert2(String id, String name, @Format("json") Map<String, String> props);

    @Query("update test_user set props=:props where id=:id")
    void update2(String id, @Format("json") Map<String, String> props);

    @Query("delete from test_user")
    void deleteAll();

    @Query("""
    {call public.fn_sqltestout(
        subject,
        scramble:VARCHAR,
        firstChar:VARCHAR)}
    """)
    void testOut(ProcTestOut testOut);
}
