package org.dimigo;

import java.util.Map.Entry;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

/**
 * <pre>
 * nettyInActionWebSocket
 * 	 |_ CommonUtil
 *
 * 1. 개요 : 
 * 2. 작성일 : 2017. 11. 4.
 * <pre>
 *
 * @author : 박명규(로컬계정)
 * @version : 1.0
 */
public class CommonUtil {
	
	public static void printHandlerFromPipeline(ChannelPipeline pipeline) {
		System.out.println("=======핸들러 목록=======");
		for (Entry<String, ChannelHandler> handler : pipeline) {
			System.out.println(handler.getKey() + " : " + handler.getValue().getClass().getSimpleName());
		}
		System.out.println("=======핸들러 목록=======");
	}
}
