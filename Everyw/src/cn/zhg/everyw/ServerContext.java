/**
 * ������ 2017��8��31�� ����7:28:00
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
	 * ����������
	 * 
	 * @param port
	 * @return
	 */
	public static ServerContext create(int port)
	{
		return new ServerContext(port);
	}

	/**
	 * ��¶������ͻ���
	 * 
	 * @param fun
	 */
	public void bindService(Function<ClientContext, Object> fun)
	{
		this.funs.add(fun);
	}

	/**
	 * �������пͻ���������
	 */
	public List<ClientContext> getClientContexts()
	{
		return clients;
	}

	/**
	 * ��������
	 */
	public void start()
	{
		new Thread(this).start();
	}

	@Override
	public void run()
	{
		Logger.d("������������");
		try (ServerSocket server = new ServerSocket(port);)
		{
			while (true)
			{
				Socket socket = server.accept();
				Logger.d("�¿ͻ�������");
				ClientContext client = new ClientContext(this, socket,funs);
				clients.add(client); 
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
