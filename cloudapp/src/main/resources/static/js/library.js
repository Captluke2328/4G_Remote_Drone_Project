class VideoStreamClient {
    
    constructor(droneId, hostname, port, endpoint) {
        this.droneId = droneId;
        this.websocket = null;
        this.hostname = hostname;
        this.port = port;
        this.endpoint = endpoint;
    }


    activateStream() {
        this.websocket = new WebSocket(this.getServerUrl());

        var activeDroneId = this.droneId;

        this.websocket.onopen = function(event) {
            this.send(activeDroneId);
        }

        this.websocket.onmessage = function(event) {
            $('#video' + activeDroneId).attr("src", "data:image/jpg;base64," + event.data);
        }
    }

    send(message) {
        if (this.websocket !=null && this.websocket.readyState == WebSocket.OPEN) {
            this.websocket.send(message);
        }
    }

    getServerUrl() {
        return "ws://" + this.hostname + ":" + this.port + this.endpoint;
    }

    disconnect() {
        if (this.webSocket != null) {
            this.webSocket.close();
        }
    }

}