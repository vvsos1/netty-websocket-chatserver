package org.dimigo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * <pre>
 * nettyInActionWebSocket
 * 	 |_ BinaryWebSocketFrameHandler
 *
 * 1. 개요 : 
 * 2. 작성일 : 2017. 11. 2.
 * <pre>
 *
 * @author : 박명규(로컬계정)
 * @version : 1.0
 */
public class BinaryWebSocketFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {
	private final String fileName;
	private final long fileSize;
	private final String fileUploader;
//	private final FileChannel fileChannel;
	private final FileOutputStream fileOutputStream;
	private final File file;
	
	public BinaryWebSocketFrameHandler(String fileName, long fileSize, String fileUploader) throws IOException{
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.fileUploader = fileUploader;
//		fileChannel = FileChannel.open(Paths.get(this.fileUploader+"_"+this.fileName), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
		// 파일은 업로더명/파일이름 형식으로 저장되므로 업로더명의 폴더가 필요함
		File uploaderDir = new File(HttpRequestHandler.FILE_ROOT+"/"+fileUploader);
		if (uploaderDir.exists() == false)
			uploaderDir.mkdirs();
		file = new File(HttpRequestHandler.FILE_ROOT+"/"+this.fileUploader+"/"+this.fileName);
		if (file.exists()) {	// 파일이 이미 있다면 오류 메세지를 던짐
			throw new IOException("File Already Exist");
		}
		fileOutputStream = new FileOutputStream(file);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
//				fileChannel.close();
				fileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));
	}
	
	@Override
	protected synchronized void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) throws Exception {
		try {
			ByteBuf buf = msg.content();
			System.out.println("파일 다운로드 시도; 크기 : "+buf.readableBytes());
//			if (buf.readableBytes() != fileSize) {
//				throw new Exception("파일이 누락되었습니다.");
//			}
			// FileChannel을 이용한 방법
//			buf.readBytes(fileChannel, 0, buf.readableBytes());
//			if (fileChannel.size() == fileSize) {
//				System.out.println("파일 다운로드 성공");
//				fileChannel.close();
//				ctx.pipeline().remove(this);
//			}
			// FileOutputStream을 이용한 방법
			buf.readBytes(fileOutputStream, buf.readableBytes());
			fileOutputStream.flush();
			if (file.length() == fileSize) {
				System.out.println("파일 다운로드 성공");
				fileOutputStream.close();
				ctx.pipeline().remove(this);
				ctx.channel().pipeline().fireUserEventTriggered(new FileInfo(fileName, fileSize, fileUploader));	// 파일 업로드 완료 알림 - TextWebSocketFrameHandler로
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("파일 다운로드 실패");
		}
	}


}
