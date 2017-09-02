/**
 * ������ 2017��8��30�� ����4:13:58
 * @author zhg
 */
package cn.zhg.everyw.demo;

import java.util.Scanner;

import cn.zhg.everyw.ApplicationContext;
import xiaogen.util.Logger;

/**
 * һ���򵥵Ŀͻ�����������
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
			System.out.println("��Ϣ:" + message);
		};
		context.registListener(listener);
		try (Scanner br = new Scanner(System.in))
		{
			System.out.println("�������������:");
			String line = br.nextLine();
			api.regist(line).error(e -> {
				System.err.println("ע��ʧ��:" + e);
				System.exit(1);
			});
			while (true)
			{
				line = br.nextLine();
				api.sendMessage(line).error(e->{
					System.out.println("���ͳɹ�");
				}).error(e->{
					System.err.println("����ʧ��:"+e);
				});
			}
		}
	}

}
