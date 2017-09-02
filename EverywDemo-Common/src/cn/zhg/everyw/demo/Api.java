/**
 * 创建于 2017年8月30日 下午3:51:47
 * @author zhg
 */
package cn.zhg.everyw.demo;

import cn.zhg.everyw.bean.Message;

/**
 * 服务器功能
 */
public interface Api
{
	/**
	 * 注册用户
	 * @param name
	 * @return
	 */
	Message<Void> regist(String name);

	/**
	 * 发送聊天信息
	 * @param message
	 */
	Message<Void> sendMessage(String message);
}
