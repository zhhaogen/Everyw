/**
 * ������ 2017��8��31�� ����11:06:50
 * @author zhg
 */
package cn.zhg.everyw.demo;

import java.util.List;
import cn.zhg.everyw.ClientContext;
import cn.zhg.everyw.ServerContext;
import cn.zhg.everyw.bean.Message;
import xiaogen.util.Logger;

/**
 * 
 */
public class Server
{
	/**
	 * ����ʵ����
	 */
	public static class ServerWoker implements Api
	{
		private ClientContext client;
		private   String name;

		/**
		 * 
		 * @param client
		 *            ��ǰ�ͻ���������
		 */
		public ServerWoker(ClientContext client)
		{
			this.client = client;
		}

		@Override
		public Message<Void> regist(String name)
		{
			Logger.d(client +"ע���û���:"+name);
			this.name = name;
			List<ClientContext> clients = client.getServerContext().getClientContexts();
			if (clients != null)
			{
				for (ClientContext c : clients)
				{
					if (name.equals(c.getSession(true).get("name")))
					{
						return new Message<Void>(null, "�û����Ѵ���");
					}
				}
			} 
			// �����û���
			client.getSession(true).put("name", name);
			return new Message<Void>();
		}

		@Override
		public Message<Void> sendMessage(String message)
		{
			Logger.d("�ͻ���["+client +"]������Ϣ:"+name+":"+message);
			List<ClientContext> clients = client.getServerContext().getClientContexts();
			if (clients != null)
			{
				clients.forEach(c -> {
					Listener listener = c.getListener(Listener.class);
					if (listener != null)
					{
						listener.onMessage(name + ":" + message);
					}
				});
			}
			return new Message<Void>();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		//��������������
		ServerContext context = ServerContext.create(8080);
		//Ϊÿ���ͻ��˰�����
		context.bindService(client -> {
			return new ServerWoker(client);
		});
		context.start();// ��������
	}

}
