package eu.aston.micronaut.sql;

import java.util.HashMap;

import eu.aston.micronaut.sql.aop.tr.Transactional;
import jakarta.inject.Singleton;

@Singleton
public class UserService {
    private final UserApi userApi;

    public UserService(UserApi userApi) {
        this.userApi = userApi;
    }

    @Transactional
    public void tr() {
        User u1 = new User("tr1","tr1",new HashMap<>());
        userApi.insert(u1);

        User u2 = new User("tr2", "tr2", new HashMap<>());
        userApi.insert(u2);

        throw new RuntimeException("rollback");
    }
}
