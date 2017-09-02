/**
 * ������ 2017��8��31�� ����8:42:35
 * @author zhg
 */
package cn.zhg.everyw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Base64;

import xiaogen.util.Logger;

/**
 * ������
 */
public class Util
{
	public static Method findMethod(Class clz,String method,String type,int alen)
	{
		Method[] mths = clz.getMethods();
		if(mths!=null)
		{
			for(Method mth:mths)
			{ 
				if(method.equals(mth.getName())&&type.equals(mth.getGenericReturnType().toString())&&alen==mth.getParameters().length)
				{
					mth.setAccessible(true);
					return mth;
				}
			}
		}
		return null; 
	}
	/**
	 * ת��ΪObject����
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object[] String2Objects(String data,int size) throws IOException, ClassNotFoundException
	{
		byte[] bytes = Base64.getDecoder().decode(data);
		ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(bytes));
		Object[] objs = new Object[size];
		for(int i=0;i<size;i++)
		{
			objs[i]=oi.readObject();
		}
		return objs;
	}
	/**
	 * ת��ΪObject
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object String2Object(String data) throws IOException, ClassNotFoundException
	{
		byte[] bytes = Base64.getDecoder().decode(data);
		ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(bytes));
		return oi.readObject();
	}

	/**
	 * ת��Ϊ�ַ���
	 * 
	 * @param obj
	 * @return
	 * @throws IOException 
	 */
	public static String Object2String(Object[] objs) throws IOException
	{
		return Base64.getEncoder().encodeToString(Object2Bytes(objs));
	}

	/**
	 * ת��Ϊbyte����
	 * 
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public static byte[] Object2Bytes(Object[] objs) throws IOException
	{
		ByteArrayOutputStream bot = new ByteArrayOutputStream(); 
		ObjectOutputStream ot = new ObjectOutputStream(bot);
		for (int i = 0; i < objs.length; i++)
		{
			ot.writeObject(objs[i]);
		} 
		return bot.toByteArray();
	}
}
