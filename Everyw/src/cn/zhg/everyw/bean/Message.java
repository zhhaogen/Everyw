/**
 * ������ 2017��8��30�� ����3:52:46
 * @author zhg
 */
package cn.zhg.everyw.bean;

import java.util.function.Consumer;

/**
 * ��Ϣ���װ��
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
	 * �Ƿ����쳣
	 * 
	 * @return
	 */
	public boolean hasError()
	{
		return hashError;
	}

	/**
	 * ����Ĭ����Ϣ
	 * 
	 * @return
	 */
	public String getError()
	{
		return error;
	}
	/**
	 * �������쳣ʱ����
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
	 * �ɹ�����ʱ����
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
	 * �з���ֵʱ����
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
