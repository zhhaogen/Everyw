/**
 * 创建于 2017年8月31日 下午7:28:00
 * @author zhg
 */
package cn.zhg.everyw;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import xiaogen.util.Logger;

/**
 * 
 */
public class ServerContext implements Runnable
{
	private int port;
	private List<ClientContext> clients;
	private List<Function<ClientContext, Object>> funs;
	private ServerContext(int port)
	{
		this.port = port;
		clients = new ArrayList<>();
		funs = new ArrayList<>();
	}

	/**
	 * 创建服务器
	 * 
	 * @param port
	 * @return
	 */
	public static ServerContext create(int port)
	{
		return new ServerContext(port);
	}

	/**
	 * 暴露服务给客户端
	 * 
	 * @param fun
	 */
	public void bindService(Function<ClientContext, Object> fun)
	{
		this.funs.add(fun);
	}

	/**
	 * 返回所有客户端上下文
	 */
	public List<ClientContext> getClientContexts()
	{
		return clients;
	}

	/**
	 * 启动服务
	 */
	public void start()
	{
		new Thread(this).start();
	}

	@Override
	public void run()
	{
		Logger.d("正在启动服务");
		try (ServerSocket server = new ServerSocket(port);)
		{
			while (true)
			{
				Socket socket = server.accept();
				Logger.d("新客户端连接");
				ClientContext client = new ClientContext(this, socket,funs);
				clients.add(client); 
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
