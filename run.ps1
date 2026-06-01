[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
java -Dfile.encoding=UTF-8 -cp "bin;lib/json.jar" Main $args
