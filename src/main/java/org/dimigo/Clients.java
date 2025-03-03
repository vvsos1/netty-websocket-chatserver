package org.dimigo;

import org.json.simple.JSONObject;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;

/**
 * <pre>
 * DimiCactus
 * 	 |_ Clients
 *
 * 1. 개요 : 
 * 2. 작성일 : 2017. 10. 26.
 * <pre>
 *
 * @author : 박명규(로컬계정)
 * @version : 1.0
 */
public class Clients {
	
	private ChannelGroup clients;
	
	public Clients(ChannelGroup clients) {
		this.clients = clients;
	}
	
	public void sendToAll(JSONObject json) {
		System.out.println(json.toJSONString());
		clients.writeAndFlush(new TextWebSocketFrame(json.toJSONString()));
	}
	
	public void findChannelAndWrite(String id, JSONObject json) {
		clients.stream()
				.filter(c -> id.equals(c.attr(AttributeKey.valueOf("id")).get()))
				.findFirst()
				.get().writeAndFlush(new TextWebSocketFrame(json.toJSONString()));
	}
	
	
}
