/**
 * 创建于 2017年8月30日 下午3:52:46
 * @author zhg
 */
package cn.zhg.everyw.bean;

import java.util.function.Consumer;

/**
 * 消息体封装类
 */
public class Message<T> implements java.io.Serializable
{ 
	private T value;
	private String error;
	private boolean hashError;
	public	Message( )
	{  
	}
	public	Message(T obj)
	{
		this.value=obj;
	}
	public	Message(T obj,String error)
	{
		this.value=obj;
		this.error=error;
		hashError=true;
	}
	
	/**
	 * 是否有异常
	 * 
	 * @return
	 */
	public boolean hasError()
	{
		return hashError;
	}

	/**
	 * 返回默认信息
	 * 
	 * @return
	 */
	public String getError()
	{
		return error;
	}
	/**
	 * 有网络异常时调用
	 * @param action
	 * @return
	 */
	public Message<T> error(Consumer<String> action)
	{
		if(hashError)
		{
			action.accept(error);
		} 
		return this;
	}
	/**
	 * 成功返回时调用
	 * @param action
	 * @return
	 */
	public Message<T> success(Consumer<Void> action)
	{
		if(error==null)
		{
			action.accept(null);
		} 
		return this;
	}
	/**
	 * 有返回值时返回
	 * @param action
	 * @return
	 */
	public Message<T> value(Consumer<T> action)
	{
		if(value!=null)
		{
			action.accept(value);
		}
		return this;
	}
	public T getValue()
	{
		return value;
	}
	/**
	 * @param error
	 */
	public void setError(String error)
	{
		this.error=error;
		hashError=true;
	}

	 
}
