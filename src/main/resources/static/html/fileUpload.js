onmessage = function (e) {
    console.log(e);
    var id = e.data.id;
    var file = e.data.file;
    console.log('file type : ' + typeof file);
    var webSocket = new WebSocket("ws://localhost:8081/ws");

    webSocket.onopen = function () {
        webSocket.send(JSON.stringify({
            "mode": "fileUpload",
            "uploader": id,
            "fileSize": file.size,
            "fileName": file.name
        }));

        var bufferSize = 1073741824;
        var pointer = 0;
        do {
            if (pointer + bufferSize > file.size) {
                bufferSize = file.size - pointer;
            }
            webSocket.send(file.slice(pointer, pointer + bufferSize));
            pointer += bufferSize;
            console.log("pointer : " + pointer);
        } while (pointer < file.size);
        var interval = setInterval(function () {
            console.log("bufferedAmount" + webSocket.bufferedAmount);
            if (webSocket.bufferedAmount == 0) {
                clearInterval(interval);
                webSocket.close();
                close();
            }
        }, 100);
    }

}