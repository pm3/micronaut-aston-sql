package eu.aston.micronaut.sql.aop.tr;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

@Singleton
@InterceptorBean(Transactional.class)
@Requires(bean = ThreadTrDbc.class)
public class TransactionalInterceptor implements MethodInterceptor<Object, Object> {

    private final ThreadTrDbc threadTrDbc;

    public TransactionalInterceptor(ThreadTrDbc threadTrDbc) {
        this.threadTrDbc = threadTrDbc;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        Object resp = null;
        try {
            threadTrDbc.startTransaction();
            resp = context.proceed();
            threadTrDbc.close(true);
        } catch (Exception e) {
            try {
                threadTrDbc.close(false);
            } catch (Exception ignore) {
            }
            if (e instanceof RuntimeException rte) throw rte;
            throw new RuntimeException(e);
        }
        return resp;
    }
}
