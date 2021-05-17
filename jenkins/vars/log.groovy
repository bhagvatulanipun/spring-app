def info(message) {
    echo "\033[1;33m[Info] ${message} \033[0m"
}

def success(message) {
    echo "\033[1;32m[Success] ${message} \033[0m"
}

def error(message) {
    echo "\033[1;31m[Error] ${message} \033[0m"
}
