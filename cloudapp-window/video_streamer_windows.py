import argparse
import cv2, imutils, socket
from cv2 import VideoCapture
import numpy as np
import time, logging, sys, configparser, argparse
from utils import Utils

"""Create Log Data"""
logformat = "%(asctime)s %(levelname)s %(module)s - %(funcName)s: %(message)s"
datefmt   = "%m-%d %H:%M"

logging.basicConfig(filename="app.log", level=logging.INFO, filemode="w",
                    format=logformat, datefmt=datefmt)

stream_handler = logging.StreamHandler(sys.stderr)
stream_handler.setFormatter(logging.Formatter(fmt=logformat, datefmt=datefmt))

logger = logging.getLogger("app")
logger.addHandler(stream_handler)

"""Create Parser and read init configurations"""
parser = argparse.ArgumentParser()
parser.add_argument('--d', nargs=1, default=None)
args = parser.parse_args()

APP_DIR = args.d[0] if args.d != None else "./"
CONFIGURATIONS = APP_DIR + 'configuration.ini'

config = configparser.ConfigParser()

if len(config.read(CONFIGURATIONS)) == 0:
    logging.error("Could Not Read Configurations File: " + CONFIGURATIONS)
    sys.exit()
    
DRONE_ID            = config['drone']['id']
HOST_IP             = config['cloud-app']['ip']
VIDEO_PORT          = int(config['cloud-app']['video-port'])

GRAYSCALE           = config['video']['grayscale'].lower() == 'true'
FRAMES_PER_SECOND   = int(config['video']['fps'])
JPEG_QUALITY        = int(config['video']['quality'])
WIDTH               = int(config['video']['width'])
HEIGHT              = int(config['video']['height'])

logger.info('FPS: %s  Quality: %s  Width %s Height %s  Grayscale: %s', str(FRAMES_PER_SECOND), str(JPEG_QUALITY), str(WIDTH), str(HEIGHT), GRAYSCALE)
logger.info('Drone ID: %s  Video Recipient: %s:%s', str(DRONE_ID), str(HOST_IP), str(VIDEO_PORT))

def main():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)    
    host_name     = socket.gethostname()
    host_ip       = socket.gethostbyname(host_name) #'10.60.215.193'
    
    server_socket.connect((host_ip,VIDEO_PORT))
    logger.info('Socket Opened, Video Streaming started')
        
    socket_address = (host_ip, VIDEO_PORT)
    
    logger.info('Listening at: %s', str(socket_address))
    
    cap = cv2.VideoCapture(0)
    fps,st,frames_to_count, cnt = (0,0,20,0)
        
    while True:
        while(cap.isOpened()):
            _,frame = cap.read()
            frame = imutils.resize(frame,width=WIDTH)
            code, buffer = cv2.imencode(".jpg", frame, [int(cv2.IMWRITE_JPEG_QUALITY), JPEG_QUALITY])
            datagramMsgBytes = Utils.create_datagram_message(DRONE_ID, buffer)
            server_socket.sendall(datagramMsgBytes)
            frame = cv2.putText(frame,'FPS: '+str(fps),(10,40),cv2.FONT_HERSHEY_SIMPLEX,0.7,(0,0,255),2)
	        
            cv2.imshow('TRANSMITTING VIDEO',frame)
            key = cv2.waitKey(1) & 0xFF
            
            if key == ord('q'):
                server_socket.close()
                break
            
            if cnt == frames_to_count:
                try:
                    fps = round(frames_to_count/(time.time()-st))
                    st=time.time()
                    cnt=0
                except:
                    pass
            cnt+=1
                    
if __name__ == "__main__":
    main()