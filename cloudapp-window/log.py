import logging
import sys

logformat = "%(asctime)s %(levelname)s %(module)s - %(funcName)s: %(message)s"
datefmt = "%m-%d %H:%M"

logging.basicConfig(filename="app.log", level=logging.INFO, filemode="w",
                    format=logformat, datefmt=datefmt)

stream_handler = logging.StreamHandler(sys.stderr)
stream_handler.setFormatter(logging.Formatter(fmt=logformat, datefmt=datefmt))

logger = logging.getLogger("app")
logger.addHandler(stream_handler)

logger.info("information")
logger.warning("warning")


def fun():
    logger.info(" fun inf")
    logger.warning("fun warn")

if __name__ == "__main__":
    fun()