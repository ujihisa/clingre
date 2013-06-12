## clingre


lingr client app in clojure

## License

GPL version 3 or later

## Author

Tatsuhiro Ujihisa

## Usage: how to start

help: 

    $ java -jar clingre-1.0.jar -h
    
login with specifying login email and password: 

    $ java -jar clingre-1.0.jar -e a@b.c -p ddd
    $ java -jar clingre-1.0.jar -u aaa -p ddd
    
login with login info in a file

    $ java -jar clingre-1.0.jar -c .clingrerc.clj

where .clingrerc.clj is like

    {:email 'a@b.c' :password 'ddd'}

or

    {:user 'aaa' :password 'ddd'}

## Usage: how to send / receive

* send: write in the following format to the process

    ["room-id" "message\n escape double-quote like \" please"]

* receive: the process prints to stdout in the [lingr format](https://github.com/lingr/lingr/wiki)
