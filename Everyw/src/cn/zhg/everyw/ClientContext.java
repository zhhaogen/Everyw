/**
 * 创建于 2017年8月31日 下午7:44:02
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import xiaogen.util.Logger;
import static cn.zhg.everyw.Util.*;

/**
 * 
 */
public class ClientContext
{
	private Map<String, Object> session;
	private ServerContext serverContext;
	private Socket socket;
	private List<Object> apis;

	private DataOutputStream writer;
	private DataInputStream reader;

	/**
	 * @param serverContext
	 * @param socket
	 * @param funs
	 * @throws IOException
	 */
	ClientContext(ServerContext serverContext, Socket socket, List<Function<ClientContext, Object>> funs)
			throws IOException
	{
		this.serverContext = serverContext;
		this.socket = socket;
		writer = new DataOutputStream(socket.getOutputStream());
		reader = new DataInputStream(socket.getInputStream());
		// 初始化能力
		apis = new ArrayList<>();
		for (Function<ClientContext, Object> fun : funs)
		{
			Object api = fun.apply(this);
			apis.add(api);
		}
		new ReadWoker().start();
	}

	/**
	 * 返回服务上下文
	 */
	public ServerContext getServerContext()
	{
		return serverContext;
	}

	/**
	 * 返回客户端能力
	 * 
	 * @param <T>
	 * @param clz
	 */
	public <T> T getListener(Class<T> clz)
	{
		Logger.d("返回能力");
		// 检验客户端是否有这能力

		InvocationHandler handler = new InvocationHandler()
		{
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
			{
				// 向客户端发送数据
				Logger.d("向客户端发送消息");
				long bid = System.currentTimeMillis();// 消息块id
				StringBuffer head = new StringBuffer("clz=" + clz.getName() + ";mth=" + method.getName() + ";type="
						+ method.getGenericReturnType() + ";length=" + args.length + ";bid=" + bid + ";");
				Logger.d(head);
				if (args.length != 0)
				{
					head.append(Object2String(args));
				}
				writer.writeUTF(head.toString());
				writer.flush();
				// 为了简化,客户端能力不返回数据
				return null;
			}
		};

		return (T) Proxy.newProxyInstance(clz.getClassLoader(), new Class[]
		{ clz }, handler);

	}

	/**
	 * @param create
	 */
	public Map<String, Object> getSession(boolean create)
	{
		if (create && session == null)
		{
			session = new HashMap<>();
		}
		return session;
	}

	/**
	 * 读取线程
	 */
	private class ReadWoker extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				String line = null;
				while ((line = reader.readUTF()) != null)
				{
					Logger.d("接收指令");
					String[] datas = line.split(";");
					String cls = datas[0].split("=")[1];
					if (datas.length >= 4)// 调用服务功能
					{
						Class clz = Class.forName(cls);
						String mth = datas[1].split("=")[1];
						String type = datas[2].split("=")[1];
						int alen = Integer.parseInt(datas[3].split("=")[1]);
						long bid = Long.parseLong(datas[4].split("=")[1]);
						Logger.d("调用服务器功能:" + datas[0] + ";" + datas[1] + ";" + datas[2] + ";" + datas[3] + ";"
								+ datas[3]);
						Object[] args = null;
						if (alen > 0)
						{
							args = String2Objects(datas[5], alen);
						}
						for (Object api : apis)
						{ 
							Class apz = api.getClass();
							if (clz.isAssignableFrom(apz))
							{
								try
								{
									Method ath = findMethod(apz, mth, type, alen);
									Object robj = ath.invoke(api, args);
									writer.writeUTF("bid=" + bid + ";" + Object2String(new Object[]
									{ robj }));
									Logger.d("写入完毕!");
								} catch (IllegalAccessException | IllegalArgumentException
										| InvocationTargetException e)
								{
									e.printStackTrace();
									// TODO 异常放入返回?
								}
								break;
							}
						}
					} else// 注册客户端功能
					{
						long bid = Long.parseLong(datas[1].split("=")[1]);
						Logger.d("注册客服端能力:" + cls);
						// TODO 校验服务端类 cls为客户端具体类

					}
				}
			} catch (IOException | ClassNotFoundException e)
			{
				e.printStackTrace();
			}finally
			{
				serverContext.getClientContexts().remove(ClientContext.this);
			}
		}
	}
}
