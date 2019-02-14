package com.thoughtmechanix.licenses.hestrix;

import com.thoughtmechanix.licenses.utils.UserContext;
import com.thoughtmechanix.licenses.utils.UserContextHolder;

import java.util.concurrent.Callable;

public final class DelegatingUserContextCallable<V> implements Callable<V> {
    /** 受 hystrix 保护的方法的句柄 */
    private final Callable<V> delegate;
    private UserContext originalUserContext;

    /***
     *
     * @param delegate
     * @param originalUserContext 自定义回调类将通过原始的回调类
     * 传递，该类将调用你的 Hystrix 受保护
     * 的代码，UserContext 来自父线程
     */
    public DelegatingUserContextCallable(Callable<V> delegate, UserContext originalUserContext) {
        this.delegate = delegate;
        this.originalUserContext = originalUserContext;
    }

    @Override
    // 通过 @HystrixCommand 注解保护方法之前call()方法被调用。
    public V call() throws Exception {
        //设置 UserContext 。ThreadLocal 变 量 存 储
        // UserContext，它与正在运行线程的 Hystrix 保护方法关联
        UserContextHolder.setContext(originalUserContext);
        try {
            // 在 Hystrix 保护方法上，一旦 UserContext 被
            //设 置 调 用 call() 方法；例如，你的
            //LicenseServer.getLicenseByOrg()方法。
            return delegate.call();
        } finally {
            originalUserContext = null;
        }
    }

    public static <V> Callable<V> create(Callable<V> delegate, UserContext userContext) {
        return new DelegatingUserContextCallable<>(delegate, userContext);
    }
}
