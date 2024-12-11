package com.ericlam.mc.eld.factories;

import java.lang.reflect.Proxy;
import java.util.Map;

import javax.inject.Provider;

import com.google.inject.Inject;
import com.google.inject.Injector;

public final class ELDFactoryProvider<T> implements Provider<T> {

	@Inject
	private Injector injector;

	private final Class<T> factoryCls;
	private final Map<Class<?>, Class<?>> typeMapping;

	public ELDFactoryProvider(Class<T> factoryCls, Map<Class<?>, Class<?>> typeMapping) {
		this.factoryCls = factoryCls;
		this.typeMapping = typeMapping;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get() {
		return (T) Proxy.newProxyInstance(
				this.getClass().getClassLoader(),
				new Class[]{factoryCls},
				new FactoryInvocationHandler(typeMapping, injector)
		);
	}

}
