// Blob 객체는 불변이기 때문에 재조립을 새로운 객체를 만듦으로써 해야 함
var MyBlobBuilder = function () {
    this.parts = [];
}

MyBlobBuilder.prototype.append = function (part) {
    this.parts.push(part);
    this.blob = undefined; // Invalidate the blob
};

MyBlobBuilder.prototype.getBlob = function () {
    if (!this.blob) {
        this.blob = new Blob(this.parts, {type: "text/plain"});
    }
    return this.blob;
};
onmessage = function (e) {
    console.log(e);
    var requester = e.data.id;
    var fileName = e.data.fileName;
    var fileUploader = e.data.fileUploader;
    var fileSize = e.data.fileSize;
    console.log('file type : ' + typeof file);
    var webSocket = new WebSocket("ws://localhost:8081/ws");
    var blobBuilder = new MyBlobBuilder();

    webSocket.onopen = function () {
        webSocket.send(JSON.stringify({
            "mode": "fileDownload",
            "requester": requester,
            "fileUploader": fileUploader,
            "fileName": fileName
        }));
    }
    var count = 0;
    // 다운받은 파일 재조립
    webSocket.onmessage = function (message) {
        if (message.data instanceof Blob || message.data instanceof ArrayBuffer) {
            var blob = message.data;


            count += blob.size;

            console.log("데이터 정보 : " + blob);
            blobBuilder.append(blob);
            if (count == fileSize) {
                postMessage(blobBuilder.getBlob());
                webSocket.close();
                close();
            }

        } else {
            console.log('worker : 서버로부터 파일 이외의 타입을 받음');
        }
    }


}