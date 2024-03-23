# clove
clove is an [imdb](https://www.imdb.com) and [vidsrc](https://vidsrc.to) scraper written in clojure

## Installation
Download from http://github.com/71zenith/clove/releases/

## Usage
```sh
$ java -jar clove.jar [args] <query>
```

## Examples
```sh
$ java -jar clove.jar Kung Fu Panda 1         # play in mpv
$ java -jar clove.jar Kung Fu Panda 1 -p vlc  # play in vlc
$ java -jar clove.jar Kung Fu Panda 1 -d      # print link
```

### TODO
* [DONE] introduce a menu
* provide common flags
