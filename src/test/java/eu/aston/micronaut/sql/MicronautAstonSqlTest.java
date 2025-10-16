package eu.aston.micronaut.sql;

import java.util.Map;

import eu.aston.micronaut.sql.where.ConditionContainer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*
drop table test_user;
create table test_user (
 id varchar(36) primary key,
 name varchar(128),
 props text,
 a_street varchar(128),
 a_city varchar(128),
 a_zip varchar(128)
);
 */
@MicronautTest
class MicronautAstonSqlTest {

    @Inject
    UserApi userApi;

    @Inject
    UserService userService;

    @Test
    void testItWorks() {
        userApi.deleteAll();
        User u1 = new User("123", "peter", Map.of("a", "a"));
        userApi.insert(u1);
        userApi.update(u1);
        userApi.save(u1);
        userApi.insert2("124", "peter4", Map.of("b", "b"));
        userApi.update2("124", Map.of("c", "c"));
        System.out.println(userApi.loadById(u1.id()));
        System.out.println(userApi.load1(u1.id()));
        System.out.println(userApi.loadO1(u1.id()));
        System.out.println(userApi.loadO2(u1.id()));
        System.out.println(userApi.loadN1());
        System.out.println(userApi.loadN2().toList());
        System.out.println(userApi.loadN3(ConditionContainer.and().eq("id", "123")));
        userApi.delete(u1);

        userApi.deleteAll();
    }

    @Test
    void testProc(){
        ProcTestOut testOut = new ProcTestOut();
        testOut.setSubject("sdc asasd asascdc");
        userApi.testOut(testOut);
        System.out.println(testOut.getScramble());
        System.out.println(testOut.getFirstChar());
    }

    @Test
    void testItTransaction() {
        try {
            userService.tr();
        } catch (Exception ignore) {
        }
        User u3 = userApi.load1("tr1");
        System.out.println("u3= " + u3);
        Assertions.assertNull(u3, "user after transaction");
    }
}