package org.dimigo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * <pre>
 * nettyInActionWebSocket
 * 	 |_ ChatServerInitializer
 *
 * 1. 개요 : 
 * 2. 작성일 : 2017. 11. 2.
 * <pre>
 *
 * @author : 박명규(로컬계정)
 * @version : 1.0
 */
public class ChatServerInitializer extends ChannelInitializer<Channel> {
	private final ChannelGroup group;
	
	public ChatServerInitializer(ChannelGroup group) {
		this.group = group;
	}
	
	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new ChunkedWriteHandler());
		pipeline.addLast(new HttpObjectAggregator(64 * 1024));
		pipeline.addLast(new HttpRequestHandler("/ws"));
		pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, false, Integer.MAX_VALUE));
		pipeline.addLast(new WebSocketFrameAggregator(Integer.MAX_VALUE));
		pipeline.addLast(new TextWebSocketFrameHandler(group));
//		pipeline.addFirst(new ChannelInboundHandlerAdapter(){
//			@Override
//			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//				ByteBuf buf = (ByteBuf) msg;
//				System.out.println("데이터 수신; Byte:"+buf.readableBytes());
//				ctx.fireChannelRead(msg);
//			}
//		});
	}

}
