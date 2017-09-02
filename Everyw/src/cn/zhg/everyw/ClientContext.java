/**
 * ������ 2017��8��31�� ����7:44:02
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
		// ��ʼ������
		apis = new ArrayList<>();
		for (Function<ClientContext, Object> fun : funs)
		{
			Object api = fun.apply(this);
			apis.add(api);
		}
		new ReadWoker().start();
	}

	/**
	 * ���ط���������
	 */
	public ServerContext getServerContext()
	{
		return serverContext;
	}

	/**
	 * ���ؿͻ�������
	 * 
	 * @param <T>
	 * @param clz
	 */
	public <T> T getListener(Class<T> clz)
	{
		Logger.d("��������");
		// ����ͻ����Ƿ���������

		InvocationHandler handler = new InvocationHandler()
		{
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
			{
				// ��ͻ��˷�������
				Logger.d("��ͻ��˷�����Ϣ");
				long bid = System.currentTimeMillis();// ��Ϣ��id
				StringBuffer head = new StringBuffer("clz=" + clz.getName() + ";mth=" + method.getName() + ";type="
						+ method.getGenericReturnType() + ";length=" + args.length + ";bid=" + bid + ";");
				Logger.d(head);
				if (args.length != 0)
				{
					head.append(Object2String(args));
				}
				writer.writeUTF(head.toString());
				writer.flush();
				// Ϊ�˼�,�ͻ�����������������
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
	 * ��ȡ�߳�
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
					Logger.d("����ָ��");
					String[] datas = line.split(";");
					String cls = datas[0].split("=")[1];
					if (datas.length >= 4)// ���÷�����
					{
						Class clz = Class.forName(cls);
						String mth = datas[1].split("=")[1];
						String type = datas[2].split("=")[1];
						int alen = Integer.parseInt(datas[3].split("=")[1]);
						long bid = Long.parseLong(datas[4].split("=")[1]);
						Logger.d("���÷���������:" + datas[0] + ";" + datas[1] + ";" + datas[2] + ";" + datas[3] + ";"
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
									Logger.d("д�����!");
								} catch (IllegalAccessException | IllegalArgumentException
										| InvocationTargetException e)
								{
									e.printStackTrace();
									// TODO �쳣���뷵��?
								}
								break;
							}
						}
					} else// ע��ͻ��˹���
					{
						long bid = Long.parseLong(datas[1].split("=")[1]);
						Logger.d("ע��ͷ�������:" + cls);
						// TODO У�������� clsΪ�ͻ��˾�����

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
