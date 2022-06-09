import cv2
import time, socket, logging, configparser, argparse, sys
from utils import Utils

parser = argparse.ArgumentParser()
parser.add_argument('--d', nargs=1, default=None)
args = parser.parse_args()

APP_DIR = args.d[0] if args.d != None else "./"
CONFIGURATIONS = APP_DIR + 'configuration.ini'

logformat = "%(asctime)s %(levelname)s %(module)s - %(funcName)s: %(message)s"
datefmt = "%m-%d %H:%M"

logging.basicConfig(filename="app.log", level=logging.INFO, filemode="w",
                    format=logformat, datefmt=datefmt)

stream_handler = logging.StreamHandler(sys.stderr)
stream_handler.setFormatter(logging.Formatter(fmt=logformat, datefmt=datefmt))

logger = logging.getLogger("app")
logger.addHandler(stream_handler)

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

video_socket = None

cap = cv2.VideoCapture(0)
cap.set(3, WIDTH)
cap.set(4, HEIGHT)
logger.info("Camera module initialized")

video_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
video_socket.connect((HOST_IP, VIDEO_PORT))
logger.info("Socket Opened, Video Streaming started")

while True:
    try:          
        ret, img = cap.read()
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        
        code, jpg_buffer = cv2.imencode(".jpg", gray, [int(cv2.IMWRITE_JPEG_QUALITY), JPEG_QUALITY])
        datagramMsgBytes = Utils.create_datagram_message(DRONE_ID, jpg_buffer)
        video_socket.sendall(datagramMsgBytes)
        
        cv2.imshow("Output", img)
        if cv2.waitKey(1) & 0XFF == ord('q'):
            break
        
    except Exception as e:
        logger.error("Video Stream Ended")
        if cap != None:
            cap.release()
            cv2.destroyAllWindows()
        if video_socket != None:
            video_socket.close()
            

                


    
    