/**
 * 创建于 2017年8月30日 下午4:11:43
 * @author zhg
 */
package cn.zhg.everyw;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.zhg.everyw.bean.Message;
import xiaogen.util.Logger;
import static cn.zhg.everyw.Util.*;
/**
 * 
 */
public class ApplicationContext
{
	private static ApplicationContext context;
	private String url;
	private int port;
	private boolean isStart;
	private DataOutputStream writer;
	private DataInputStream reader;
	private Socket socket;
	private List listeners;
	private Map<Long,String> map;
	private Object lock=new Object();
	private ApplicationContext(String url)
	{

		int i = url.lastIndexOf(":");
		if (i == -1)
		{
			this.url = url;
			this.port = 80;
		} else
		{
			this.url = url.substring(0, i);
			this.port = Integer.parseInt(url.substring(i + 1));
		}
		isStart = false;
		listeners=new ArrayList<>(); 
		map=new HashMap<>();
	}

	/**
	 * 启动连接 
	 * @throws IOException @throws
	 */
	private synchronized void start()
	{
		try
		{
			socket = new Socket(url, port);
			writer = new DataOutputStream(socket.getOutputStream());
			reader = new DataInputStream(socket.getInputStream());
			new ReadWorker().start();
			isStart = true;
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void close()
	{
		if (isStart)
		{
			try
			{
				writer.close();
				reader.close();
				socket.close();
			} catch (Exception igr)
			{
			}
		}
	}

	/**
	 * 获取上下文实现
	 * 
	 * @param url
	 * @return
	 */
	public static ApplicationContext getContext(String url)
	{
		if (context == null)
		{
			context = new ApplicationContext(url);
		} 
		return context;
	}

	/**
	 * 获取api服务
	 * 
	 * @param clz
	 * @param url
	 * @return
	 */
	public <T> T getService(Class<T> clz)
	{
		if (!isStart)
		{
			start();
		}
		InvocationHandler handler = new InvocationHandler()
		{

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Exception
			{
				try
				{
					// 向服务端发送数据
					long bid=System.currentTimeMillis();//消息块id
					StringBuffer head = new StringBuffer("clz=" + clz.getName() + ";mth="
							+ method.getName()+";type="+method.getGenericReturnType() + ";length=" + args.length + ";bid="+bid+";");
					Logger.d(head);
					if (args.length != 0)
					{
						head.append(Object2String(args));
					}
					writer.writeUTF(head.toString());
					writer.flush();
					Logger.d("发送完消息");
					//等待来接服务器的消息
					String data =getAndwait(bid);
					Logger.d("接收消息");
					return String2Object(data);
					// 服务端接收数据
				} catch (Throwable ex)
				{
					ex.printStackTrace();
					Message result = (Message) method.getReturnType().newInstance();
					result.setError(ex.getMessage());
					return result;
				}
			}
		};
		return (T) Proxy.newProxyInstance(clz.getClassLoader(), new Class[]
		{ clz }, handler);
	} 

	/**
	 * 阻塞等待返回消息包
	 * @param key
	 * @return
	 */
	private   String  getAndwait(long key)
	{ 
		synchronized(lock)
		{
			while(!map.containsKey(key))
			{
				try
				{
					lock.wait();
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
		return map.get(key);
	}

	/**
	 * 注册能力
	 */
	public void registListener(Object listener)
	{
		listeners.add(listener);
		Class  clz = listener.getClass();
		long bid=System.currentTimeMillis();//消息块id
		StringBuffer head = new StringBuffer("clz=" + clz.getName()  + ";bid="+bid+";");
		try
		{
			writer.writeUTF(head.toString());
			writer.flush();
		} catch (IOException e)
		{
			e.printStackTrace();
		} 
	}
	private class ReadWorker extends Thread
	{
		public void run()
		{
			String line=null;
			try
			{
				while((line=reader.readUTF())!=null)
				{ 
					String[] datas = line.split(";"); 
					if(datas.length==2)//服务端能力回调
					{
						long bid=Long.parseLong(datas[0].split("=")[1]);
						map.put(bid, datas[1]);
						Logger.d("服务端能力回调:"+bid); 
						synchronized(lock)
						{
							lock.notifyAll();
						} 
					}else//服务器调用客户端能力
					{
						String cls = datas[0].split("=")[1];
						Class clz = Class.forName(cls);
						String mth = datas[1].split("=")[1];
						String type = datas[2].split("=")[1];
						int alen = Integer.parseInt(datas[3].split("=")[1]);
						long bid = Long.parseLong(datas[4].split("=")[1]);
						Logger.d("调用客户端能力:"+datas[0]+";"+datas[1]);
						Object[] args = null;
						if (alen > 0)
						{
							args = String2Objects(datas[5], alen);
						}
						for (Object listener : listeners)
						{ 
							Class lcz = listener.getClass();
							if (clz.isAssignableFrom(lcz))
							{  
								try
								{
									Method ath = findMethod(lcz, mth, type, alen);
									Logger.d("找到能力方法:"+ath);
									ath.invoke(listener, args);
								} catch (IllegalAccessException | IllegalArgumentException
										| InvocationTargetException e)
								{
									e.printStackTrace();
								}
							} 
						}
					}
				}
			} catch (IOException | ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}
}
