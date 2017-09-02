/**
 * 创建于 2017年8月30日 下午4:13:58
 * @author zhg
 */
package cn.zhg.everyw.demo;

import java.util.Scanner;

import cn.zhg.everyw.ApplicationContext;
import xiaogen.util.Logger;

/**
 * 一个简单的客户端聊天例子
 */
public class Client
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Logger.debug=false;
		String url = "127.0.0.1:8080";
		ApplicationContext context = ApplicationContext.getContext(url);
		Api api = context.getService(Api.class);
		Listener listener = message -> {
			System.out.println("消息:" + message);
		};
		context.registListener(listener);
		try (Scanner br = new Scanner(System.in))
		{
			System.out.println("请输入你的名字:");
			String line = br.nextLine();
			api.regist(line).error(e -> {
				System.err.println("注册失败:" + e);
				System.exit(1);
			});
			while (true)
			{
				line = br.nextLine();
				api.sendMessage(line).error(e->{
					System.out.println("发送成功");
				}).error(e->{
					System.err.println("发送失败:"+e);
				});
			}
		}
	}

}
