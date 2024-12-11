package com.ericlam.mc.eld.factories;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import com.google.inject.Injector;

public final class FactoryInvocationHandler implements InvocationHandler {

	private final Map<Class<?>, Class<?>> typeMapping;
	private final Injector injector;

	public FactoryInvocationHandler(Map<Class<?>, Class<?>> typeMapping, Injector injector) {
		this.typeMapping = typeMapping;
		this.injector = injector;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		var implementationCls = typeMapping.getOrDefault(method.getReturnType(), method.getReturnType());
		var constructorArgs = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
		try {
			var constructor = implementationCls.getConstructor(constructorArgs);
			var instance = constructor.newInstance(args);
			injector.injectMembers(instance);
			return instance;
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("No such constructor found for " + implementationCls.getName() + " with args: " + Arrays.toString(constructorArgs), e);
		}
	}
}
