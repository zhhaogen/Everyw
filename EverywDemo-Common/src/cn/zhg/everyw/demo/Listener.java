/**
 * 创建于 2017年8月30日 下午4:16:37
 * @author zhg
 */
package cn.zhg.everyw.demo;

/**
 * 客户端能力
 */
public interface Listener
{
	/**
	 * 接收到消息
	 */
	void onMessage(String msg);
}
