Go to /etc/rc.local

create two files
Start Script: /usr/local/bin/jar-start.sh
Stop Script: /usr/local/bin/jar-stop.sh

start script:

#!/bin/bash
java -jar myapp.jar 


stop script:

#!/bin/bash
pid=`ps aux | grep myapp | awk '{print $2}'`
kill -9 $pid


Create the following script (myapp) and put it on /etc/init.d.

put this script in /etc/init.d

#!/bin/bash
# MyApp
#
# description: bla bla

case $1 in
    start)
        /bin/bash /usr/local/bin/jat-start.sh
    ;;
    stop)
        /bin/bash /usr/local/bin/jat-stop.sh
    ;;
    restart)
        /bin/bash /usr/local/bin/jar-stop.sh
        /bin/bash /usr/local/bin/jar-start.sh
    ;;
esac
exit 0


now to start after boot type

update-rc.d myapp defaults 

info
http://raspberrypi.stackexchange.com/questions/13034/executing-a-jar-file-when-raspberry-boots-up