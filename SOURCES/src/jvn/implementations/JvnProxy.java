/***
 * JAVANAISE Implementation
 * JvnProxy class
 * make transparent the use of a shared object
 *
 */

package jvn.implementations;


import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import jvn.interfaces.JvnObject;
import jvn.utils.JvnException;
import jvn.utils.MethodType;

public class JvnProxy implements InvocationHandler {
	
	private JvnObject jo;

	/**
	 * Create a shared object obj with the name name if it doesn't exits yet.
	 * @param obj the object to share
	 * @param name the name of the shared object
	 * @throws JvnException
	 */
	private JvnProxy(Serializable obj, String name) throws JvnException {
		// initialize/get the JVN server
		JvnServerImpl server = JvnServerImpl.jvnGetServer();
		
		this.jo = server.jvnLookupObject(name);
		
		// look up the IRC object in the JVN server
		// if not found, create it, and register it in the JVN server
		if(jo==null){
			this.jo = server.jvnCreateObject(obj);	
			if(jo == null){
				throw new JvnException("Cannot create object. Server is full");
			}
			
			this.jo.jvnUnLock();
			server.jvnRegisterObject(name, jo);
		}
	}

	/**
	 * Create a new Proxy instance that allows to share the object obj and register it (if it doesn't exist) in the store with the name name.
	 * @param obj the object to share
	 * @param name the name of the shared object
	 * @return the shared object 
	 * @throws IllegalArgumentException
	 * @throws JvnException
	 */
	public static Object newInstance(Serializable obj, String name) throws IllegalArgumentException, JvnException {
		return java.lang.reflect.Proxy.newProxyInstance(
				obj.getClass().getClassLoader(),
				obj.getClass().getInterfaces(),
				new JvnProxy(obj, name));
	}

	/**
	 * Interception of the method invocations to lock and unlock the shared object
	 */
	public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
		String type;
		MethodType methodType = method.getAnnotation(MethodType.class);
		
		// Determine the lock to use by looking at the MethodType annotation (read or write)
		if(methodType != null){
			type = methodType.type();
			
			if(type.compareTo("read") == 0){
				jo.jvnLockRead();
			}else if(type.compareTo("write") == 0){
				jo.jvnLockWrite();
			}else{
				throw new JvnException("Erreur lors de l'invocation de la methode "+method.getName()+". Cette methode possede un type incorrect.");
			}
		}else{
			throw new JvnException("Erreur lors de l'invocation de la methode "+method.getName()+". Cette methode n'a pas de type.");
		}
		
		// Invoke the method
		Object result = method.invoke(jo.jvnGetObjectState(), args);

		// unlock the object 
		jo.jvnUnLock();
		
		// Return the result of the invocation
		return result;
	}

}
