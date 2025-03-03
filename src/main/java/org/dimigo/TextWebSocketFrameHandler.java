package org.dimigo;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

/**
 * <pre>
 * nettyInActionWebSocket |_ TextWebSocketFrameHandler
 *
 * 1. 개요 : 2. 작성일 : 2017. 11. 2.
 *
 * <pre>
 *
 * @author : 박명규(로컬계정)
 * @version : 1.0
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final ChannelGroup clients;
    private static Clients out;
    private static JSONParser jsonParser = new JSONParser();

    public TextWebSocketFrameHandler(ChannelGroup group) {
        this.clients = group;
        if (out == null)
            out = new Clients(group);
    }

    // 핸들러 이름 : httpServerCodec핸들러 클래스 :
    // io.netty.handler.codec.http.HttpServerCodec
    // 핸들러 이름 : httpAggregator핸들러 클래스 :
    // io.netty.handler.codec.http.HttpObjectAggregator
    // 핸들러 이름 : handshaker핸들러 클래스 :
    // io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker$2
    // 핸들러 이름 : websocketHandler핸들러 클래스 : WebSocketServer.WebSocketHandler
    @SuppressWarnings({"deprecation", "unchecked"})
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            System.out.println("핸드쉐이크 완료");
            // http 요청을 wrap해주는 핸들러
            ctx.pipeline().remove(HttpRequestHandler.class);
            // Ping, Pong 형식의 frame을 자동 처리하는 핸들러
            ctx.pipeline().remove(WebSocketServerProtocolHandler.class);
        } else if (evt instanceof FileInfo) {
            FileInfo fileInfo = (FileInfo) evt;
            JSONObject obj = new JSONObject();
            obj.put("mode", "fileUploadComplete");
            obj.put("fileName", fileInfo.getFileName());
            obj.put("fileUploader", fileInfo.getFileUploader());
            obj.put("fileSize", fileInfo.getFileSize());

            out.sendToAll(obj);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(msg.text());
            System.out.println("클라이언트로부터의 메세지:" + jsonObject.toJSONString());
            String mode = (String) jsonObject.get("mode");

            if ("firstConnect".equals(mode)) // 만약 처음 접속이라면
                firstConnect(ctx, jsonObject);
            if ("general".equals(mode))
                generalChat(ctx, jsonObject); // 전체채팅
            if ("whisper".equals(mode))
                whisperChat(ctx, jsonObject); // 귓속말
            if ("fileUpload".equals(mode)) // 파일 업로드 요청
                fileUpload(ctx, jsonObject);
            if ("fileDownload".equals(mode)) // 파일 다운로드 요청
                fileDownload(ctx, jsonObject);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("클라이언트에서의 입력값이 잘못되었습니다. 채널을 닫습니다.");
            ctx.writeAndFlush(new CloseWebSocketFrame());
        }
    }

    private void fileDownload(ChannelHandlerContext ctx, JSONObject jsonObject) throws IllegalArgumentException, InterruptedException {
        try {
            String requester = (String) jsonObject.get("requester");
            String fileName = (String) jsonObject.get("fileName");
            String fileUploader = (String) jsonObject.get("fileUploader");

            validate(requester, fileName, fileUploader);

            File file = new File(fileUploader + "/" + fileName);
            FileChannel fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ);

            long fileSize = file.length();
            long pointer = 0;
            int bufferSize = 1073741824;
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            do {
                if (pointer + bufferSize > fileSize) {
                    bufferSize = (int) (fileSize - pointer);
                }
                buffer = ByteBuffer.allocate(bufferSize);
                fileChannel.read(buffer, pointer);
                System.out.println("버퍼 크기 : " + buffer.position());
                ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(buffer.array())));
                pointer += bufferSize;
                System.out.println(pointer < fileSize);
            } while (pointer < fileSize);

            System.out.println("보내기 완료");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("파일을 찾을 수 없습니다.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void fileUpload(ChannelHandlerContext ctx, JSONObject jsonObject) throws IllegalArgumentException {
        try {
            // 파일에 대한 정보를 먼저 클라이언트에서 전송함 (데이터는 그 이후 전송)
            String uploader = (String) jsonObject.get("uploader");
            long fileSize = (long) jsonObject.get("fileSize");
            String fileName = (String) jsonObject.get("fileName");

            validate(uploader, "" + fileSize, fileName);
            System.out.printf("파일 업로드 요청; uploader : %s, fileSize : %s, fileName : %s\n", uploader, fileSize, fileName);

            BinaryWebSocketFrameHandler handler;
            handler = new BinaryWebSocketFrameHandler(fileName, fileSize, uploader);
            ctx.pipeline().addLast(handler);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("파일 입출력 오류입니다.");
        }

    }

    @SuppressWarnings("unchecked")
    private void firstConnect(ChannelHandlerContext ctx, JSONObject jsonObject) throws IllegalArgumentException { // 처음
        // 접속했을때
        // 처리

        String id = (String) jsonObject.get("id");

        validate(id);

        clients.add(ctx.channel());
        ctx.channel().attr(AttributeKey.valueOf("id")).set(id);

        JSONObject obj = new JSONObject();
        obj.put("mode", "notify");
        obj.put("message", jsonObject.get("id") + "님이 접속하였습니다");
        out.sendToAll(obj);
    }

    @SuppressWarnings("unchecked")
    private void whisperChat(ChannelHandlerContext ctx, JSONObject jsonObject) throws IllegalArgumentException {

        String id = (String) jsonObject.get("id");
        String target = (String) jsonObject.get("target");
        String message = (String) jsonObject.get("message");

        validate(id, target, message);

        JSONObject obj = new JSONObject();
        obj.put("mode", "whisper");
        obj.put("id", id);
        obj.put("target", target);
        obj.put("message", message);

        out.findChannelAndWrite(target, obj);

    }

    @SuppressWarnings("unchecked")
    private void generalChat(ChannelHandlerContext ctx, JSONObject jsonObject) throws IllegalArgumentException {

        String id = (String) jsonObject.get("id");
        String message = (String) jsonObject.get("message");

        validate(id, message); // 입력값 검증

        JSONObject obj = new JSONObject();
        obj.put("mode", "general");
        obj.put("id", id);
        obj.put("message", message);

        // 보내기
        out.sendToAll(obj);

    }

    private void validate(Object... args) throws IllegalArgumentException {
        for (Object arg : args) {
            if (arg == null) {
                throw new IllegalArgumentException();
            }
            if (arg instanceof Integer || arg instanceof Long) {
                if ((Long) arg == 0) {
                    throw new IllegalArgumentException();
                }

            }
            if (arg instanceof String) {
                if ("".equals(((String) arg).trim())) {
                    throw new IllegalArgumentException();
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("에러 발생, 메세지 : " + cause.getMessage());
    }

}
