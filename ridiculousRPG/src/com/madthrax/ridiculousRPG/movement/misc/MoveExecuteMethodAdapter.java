package com.madthrax.ridiculousRPG.movement.misc;

import java.lang.reflect.Method;

import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

/**
 * This MovementAdapter allows you to invoke any method on any object.<br>
 * Feel free to do whatever you want;)<br>
 * Furthermore you are able to define a condition. If you do so, the method
 * will only be executed if this condition returns the boolean value true.<br>
 * This move cannot be blocked.<br>
 * You may use this {@link MovementHandler} stand alone (without an event).
 */
public class MoveExecuteMethodAdapter extends MovementHandler {
	private Object objectForInvocation;
	private Method methodToInvoke;
	private Object[] methodParameter;

	private Object objectForConditionInvocation;
	private Method methodToInvokeCondition;
	private Object[] methodParameterCondition;

	protected MoveExecuteMethodAdapter(Object objectForInvocation, Method methodToInvoke, Object[] methodParameter) {
		this.objectForInvocation = objectForInvocation;
		this.methodToInvoke = methodToInvoke;
		this.methodParameter = methodParameter;
	}
	/**
	 * This MovementAdapter allows you to invoke any method on any object.<br>
	 * Feel free to do whatever you want;)<br>
	 * This move cannot be blocked.
	 * @param objectForInvocation
	 * If the method is static, this argument should be null.
	 * @param methodToInvoke
	 * @param methodParameter
	 */
	public static MovementHandler $(Object objectForInvocation, Method methodToInvoke, Object... methodParameter) {
		return new MoveExecuteMethodAdapter(objectForInvocation, methodToInvoke, methodParameter);
	}
	/**
	 * Define a condition. If you do so, the main method
	 * will only be invoked if this condition (invocation) returns the boolean value true.<br>
	 * @param objectForInvocation
	 * If the method is static, this argument should be null.
	 * @param methodToInvoke
	 * @param methodParameter
	 */
	public void setCondition(Object objectForInvocation, Method methodToInvoke, Object... methodParameter) {
		this.objectForConditionInvocation = objectForInvocation;
		this.methodToInvokeCondition = methodToInvoke;
		this.methodParameterCondition = methodParameter;
	}
	@Override
	public void tryMove(Movable movable, float deltaTime) {
		try {
			if (methodToInvokeCondition==null) {
				methodToInvoke.invoke(objectForInvocation, methodParameter);
			} else {
			    Object result = methodToInvokeCondition.invoke(objectForConditionInvocation, methodParameterCondition);
			    if (result instanceof Boolean && (Boolean)result) {
					methodToInvoke.invoke(objectForInvocation, methodParameter);
			    }
			}
		} catch (Exception e) {
			// eat all exceptions and print them out onto the shell
			e.printStackTrace();
		}
		finished = true;
	}
}
