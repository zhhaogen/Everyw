/**
 * ������ 2017��8��30�� ����3:51:47
 * @author zhg
 */
package cn.zhg.everyw.demo;

import cn.zhg.everyw.bean.Message;

/**
 * ����������
 */
public interface Api
{
	/**
	 * ע���û�
	 * @param name
	 * @return
	 */
	Message<Void> regist(String name);

	/**
	 * ����������Ϣ
	 * @param message
	 */
	Message<Void> sendMessage(String message);
}
