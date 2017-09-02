/**
 * 创建于 2017年8月31日 上午11:06:50
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
	 * 服务实现者
	 */
	public static class ServerWoker implements Api
	{
		private ClientContext client;
		private   String name;

		/**
		 * 
		 * @param client
		 *            当前客户端上下文
		 */
		public ServerWoker(ClientContext client)
		{
			this.client = client;
		}

		@Override
		public Message<Void> regist(String name)
		{
			Logger.d(client +"注册用户名:"+name);
			this.name = name;
			List<ClientContext> clients = client.getServerContext().getClientContexts();
			if (clients != null)
			{
				for (ClientContext c : clients)
				{
					if (name.equals(c.getSession(true).get("name")))
					{
						return new Message<Void>(null, "用户名已存在");
					}
				}
			} 
			// 储存用户名
			client.getSession(true).put("name", name);
			return new Message<Void>();
		}

		@Override
		public Message<Void> sendMessage(String message)
		{
			Logger.d("客户端["+client +"]发送消息:"+name+":"+message);
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
		//创建服务上下文
		ServerContext context = ServerContext.create(8080);
		//为每个客户端绑定能力
		context.bindService(client -> {
			return new ServerWoker(client);
		});
		context.start();// 启动服务
	}

}
