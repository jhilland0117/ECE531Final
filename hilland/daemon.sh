#!/bin/sh

# stuff that always happens
# start function
# stop function
# case handlings
# exit

# chmod 755 file_name
# move file to /etc/init.d/
# arm image, su -, to get admin for cp and mv

# executable goes in /usr/sbin

DAEMON_NAME="hillandFinal"

start() {
    printf "Starting $DAEMON_NAME: "
    /usr/sbin/$DAEMON_NAME
    touch /var/lock/$DAEMON_NAME
    echo "OK"
}

stop() {
    printf "Stopping $DAEMON_NAME: "
    killall $DAEMON_NAME
    rm -f /var/lock/$DAEMON_NAME
    echo "OK"
}

restart() {
    stop
    start
}

case "$1" in
    start)
    start
    ;;
    stop)
    stop
    ;;
    restart|reload)
    restart
    ;;
    *)
    echo "Usage: $0 {start|stop|restart}"
    exit 1
esac

exit $?